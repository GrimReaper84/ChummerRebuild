package de.shalorian.chummer.model.character.weapon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

/**
 * Rückstoßkompensation (RK) einer Waffe in der GRW-Schreibweise "x (y)". SR4-Grundregelwerk S. 357
 * (<i>Ausrüstungswerte</i>): Der Wert außerhalb der Klammer ist die fest eingebaute RK, der Wert in
 * Klammern die RK einschließlich abnehmbaren bzw. nur bei Benutzung wirksamen Zubehörs. Die
 * Waffentabellen bestätigen das: Die Ruger 100 mit eingebautem Schockpolster steht mit "(1)" in der
 * Tabelle (S. 366), die Ultimax HMG-2 mit Gasventilsystem 3, Schockpolster und Dreibein mit "3 (10)".
 *
 * <p>Anzeige: "–" ohne RK, "x" wenn beide Werte gleich sind, "(y)" ohne festen Anteil, sonst "x (y)".
 *
 * @param fixed fest eingebaute RK (x)
 * @param total RK inkl. abnehmbarem Zubehör (y), nie kleiner als {@code fixed}
 */
public record Recoil(int fixed, int total) {

  /** Keine Rückstoßkompensation (GRW "–" bzw. Kategorie ohne RK-Spalte). */
  public static final Recoil NONE = new Recoil(0, 0);

  private static final Pattern FORMAT = Pattern.compile("(\\d+)?\\s*(?:\\((\\d+)\\))?");

  public Recoil {
    if (fixed < 0 || total < fixed) {
      throw new IllegalArgumentException("Ungültige Rückstoßkompensation: %d (%d)".formatted(fixed,
          total));
    }
  }

  /**
   * Liest einen RK-Wert in GRW-Schreibweise: "–" oder leer, "1", "(1)", "2 (3)".
   *
   * @throws IllegalArgumentException bei einem anderen Format (Datenfehler im Katalog)
   */
  public static @NonNull Recoil parse(@NonNull String text) {
    String trimmed = text.trim();
    if (trimmed.isEmpty() || trimmed.equals("–")) {
      return NONE;
    }
    Matcher matcher = FORMAT.matcher(trimmed);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Unbekannter Rückstoßwert: \"" + text + "\"");
    }
    String fixedText = matcher.group(1);
    String totalText = matcher.group(2);
    int fixed = fixedText == null ? 0 : Integer.parseInt(fixedText);
    return new Recoil(fixed, totalText == null ? fixed : Integer.parseInt(totalText));
  }

  /**
   * RK mit zusätzlichem Zubehör: {@code fixedRc} zählt in beide Werte (fest eingebaut, z.B.
   * Gasventilsystem), {@code removableRc} nur in den Klammerwert (z.B. Schockpolster, Zweibein).
   */
  public @NonNull Recoil plus(int fixedRc, int removableRc) {
    return new Recoil(fixed + fixedRc, total + fixedRc + removableRc);
  }

  /** Anzeige wie in den GRW-Tabellen, z.B. "2 (3)". */
  public @NonNull String displayText() {
    if (total == 0) {
      return "–";
    }
    if (fixed == total) {
      return String.valueOf(fixed);
    }
    return fixed == 0 ? "(" + total + ")" : fixed + " (" + total + ")";
  }
}
