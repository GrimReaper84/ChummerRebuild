package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/**
 * Eine Kraft aus {@code critter_powers.xml}: Critterkräfte (GRW S. 337–342) und Sprite-Kräfte (S. 283).
 *
 * @param type     M oder P, „wie Zauber“, bei Sprites „–“
 * @param action   Auto, Einfach oder Komplex
 * @param range    Selbst, B, BF oder Speziell
 * @param duration Immer, Sofort, Aufrechterhalten, Permanent oder Speziell
 * @param note     Kurzwirkung in eigenen Worten
 * @param sprite   ob es eine Sprite-Kraft ist
 */
public record CritterPowerData(@NonNull String name, @NonNull String type, @NonNull String action,
    @NonNull String range, @NonNull String duration, @NonNull CritterPowerEffect effect, @NonNull String note,
    boolean sprite, @NonNull String source, int page) implements CatalogEntry, Sourced {

  /** Tooltip: Angaben wie im GRW-Kopf der Kraft, dann die Wirkung. */
  public @NonNull String describe() {
    String head = sprite ? "Sprite-Kraft" : "Art: " + type + " · Handlung: " + action + " · Reichweite: " + range
        + " · Dauer: " + duration;
    return head + "\n" + note + "\n(" + source + " S. " + page + ")";
  }
}
