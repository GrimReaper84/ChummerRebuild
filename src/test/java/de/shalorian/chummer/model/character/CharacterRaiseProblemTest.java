package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.template.ArchetypeLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Gründe für gesperrte Steigerungen bei der Erschaffung (Tooltip am +-Knopf). */
class CharacterRaiseProblemTest {

  @Test
  void secondAttributeOnRacialMaximum() {
    // GRW S. 106: nur ein Attribut auf dem natürlichen Maximum
    Character c = new Character("Test", MetatypeDataLoader.find("Troll"));
    c.getAttribute(AttributeType.CHA).setValue(4);
    c.getAttribute(AttributeType.STR).setValue(9);
    assertEquals("Nur ein Attribut auf dem Rassenmaximum (GRW S. 106)", c.getAttributeRaiseProblem(AttributeType.STR));
    assertNull(c.getAttributeRaiseProblem(AttributeType.BOD));
  }

  @Test
  void attributeBpCap() {
    // GRW S. 106: höchstens die Hälfte der 400 BP für Attribute; sechs Attribute auf 4 (je 30) und zwei auf 2 (je 10)
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    for (AttributeType type : AttributeType.values()) {
      c.getAttribute(type).setValue(4);
    }
    c.getAttribute(AttributeType.STR).setValue(2);
    c.getAttribute(AttributeType.WIL).setValue(2);
    assertEquals("Höchstens 200 BP für Attribute (GRW S. 106)", c.getAttributeRaiseProblem(AttributeType.STR));
  }

  @Test
  void groupCapAtCreation() {
    // GRW S. 108: Gruppen bei der Erschaffung höchstens 4
    Character c = Objects.requireNonNull(ArchetypeLoader.findByName("Drohnenrigger")).create();
    assertEquals("Gruppen bei der Erschaffung höchstens 4 (GRW S. 108)", c.getGroupRaiseProblem("Elektronik"));
  }

  @Test
  void sixFiveRule() {
    // GRW S. 108: eine Fertigkeit auf 6 oder zwei auf 5
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Objects.requireNonNull(c.getSkill("Wahrnehmung")).setRating(6);
    Skill dodge = Objects.requireNonNull(c.getSkill("Ausweichen"));
    dodge.setRating(4);
    assertEquals("Bei der Erschaffung eine Fertigkeit auf 6 oder zwei auf 5 (GRW S. 108)",
        c.getSkillRaiseProblem(dodge));
  }

  @Test
  void magicalSkillWithoutGift() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    assertEquals("Nur für Zauberer und Magieradepten (GRW S. 148)",
        c.getSkillRaiseProblem(Objects.requireNonNull(c.getSkill("Spruchzauberei"))));
  }

  @Test
  void careerAndGmModeHaveNoCreationCaps() {
    Character c = Objects.requireNonNull(ArchetypeLoader.findByName("Drohnenrigger")).create();
    c.setGmMode(true);
    assertNull(c.getGroupRaiseProblem("Elektronik"));
  }

  @Test
  void bpShortfallOnlyAtCreationAndOutsideGmMode() {
    Character c = Objects.requireNonNull(ArchetypeLoader.findByName("Straßensamurai")).create();
    assertEquals(0, c.getRemainingBP());
    assertEquals("Nicht genug BP (3 nötig, 0 übrig)", c.getBpShortfallProblem(Character.SPELL_BP_COST));
    assertNull(c.getBpShortfallProblem(0));
    c.setGmMode(true);
    assertNull(c.getBpShortfallProblem(3));
    c.setGmMode(false);
    c.setCreated(true);
    assertNull(c.getBpShortfallProblem(3));
  }

  @Test
  void decouplingAtCreationNeedsRuleOptionOrGmMode() {
    // GRW S. 108: bei der Erschaffung nicht auflösbar, außer der Spielleiter erlaubt es
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    assertFalse(c.canDecoupleGroups());
    c.getRuleOptions().setDecoupleAtCreation(true);
    assertTrue(c.canDecoupleGroups());
    c.getRuleOptions().setDecoupleAtCreation(false);
    c.setGmMode(true);
    assertTrue(c.canDecoupleGroups());
  }
}
