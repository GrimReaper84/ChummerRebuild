package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Würfelpools von Fertigkeiten und Attributproben (GRW S. 146–148, 120/122, 166/167). */
class CharacterDicePoolTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name), name);
  }

  private static int dice(Character c, String skill) {
    return c.getSkillDicePool(skill(c, skill)).map(DicePool::dice).orElseThrow();
  }

  @Test
  void poolIsRatingPlusLinkedAttribute() {
    // GRW S. 146: Infiltration 3 und Geschicklichkeit 4 ergeben 7 Würfel
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(4);
    skill(c, "Infiltration").setRating(3);
    assertEquals(7, dice(c, "Infiltration"));
    assertEquals("Infiltration 3 + Geschicklichkeit 4",
        c.getSkillDicePool(skill(c, "Infiltration")).orElseThrow().breakdown());
  }

  @Test
  void defaultingUsesAttributeMinusOne() {
    // GRW S. 147: ohne Wert in Entfesseln nur Geschicklichkeit − 1
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(4);
    assertEquals(3, dice(c, "Entfesseln"));
  }

  @Test
  void defaultingNeverGoesBelowZero() {
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(1);
    assertEquals(0, dice(c, "Entfesseln"));
  }

  @Test
  void noDefaultingForItalicSkills() {
    Character c = mensch();
    assertTrue(c.getSkillDicePool(skill(c, "Medizin")).isEmpty());
    skill(c, "Medizin").setRating(1);
    assertTrue(c.getSkillDicePool(skill(c, "Medizin")).isPresent());
  }

  @Test
  void uncouthMakesSocialSkillsCluelessBelowOne() {
    // GRW S. 122: Ungehobelt – ohne Wert keine Probe auf Soziale Fertigkeiten
    Character c = mensch();
    assertTrue(c.getSkillDicePool(skill(c, "Führung")).isPresent());
    c.addQuality(q("Ungehobelt"), false);
    assertTrue(c.getSkillDicePool(skill(c, "Führung")).isEmpty());
    assertTrue(c.getSkillDicePool(skill(c, "Entfesseln")).isPresent());
    skill(c, "Führung").setRating(1);
    assertTrue(c.getSkillDicePool(skill(c, "Führung")).isPresent());
  }

  @Test
  void incompetenceAllowsNoTest() {
    // GRW S. 120
    Character c = mensch();
    c.addQuality(q("Inkompetenz"), false, "Pistolen");
    assertTrue(c.getSkillDicePool(skill(c, "Pistolen")).isEmpty());
  }

  @Test
  void magicalSkillsNeedAccessAndUseEffectiveMagic() {
    Character c = mensch();
    assertTrue(c.getSkillDicePool(skill(c, "Spruchzauberei")).isEmpty());
    c.addQuality(q("Zauberer"), false);
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(3);
    skill(c, "Spruchzauberei").setRating(2);
    assertEquals(5, dice(c, "Spruchzauberei"));
  }

  @Test
  void armorEncumbranceLowersAgilityPools() {
    // GRW S. 190: KON 3 mit 11/7 Panzerung → −4 auf Geschicklichkeit
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getAttribute(AttributeType.AGI).setValue(5);
    skill(c, "Pistolen").setRating(3);
    c.getEquipment().addArmor(new Armor(Objects.requireNonNull(
        ArmorDataLoader.findByName("Actioneer-Geschäftskleidung"))));
    c.getEquipment().addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Gefütterter Mantel"))));
    assertEquals(4, dice(c, "Pistolen"));
  }

  @Test
  void woundModifierLowersPools() {
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(4);
    skill(c, "Pistolen").setRating(3);
    c.setPhysicalDamage(3);
    assertEquals(6, dice(c, "Pistolen"));
    assertEquals("Pistolen 3 + Geschicklichkeit 4 − 1 Verletzung",
        c.getSkillDicePool(skill(c, "Pistolen")).orElseThrow().breakdown());
  }

  @Test
  void knowledgeSkillsDefaultExceptLanguages() {
    // GRW S. 146: von den Wissensfertigkeiten ist nur Sprache kursiv
    Character c = mensch();
    c.getAttribute(AttributeType.INT).setValue(4);
    c.addKnowledgeSkill("Seattle", KnowledgeSkillCategory.STREET);
    c.addKnowledgeSkill("Japanisch", KnowledgeSkillCategory.LANGUAGE);
    KnowledgeSkill street = c.getKnowledgeSkills().get(0);
    KnowledgeSkill language = c.getKnowledgeSkills().get(1);
    assertEquals(3, c.getKnowledgeSkillDicePool(street).orElseThrow().dice());
    assertTrue(c.getKnowledgeSkillDicePool(language).isEmpty());
    street.setRating(2);
    assertEquals(6, c.getKnowledgeSkillDicePool(street).orElseThrow().dice());
  }

  @Test
  void uneducatedMakesAcademicKnowledgeClueless() {
    // GRW S. 121
    Character c = mensch();
    c.addKnowledgeSkill("Chemie", KnowledgeSkillCategory.ACADEMIC);
    KnowledgeSkill academic = c.getKnowledgeSkills().getFirst();
    assertTrue(c.getKnowledgeSkillDicePool(academic).isPresent());
    c.addQuality(q("Ungebildet"), false);
    assertTrue(c.getKnowledgeSkillDicePool(academic).isEmpty());
  }

  @Test
  void nativeLanguageHasNoPool() {
    Character c = mensch();
    c.setNativeLanguage("Englisch");
    assertTrue(c.getKnowledgeSkillDicePool(Objects.requireNonNull(c.getNativeLanguage())).isEmpty());
  }

  @Test
  void specializationAddsTwoDice() {
    // GRW S. 147: Infiltration 5 (Stadt +2) mit Geschicklichkeit 3 → 8, in der Stadt 10
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(3);
    skill(c, "Infiltration").setRating(5);
    DicePool pool = c.getSkillDicePool(skill(c, "Infiltration")).orElseThrow();
    assertEquals(8, pool.dice());
    assertEquals(10, Character.withSpecialization(pool));
  }

  @Test
  void uneducatedAndInfirmBlockDefaultingInTheirCategory() {
    // GRW S. 120/121
    Character c = mensch();
    c.addQuality(q("Ungebildet"), false);
    c.addQuality(q("Körperliche Schwäche"), true); // gratis: beide zusammen über 35 BP
    assertTrue(c.getSkillDicePool(skill(c, "Computer")).isEmpty());
    assertTrue(c.getSkillDicePool(skill(c, "Entfesseln")).isEmpty());
    assertTrue(c.getSkillDicePool(skill(c, "Führung")).isPresent());
  }

  @Test
  void magicalSkillUsesMagicReducedByEssenceLoss() {
    // GRW S. 208: jeder angebrochene Punkt Essenzverlust senkt die Magie um 1
    Character c = mensch();
    c.addQuality(q("Zauberer"), false);
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(4);
    skill(c, "Spruchzauberei").setRating(2);
    c.getEquipment().addCyberware(new Cyberware(
        Objects.requireNonNull(CyberwareDataLoader.findByName("Datenbuchse")), CyberwareGrade.STANDARD, 0));
    assertEquals(5, dice(c, "Spruchzauberei"));
  }

  @Test
  void attributeTestsAddTwoAttributes() {
    // GRW S. 167: Selbstbeherrschung = Willenskraft + Charisma
    Character c = mensch();
    c.getAttribute(AttributeType.WIL).setValue(3);
    c.getAttribute(AttributeType.CHA).setValue(4);
    assertEquals(7, c.getAttributeTestPool(AttributeTest.COMPOSURE).dice());
  }
}
