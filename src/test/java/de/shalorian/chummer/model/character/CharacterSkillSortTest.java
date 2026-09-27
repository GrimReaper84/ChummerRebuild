package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deckt die Fachlogik hinter der alphabetischen Skills-Tab-Ansicht ab: {@link
 * Character#getIndividualSkills()} und {@link Character#getCoupledGroupNames()} müssen
 * gekoppelte Gruppenmitglieder und Einzelfertigkeiten sauber trennen, jeweils alphabetisch sortiert.
 */
class CharacterSkillSortTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  @Test
  void individualSkillsExcludeCoupledGroupMembers() {
    Character c = mensch("Test");

    List<Skill> individual = c.getIndividualSkills();

    assertFalse(individual.stream().anyMatch(s -> "Klingenwaffen".equals(s.getName())),
        "Klingenwaffen gehört zur gekoppelten Gruppe Nahkampf");
    assertTrue(individual.stream().anyMatch(s -> "Projektilwaffen".equals(s.getName())),
        "Projektilwaffen ist nie gruppiert");
  }

  @Test
  void individualSkillsIncludeDecoupledFormerGroupMembers() {
    Character c = mensch("Test");
    c.raiseGroup("Nahkampf");
    c.decoupleGroup("Nahkampf");

    List<Skill> individual = c.getIndividualSkills();

    assertTrue(individual.stream().anyMatch(s -> "Klingenwaffen".equals(s.getName())),
        "Nach dem Auskoppeln verhält sich Klingenwaffen wie eine Einzelfertigkeit");
  }

  @Test
  void individualSkillsAreSortedAlphabetically() {
    Character c = mensch("Test");

    List<String> names = c.getIndividualSkills().stream().map(Skill::getName).toList();

    List<String> sorted = names.stream().sorted().toList();
    assertEquals(sorted, names);
  }

  @Test
  void coupledGroupNamesContainOnlyStillCoupledGroups() {
    Character c = mensch("Test");
    c.raiseGroup("Feuerwaffen");
    c.decoupleGroup("Feuerwaffen");

    List<String> groups = c.getCoupledGroupNames();

    assertTrue(groups.contains("Nahkampf"));
    assertFalse(groups.contains("Feuerwaffen"), "Entkoppelte Gruppen zählen nicht mehr als Gruppe");
  }

  @Test
  void coupledGroupNamesAreSortedAlphabetically() {
    Character c = mensch("Test");

    List<String> groups = c.getCoupledGroupNames();

    List<String> sorted = groups.stream().sorted().toList();
    assertEquals(sorted, groups);
  }
}
