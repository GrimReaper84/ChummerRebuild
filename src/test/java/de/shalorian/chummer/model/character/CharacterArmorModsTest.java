package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.ArmorModDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorProtection;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.Weapon;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Rüstung mit Einbauten am Charakter: Schutz, Sonderschutz, Behinderung, Schild-Malus, Waffen aus Rüstung. */
class CharacterArmorModsTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Armor wear(Character c, String name) {
    Armor armor = new Armor(Objects.requireNonNull(ArmorDataLoader.findByName(name), name));
    c.getEquipment().addArmor(armor);
    return armor;
  }

  private static ArmorMod mod(String name, int rating) {
    return new ArmorMod(Objects.requireNonNull(ArmorModDataLoader.findByName(name), name), rating);
  }

  private static int dice(Character c, String skillName) {
    Skill skill = Objects.requireNonNull(c.getSkill(skillName));
    skill.setRating(1);
    return c.getSkillDicePool(skill).orElseThrow().dice();
  }

  @Test
  void fullBodyArmorWithHelmetProtects12To10() {
    Character c = mensch();
    wear(c, "Ganzkörperpanzerung").install(mod("Helm (Ganzkörperpanzerung)", 0));
    assertEquals(12, c.getArmorBallistic());
    assertEquals(10, c.getArmorImpact());
  }

  @Test
  void helmetAccessoryCountsForEncumbrance() {
    // KON 5 → Grenze 10; GKP 10/8 ohne Helm keine Behinderung, mit Helm 12/10 → Ballistik 2 über → −1
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(5);
    Armor fullBody = wear(c, "Ganzkörperpanzerung");
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());
    fullBody.install(mod("Helm (Ganzkörperpanzerung)", 0));
    assertEquals(-1, c.getEquipment().getArmorEncumbrancePenalty());
  }

  @Test
  void specialProtectionUsesHighestModificationOfEquippedArmor() {
    Character c = mensch();
    wear(c, "Panzerjacke").install(mod("Elektrische Isolierung", 3));
    Armor shield = wear(c, "Ballistischer Schild");
    shield.install(mod("Elektrische Isolierung", 2));
    assertEquals(3, c.getEquipment().getSpecialProtection(ArmorProtection.ELECTRICITY));
    assertEquals(0, c.getEquipment().getSpecialProtection(ArmorProtection.FIRE));
    shield.setEquipped(false);
    c.getEquipment().getArmor().getFirst().setEquipped(false);
    assertEquals(0, c.getEquipment().getSpecialProtection(ArmorProtection.ELECTRICITY));
  }

  @Test
  void specialProtectionLinesUseTotalArmorOfCharacter() {
    // Panzerjacke 8/6 + Elektrische Isolierung 3 → 11/9; Chemische Isolierung gibt Würfel (S. 372)
    Character c = mensch();
    Armor jacket = wear(c, "Panzerjacke");
    jacket.install(mod("Elektrische Isolierung", 3));
    jacket.install(mod("Chemische Isolierung", 2));
    assertEquals(List.of("gegen Elektrizität 11/9", "Toxinwiderstand (Kontakt) +2 Würfel, gegen Säure +2 Panzerung"),
        c.getEquipment().getSpecialProtections().stream().map(Object::toString).toList());
    jacket.setEquipped(false);
    assertTrue(c.getEquipment().getSpecialProtections().isEmpty());
  }

  @Test
  void installAtCreationRespectsAvailabilityIncludingSurcharge() {
    // Ganzkörperpanzerung 14E + Chemische Versiegelung +6 = 20 > 12; Wärmedämpfung 10V ≤ 12 (S. 111, 373)
    Character c = mensch();
    Armor fullBody = wear(c, "Ganzkörperpanzerung");
    ArmorModData seal = Objects.requireNonNull(ArmorModDataLoader.findByName("Chemische Versiegelung"));
    ArmorModData damping = Objects.requireNonNull(ArmorModDataLoader.findByName("Wärmedämpfung"));
    assertFalse(c.getEquipment().isAvailableForInstall(fullBody, seal, 0));
    assertTrue(c.getEquipment().isAvailableForInstall(fullBody, damping, 6));
    c.setCreated(true);
    assertTrue(c.getEquipment().isAvailableForInstall(fullBody, seal, 0));
  }

  @Test
  void taserShieldBecomesWeapon() {
    Character c = mensch();
    wear(c, "Taserschild");
    assertEquals(List.of("Taserschild"), c.getEquipment().getArmorWeapons().stream().map(Weapon::getName).toList());
  }

  @Test
  void shieldPenalizesDefaultingPool() {
    // Pistolen ohne Stufe: GES 4 − 1 = 3, mit Schild 2 (KON 6, damit keine Behinderung mitspielt)
    Character c = mensch();
    c.getAttribute(AttributeType.AGI).setValue(4);
    c.getAttribute(AttributeType.BOD).setValue(6);
    Skill pistols = Objects.requireNonNull(c.getSkill("Pistolen"));
    assertEquals(3, c.getSkillDicePool(pistols).orElseThrow().dice());
    wear(c, "Schutzschild");
    assertEquals(2, c.getSkillDicePool(pistols).orElseThrow().dice());
  }

  @Test
  void equippedShieldPenalizesPhysicalTests() {
    Character c = mensch();
    int pistolsBefore = dice(c, "Pistolen");
    int hackingBefore = dice(c, "Hacking");
    Armor shield = wear(c, "Schutzschild");
    assertTrue(c.getEquipment().hasShieldEquipped());
    assertEquals(pistolsBefore - 1, dice(c, "Pistolen"));
    assertEquals(hackingBefore, dice(c, "Hacking"));
    assertTrue(c.getSkillDicePool(Objects.requireNonNull(c.getSkill("Pistolen"))).orElseThrow().breakdown()
        .endsWith("− Schild 1"));
    shield.setEquipped(false);
    assertEquals(pistolsBefore, dice(c, "Pistolen"));
  }

  @Test
  void shieldPenalizesAttributeTestsWithPhysicalAttribute() {
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getAttribute(AttributeType.WIL).setValue(3);
    List<AttributeTest> tests = Arrays.asList(AttributeTest.values());
    int before = tests.stream().mapToInt(t -> c.getAttributeTestPool(t).dice()).sum();
    wear(c, "Schutzschild");
    long physicalTests = tests.stream().filter(t -> t.getFirst().isPhysical() || t.getSecond().isPhysical()).count();
    int after = tests.stream().mapToInt(t -> c.getAttributeTestPool(t).dice()).sum();
    assertEquals(before - physicalTests, after);
  }

  @Test
  void equippedShieldsAndShockFrillsBecomeWeapons() {
    Character c = mensch();
    Armor shield = wear(c, "Schutzschild");
    Armor jacket = wear(c, "Panzerjacke");
    jacket.install(mod("Elektrische Isolierung", 1));
    jacket.install(mod("Schockstreifen", 0));
    assertEquals(List.of("Schutzschild", "Schockstreifen"),
        c.getEquipment().getArmorWeapons().stream().map(Weapon::getName).toList());
    assertEquals("(STR÷2)G", c.getEquipment().getArmorWeapons().getFirst().getData().damage());
    shield.setEquipped(false);
    jacket.setEquipped(false);
    assertTrue(c.getEquipment().getArmorWeapons().isEmpty());
    assertFalse(c.getEquipment().hasShieldEquipped());
  }
}
