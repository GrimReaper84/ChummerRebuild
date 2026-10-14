package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreVehicleRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static Vehicle vehicle(String name) {
    return new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName(name), name));
  }

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name), name));
  }

  private static Gear gear(String name, int rating) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), rating);
  }

  @Test
  void roundtripPreservesVehicleEquipment() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    comet.installRiggerAdapter();
    comet.mountWeapon(comet.addMount(), weapon("Ingram White Knight"));
    comet.addMount();
    comet.addGear(gear("Medkit", 3));
    Objects.requireNonNull(comet.getSensorPackage()).install(gear("Geruchsscanner", 2));
    original.getEquipment().addVehicle(comet);

    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    doberman.mountWeapon(doberman.getMounts().get(0), weapon("Ares Antioch-2"));
    original.getEquipment().addVehicle(doberman);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Vehicle> vehicles = loaded.getEquipment().getVehicles();
    assertEquals(2, vehicles.size());
    Vehicle loadedComet = vehicles.get(0);
    assertTrue(loadedComet.isRiggerAdapterPurchased());
    List<WeaponMount> mounts = loadedComet.getMounts();
    assertEquals(2, mounts.size());
    assertEquals("Ingram White Knight", Objects.requireNonNull(mounts.get(0).getWeapon()).getName());
    assertNull(mounts.get(1).getWeapon());
    assertEquals(1, loadedComet.getGear().size());
    assertEquals(3, loadedComet.getGear().get(0).getRating());
    assertEquals(1, Objects.requireNonNull(loadedComet.getSensorPackage()).getInstalled().size());

    Vehicle loadedDoberman = vehicles.get(1);
    assertEquals(1, loadedDoberman.getMounts().size());
    assertTrue(loadedDoberman.getMounts().get(0).isIncluded());
    assertEquals("Ares Antioch-2",
        Objects.requireNonNull(loadedDoberman.getMounts().get(0).getWeapon()).getName());
    assertEquals(3, loadedDoberman.getIncludedGear().size());

    assertEquals(original.getEquipment().getVehicleNuyenCost(),
        loaded.getEquipment().getVehicleNuyenCost());
    assertTrue(loaded.getEquipment().getWeapons().isEmpty());
  }

  @Test
  void unknownVehiclesAndUnfittingWeaponsAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch","vehicles":[
          {"name":"Unbekanntes Auto"},
          {"name":"Steel Lynx (Groß)","riggerAdapter":true,"mounts":[
            {"included":true,"weapon":{"name":"Panther XXL"}},
            {"included":false,"weapon":{"name":"Ingram White Knight"}}]}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    List<Vehicle> vehicles = loaded.getEquipment().getVehicles();
    assertEquals(1, vehicles.size());
    Vehicle lynx = vehicles.get(0);
    assertFalse(lynx.isRiggerAdapterPurchased()); // Drohne: mitgeliefert, kein Kauf
    assertEquals(1, lynx.getMounts().size());     // Rumpf 4: keine zweite Halterung
    assertNull(lynx.getMounts().get(0).getWeapon()); // Sturmkanone passt nicht
  }
}
