package de.shalorian.chummer.model.character.condition;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Inhalte, die der Bereich „Cheat-Sheet“ auf Wunsch zeigt (Anzeige-Einstellung, ADR-065): Wahrnehmung und Ausweichen
 * als häufige Fertigkeitsproben sowie Bewegung (Gehen, Laufen und Sprinten, GRW S. 177), die keinen eigenen Pool hat.
 * Die Attributproben stehen fest im Status-Panel. Die Reihenfolge ist die Anzeigereihenfolge.
 */
public enum CheatSheetItem {
  PERCEPTION("Wahrnehmung", "Wahrnehmung"),
  DODGE("Ausweichen", "Ausweichen"),
  MOVEMENT("Bewegung (Gehen, Laufen, Sprinten)", null);

  private final @NonNull String displayName;
  private final @Nullable String skillName;

  CheatSheetItem(@NonNull String displayName, @Nullable String skillName) {
    this.displayName = displayName;
    this.skillName = skillName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Name der aktiven Fertigkeit in {@code skills.xml}; {@code null} bei Bewegung. */
  public @Nullable String getSkillName() {
    return skillName;
  }
}
