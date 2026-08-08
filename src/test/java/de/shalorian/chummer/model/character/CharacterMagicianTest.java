package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SR4-Grundregelwerk S. 208: Magische Aktionsfertigkeiten und Zauber sind nur Charakteren mit der
 * Gabe "Magier" zugänglich — {@link Character#isMagician()} muss das genauer erkennen als {@link
 * Character#hasMagic()}, das auch für Adepten {@code true} liefert.
 */
class CharacterMagicianTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  @Test
  void newCharacterIsNotMagician() {
    assertFalse(mensch("Test").isMagician());
  }

  @Test
  void magicianQualityMakesCharacterAMagician() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);

    c.addQuality(magier, false);

    assertTrue(c.isMagician());
    assertTrue(c.hasMagic());
  }

  @Test
  void adeptQualityDoesNotMakeCharacterAMagician() {
    Character c = mensch("Test");
    QualityData adept = QualityDataLoader.findByName("Adept");
    assertNotNull(adept);

    c.addQuality(adept, false);

    assertFalse(c.isMagician());
    assertTrue(c.hasMagic());
  }

  @Test
  void removingMagicianQualityRevokesMagicianStatus() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);

    c.addQuality(magier, false);
    c.removeQuality("Magier");

    assertFalse(c.isMagician());
  }

  @Test
  void nonMagicianCannotRaiseMagicalSkillsEvenInCareerMode() {
    Character c = mensch("Test");
    Skill spellcasting = c.getSkill("Spruchzauberei");
    assertNotNull(spellcasting);

    assertFalse(c.canRaiseSkill(spellcasting));

    c.setCreated(true);
    assertFalse(c.canRaiseSkill(spellcasting), "Magier-Sperre gilt auch im Karrieremodus");
  }

  @Test
  void magicianCanRaiseMagicalSkills() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);
    c.addQuality(magier, false);
    Skill spellcasting = c.getSkill("Spruchzauberei");
    assertNotNull(spellcasting);

    assertTrue(c.canRaiseSkill(spellcasting));
  }

  @Test
  void magicianStatusDoesNotAffectNonMagicalSkills() {
    Character c = mensch("Test");
    Skill bogen = c.getSkill("Bogenschießen");
    assertNotNull(bogen);

    assertTrue(c.canRaiseSkill(bogen));
  }
}
