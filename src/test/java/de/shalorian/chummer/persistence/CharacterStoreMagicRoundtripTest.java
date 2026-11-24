package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Gebundene und aktive Foki sowie der Schutzpatron überstehen Speichern und Laden. */
class CharacterStoreMagicRoundtripTest {

  @TempDir
  File tempDir;

  @Test
  void fociAndMentorRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Zauberer")), false);
    Objects.requireNonNull(original.getMagicOrResonance()).setValue(4);
    original.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Schutzpatron")), false,
        "Katze (Akrobatik)");
    Gear spell = original.getEquipment().addGear(new Gear(
        Objects.requireNonNull(GearDataLoader.findByName("Zauberspruchfokus")), 3, 1, "Kampf", null));
    Gear weapon = original.getEquipment().addGear(new Gear(
        Objects.requireNonNull(GearDataLoader.findByName("Waffenfokus")), 2, 1, "Klingenwaffen", null));
    original.getFoci().bind(spell);
    original.getFoci().setActive(spell, true);
    original.getFoci().bind(weapon);

    File file = new File(tempDir, "magie.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    List<Gear> foci = loaded.getFoci().getAll();
    assertEquals(2, foci.size());
    assertEquals("Kampf", foci.getFirst().getExtra());
    assertTrue(foci.getFirst().isFocusActive());
    assertTrue(foci.get(1).isBound());
    assertFalse(foci.get(1).isFocusActive());
    // Patron samt gewähltem Vorteil: Katze (Akrobatik) gibt +2 auf Akrobatik
    assertTrue(loaded.getSituationalBonuses().stream()
        .anyMatch(b -> b.source().equals("Katze") && b.detail().equals("Akrobatik") && b.dice() == 2));
  }

  @Test
  void mergedFociFromOldSavesAreSplit() throws IOException {
    // Ältere Stände (bis Beta-Lauf 4) führten zwei gleiche Foki als einen Eintrag mit Menge 2, auch im Fahrzeug
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Zauberer")), false);
    Gear power = original.getEquipment().addGear(kraftfokus());
    original.getFoci().bind(power);
    Vehicle van = new Vehicle(
        Objects.requireNonNull(VehicleDataLoader.findByName("Mercury Comet (Mittelklassewagen)")));
    van.addGear(kraftfokus());
    original.getEquipment().addVehicle(van);
    File file = new File(tempDir, "alt.json");
    CharacterStore.save(original, file);
    Files.writeString(file.toPath(), Files.readString(file.toPath()).replace("\"quantity\" : 1", "\"quantity\" : 2"));

    Character loaded = CharacterStore.load(file);

    List<Gear> foci = loaded.getFoci().getAll();
    assertEquals(2, foci.size());
    assertTrue(foci.getFirst().isBound());
    assertFalse(foci.get(1).isBound());
    assertEquals(1, foci.get(1).getQuantity());
    List<Gear> vehicleGear = loaded.getEquipment().getVehicles().getFirst().getGear();
    assertEquals(2, vehicleGear.size());
    assertTrue(vehicleGear.stream().allMatch(g -> g.isFocus() && g.getQuantity() == 1));
  }

  private static Gear kraftfokus() {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName("Kraftfokus")), 1, 1, "", null);
  }
}
