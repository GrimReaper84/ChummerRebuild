package de.shalorian.chummer.model.character.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.gear.ExtraKind;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Vorschläge für Wissens- und Sprachfertigkeiten (GRW S. 110, 155, 156; ADR-032). */
class KnowledgeSuggestionLoaderTest {

  @Test
  void everyCategoryHasSuggestions() {
    for (KnowledgeSkillCategory category : KnowledgeSkillCategory.values()) {
      assertFalse(KnowledgeSuggestionLoader.names(category).isEmpty(), category.name());
    }
  }

  @Test
  void languagesAndLingos() {
    List<String> languages = KnowledgeSuggestionLoader.names(KnowledgeSkillCategory.LANGUAGE);
    assertTrue(languages.containsAll(List.of("Deutsch", "Englisch", "Sperethiel", "Or'zet", "Japanisch")));
    List<String> specs = KnowledgeSuggestionLoader.specializations("Sperethiel", KnowledgeSkillCategory.LANGUAGE);
    assertTrue(specs.containsAll(List.of("Lesen/Schreiben", "Sprechen", "Stadtsprache", "Wiz", "Orbital")));
  }

  @Test
  void specializationsFromTheTables() {
    // S. 155: Biologie mit Parazoologie, Seattler Straßengangs mit Halloweeners
    assertTrue(KnowledgeSuggestionLoader.specializations("biologie", KnowledgeSkillCategory.ACADEMIC)
        .contains("Parazoologie"));
    assertTrue(KnowledgeSuggestionLoader.specializations("Seattler Straßengangs", KnowledgeSkillCategory.STREET)
        .contains("Halloweeners"));
    assertEquals(List.of(), KnowledgeSuggestionLoader.specializations("Gang-Identifikation",
        KnowledgeSkillCategory.STREET));
    assertEquals(List.of(), KnowledgeSuggestionLoader.specializations("Biologie", KnowledgeSkillCategory.STREET));
  }

  @Test
  void softExtrasSuggestButAllowFreeText() {
    assertTrue(ExtraKind.LANGUAGE.suggestions().contains("Japanisch"));
    assertFalse(ExtraKind.LANGUAGE.isChoice());
    assertTrue(ExtraKind.KNOWLEDGE_SKILL.suggestions().contains("Magietheorie"));
    assertFalse(ExtraKind.KNOWLEDGE_SKILL.suggestions().contains("Japanisch"));
    assertFalse(ExtraKind.KNOWLEDGE_SKILL.isChoice());
  }
}
