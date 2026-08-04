package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Kategorien für Wissensfertigkeiten. Das SR4-Grundregelwerk (S. 109/144) kennt genau **vier**
 * echte Wissensfertigkeiten-Kategorien — ACADEMIC, PROFESSIONAL, STREET und HOBBY —, jede fest
 * mit einem Attribut verknüpft (Straße/Hobby → INT, Akademisch/Beruf → LOG). Sprachfertigkeiten
 * sind laut Regeltext keine eigene, gleichrangige Kategorie, sondern ein separates Konzept, das
 * lediglich denselben Wissensfertigkeitspunkte-Pool anzapft ("Sprachfertigkeiten gelten bei der
 * Charaktererschaffung als Wissensfertigkeiten, und die Fertigkeitspunkte zum Erwerb dieser
 * Fertigkeiten stammen aus demselben Pool"). LANGUAGE ist hier dennoch als fünfter Wert
 * modelliert — als pragmatische Vereinfachung, um zusätzlich gekaufte Sprachen (jenseits der
 * kostenlosen Muttersprache, siehe {@link KnowledgeSkill#isNativeLanguage()}) im selben
 * Datenmodell abzubilden, ohne eine eigene Struktur nur für Sprachen einzuführen.
 */
public enum KnowledgeSkillCategory {
  ACADEMIC("Akademisch", AttributeType.LOG),
  HOBBY("Hobby", AttributeType.INT),
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
