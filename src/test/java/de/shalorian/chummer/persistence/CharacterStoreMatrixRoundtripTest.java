package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Kommlink-Einbauten, laufende Programme, aktives Kommlink und Fahrzeug-Upgrades überstehen Speichern und Laden. */
class CharacterStoreMatrixRoundtripTest {

  @TempDir
  File tempDir;

  private static GearData gear(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void matrixRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Gear carried = new Gear(gear("Hermes Ikon"), 0);
    carried.install(new Gear(gear("Novatech Navi"), 0));
    carried.install(new Gear(gear("Prozessor-Modul"), 5));
    carried.install(new Gear(gear("Firewall-Paket"), 5));
    Gear analyse = new Gear(gear("Analyse"), 4);
    carried.install(analyse);
    analyse.setRunning(true);
    carried.install(new Gear(gear("Befehl"), 2));
    original.getEquipment().addGear(carried);

    Cyberware headware = new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName("Kommlink")),
        CyberwareGrade.STANDARD, 0);
    Gear implanted = new Gear(gear("Meta Link"), 0);
    headware.setCommlink(implanted);
    original.getEquipment().addCyberware(headware);
    original.getEquipment().setActiveCommlink(implanted);

    Vehicle vehicle = new Vehicle(VehicleDataLoader.getAllVehicles().values().iterator().next());
    vehicle.addMatrixUpgrade(new Gear(gear("Signal-Modul"), vehicle.getDeviceRating() + 1));
    original.getEquipment().addVehicle(vehicle);

    File file = new File(tempDir, "charakter.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    Gear loadedCarried = loaded.getEquipment().getGear().getFirst();
    MatrixStats stats = Objects.requireNonNull(loadedCarried.getMatrixStats());
    assertEquals(5, stats.hardwareProcessor());
    assertEquals(5, stats.firewall());
    assertEquals(1, stats.runningPrograms());
    List<Gear> programs = loadedCarried.getInstalled().stream().filter(Gear::isProgram).toList();
    assertEquals(2, programs.size());
    assertTrue(programs.getFirst().isRunning());

    List<Gear> commlinks = loaded.getEquipment().getCommlinks();
    assertEquals(2, commlinks.size());
    assertSame(commlinks.get(1), loaded.getEquipment().getActiveCommlink());
    assertEquals("Meta Link", commlinks.get(1).getName());

    Vehicle loadedVehicle = loaded.getEquipment().getVehicles().getFirst();
    assertEquals(vehicle.getDeviceRating() + 1, loadedVehicle.getMatrixStats().signal());
  }

  @Test
  void singleCommlinkIsNotSavedAsExplicitChoice() throws IOException {
    // nur die ausdrückliche Wahl wird gespeichert; ein einzelnes Kommlink ist auch nach dem Laden nur der Fallback
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment().addGear(new Gear(gear("Meta Link"), 0));
    File file = new File(tempDir, "einzeln.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    assertNull(loaded.getEquipment().getChosenCommlink());
    loaded.getEquipment().addGear(new Gear(gear("Hermes Ikon"), 0));
    assertNull(loaded.getEquipment().getActiveCommlink());
  }

  @Test
  void chosenCarriedCommlinkSurvivesAnUnknownEntryBeforeIt() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment().addGear(new Gear(gear("Meta Link"), 0));
    Gear chosen = new Gear(gear("Hermes Ikon"), 0);
    original.getEquipment().addGear(chosen);
    original.getEquipment().setActiveCommlink(chosen);
    File file = new File(tempDir, "unbekannt.json");
    CharacterStore.save(original, file);
    // ein unbekannter Eintrag vor den Kommlinks verschiebt die Positionen
    String json = Files.readString(file.toPath(), StandardCharsets.UTF_8)
        .replaceFirst("\"name\" : \"Meta Link\"", "\"name\" : \"Unbekanntes Gerät\"");
    Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    Character loaded = CharacterStore.load(file);

    assertEquals("Hermes Ikon", Objects.requireNonNull(loaded.getEquipment().getChosenCommlink()).getName());
  }
}
