package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.initiation.TechniqueDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Initiation und Wandlung überstehen Speichern und Laden, samt höherem Maximum. */
class CharacterStoreInitiationRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static Character career(String gift, int value) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    QualityData quality = QualityDataLoader.findByName(gift);
    assertNotNull(quality, gift);
    c.addQuality(quality, false);
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(value);
    c.setCreated(true);
    return c;
  }

  private static TechniqueData technique(String name) {
    return Objects.requireNonNull(TechniqueDataLoader.findByName(name), name);
  }

  @Test
  void initiateWithMagicAboveSixRoundtrips() throws IOException {
    Character original = career("Zauberer", 6);
    original.getInitiation().raiseGrade();
    original.getInitiation().raiseGrade();
    original.getInitiation().learn(technique("Zentrierung"));
    Objects.requireNonNull(original.getMagicOrResonance()).setValue(8);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(2, loaded.getInitiation().getGrade());
    assertEquals(List.of("Zentrierung"), loaded.getInitiation().getTechniques().stream()
        .map(TechniqueData::name).toList());
    assertEquals(8, Objects.requireNonNull(loaded.getMagicOrResonance()).getValue());
    assertEquals(8, loaded.getMagicOrResonance().getMax());
  }

  @Test
  void echoesLearnedSeveralTimesRoundtrip() throws IOException {
    Character original = career("Technomancer", 5);
    original.getInitiation().raiseGrade();
    original.getInitiation().raiseGrade();
    original.getInitiation().learn(technique("System-Upgrade"));
    original.getInitiation().learn(technique("System-Upgrade"));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(2, loaded.getInitiation().count(technique("System-Upgrade")));
  }

  @Test
  void oldFileWithoutInitiationLoadsGradeZero() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}", StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(0, loaded.getInitiation().getGrade());
  }

  @Test
  void unknownTechniqueIsSkippedOnLoad() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "selectedQualities":[{"name":"Zauberer"}],
         "magic":3,"created":true,
         "initiation":{"grade":2,"techniques":["Gibt es nicht","Maskierung"]}}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(2, loaded.getInitiation().getGrade());
    assertEquals(List.of("Maskierung"), loaded.getInitiation().getTechniques().stream()
        .map(TechniqueData::name).toList());
  }
}
