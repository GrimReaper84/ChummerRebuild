package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Die fünf SR4-Kategorien für Wissensfertigkeiten. Jede Kategorie ist fest mit einem Attribut
 * verknüpft (Street/Interest/Language → INT, Academic/Professional → LOG); das Attribut ist
 * relevant für eine spätere Würfelpool-Anzeige, nicht für die BP-/Karma-Kostenberechnung.
 */
public enum KnowledgeSkillCategory {
  ACADEMIC("Akademisch", AttributeType.LOG),
  INTEREST("Interesse", AttributeType.INT),
  LANGUAGE("Sprache", AttributeType.INT),
  PROFESSIONAL("Beruflich", AttributeType.LOG),
  STREET("Straße", AttributeType.INT);

  private final @NonNull String displayName;
  private final @NonNull AttributeType linkedAttribute;

  KnowledgeSkillCategory(@NonNull String displayName, @NonNull AttributeType linkedAttribute) {
    this.displayName = displayName;
    this.linkedAttribute = linkedAttribute;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  public @NonNull AttributeType getLinkedAttribute() {
    return linkedAttribute;
  }
}
