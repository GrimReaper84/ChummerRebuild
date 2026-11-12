package de.shalorian.chummer.model.character.cyberware;

import org.jspecify.annotations.NonNull;

/**
 * Eine rechnende Wirkung eines Implantats aus {@code <effect type="…" target="…">Formel</effect>}.
 *
 * @param target  Attributkürzel ({@code "STR"}) bzw. Fertigkeitsgruppe; leer, wenn der Typ kein Ziel braucht
 * @param formula Wert als {@link RatingFormula}, z.B. {@code "Rating"} oder {@code "1"}
 */
public record ImplantEffect(@NonNull ImplantEffectType type, @NonNull String target, @NonNull String formula) {

  /** Wert der Wirkung bei dieser Stufe. */
  public int value(int rating) {
    return RatingFormula.evaluate(formula, rating).intValueExact();
  }
}
