package de.shalorian.chummer.model.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Regeln für Schergen (GRW S. 324/325). */
class NpcRulesTest {

  static Character human(int bod, int wil) {
    Character c = new Character("Scherge", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.BOD).setValue(bod);
    c.getAttribute(AttributeType.WIL).setValue(wil);
    return c;
  }

  @Test
  void monitorUsesHigherOfBodyAndWillpowerRoundedUp() {
    assertEquals(10, NpcRules.monitorBoxes(human(4, 3)));
    assertEquals(11, NpcRules.monitorBoxes(human(3, 5)));
    assertEquals(11, NpcRules.monitorBoxes(human(5, 5)));
    assertEquals(9, NpcRules.monitorBoxes(human(1, 1)));
  }

  @Test
  void augmentedBodyCounts() {
    // Hyperschilddrüse +1 KON (S. 393): 8 + ⌈5/2⌉ = 11 statt 10
    Character c = human(4, 3);
    c.getImplants().add(new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName("Hyperschilddrüse")),
        CyberwareGrade.STANDARD, 0));
    assertEquals(5, c.getAugmentedAttributeValue(AttributeType.BOD));
    assertEquals(11, NpcRules.monitorBoxes(c));
  }

  @Test
  void woundModifierPerThreeBoxes() {
    assertEquals(0, NpcRules.woundModifier(0));
    assertEquals(0, NpcRules.woundModifier(2));
    assertEquals(-1, NpcRules.woundModifier(3));
    assertEquals(-2, NpcRules.woundModifier(6));
  }

  @Test
  void fleeThresholdsAreStrict() {
    assertFalse(ProfessionalRating.of(0).isFleeing(2, 8));
    assertTrue(ProfessionalRating.of(0).isFleeing(3, 8));
    assertFalse(ProfessionalRating.of(2).isFleeing(4, 8));
    assertTrue(ProfessionalRating.of(2).isFleeing(5, 8));
    assertFalse(ProfessionalRating.of(4).isFleeing(6, 8));
    assertTrue(ProfessionalRating.of(4).isFleeing(7, 8));
    assertFalse(ProfessionalRating.of(6).isFleeing(8, 8));
  }

  @Test
  void ratingClasses() {
    assertEquals(ProfessionalRating.UNTRAINED, ProfessionalRating.of(0));
    assertEquals(ProfessionalRating.SEMI_TRAINED, ProfessionalRating.of(1));
    assertEquals(ProfessionalRating.TRAINED, ProfessionalRating.of(4));
    assertEquals(ProfessionalRating.ELITE, ProfessionalRating.of(5));
    assertThrows(IllegalArgumentException.class, () -> ProfessionalRating.of(7));
    assertThrows(IllegalArgumentException.class, () -> ProfessionalRating.of(-1));
  }

  @Test
  void leaderNeedsFourMorePointsEach() {
    Character grunt = human(3, 3);
    Objects.requireNonNull(grunt.getSkill("Pistolen")).setRating(3);
    // Scherge: KON 3, WIL 3, sonst 1 → Summe 12; Anführer zunächst 15 (+3)
    Character leader = human(3, 3);
    leader.getAttribute(AttributeType.AGI).setValue(2);
    leader.getAttribute(AttributeType.REA).setValue(2);
    leader.getAttribute(AttributeType.STR).setValue(2);
    Objects.requireNonNull(leader.getSkill("Pistolen")).setRating(6);
    // Attribute +3, Fertigkeiten +3
    assertEquals("Ein Anführer sollte in der Summe mindestens 4 Punkte mehr Attribute und Fertigkeiten haben als ein "
        + "Scherge (S. 325)", NpcRules.leaderHint(grunt, leader));
    leader.getAttribute(AttributeType.STR).setValue(3);
    String onlySkills = NpcRules.leaderHint(grunt, leader);
    assertNotNull(onlySkills);
    assertTrue(onlySkills.contains("mehr Fertigkeiten"), onlySkills);
    Objects.requireNonNull(leader.getSkill("Ausweichen")).setRating(1);
    assertNull(NpcRules.leaderHint(grunt, leader));
    leader.getAttribute(AttributeType.STR).setValue(2);
    String onlyAttributes = NpcRules.leaderHint(grunt, leader);
    assertNotNull(onlyAttributes);
    assertTrue(onlyAttributes.contains("mehr Attribute haben"), onlyAttributes);
  }
}
