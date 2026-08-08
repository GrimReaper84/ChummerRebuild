package de.shalorian.chummer.model.character.attribute;

import org.jspecify.annotations.NonNull;

public enum SpecialAttributeType {
  EDGE("Edge", "EDG"),
  MAGIC("Magie", "MAG"),
  RESONANCE("Resonanz", "RES");

  private final @NonNull String displayName;
  private final @NonNull String abbreviation;

  SpecialAttributeType(@NonNull String displayName, @NonNull String abbreviation) {
    this.displayName = displayName;
    this.abbreviation = abbreviation;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  public @NonNull String getAbbreviation() {
    return abbreviation;
  }
}
