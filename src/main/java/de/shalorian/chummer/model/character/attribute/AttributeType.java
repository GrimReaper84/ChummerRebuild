package de.shalorian.chummer.model.character.attribute;

import org.jspecify.annotations.NonNull;

public enum AttributeType {
  BOD("Körper", "BOD"),
  AGI("Geschicklichkeit", "AGI"),
  REA("Reaktion", "REA"),
  STR("Stärke", "STR"),
  CHA("Charisma", "CHA"),
  INT("Intuition", "INT"),
  LOG("Logik", "LOG"),
  WIL("Willenskraft", "WIL");

  private final @NonNull String displayName;
  private final @NonNull String abbreviation;

  AttributeType(@NonNull String displayName, @NonNull String abbreviation) {
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