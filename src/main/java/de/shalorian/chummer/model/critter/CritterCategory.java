package de.shalorian.chummer.model.critter;

import org.jspecify.annotations.NonNull;

/** Abschnitt des Critter-Kapitels (GRW S. 343–351); Geister stehen in {@code spirits.xml}. */
public enum CritterCategory {
  ORDINARY("Gewöhnliche Critter"),
  PARACRITTER("Paracritter"),
  DRACOFORM("Dracoformen");

  private final @NonNull String displayName;

  CritterCategory(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}
