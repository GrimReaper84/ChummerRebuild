package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.buy;
import static de.shalorian.chummer.model.character.AdeptTestSupport.power;
import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.add;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.data;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.mensch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Rechnende Wirkungen von Implantaten (GRW S. 388, 389, 393, 394) und ihr Zusammenspiel mit Adeptenkräften. */
class CharacterImplantEffectsTest {

  private static int dice(Character c, String skillName) {
    Skill skill = Objects.requireNonNull(c.getSkill(skillName), skillName);
    skill.setRating(1);
    return c.getSkillDicePool(skill).orElseThrow().dice();
  }

  @Test
  void muscleReplacementRaisesStrengthAndAgility() {
    Character c = mensch();
    add(c, "Kunstmuskeln", 2);
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.STR));
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.AGI));
    assertEquals(1, c.getAttribute(AttributeType.STR).getValue());
  }

  @Test
  void implantAndAdeptBoostsShareTheAugmentedMaximum() {
    // natürlich 6, Kraft +2, Kunstmuskeln +4: gedeckelt auf 9 = 6 × 1,5 (S. 91)
    Character c = withGift("Adept", 6);
    c.getAttribute(AttributeType.STR).setValue(6);
    buy(c, "Gesteigertes körperliches Attribut", "STR", 2);
    add(c, "Kunstmuskeln", 4);
    assertEquals(9, c.getAugmentedAttributeValue(AttributeType.STR));
  }

  @Test
  void wiredReflexesRaiseReactionAndPasses() {
    Character c = mensch();
    add(c, "Reflexbooster", 2);
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.REA));
    assertEquals(3, c.getInitiativePasses());
    assertEquals(4, c.getInitiativeAttribute());
  }

  @Test
  void synapticBoosterAddsPasses() {
    Character c = mensch();
    add(c, "Synapsenbeschleuniger", 1);
    assertEquals(2, c.getInitiativePasses());
  }

  @Test
  void implantArmorAddsWithoutEncumbrance() {
    Character c = mensch();
    add(c, "Dermalpanzerung", 2);
    add(c, "Kompositknochen (Titan)", 0);
    assertEquals(3, c.getArmorBallistic());
    assertEquals(3, c.getArmorImpact());
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());
    assertEquals(1, c.getAugmentedAttributeValue(AttributeType.AGI));
  }

  @Test
  void aluminiumBonesAddImpactOnly() {
    Character c = mensch();
    add(c, "Kompositknochen (Aluminium)", 0);
    assertEquals(0, c.getArmorBallistic());
    assertEquals(1, c.getArmorImpact());
  }

  @Test
  void synthacardiumHelpsOnlyAthletics() {
    Character c = mensch();
    add(c, "Synthacardium", 2);
    assertEquals(1 + 1 + 2, dice(c, "Laufen"));
    assertEquals(1 + 1, dice(c, "Pistolen"));
  }

  @Test
  void pheromonesHelpSocialSkills() {
    Character c = mensch();
    add(c, "Maßgeschneiderte Pheromone", 3);
    assertEquals(1 + 1 + 3, dice(c, "Gebräuche"));
    assertEquals(1 + 1, dice(c, "Laufen"));
  }

  @Test
  void enhancedArticulationHelpsSkillsWithPhysicalAttribute() {
    Character c = mensch();
    add(c, "Hochleistungsgelenke", 0);
    assertEquals(1 + 1 + 1, dice(c, "Pistolen"));
    assertEquals(1 + 1, dice(c, "Hacking"));
  }

  @Test
  void poolBreakdownNamesImplants() {
    Character c = mensch();
    add(c, "Hochleistungsgelenke", 0);
    Skill pistols = Objects.requireNonNull(c.getSkill("Pistolen"));
    pistols.setRating(1);
    assertEquals("Pistolen 1 + Geschicklichkeit 1 + Implantate 1",
        c.getSkillDicePool(pistols).orElseThrow().breakdown());
  }

  @Test
  void reflexRecorderHelpsChosenSkillOrGroup() {
    Character c = mensch();
    add(c, "Reflexrekorder (Fertigkeit)", "Pistolen");
    add(c, "Reflexrekorder (Fertigkeitsgruppe)", "Athletik");
    assertEquals(1 + 1 + 1, dice(c, "Pistolen"));
    assertEquals(1 + 1 + 1, dice(c, "Klettern"));
    assertEquals(1 + 1, dice(c, "Gewehre"));
  }

  @Test
  void mnemonicEnhancerHelpsKnowledgeSkills() {
    Character c = mensch();
    add(c, "Mnemoverstärker", 2);
    c.addKnowledgeSkill("Seattle", KnowledgeSkillCategory.STREET);
    KnowledgeSkill street = c.getKnowledgeSkills().getFirst();
    street.setRating(2);
    assertEquals(2 + 1 + 2, c.getKnowledgeSkillDicePool(street).orElseThrow().dice());
  }

  @Test
  void damageCompensatorIgnoresBoxes() {
    Character c = mensch();
    add(c, "Schadenskompensator", 2);
    c.setPhysicalDamage(4);
    assertEquals(0, c.getWoundModifier());
    c.setPhysicalDamage(5);
    assertEquals(-1, c.getWoundModifier());
  }

  @Test
  void suprathyroidRaisesBodyAndMonitor() {
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(2);
    int before = c.getPhysicalBoxes();
    add(c, "Hyperschilddrüse", 0);
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.BOD));
    assertEquals(before + 1, c.getPhysicalBoxes());
  }

  @Test
  void cerebralBoosterDoesNotAddKnowledgePoints() {
    // Implantat-Logik erhöht die freien Wissensfertigkeitspunkte nicht (S. 111)
    Character c = mensch();
    int points = c.getKnowledgeSkillPointPool();
    add(c, "Zerebralbooster", 2);
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.LOG));
    assertEquals(points, c.getKnowledgeSkillPointPool());
  }

  @Test
  void improvedReflexesAndTechnicalBoostersExcludeEachOther() {
    // Gesteigerte Reflexe ist mit technischer Initiativesteigerung nicht kompatibel (S. 230)
    Character adept = withGift("Adept", 6);
    assertNull(adept.getImplants().getBlockReason(data("Reflexbooster")));
    buy(adept, "Gesteigerte Reflexe", "", 1);
    assertEquals("Nicht zusammen mit Gesteigerte Reflexe",
        adept.getImplants().getBlockReason(data("Reaktionsverbesserung")));

    Character wired = withGift("Adept", 6);
    add(wired, "Reflexbooster", 1);
    assertNotNull(wired.getAdeptPowers().getBlockReason(power("Gesteigerte Reflexe")));
    assertNull(wired.getImplants().getBlockReason(data("Hyperschilddrüse")));
  }

  @Test
  void reflexRecordersDoNotStackOnOneSkill() {
    // je Fertigkeit nur ein Reflexrekorder (S. 394), auch nicht Fertigkeit plus Gruppe
    Character c = mensch();
    add(c, "Reflexrekorder (Fertigkeitsgruppe)", "Feuerwaffen");
    assertEquals("Gruppe Feuerwaffen hat bereits einen Reflexrekorder",
        c.getImplants().getAddProblem(data("Reflexrekorder (Fertigkeit)"), "Pistolen"));
    assertNull(c.getImplants().getAddProblem(data("Reflexrekorder (Fertigkeit)"), "Laufen"));

    Character d = mensch();
    add(d, "Reflexrekorder (Fertigkeit)", "Pistolen");
    assertEquals("Eine Fertigkeit der Gruppe hat bereits einen Reflexrekorder",
        d.getImplants().getAddProblem(data("Reflexrekorder (Fertigkeitsgruppe)"), "Feuerwaffen"));
    // aus einer Speicherdatei mit beiden zählt nur einer
    add(d, "Reflexrekorder (Fertigkeitsgruppe)", "Feuerwaffen");
    assertEquals(1 + 1 + 1, dice(d, "Pistolen"));
  }

  @Test
  void highPainToleranceExcludesPainEditorAndDamageCompensator() {
    // S. 116: Hohe Schmerztoleranz nicht mit Schmerzeditor oder Schadenskompensator
    Character tolerant = mensch();
    tolerant.addQuality(AdeptTestSupport.quality("Hohe Schmerztoleranz 1"), false);
    assertEquals("Nicht zusammen mit Hohe Schmerztoleranz",
        tolerant.getImplants().getBlockReason(data("Schadenskompensator")));
    assertNotNull(tolerant.getImplants().getBlockReason(data("Schmerzeditor")));

    Character implanted = mensch();
    add(implanted, "Schmerzeditor", 0);
    assertEquals("Nicht zusammen mit Schmerzeditor",
        implanted.getQualityBlockReason(AdeptTestSupport.quality("Hohe Schmerztoleranz 1"), false));
  }
}
