package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Geladene Munition und Fahrzeugschaden überstehen Speichern und Laden. */
class CharacterStoreCombatRoundtripTest {

  @TempDir
  File tempDir;

  private Character roundtrip(Character original) throws IOException {
    File file = new File(tempDir, "kampf.json");
    CharacterStore.save(original, file);
    return CharacterStore.load(file);
  }

  @Test
  void loadedAmmoRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Weapon predator = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Ares Predator IV")));
    original.getEquipment().addWeapon(predator);
    Gear apds = original.getEquipment().addGear(new Gear(
        Objects.requireNonNull(GearDataLoader.findByName("APDS-Geschosse")), 0, 10, "Schwere Pistolen", null));
    original.getEquipment().loadAmmo(predator, apds);

    Character loaded = roundtrip(original);

    Weapon loadedWeapon = loaded.getEquipment().getWeapons().getFirst();
    Gear ammo = loaded.getEquipment().getLoadedAmmo(loadedWeapon);
    assertNotNull(ammo);
    assertEquals("APDS-Geschosse", ammo.getName());
    assertEquals("−5", loaded.getEquipment().getWeaponArmorPiercingText(loadedWeapon));
  }

  @Test
  void vehicleDamageRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Vehicle lynx = new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName("Steel Lynx (Groß)")));
    lynx.setDamage(4);
    original.getEquipment().addVehicle(lynx);

    assertEquals(4, roundtrip(original).getEquipment().getVehicles().getFirst().getDamage());
  }
}
