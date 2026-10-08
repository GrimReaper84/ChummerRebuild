package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.AccessoryData;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreWeaponRoundtripTest {

  @Test
  void roundtripPreservesArrowsOnBow() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Weapon bow = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), 5);
    bow.addArrows(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Pfeil")), 4, 20, "",
        null));
    original.getEquipment().addWeapon(bow);

    CharacterStore.save(original, tempFile());
    List<Weapon> weapons = CharacterStore.load(tempFile()).getEquipment().getWeapons();

    Gear arrows = weapons.get(0).getArrows().get(0);
    assertEquals(4, arrows.getRating());
    assertEquals(20, arrows.getQuantity());
    assertEquals(500 + 100, weapons.get(0).getTotalNuyenCost());
  }

  @Test
  void arrowsOnNonBowsAndInGearListAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "weapons":[{"name":"Leichte Armbrust","rating":0,
                     "arrows":[{"name":"Pfeil","rating":3,"quantity":10,"extra":""}]},
                    {"name":"Bogen","rating":3,
                     "arrows":[{"name":"Survival-Kit","rating":0,"quantity":1,"extra":""},
                               {"name":"Pfeil","rating":2,"quantity":10,"extra":""}]}],
         "gear":[{"name":"Pfeil","rating":3,"quantity":10,"extra":""}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertTrue(loaded.getEquipment().getWeapons().get(0).getArrows().isEmpty());
    assertEquals(1, loaded.getEquipment().getWeapons().get(1).getArrows().size());
    assertTrue(loaded.getEquipment().getGear().isEmpty());
  }

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  @Test
  void roundtripPreservesWeaponsAndBowRating() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment().addWeapon(
        new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Katana"))));
    original.getEquipment().addWeapon(
        new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), 5));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Weapon> weapons = loaded.getEquipment().getWeapons();
    assertEquals(2, weapons.size());
    assertEquals("Katana", weapons.get(0).getName());
    assertEquals(0, weapons.get(0).getRating());
    assertEquals("Bogen", weapons.get(1).getName());
    assertEquals(5, weapons.get(1).getRating());
    assertEquals(original.getEquipment().getWeaponNuyenCost(),
        loaded.getEquipment().getWeaponNuyenCost());
  }

  @Test
  void fileWithoutWeaponsNodeLoadsWithoutWeapons() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    assertTrue(CharacterStore.load(tempFile()).getEquipment().getWeapons().isEmpty());
  }

  @Test
  void unknownNamesAndInvalidRatingsAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "weapons":[{"name":"Gibt es nicht","rating":0},
                    {"name":"Bogen","rating":12},
                    {"name":"Katana","rating":0}]}
        """, StandardCharsets.UTF_8);

    List<Weapon> weapons = CharacterStore.load(tempFile()).getEquipment().getWeapons();

    assertEquals(1, weapons.size());
    assertEquals("Katana", weapons.get(0).getName());
  }

  @Test
  void roundtripPreservesAccessoriesWithMount() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Weapon rifle = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("AK-97")));
    rifle.addAccessory(new WeaponAccessory(accessory("Lasermarkierer"), Mount.UNDERBARREL));
    rifle.addAccessory(new WeaponAccessory(accessory("Smartgunsystem, intern"), null));
    original.getEquipment().addWeapon(rifle);

    CharacterStore.save(original, tempFile());
    Weapon loaded = CharacterStore.load(tempFile()).getEquipment().getWeapons().get(0);

    assertEquals(rifle.getAccessories(), loaded.getAccessories());
    assertEquals(rifle.getTotalNuyenCost(), loaded.getTotalNuyenCost());
  }

  @Test
  void unknownOrInvalidAccessoriesAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "weapons":[{"name":"Uzi IV","rating":0,"accessories":[
           {"name":"Gibt es nicht","mount":"TOP"},
           {"name":"Zielfernrohr","mount":"NIRGENDS"},
           {"name":"Zielfernrohr","mount":"UNDERBARREL"},
           {"name":"Schalldämpfer","mount":"BARREL"},
           {"name":"Lasermarkierer","mount":"TOP"}]}]}
        """, StandardCharsets.UTF_8);

    List<WeaponAccessory> accessories =
        CharacterStore.load(tempFile()).getEquipment().getWeapons().get(0).getAccessories();

    assertEquals(List.of(new WeaponAccessory(accessory("Lasermarkierer"), Mount.TOP)), accessories);
  }

  private static AccessoryData accessory(String name) {
    return Objects.requireNonNull(AccessoryDataLoader.findByName(name));
  }
}
