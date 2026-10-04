package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
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
    assertEquals("Datenbuchse", implants.get(0).getName());
    assertEquals(CyberwareGrade.ALPHAWARE, implants.get(0).getGrade());
    assertEquals(0, implants.get(0).getRating());
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
    assertEquals(CyberwareGrade.STANDARD, loaded.getEquipment().getCyberware().get(0).getGrade());
  }
}
