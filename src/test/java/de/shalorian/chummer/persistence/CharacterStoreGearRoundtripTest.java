package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreGearRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void roundtripPreservesRatingQuantityExtraAndChosenPrice() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment().addGear(new Gear(find("Automatischer Dietrich"), 4));
    original.getEquipment().addGear(new Gear(find("Standard-RFID-Marker"), 0, 40, "", null));
    original.getEquipment().addGear(new Gear(find("Gefälschte Lizenz"), 3, 1, "Jagdlizenz", null));
    original.getEquipment().addGear(new Gear(find("Sim-Aufzeichnung"), 0, 2, "", 80));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Gear> gear = loaded.getEquipment().getGear();
    assertEquals(4, gear.size());
    assertEquals(4, gear.get(0).getRating());
    assertNull(gear.get(0).getChosenPackPrice());
    assertEquals(40, gear.get(1).getQuantity());
    assertEquals("Jagdlizenz", gear.get(2).getExtra());
    assertEquals(3, gear.get(2).getRating());
    assertEquals(80, gear.get(3).getChosenPackPrice());
    assertEquals(original.getEquipment().getGearNuyenCost(),
        loaded.getEquipment().getGearNuyenCost());
  }

  @Test
  void roundtripPreservesInstalledOperatingSystem() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Gear commlink = original.getEquipment().addGear(new Gear(find("Renraku Sensei"), 0));
    commlink.install(new Gear(find("Mangadyne Deva"), 0));
    original.getEquipment().addGear(new Gear(find("Meta Link"), 0));

    CharacterStore.save(original, tempFile());
    List<Gear> gear = CharacterStore.load(tempFile()).getEquipment().getGear();

    assertEquals(2, gear.size());
    assertEquals("Mangadyne Deva", gear.get(0).getInstalled().get(0).getName());
    assertTrue(gear.get(1).getInstalled().isEmpty());
    assertEquals(1_800, gear.get(0).getNuyenCost());
  }

  @Test
  void unfittingInstalledGearIsSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "gear":[{"name":"Meta Link","rating":0,"quantity":1,"extra":"",
                  "installed":[{"name":"Vector Xim","rating":0,"quantity":1,"extra":""},
                               {"name":"Redcap Nix","rating":0,"quantity":1,"extra":""},
                               {"name":"Survival-Kit","rating":0,"quantity":1,"extra":""}]},
                 {"name":"Meta Link","rating":0,"quantity":3,"extra":""},
                 {"name":"Iris Orb","rating":0,"quantity":1,"extra":""}]}
        """, StandardCharsets.UTF_8);

    List<Gear> gear = CharacterStore.load(tempFile()).getEquipment().getGear();

    assertEquals(1, gear.size());
    assertEquals(1, gear.get(0).getInstalled().size());
    assertEquals("Vector Xim", gear.get(0).getInstalled().get(0).getName());
  }

  @Test
  void installsBeyondCapacityAreSkippedOnLoad() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "gear":[{"name":"Brille","rating":1,"quantity":1,"extra":"",
                  "installed":[{"name":"Infrarot","rating":0,"quantity":1,"extra":""},
                               {"name":"Restlichtverstärkung","rating":0,"quantity":1,"extra":""}]}]}
        """, StandardCharsets.UTF_8);

    List<Gear> gear = CharacterStore.load(tempFile()).getEquipment().getGear();

    assertEquals(1, gear.get(0).getInstalled().size());
    assertEquals("Infrarot", gear.get(0).getInstalled().get(0).getName());
  }

  @Test
  void fileWithoutGearNodeLoadsWithoutGear() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    assertTrue(CharacterStore.load(tempFile()).getEquipment().getGear().isEmpty());
  }

  @Test
  void unknownNamesAndInvalidValuesAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "gear":[{"name":"Gibt es nicht","rating":0,"quantity":1,"extra":""},
                 {"name":"Automatischer Dietrich","rating":9,"quantity":1,"extra":""},
                 {"name":"Standard-RFID-Marker","rating":0,"quantity":15,"extra":""},
                 {"name":"Werkzeugkiste","rating":0,"quantity":1,"extra":""},
                 {"name":"Sim-Aufzeichnung","rating":0,"quantity":1,"extra":"","packPrice":2},
                 {"name":"Survival-Kit","rating":0,"quantity":2,"extra":""}]}
        """, StandardCharsets.UTF_8);

    List<Gear> gear = CharacterStore.load(tempFile()).getEquipment().getGear();

    assertEquals(1, gear.size());
    assertEquals("Survival-Kit", gear.get(0).getName());
    assertEquals(2, gear.get(0).getQuantity());
  }
}
