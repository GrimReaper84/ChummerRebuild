package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
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

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  @Test
  void roundtripPreservesWeaponsAndBowRating() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.addWeapon(new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Katana"))));
    original.addWeapon(new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), 5));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Weapon> weapons = loaded.getWeapons();
    assertEquals(2, weapons.size());
    assertEquals("Katana", weapons.get(0).getName());
    assertEquals(0, weapons.get(0).getRating());
    assertEquals("Bogen", weapons.get(1).getName());
    assertEquals(5, weapons.get(1).getRating());
    assertEquals(original.getWeaponNuyenCost(), loaded.getWeaponNuyenCost());
  }

  @Test
  void fileWithoutWeaponsNodeLoadsWithoutWeapons() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    assertTrue(CharacterStore.load(tempFile()).getWeapons().isEmpty());
  }

  @Test
  void unknownNamesAndInvalidRatingsAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "weapons":[{"name":"Gibt es nicht","rating":0},
                    {"name":"Bogen","rating":12},
                    {"name":"Katana","rating":0}]}
        """, StandardCharsets.UTF_8);

    List<Weapon> weapons = CharacterStore.load(tempFile()).getWeapons();

    assertEquals(1, weapons.size());
    assertEquals("Katana", weapons.get(0).getName());
  }

  @Test
  void roundtripPreservesAccessoriesWithMount() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Weapon rifle = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("AK-97")));
    rifle.addAccessory(new WeaponAccessory(accessory("Lasermarkierer"), Mount.UNDERBARREL));
    rifle.addAccessory(new WeaponAccessory(accessory("Smartgunsystem, intern"), null));
    original.addWeapon(rifle);

    CharacterStore.save(original, tempFile());
    Weapon loaded = CharacterStore.load(tempFile()).getWeapons().get(0);

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
        CharacterStore.load(tempFile()).getWeapons().get(0).getAccessories();

    assertEquals(List.of(new WeaponAccessory(accessory("Lasermarkierer"), Mount.TOP)), accessories);
  }

  private static AccessoryData accessory(String name) {
    return Objects.requireNonNull(AccessoryDataLoader.findByName(name));
  }
}
