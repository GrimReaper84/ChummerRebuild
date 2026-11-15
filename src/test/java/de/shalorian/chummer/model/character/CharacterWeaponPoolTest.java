package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Würfelpool einer Waffe mit Smartlink, Lasermarkierer und Spezialisierung (GRW S. 147, 177, 180, 380). */
class CharacterWeaponPoolTest {

  private static Character shooter() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.AGI).setValue(4);
    Objects.requireNonNull(c.getSkill("Pistolen")).setRating(4);
    return c;
  }

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name), name));
  }

  private static DicePool pool(Character c, Weapon weapon) {
    return c.getEquipment().getWeaponDicePool(weapon).orElseThrow();
  }

  private static Gear gear(String name, int rating) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), rating);
  }

  private static void implantSmartlink(Character c) {
    c.getEquipment().addCyberware(new Cyberware(
        Objects.requireNonNull(CyberwareDataLoader.findByName("Smartlink")), CyberwareGrade.STANDARD, 0));
  }

  private static Weapon withLaser(String name) {
    Weapon weapon = weapon(name);
    weapon.addAccessory(new WeaponAccessory(
        Objects.requireNonNull(AccessoryDataLoader.findByName("Lasermarkierer")), Mount.TOP));
    return weapon;
  }

  @Test
  void poolIsSkillPlusAgility() {
    DicePool pool = pool(shooter(), weapon("Ruger Super Warhawk"));
    assertEquals(8, pool.dice());
    assertEquals("Pistolen 4 + Geschicklichkeit 4", pool.breakdown());
  }

  @Test
  void smartgunNeedsSmartlink() {
    // S. 180: +2 nur mit Smartlink und Smartgun-Waffe; die Ares Predator IV hat ein Smartgunsystem
    Character c = shooter();
    Weapon predator = weapon("Ares Predator IV");
    assertEquals(8, pool(c, predator).dice());
    implantSmartlink(c);
    assertEquals(10, pool(c, predator).dice());
    assertEquals(8, pool(c, weapon("Ruger Super Warhawk")).dice());
  }

  @Test
  void laserSightGivesOneButNotOnTopOfSmartlink() {
    // S. 180: Lasermarkierer +1, nicht kumulativ mit dem Smartgun-Bonus
    Character c = shooter();
    assertEquals(9, pool(c, withLaser("Ruger Super Warhawk")).dice());
    implantSmartlink(c);
    assertEquals(10, pool(c, withLaser("Ares Predator IV")).dice());
  }

  @Test
  void deviceSmartlinkNeedsImageLink() {
    // S. 380: ein Smartlink im Gerät braucht dort eine Bildverbindung
    Character c = shooter();
    Gear glasses = c.getEquipment().addGear(gear("Brille", 2));
    glasses.install(gear("Smartlink", 0));
    assertFalse(c.getEquipment().hasSmartlink());
    glasses.install(gear("Bildverbindung", 0));
    assertTrue(c.getEquipment().hasSmartlink());
  }

  @Test
  void meleeWeaponsGetNoSmartlinkBonus() {
    Character c = shooter();
    implantSmartlink(c);
    Objects.requireNonNull(c.getSkill("Klingenwaffen")).setRating(3);
    assertEquals(7, pool(c, weapon("Katana")).dice());
  }

  @Test
  void woundsAndSpecialization() {
    Character c = shooter();
    c.setCreated(true);
    c.setPhysicalDamage(3);
    Weapon predator = weapon("Ares Predator IV");
    assertEquals(7, pool(c, predator).dice());
    assertFalse(c.getEquipment().isWeaponSpecialized(predator));
    Objects.requireNonNull(c.getSkill("Pistolen")).setSpecialization("Schwere Pistolen");
    assertTrue(c.getEquipment().isWeaponSpecialized(predator));
  }
}
