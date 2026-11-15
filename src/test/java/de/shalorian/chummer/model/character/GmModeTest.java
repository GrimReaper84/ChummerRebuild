package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.ExoticSkills;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import de.shalorian.chummer.persistence.CharacterStore;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Spielleiter-Modus: Regelsperren entfallen, GRW-Obergrenzen bleiben, nichts wird gespeichert (ADR-060). */
class GmModeTest {

  private static Character character() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static QualityData quality(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name), name);
  }

  @Test
  void creationCapsLifted() {
    Character c = character();
    skill(c, "Ausweichen").setRating(6);
    Skill throwing = skill(c, "Wurfwaffen");
    throwing.setRating(4);
    assertFalse(c.canRaiseSkill(throwing)); // 6/5-Regel: schon eine Fertigkeit auf 6
    for (int i = 0; i < 4; i++) {
      c.raiseGroup("Feuerwaffen");
    }
    assertFalse(c.canRaiseGroupAtCreation("Feuerwaffen")); // Gruppen bei der Erschaffung höchstens 4
    c.setGmMode(true);
    assertTrue(c.canRaiseSkill(throwing));
    assertTrue(c.canRaiseGroupAtCreation("Feuerwaffen"));
  }

  @Test
  void rulebookMaximaStay() {
    // GRW-Obergrenze 6 bleibt auch im Spielleiter-Modus: canRaiseSkill hebt nur Sperren auf, nicht den Höchstwert
    Character c = character();
    c.setGmMode(true);
    Skill dodge = skill(c, "Ausweichen");
    dodge.setRating(6);
    assertEquals(6, dodge.getMaxRating());
    assertThrows(IllegalArgumentException.class, () -> dodge.setRating(7));
  }

  @Test
  void spellAndComplexFormCapsLifted() {
    // Erschaffung: Zauber höchstens 2 × Spruchzauberei, Komplexe Formen höchstens 2 × LOG
    Character c = character();
    c.addQuality(quality("Technomancer"), false);
    c.getAttribute(AttributeType.LOG).setValue(1);
    c.addComplexForm(Objects.requireNonNull(ComplexFormDataLoader.findByName("Analyse")));
    c.addComplexForm(Objects.requireNonNull(ComplexFormDataLoader.findByName("Befehl")));
    assertFalse(c.canAddComplexFormAtCreation());
    assertFalse(c.canAddSpellAtCreation());
    c.setGmMode(true);
    assertTrue(c.canAddComplexFormAtCreation());
    assertTrue(c.canAddSpellAtCreation());
  }

  @Test
  void mysticAdeptMayLowerPowerShareInCareer() {
    Character c = withGift("Magieradept", 4);
    c.getAdeptPowers().setAdeptMagic(2);
    c.setCreated(true);
    assertEquals(2, c.getAdeptPowers().getMinAdeptMagic());
    c.setGmMode(true);
    assertEquals(0, c.getAdeptPowers().getMinAdeptMagic()); // keine Kraft gekauft: bis 0
  }

  @Test
  void stackingRulesStay() {
    // Ungebildet bei schon belegter Gruppe mit doppelten Kosten bleibt gesperrt, weil die Rechnung es nicht abbildet
    Character c = character();
    c.raiseGroup("Elektronik");
    c.setGmMode(true);
    assertEquals("Fertigkeitsgruppe Elektronik hat bereits einen Wert",
        c.getQualityBlockReason(quality("Ungebildet"), false));
  }

  @Test
  void qualityLimitsLifted() {
    Character c = character();
    c.setCreated(true);
    QualityData mage = quality("Zauberer");
    assertEquals("Nur bei der Erschaffung wählbar (S. 313)", c.getQualityBlockReason(mage, false));
    c.setGmMode(true);
    assertNull(c.getQualityBlockReason(mage, false));
    c.addQuality(mage, false);
    assertEquals("Bereits gewählt", c.getQualityBlockReason(mage, false)); // strukturell bleibt
  }

  @Test
  void careerLocksLifted() {
    Character c = character();
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    Skill spellcasting = skill(c, "Spruchzauberei");
    c.setCreated(true);
    assertEquals("Im Karrieremodus wird nur gesteigert", c.getExoticSkillRemovalProblem(whip));
    assertFalse(c.canRaiseSkill(spellcasting)); // Mundaner
    c.setGmMode(true);
    assertNull(c.getExoticSkillRemovalProblem(whip));
    assertTrue(c.canRaiseSkill(spellcasting));
    assertFalse(c.canRaiseSkill(skill(c, ExoticSkills.MELEE))); // Grundzeile bleibt ohne Wert
  }

  @Test
  void availabilityAtCreation() {
    Character c = character();
    assertFalse(c.getEquipment().isCyberwareGradeAvailable(CyberwareGrade.BETAWARE));
    c.setGmMode(true);
    assertTrue(c.getEquipment().isCyberwareGradeAvailable(CyberwareGrade.BETAWARE));
  }

  @Test
  void notSaved() {
    Character c = character();
    c.setGmMode(true);
    assertFalse(CharacterStore.restore(CharacterStore.snapshot(c)).isGmMode());
  }
}
