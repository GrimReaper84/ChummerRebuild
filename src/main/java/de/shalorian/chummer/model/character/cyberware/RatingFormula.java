package de.shalorian.chummer.model.character.cyberware;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

/**
 * Wertet die Formel-Felder der Cyberware-/Bioware-Kataloge (Essenzkosten, Preis) für eine konkrete
 * Stufe aus. Im C#-Original wurden diese Ausdrücke per XPath berechnet; der Java-Rebuild braucht nur
 * die in den GRW-Tabellen tatsächlich vorkommenden, linearen Muster (siehe {@code
 * docs/analysen/cyberware_analyse.md}, Regel "Essenzkosten-Formel pro Item").
 *
 * <p>Unterstützte Ausdrücke (Leerzeichen beliebig, Dezimaltrenner {@code .}):
 * <ul>
 *   <li>Fixwert, z.B. {@code "0.5"} oder {@code "10000"} — unabhängig von der Stufe</li>
 *   <li>{@code "Rating"} — die Stufe selbst</li>
 *   <li>{@code "Rating * X"} — z.B. {@code "Rating * 0.5"} (GRW: "Stufe x 0,5")</li>
 *   <li>{@code "Rating + X"} und {@code "Rating - X"}</li>
 *   <li>{@code "X + Rating * Y"} — Grundpreis plus Stufenaufschlag, z.B. Lenkrakete
 *       {@code "500 + Rating * 500"} (GRW S. 371: "+ (Sensorstufe x 500) ¥")</li>
 * </ul>
 * Jeder andere Ausdruck führt zu einer {@link IllegalArgumentException} statt eines stillen
 * Fallbacks — ein unbekanntes Muster ist ein Datenfehler im Katalog und soll früh auffallen. Komplexere
 * Formeln des C#-Originals ({@code FixedValues(...)}, verschachtelte Ausdrücke) werden bewusst nicht
 * unterstützt, siehe {@code docs/plaene/cyberware_plan.md} Abschnitt 13 (akzeptiertes Risiko).
 */
public final class RatingFormula {

  private static final String NUMBER = "(\\d+(?:\\.\\d+)?)";
  private static final Pattern FIXED = Pattern.compile(NUMBER);
  private static final Pattern RATING_ONLY = Pattern.compile("Rating");
  private static final Pattern RATING_OPERATION = Pattern.compile("Rating\\s*([*+-])\\s*" + NUMBER);
  private static final Pattern BASE_PLUS_RATING_TIMES = Pattern.compile(
      NUMBER + "\\s*\\+\\s*Rating\\s*\\*\\s*" + NUMBER);

  private RatingFormula() {
  }

  /**
   * Berechnet den Wert von {@code formula} für die angegebene Stufe.
   *
   * @param formula Formelausdruck aus dem Katalog; darf nicht leer sein
   * @param rating  gewählte Stufe; bei Einträgen ohne Stufe {@code 0} (wird dann von Fixwerten ignoriert)
   * @return exakter Dezimalwert (keine Rundung — gerundet wird erst an der fachlichen Verwendungsstelle)
   * @throws IllegalArgumentException wenn der Ausdruck keinem der unterstützten Muster entspricht
   */
  public static @NonNull BigDecimal evaluate(@NonNull String formula, int rating) {
    String expression = formula.trim();
    if (FIXED.matcher(expression).matches()) {
      return new BigDecimal(expression);
    }
    if (RATING_ONLY.matcher(expression).matches()) {
      return BigDecimal.valueOf(rating);
    }
    Matcher operation = RATING_OPERATION.matcher(expression);
    if (operation.matches()) {
      BigDecimal ratingValue = BigDecimal.valueOf(rating);
      BigDecimal operand = new BigDecimal(operation.group(2));
      return switch (operation.group(1)) {
        case "*" -> ratingValue.multiply(operand);
        case "+" -> ratingValue.add(operand);
        default -> ratingValue.subtract(operand);
      };
    }
    Matcher basePlus = BASE_PLUS_RATING_TIMES.matcher(expression);
    if (basePlus.matches()) {
      return new BigDecimal(basePlus.group(1))
          .add(BigDecimal.valueOf(rating).multiply(new BigDecimal(basePlus.group(2))));
    }
    throw new IllegalArgumentException("Nicht unterstützter Formelausdruck: \"" + formula + "\"");
  }
}
