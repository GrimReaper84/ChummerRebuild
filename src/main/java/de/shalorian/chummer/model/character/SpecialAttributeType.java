package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Kennzeichnet die drei Sonderattribute Edge, Magic und Resonance. Bewusst getrennt von
 * {@link AttributeType}: Sonderattribute haben eine abweichende Kostenformel (siehe
 * {@link SpecialAttribute}) und dürfen nicht in den {@link Character#MAX_ATTRIBUTE_BP}-Deckel für
 * Körperliche/Geistige Attribute einfließen.
 */
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
