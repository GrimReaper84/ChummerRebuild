package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.buy;
import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Rechnerische Wirkungen der Adeptenkräfte (GRW S. 230–232) und Astralzugang (S. 108, 149). */
class CharacterAdeptPowerEffectsTest {

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name), name);
  }

  @Test
  void improvedReflexesRaiseReactionInitiativeAndPasses() {
    // GRW S. 230: Stufe 2 → Reaktion +2, Initiative +2, 3 Durchgänge
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.REA).setValue(3);
    c.getAttribute(AttributeType.INT).setValue(3);
    int initiative = c.getInitiativeAttribute();
    buy(c, "Gesteigerte Reflexe", "", 2);
    assertEquals(5, c.getAugmentedAttributeValue(AttributeType.REA));
    assertEquals(initiative + 2, c.getInitiativeAttribute());
    assertEquals(3, c.getInitiativePasses());
  }

  @Test
  void improvedAttributeRaisesAugmentedValue() {
    // GRW S. 230: STR 5 + Stufe 2 → 7
    Character c = withGift("Adept", 6);
    c.getAttribute(AttributeType.STR).setValue(5);
    buy(c, "Gesteigertes körperliches Attribut", "STR", 2);
    assertEquals(7, c.getAugmentedAttributeValue(AttributeType.STR));
    assertEquals(5, c.getAttribute(AttributeType.STR).getValue());
  }

  @Test
  void improvedBodyRaisesConditionMonitorAndLowersEncumbrance() {
    // GRW S. 94, 190: verstärkte Konstitution zählt
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.BOD).setValue(2);
    c.getEquipment().addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Panzerjacke"))));
    assertEquals(9, c.getPhysicalBoxes());
    assertEquals(2, c.getPhysicalOverflowBoxes());
    int penalty = c.getEquipment().getArmorEncumbrancePenalty(); // 8/6 gegen 4 → −2 −1
    assertEquals(-3, penalty);

    buy(c, "Gesteigertes körperliches Attribut", "BOD", 2);
    assertEquals(10, c.getPhysicalBoxes());
    assertEquals(4, c.getPhysicalOverflowBoxes());
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty()); // 8/6 gegen 8 → keine Behinderung
  }

  @Test
  void careerKarmaCostIncludesImprovedAttribute() {
    // GRW S. 230: STR 4 + Stufe 1 → nächste Stufe kostet (4 + 1 + 1) × 5
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.STR).setValue(4);
    assertEquals(25, c.getAttributeKarmaCost(AttributeType.STR));
    buy(c, "Gesteigertes körperliches Attribut", "STR", 1);
    assertEquals(30, c.getAttributeKarmaCost(AttributeType.STR));
  }

  @Test
  void improvedSkillAddsDice() {
    // GRW S. 232
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.AGI).setValue(3);
    skill(c, "Pistolen").setRating(3);
    int before = c.getSkillDicePool(skill(c, "Pistolen")).orElseThrow().dice();
    buy(c, "Verbesserte Fertigkeit", "Pistolen", 1);
    assertEquals(before + 1, c.getSkillDicePool(skill(c, "Pistolen")).orElseThrow().dice());
    assertTrue(c.getSkillDicePool(skill(c, "Pistolen")).orElseThrow().breakdown().contains("Adeptenkräfte 1"));
  }

  @Test
  void improvedPerceptionAddsDiceToPerceptionAndAssensing() {
    // GRW S. 232: auch Askennen
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.INT).setValue(3);
    skill(c, "Wahrnehmung").setRating(2);
    buy(c, "Astrale Wahrnehmung", "", 1);
    skill(c, "Askennen").setRating(1);
    buy(c, "Verbesserte Wahrnehmung", "", 2);
    assertEquals(7, c.getSkillDicePool(skill(c, "Wahrnehmung")).orElseThrow().dice());
    assertEquals(6, c.getSkillDicePool(skill(c, "Askennen")).orElseThrow().dice());
  }

  @Test
  void painResistanceIgnoresBoxes() {
    // GRW S. 231: Stufe 3 → bis 3 Kästchen ohne Modifikator
    Character c = withGift("Adept", 4);
    buy(c, "Schmerzresistenz", "", 3);
    c.setPhysicalDamage(3);
    c.setStunDamage(3);
    assertEquals(0, c.getWoundModifier());
    c.setPhysicalDamage(6);
    assertEquals(-1, c.getWoundModifier());
  }

  @Test
  void mysticArmorAddsProtectionWithoutEncumbrance() {
    // GRW S. 231
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getEquipment().addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Panzerjacke"))));
    int penalty = c.getEquipment().getArmorEncumbrancePenalty();
    buy(c, "Mystischer Panzer", "", 2);
    assertEquals(10, c.getArmorBallistic());
    assertEquals(8, c.getArmorImpact());
    assertEquals(penalty, c.getEquipment().getArmorEncumbrancePenalty());
  }

  @Test
  void astralPerceptionUnlocksAssensingForAdepts() {
    // GRW S. 108, 149
    Character adept = withGift("Adept", 3);
    assertFalse(adept.canRaiseSkill(skill(adept, "Askennen")));
    assertFalse(adept.canRaiseSkill(skill(adept, "Astralkampf")));
    buy(adept, "Astrale Wahrnehmung", "", 1);
    assertTrue(adept.canRaiseSkill(skill(adept, "Askennen")));
    assertTrue(adept.canRaiseSkill(skill(adept, "Astralkampf")));
    assertFalse(adept.canRaiseSkill(skill(adept, "Spruchzauberei")));

    Character mystic = withGift("Magieradept", 3);
    assertFalse(mystic.canRaiseSkill(skill(mystic, "Askennen")));
    assertTrue(mystic.canRaiseSkill(skill(mystic, "Spruchzauberei")));
    mystic.getAdeptPowers().setAdeptMagic(1);
    buy(mystic, "Astrale Wahrnehmung", "", 1);
    assertTrue(mystic.canRaiseSkill(skill(mystic, "Askennen")));

    Character magician = withGift("Zauberer", 3);
    assertTrue(magician.canRaiseSkill(skill(magician, "Askennen")));
  }

  @Test
  void removingAstralPerceptionLocksAssensing() {
    Character c = withGift("Adept", 3);
    AdeptPower astral = buy(c, "Astrale Wahrnehmung", "", 1);
    skill(c, "Askennen").setRating(2);
    assertEquals(List.of("Askennen 2 → 0 (nicht mehr erlernbar)"), c.getAdeptPowerRemovalConsequences(astral));
    c.removeAdeptPower(astral);
    assertEquals(0, skill(c, "Askennen").getRating());
  }
  @Test
  void improvedBodyRemovesEncumbranceExactlyAtLimit() {
    // GRW S. 190: Grenze doppelte Konstitution; KON 3 + 1 → 8, Panzerjacke 8/6 liegt genau darauf
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getEquipment().addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Panzerjacke"))));
    assertEquals(-1, c.getEquipment().getArmorEncumbrancePenalty());
    buy(c, "Gesteigertes körperliches Attribut", "BOD", 1);
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());
  }
}
