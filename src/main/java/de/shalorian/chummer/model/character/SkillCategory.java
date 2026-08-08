package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

public enum SkillCategory {
  COMBAT("Kampf"),
  PHYSICAL("Körperlich"),
  SOCIAL("Sozial"),
  TECHNICAL("Technisch"),
  VEHICLE("Fahrzeuge"),
  MAGICAL("Magie");

  private final @NonNull String displayName;

  SkillCategory(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}