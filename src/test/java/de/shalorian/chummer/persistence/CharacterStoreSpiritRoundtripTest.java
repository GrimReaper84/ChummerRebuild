package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Geister und Sprites überstehen Speichern und Laden. */
class CharacterStoreSpiritRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static Character career(String gift) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    QualityData quality = QualityDataLoader.findByName(gift);
    assertNotNull(quality, gift);
    c.addQuality(quality, false);
    c.setCreated(true);
    return c;
  }

  @Test
  void spiritsRoundtrip() throws IOException {
    Character original = career("Zauberer");
    original.getSpirits().add(new Spirit(Objects.requireNonNull(SpiritDataLoader.findByName("Feuergeist")), 5, 3,
        true, List.of("Grauen")));
    original.getSpirits().add(new Spirit(Objects.requireNonNull(SpiritDataLoader.findByName("Luftgeist")), 4, 1,
        false));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Spirit> spirits = loaded.getSpirits().getAll();
    assertEquals(2, spirits.size());
    assertEquals("Feuergeist", spirits.getFirst().getName());
    assertEquals(5, spirits.getFirst().getForce());
    assertEquals(3, spirits.getFirst().getServices());
    assertFalse(spirits.get(1).isBound());
    assertEquals(List.of("Grauen"), spirits.getFirst().getOptions());
    assertEquals(List.of(), spirits.get(1).getOptions());
  }

  @Test
  void unknownTypesAndOldFilesLoad() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "selectedQualities":[{"name":"Technomancer"}],
         "spirits":[{"name":"Gibt es nicht","force":3,"services":1,"bound":true},
                    {"name":"Kurier-Sprite","force":3,"services":2,"bound":true}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(List.of("Kurier-Sprite"), loaded.getSpirits().getAll().stream().map(Spirit::getName).toList());
    // ältere Dateien kennen keine Optionen
    assertEquals(List.of(), loaded.getSpirits().getAll().getFirst().getOptions());
  }

  @Test
  void unknownOptionsAreSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Neu","metatype":"Mensch",
         "selectedQualities":[{"name":"Technomancer"}],
         "spirits":[{"name":"Kurier-Sprite","force":6,"services":2,"bound":true,
                     "options":["Befehl","Gibt es nicht","Befehl"]}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(List.of("Befehl"), loaded.getSpirits().getAll().getFirst().getOptions());
  }
}
