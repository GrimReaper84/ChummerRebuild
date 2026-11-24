package de.shalorian.chummer.model.character.attribute;

import org.jspecify.annotations.NonNull;

/**
 * Die acht Grundattribute. Der Enum-Name ist der Schlüssel in XML-Daten und Speicherdateien (z.B. {@code
 * attribute="AGI"}), Anzeigename und Kürzel folgen dem deutschen GRW (Konstitution/KON, Geschicklichkeit/GES …).
 */
public enum AttributeType {
  BOD("Konstitution", "KON"),
  AGI("Geschicklichkeit", "GES"),
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

  /**
   * Anzeige-Kürzel zu einem Schlüssel wie {@code "AGI"} (z.B. {@code Skill#getLinkedAttribute}); Sonderattribute (MAG,
   * RES, EDG) und unbekannte Schlüssel bleiben, wie sie sind.
   */
  public static @NonNull String abbreviationOf(@NonNull String key) {
    for (AttributeType type : values()) {
      if (type.name().equals(key)) {
        return type.abbreviation;
      }
    }
    return key;
  }

  /** Körperliches Attribut: Konstitution, Geschicklichkeit, Reaktion oder Stärke. */
  public boolean isPhysical() {
    return switch (this) {
      case BOD, AGI, REA, STR -> true;
      case CHA, INT, LOG, WIL -> false;
    };
  }
}