package de.shalorian.chummer.model.character.magic;

import org.jspecify.annotations.NonNull;

/**
 * Magische Tradition eines Magier-Charakters. SR4-Grundregelwerk S. 118 nennt Hermetik und
 * Schamanismus als die beiden verbreitetsten Traditionen und bildet damit den GRW-Kern ab —
 * Spieler:innen können laut Regelwerk auch eigene Traditionen entwickeln, dieses erweiterte
 * Subsystem ist bewusst nicht Teil dieser Version (siehe {@code docs/magie_analyse.md}). Die
 * Drain-Attributformel ist reiner Anzeigetext, da die Würfelpool-Berechnung noch nicht existiert.
 */
public enum Tradition {
  HERMETIC("Hermetisch", "WIL + LOG"),
  SHAMANIC("Schamanistisch", "WIL + CHA");

  private final @NonNull String displayName;
  private final @NonNull String drainFormula;

  Tradition(@NonNull String displayName, @NonNull String drainFormula) {
    this.displayName = displayName;
    this.drainFormula = drainFormula;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  public @NonNull String getDrainFormula() {
    return drainFormula;
  }
}
