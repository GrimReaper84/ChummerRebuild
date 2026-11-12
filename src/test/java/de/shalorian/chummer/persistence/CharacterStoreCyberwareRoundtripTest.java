package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.gear.Gear;
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

class CharacterStoreCyberwareRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static CyberwareData catalog(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data);
    return data;
  }

  @Test
  void roundtripPreservesImplantsWithGradeAndRating() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Datenbuchse"), CyberwareGrade.ALPHAWARE, 0));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Dermalpanzerung"), CyberwareGrade.STANDARD, 2));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Toxinextraktor"), CyberwareGrade.DELTAWARE, 4));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Cyberware> implants = loaded.getEquipment().getCyberware();
    assertEquals(3, implants.size());
    assertEquals("Datenbuchse", implants.getFirst().getName());
    assertEquals(CyberwareGrade.ALPHAWARE, implants.getFirst().getGrade());
    assertEquals(0, implants.getFirst().getRating());
    assertEquals("Dermalpanzerung", implants.get(1).getName());
    assertEquals(2, implants.get(1).getRating());
    assertEquals(EquipmentKind.BIOWARE, implants.get(2).getKind());
    assertEquals(CyberwareGrade.DELTAWARE, implants.get(2).getGrade());
    assertEquals(4, implants.get(2).getRating());
    assertEquals(original.getEssence(), loaded.getEssence());
  }

  @Test
  void fileWithoutCyberwareNodeLoadsWithoutImplants() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertTrue(loaded.getEquipment().getCyberware().isEmpty());
    assertEquals(6.0, loaded.getEssence());
  }

  @Test
  void unknownImplantNameIsSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "cyberware":[{"name":"Gibt es nicht","grade":"STANDARD","rating":0},
                      {"name":"Datenbuchse","grade":"UNBEKANNT","rating":0}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(1, loaded.getEquipment().getCyberware().size());
    assertEquals(CyberwareGrade.STANDARD, loaded.getEquipment().getCyberware().getFirst().getGrade());
  }

  @Test
  void roundtripPreservesChoicePriceAccessoriesAndCommlink() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Cyberware eyes = new Cyberware(catalog("Cyberaugen-Basissystem"), CyberwareGrade.ALPHAWARE, 3);
    eyes.install(new Cyberware(catalog("Smartlink"), CyberwareGrade.STANDARD, 0));
    eyes.install(new Cyberware(catalog("Sichtverbesserung"), CyberwareGrade.STANDARD, 2));
    original.getEquipment().addCyberware(eyes);
    original.getEquipment().addCyberware(
        new Cyberware(catalog("Reflexrekorder (Fertigkeit)"), CyberwareGrade.STANDARD, 0, "Pistolen", null));
    original.getEquipment().addCyberware(
        new Cyberware(catalog("Kosmetische Modifikation"), CyberwareGrade.STANDARD, 0, "", 3500));
    Cyberware headware = new Cyberware(catalog("Kommlink"), CyberwareGrade.STANDARD, 0);
    Gear commlink = new Gear(Objects.requireNonNull(GearDataLoader.findByName("Meta Link")), 0);
    commlink.install(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Vector Xim")), 0));
    headware.setCommlink(commlink);
    original.getEquipment().addCyberware(headware);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Cyberware> implants = loaded.getEquipment().getCyberware();
    assertEquals(4, implants.size());
    Cyberware loadedEyes = implants.getFirst();
    assertEquals(List.of("Smartlink", "Sichtverbesserung"),
        loadedEyes.getInstalled().stream().map(Cyberware::getName).toList());
    assertEquals(CyberwareGrade.ALPHAWARE, loadedEyes.getInstalled().getFirst().getGrade());
    assertEquals(2, loadedEyes.getInstalled().get(1).getRating());
    assertEquals("Pistolen", implants.get(1).getChoice());
    assertEquals(3500, implants.get(2).getChosenPrice());
    Gear loadedCommlink = implants.get(3).getCommlink();
    assertNotNull(loadedCommlink);
    assertEquals("Meta Link", loadedCommlink.getName());
    assertEquals("Vector Xim", loadedCommlink.getInstalled().getFirst().getName());
    assertEquals(original.getEquipment().getCyberwareNuyenCost(), loaded.getEquipment().getCyberwareNuyenCost());
  }

  @Test
  void invalidCosmeticPriceFallsBackToMinimum() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "cyberware":[{"name":"Kosmetische Modifikation","grade":"STANDARD","rating":0,"price":99}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(200, loaded.getEquipment().getCyberware().getFirst().getChosenPrice());
  }

  @Test
  void damageAboveNaturalMonitorSurvivesThanksToImplant() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getAttribute(AttributeType.BOD).setValue(2);
    original.getEquipment().addCyberware(new Cyberware(catalog("Hyperschilddrüse"), CyberwareGrade.STANDARD, 0));
    int damage = original.getPhysicalBoxes() + original.getPhysicalOverflowBoxes();
    original.setPhysicalDamage(damage);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(damage, loaded.getPhysicalDamage());
  }
}
