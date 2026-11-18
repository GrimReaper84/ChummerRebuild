package de.shalorian.chummer.model.character.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import org.junit.jupiter.api.Test;

/** Pools der Proben im Status-Panel (ADR-061). */
class StatusTestTest {

  private static Character runner() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.INT).setValue(4);
    c.getAttribute(AttributeType.REA).setValue(3);
    c.getAttribute(AttributeType.WIL).setValue(3);
    c.getAttribute(AttributeType.CHA).setValue(2);
    return c;
  }

  @Test
  void attributeTestsMatchConditionSection() {
    Character c = runner();
    for (StatusTest test : StatusTest.values()) {
      AttributeTest attributeTest = test.getAttributeTest();
      if (attributeTest != null) {
        assertEquals(c.getAttributeTestPool(attributeTest).dice(), c.getStatusTestPool(test).orElseThrow().dice());
      }
    }
    assertEquals(5, c.getStatusTestPool(StatusTest.COMPOSURE).orElseThrow().dice());
  }

  @Test
  void skillTestsUseSkillPool() {
    Character c = runner();
    Skill perception = c.getSkill("Wahrnehmung");
    assertNotNull(perception);
    perception.setRating(3);
    assertEquals(7, c.getStatusTestPool(StatusTest.PERCEPTION).orElseThrow().dice());
    // Ausweichen ohne Stufe: Defaulting mit Reaktion − 1
    assertEquals(2, c.getStatusTestPool(StatusTest.DODGE).orElseThrow().dice());
  }

  @Test
  void sprintUsesSprintPool() {
    Character c = runner();
    assertEquals(c.getSprintPool(), c.getStatusTestPool(StatusTest.SPRINT));
  }

  @Test
  void everyTestHasAtMostOneSource() {
    for (StatusTest test : StatusTest.values()) {
      assertTrue(test.getAttributeTest() == null || test.getSkillName() == null, test.name());
    }
  }
}
