package de.shalorian.chummer.model.character.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Inhalte des Cheat-Sheets (ADR-065). */
class CheatSheetItemTest {

  private static Character runner() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.INT).setValue(4);
    c.getAttribute(AttributeType.REA).setValue(3);
    return c;
  }

  @Test
  void skillItemsUseSkillPool() {
    Character c = runner();
    Skill perception = c.getSkill("Wahrnehmung");
    assertNotNull(perception);
    perception.setRating(3);
    assertEquals(7, c.getCheatSheetPool(CheatSheetItem.PERCEPTION).orElseThrow().dice());
    // Ausweichen ohne Stufe: Defaulting mit Reaktion − 1
    assertEquals(2, c.getCheatSheetPool(CheatSheetItem.DODGE).orElseThrow().dice());
  }

  @Test
  void movementHasNoPool() {
    assertTrue(runner().getCheatSheetPool(CheatSheetItem.MOVEMENT).isEmpty());
  }

  @Test
  void orderIsDisplayOrder() {
    assertEquals(List.of(CheatSheetItem.PERCEPTION, CheatSheetItem.DODGE, CheatSheetItem.MOVEMENT),
        List.of(CheatSheetItem.values()));
  }
}
