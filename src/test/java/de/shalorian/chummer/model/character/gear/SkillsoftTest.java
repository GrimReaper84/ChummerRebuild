package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Talentsofts mit Talentleitungen bzw. Neuralinterface (GRW S. 377, 389). */
class SkillsoftTest {

  private static Character character() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.AGI).setValue(4);
    c.getAttribute(AttributeType.INT).setValue(3);
    return c;
  }

  private static Gear soft(Character c, String name, int rating, String extra) {
    GearData data = Objects.requireNonNull(GearDataLoader.findByName(name), name);
    return c.getEquipment().addGear(new Gear(data, rating, data.packSize(), extra, null));
  }

  private static void implant(Character c, String name, int rating) {
    c.getEquipment().addCyberware(new Cyberware(
        Objects.requireNonNull(CyberwareDataLoader.findByName(name), name), CyberwareGrade.STANDARD, rating));
  }

  private static int pool(Character c, String skill) {
    return c.getSkillDicePool(Objects.requireNonNull(c.getSkill(skill))).map(DicePool::dice).orElse(-1);
  }

  @Test
  void activesoftNeedsSkillwires() {
    Character c = character();
    Objects.requireNonNull(c.getSkill("Pistolen")).setRating(1);
    Gear pistols = soft(c, "Aktionssoft", 4, "Pistolen");
    pistols.setRunning(true);
    assertEquals("keine Talentleitungen", c.getSkillsoftStatus(pistols).problem());
    assertEquals(5, pool(c, "Pistolen"));
    implant(c, "Talentleitungen", 3);
    assertEquals("Stufe über den Talentleitungen (3)", c.getSkillsoftStatus(pistols).problem());
    assertEquals(5, pool(c, "Pistolen"));
    pistols.setRunning(false); // sonst 7 von 6 Stufen geladen: überlastet
    Gear low = soft(c, "Aktionssoft", 3, "Pistolen");
    low.setRunning(true);
    assertEquals(3, c.getSkillsoftStatus(low).rating());
    assertEquals(7, pool(c, "Pistolen")); // höherer Wert zählt: Soft 3 statt Fertigkeit 1, + GES 4
  }

  @Test
  void ownSkillWinsWhenHigher() {
    Character c = character();
    implant(c, "Talentleitungen", 4);
    Objects.requireNonNull(c.getSkill("Pistolen")).setRating(5);
    soft(c, "Aktionssoft", 4, "Pistolen").setRunning(true);
    assertEquals(9, pool(c, "Pistolen"));
  }

  @Test
  void loadedLimit() {
    Character c = character();
    Gear first = soft(c, "Aktionssoft", 2, "Pistolen");
    assertEquals("Aktionssofts brauchen Talentleitungen (GRW S. 389).", c.getSkillsoftLoadProblem(first));
    implant(c, "Talentleitungen", 2);
    assertNull(c.getSkillsoftLoadProblem(first));
    first.setRunning(true);
    Gear second = soft(c, "Aktionssoft", 2, "Gewehre");
    assertNull(c.getSkillsoftLoadProblem(second));
    second.setRunning(true);
    Gear third = soft(c, "Aktionssoft", 1, "Ausweichen");
    assertEquals(4, c.getLoadedActivesoftRating());
    assertEquals("Talentleitungen ausgelastet: 4 von 4 Stufen geladen (GRW S. 389)", c.getSkillsoftLoadProblem(third));
  }

  @Test
  void overloadedSkillwiresStopAllActivesofts() {
    // Review-Befund 3: z.B. aus einer Datei oder nach dem Senken der Leitungsstufe 8 von 6 Stufen geladen (S. 389)
    Character c = character();
    implant(c, "Talentleitungen", 3);
    for (String skill : new String[] {"Pistolen", "Gewehre", "Ausweichen", "Klingenwaffen"}) {
      soft(c, "Aktionssoft", 2, skill).setRunning(true);
    }
    Gear any = c.getSkillsofts().getFirst();
    assertEquals(0, c.getSkillsoftStatus(any).rating());
    assertEquals("Talentleitungen überlastet: 8 von 6 Stufen geladen", c.getSkillsoftStatus(any).problem());
    any.setRunning(false);
    assertEquals(2, c.getSkillsoftStatus(c.getSkillsofts().get(1)).rating());
  }

  @Test
  void knowsoftNeedsNeuralInterface() {
    Character c = character();
    c.addKnowledgeSkill("Japanisch", KnowledgeSkillCategory.LANGUAGE);
    Gear lingua = soft(c, "Linguasoft", 3, "Japanisch");
    lingua.setRunning(true);
    assertEquals("kein Neuralinterface (Datenbuchse oder Sim-Modul)", c.getSkillsoftStatus(lingua).problem());
    implant(c, "Datenbuchse", 0);
    assertEquals(3, c.getSkillsoftStatus(lingua).rating());
    assertEquals(6, c.getKnowledgeSkillDicePool(c.getKnowledgeSkills().getFirst()).orElseThrow().dice());
    Gear knowsoft = soft(c, "Wissenssoft", 2, "Konzernpolitik");
    knowsoft.setRunning(true);
    assertTrue(c.getSkillsoftPool(knowsoft).isEmpty()); // ohne eigene Fertigkeit keine Kategorie (Annahme A4)
    Gear other = soft(c, "Linguasoft", 2, "Russisch");
    other.setRunning(true);
    assertEquals(5, c.getSkillsoftPool(other).orElseThrow().dice()); // Stufe 2 + INT 3
  }

  @Test
  void noMagicSkillsAndNoTemplatesForActivesofts() {
    assertTrue(ExtraKind.ACTIVE_SKILL.suggestions().contains("Pistolen"));
    assertFalse(ExtraKind.ACTIVE_SKILL.suggestions().contains("Spruchzauberei"));
    assertFalse(ExtraKind.ACTIVE_SKILL.suggestions().contains("Exotische Nahkampfwaffe"));
    GearData data = Objects.requireNonNull(GearDataLoader.findByName("Aktionssoft"));
    assertThrows(IllegalArgumentException.class, () -> new Gear(data, 2, data.packSize(), "Spruchzauberei", null));
  }

  @Test
  void notLoadedDoesNothing() {
    Character c = character();
    implant(c, "Talentleitungen", 4);
    Gear pistols = soft(c, "Aktionssoft", 4, "Pistolen");
    Skill skill = Objects.requireNonNull(c.getSkill("Pistolen"));
    assertEquals(3, c.getSkillDicePool(skill).orElseThrow().dice()); // ohne Wert GES − 1
    assertEquals("nicht geladen", c.getSkillsoftStatus(pistols).problem());
  }
}
