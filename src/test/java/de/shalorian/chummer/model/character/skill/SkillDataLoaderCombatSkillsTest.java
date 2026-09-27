package de.shalorian.chummer.model.character.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Kampf-Aktionsfertigkeiten laut SR4-Grundregelwerk S. 147/148 (Namen, Attribut, Gruppe). */
class SkillDataLoaderCombatSkillsTest {

  private static Map<String, Skill> skillsByName() {
    return SkillDataLoader.createFreshSkills().stream()
        .collect(Collectors.toMap(Skill::getName, Function.identity()));
  }

  @Test
  void combatSkillsMatchRulebook() {
    Set<String> combat = SkillDataLoader.createFreshSkills().stream()
        .filter(s -> s.getCategory() == SkillCategory.COMBAT)
        .map(Skill::getName)
        .collect(Collectors.toSet());

    assertEquals(Set.of("Ausweichen", "Exotische Fernkampfwaffe", "Exotische Nahkampfwaffe",
        "Gewehre", "Klingenwaffen", "Knüppel", "Pistolen", "Projektilwaffen", "Schnellfeuerwaffen",
        "Schwere Waffen", "Waffenloser Kampf", "Wurfwaffen"), combat);
  }

  @Test
  void combatSkillGroupsMatchRulebook() {
    Map<String, Skill> skills = skillsByName();
    for (String name : List.of("Klingenwaffen", "Knüppel", "Waffenloser Kampf")) {
      assertEquals("Nahkampf", skills.get(name).getGroupName(), name);
    }
    for (String name : List.of("Gewehre", "Pistolen", "Schnellfeuerwaffen")) {
      assertEquals("Feuerwaffen", skills.get(name).getGroupName(), name);
    }
    for (String name : List.of("Ausweichen", "Exotische Fernkampfwaffe", "Exotische Nahkampfwaffe",
        "Projektilwaffen", "Schwere Waffen", "Wurfwaffen")) {
      assertNull(skills.get(name).getGroupName(), name);
    }
  }

  @Test
  void dodgeIsLinkedToReaction() {
    assertEquals("REA", skillsByName().get("Ausweichen").getLinkedAttribute());
  }

  @Test
  void explosivesIsTechnicalLogicSkillWithoutGroup() {
    Skill explosives = skillsByName().get("Sprengstoffe");

    assertEquals(SkillCategory.TECHNICAL, explosives.getCategory());
    assertEquals("LOG", explosives.getLinkedAttribute());
    assertNull(explosives.getGroupName());
  }

  @Test
  void noNonRulebookCombatSkillsRemain() {
    Map<String, Skill> skills = skillsByName();
    for (String old : List.of("Klingen", "Automatikwaffen", "Langwaffen", "Schrotflinten",
        "Bogenschießen", "Sprengstoff")) {
      assertFalse(skills.containsKey(old), old);
    }
  }
}
