package de.shalorian.chummer.model.character.spirit;

import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import org.jspecify.annotations.NonNull;

/**
 * Ein Wert aus dem Werteblock eines Geistes oder Sprites, z.B. „K“ = Kraft + 4 oder „Firewall“ = Stufe − 2.
 *
 * @param formula RatingFormula mit {@code Rating} = Kraft bzw. Stufe
 * @param min     Untergrenze (körperliche Attribute eines Geistes mindestens 1, GRW S. 347), sonst 0
 */
public record SpiritStat(@NonNull String label, @NonNull String formula, int min) {

  /** Wert für Kraft bzw. Stufe {@code force}. */
  public int valueFor(int force) {
    return Math.max(min, RatingFormula.evaluate(formula, force).intValue());
  }
}
