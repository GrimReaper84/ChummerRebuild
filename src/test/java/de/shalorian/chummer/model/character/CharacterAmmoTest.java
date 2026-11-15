package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Geladene Munition je Waffe (GRW S. 369, Nutzerentscheidung 2026-11-15). */
class CharacterAmmoTest {

  private final Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
  private final Equipment equipment = c.getEquipment();

  private Weapon weapon(String name) {
    Weapon weapon = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name), name));
    equipment.addWeapon(weapon);
    return weapon;
  }

  private Gear ammo(String name, String weaponClass) {
    return equipment.addGear(new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), 0, 10,
        weaponClass, null));
  }

  @Test
  void onlyAmmoOfTheSameWeaponClassFits() {
    Weapon predator = weapon("Ares Predator IV");
    Gear apds = ammo("APDS-Geschosse", "Schwere Pistolen");
    ammo("APDS-Geschosse", "Sturmgewehre");
    assertEquals(List.of(apds), equipment.getAmmoCandidates(predator));
  }

  @Test
  void loadedAmmoChangesDamageApAndArmor() {
    Weapon predator = weapon("Ares Predator IV");
    Gear gel = ammo("Gelgeschosse", "Schwere Pistolen");
    assertEquals("5K", equipment.getWeaponDamageText(predator));
    assertEquals("Ballistisch", equipment.getWeaponArmorType(predator));
    equipment.loadAmmo(predator, gel);
    assertEquals("4G", equipment.getWeaponDamageText(predator));
    assertEquals("+1", equipment.getWeaponArmorPiercingText(predator));
    assertEquals("Stoß", equipment.getWeaponArmorType(predator));
    equipment.loadAmmo(predator, null);
    assertEquals("5K", equipment.getWeaponDamageText(predator));
  }

  @Test
  void tracerOnlyInFullAutoWeapons() {
    // S. 369: Leuchtspur nur in vollautomatischen Waffen
    Weapon predator = weapon("Ares Predator IV");
    Gear pistolTracer = ammo("Leuchtspurgeschosse", "Schwere Pistolen");
    assertThrows(IllegalArgumentException.class, () -> equipment.loadAmmo(predator, pistolTracer));
    Weapon ak = weapon("AK-97");
    Gear rifleTracer = ammo("Leuchtspurgeschosse", "Sturmgewehre");
    equipment.loadAmmo(ak, rifleTracer);
    assertEquals(rifleTracer, equipment.getLoadedAmmo(ak));
  }

  @Test
  void removedStackFallsBackToStandard() {
    Weapon predator = weapon("Ares Predator IV");
    Gear apds = ammo("APDS-Geschosse", "Schwere Pistolen");
    equipment.loadAmmo(predator, apds);
    assertEquals("–5", equipment.getWeaponArmorPiercingText(predator));
    equipment.removeGear(apds);
    assertNull(equipment.getLoadedAmmo(predator));
    assertEquals("–1", equipment.getWeaponArmorPiercingText(predator));
  }
}
