package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Gespeicherte Qualities: Auswahl bleibt erhalten, alte Namen werden auf die GRW-Namen übersetzt. */
class CharacterStoreQualityTest {

  @TempDir
  Path dir;

  @Test
  void choiceSurvivesSaveAndLoad() throws IOException {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Talentiert")), false, "Pistolen");
    File file = dir.resolve("c.json").toFile();
    CharacterStore.save(c, file);

    List<CharacterQuality> loaded = CharacterStore.load(file).getSelectedQualities();
    assertEquals(1, loaded.size());
    assertEquals("Pistolen", loaded.getFirst().choice());
  }

  @Test
  void oldNamesAreTranslated() throws IOException {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Zauberer")), false);
    File file = dir.resolve("alt.json").toFile();
    CharacterStore.save(c, file);
    String json = Files.readString(file.toPath(), StandardCharsets.UTF_8).replace("\"Zauberer\"", "\"Magier\"");
    Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);

    List<CharacterQuality> loaded = CharacterStore.load(file).getSelectedQualities();
    assertEquals("Zauberer", loaded.getFirst().data().name());
  }

  /** Qualities werden vor Attributen und Fertigkeiten geladen, sonst lägen die erhöhten Werte über dem Maximum. */
  @Test
  void raisedMaximaSurviveSaveAndLoad() throws IOException {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Talentiert")), false, "Pistolen");
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Außergewöhnliches Attribut")), true, "STR");
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Glück")), true);
    Objects.requireNonNull(c.getSkill("Pistolen")).setRating(7);
    c.getAttribute(AttributeType.STR).setValue(7);
    c.getEdge().setValue(8);
    File file = dir.resolve("max.json").toFile();
    CharacterStore.save(c, file);

    Character loaded = CharacterStore.load(file);
    assertEquals(7, Objects.requireNonNull(loaded.getSkill("Pistolen")).getRating());
    assertEquals(7, loaded.getAttribute(AttributeType.STR).getValue());
    assertEquals(8, loaded.getEdge().getValue());
  }
}
