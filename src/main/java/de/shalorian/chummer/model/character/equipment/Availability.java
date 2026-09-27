package de.shalorian.chummer.model.character.equipment;

import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import org.jspecify.annotations.NonNull;

/**
 * Verfügbarkeit eines Katalogeintrags: Formel (Fixwert oder stufenabhängig, z.B. {@code "Rating *
 * 5"}, ausgewertet über {@link RatingFormula}) plus {@link Legality}. Gemeinsamer Baustein für alle
 * Kaufkategorien (Cyberware, Rüstung, künftig Waffen und Gear).
 *
 * <p>SR4-Grundregelwerk S. 111: "Die Maximalstufe, die ein während der Charaktererschaffung
 * erworbener Ausrüstungsgegenstand aufweisen darf, beträgt 6. Die höchste Verfügbarkeit ist 12." Die
 * Implantatkategorien (Alpha-/Beta-/Deltaware) haben im GRW keinen Verfügbarkeitsmodifikator
 * (Tabelle S. 359 nennt nur Essenz und Preis) — anders als im C#-Original wird deshalb hier nichts
 * aufgeschlagen.
 *
 * @param formula  Verfügbarkeitsformel; {@code "0"} für Einträge ohne Verfügbarkeit (GRW "–")
 * @param legality Legalitätsstufe
 */
public record Availability(@NonNull String formula, @NonNull Legality legality) {

  /** Höchste bei der Charaktererschaffung zulässige Verfügbarkeit (GRW S. 111). */
  public static final int MAX_AVAILABILITY_AT_CREATION = 12;

  /** Verfügbarkeit von Einträgen ohne Angabe (GRW "–"). */
  public static final Availability NONE = new Availability("0", Legality.LEGAL);

  /**
   * Liest eine Verfügbarkeit aus den Katalog-XMLs: {@code <avail>} (Formel; leer bzw. fehlend = "–")
   * und {@code <legality>} ({@code E}/{@code V}; leer bzw. fehlend = legal).
   *
   * @throws IllegalArgumentException bei unbekanntem Legalitätskürzel
   */
  public static @NonNull Availability parse(@NonNull String avail, @NonNull String legality) {
    String formula = avail.trim();
    return new Availability(formula.isEmpty() ? NONE.formula() : formula,
        Legality.fromCode(legality));
  }

  /** Verfügbarkeitswert für die angegebene Stufe ({@code 0} bei Einträgen ohne Stufe). */
  public int evaluate(int rating) {
    return RatingFormula.evaluate(formula, rating).intValueExact();
  }

  /** Ob ein Gegenstand dieser Stufe bei der Charaktererschaffung gekauft werden darf (≤ 12, GRW S. 111). */
  public boolean isAllowedAtCreation(int rating) {
    return evaluate(rating) <= MAX_AVAILABILITY_AT_CREATION;
  }

  /** Anzeige wie im GRW, z.B. {@code "15E"}, {@code "4"} oder {@code "–"} (keine Angabe). */
  public @NonNull String displayText(int rating) {
    int value = evaluate(rating);
    if (value == 0 && legality == Legality.LEGAL) {
      return "–";
    }
    return value + legality.getCode();
  }
}
