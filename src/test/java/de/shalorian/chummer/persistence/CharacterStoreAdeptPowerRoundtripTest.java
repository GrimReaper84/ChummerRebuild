package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreAdeptPowerRoundtripTest {

  @TempDir
  File tempDir;

  private static AdeptPowerData power(String name) {
    AdeptPowerData data = AdeptPowerDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  private static Character mysticAdept() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Magieradept")), false);
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(5);
    c.getAttribute(AttributeType.BOD).setValue(2);
    return c;
  }

  @Test
  void roundtripPreservesPowersLevelsChoicesAndSplit() throws IOException {
    Character original = mysticAdept();
    original.getAdeptPowers().setAdeptMagic(3);
    AdeptPower attribute = original.getAdeptPowers().add(power("Gesteigertes körperliches Attribut"), "BOD");
    original.getAdeptPowers().raise(attribute);
    original.getAdeptPowers().add(power("Geschärfter Sinn"), "Infrarotsicht");
    original.setPhysicalDamage(original.getPhysicalBoxes() + original.getPhysicalOverflowBoxes());

    File file = new File(tempDir, "adept.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    assertEquals(3, loaded.getAdeptPowers().getAdeptMagic());
    List<AdeptPower> powers = loaded.getAdeptPowers().getPowers();
    assertEquals(2, powers.size());
    assertEquals("Gesteigertes körperliches Attribut (Körper)", powers.getFirst().displayName());
    assertEquals(2, powers.getFirst().getLevel());
    assertEquals("Infrarotsicht", powers.get(1).getChoice());
    // Schaden erst nach den Kräften geladen: die verstärkte KON vergrößert die Spalten
    assertEquals(original.getPhysicalDamage(), loaded.getPhysicalDamage());
  }

  @Test
  void unknownPowersAreSkipped() throws IOException {
    Character original = mysticAdept();
    original.getAdeptPowers().setAdeptMagic(1);
    File file = new File(tempDir, "adept.json");
    CharacterStore.save(original, file);
    String json = Files.readString(file.toPath())
        .replace("\"adeptPowers\" : [ ]", "\"adeptPowers\" : [ { \"name\" : \"Gibt es nicht\", \"level\" : 1 } ]");
    assertTrue(json.contains("Gibt es nicht"));
    Files.writeString(file.toPath(), json);

    assertEquals(0, CharacterStore.load(file).getAdeptPowers().getPowers().size());
  }
  @Test
  void oldSaveWithAssensingButNoAstralPerceptionKeepsValueButLocksSkill() throws IOException {
    // Plan Abschnitt 13: Werte bleiben, die Fertigkeit wird gesperrt angezeigt
    Character magician = new Character("Test", MetatypeDataLoader.find("Mensch"));
    magician.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Zauberer")), false);
    Objects.requireNonNull(magician.getSkill("Askennen")).setRating(2);
    File file = new File(tempDir, "alt.json");
    CharacterStore.save(magician, file);
    String json = Files.readString(file.toPath());
    assertTrue(json.contains("\"Zauberer\""));
    Files.writeString(file.toPath(), json.replace("\"Zauberer\"", "\"Magieradept\""));

    Character loaded = CharacterStore.load(file);
    Skill assensing = Objects.requireNonNull(loaded.getSkill("Askennen"));
    assertEquals(2, assensing.getRating());
    assertFalse(loaded.canRaiseSkill(assensing));
    assertTrue(loaded.getSkillDicePool(assensing).isEmpty());
  }
}
