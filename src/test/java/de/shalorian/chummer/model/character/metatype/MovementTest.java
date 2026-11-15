package de.shalorian.chummer.model.character.metatype;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.DicePool;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Bewegungsweiten je Metatyp und Sprinten (GRW S. 177, Tabelle am Bild geprüft). */
class MovementTest {

  @Test
  void movementPerMetatype() {
    assertEquals(new Movement(10, 25), MetatypeDataLoader.find("Mensch").movement());
    assertEquals(new Movement(10, 25), MetatypeDataLoader.find("Elf").movement());
    assertEquals(new Movement(10, 25), MetatypeDataLoader.find("Ork").movement());
    assertEquals(new Movement(15, 35), MetatypeDataLoader.find("Troll").movement());
    assertEquals(new Movement(8, 20), MetatypeDataLoader.find("Zwerg").movement());
  }

  @Test
  void sprintRollsRunningPlusStrength() {
    Character c = new Character("Test", MetatypeDataLoader.find("Troll"));
    assertEquals(new Movement(15, 35), c.getMovement());
    c.getAttribute(AttributeType.STR).setValue(5);
    assertEquals(4, c.getSprintPool().map(DicePool::dice).orElseThrow()); // ohne Wert: STR − 1
    Objects.requireNonNull(c.getSkill("Laufen")).setRating(3);
    assertEquals(8, c.getSprintPool().map(DicePool::dice).orElseThrow());
  }
}
