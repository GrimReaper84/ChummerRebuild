package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreNuyenArmorRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  @Test
  void roundtripPreservesArmorAndLifestyle() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Panzerjacke"))));
    original.addArmor(new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Helm")), false));
    original.getLifestyle().setLevel(LifestyleLevel.MIDDLE);
    original.getLifestyle().setMonths(3);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Armor> armor = loaded.getArmor();
    assertEquals(2, armor.size());
    assertEquals("Panzerjacke", armor.get(0).getName());
    assertTrue(armor.get(0).isEquipped());
    assertEquals("Helm", armor.get(1).getName());
    assertFalse(armor.get(1).isEquipped());
    assertEquals(LifestyleLevel.MIDDLE, loaded.getLifestyle().getLevel());
    assertEquals(3, loaded.getLifestyle().getMonths());
    assertEquals(original.getSpentNuyen(), loaded.getSpentNuyen());
  }

  @Test
  void fileWithoutNodesLoadsDefaults() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertTrue(loaded.getArmor().isEmpty());
    assertEquals(LifestyleLevel.STREET, loaded.getLifestyle().getLevel());
    assertEquals(1, loaded.getLifestyle().getMonths());
  }

  @Test
  void unknownNamesAreSkippedOrDefaulted() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "armor":[{"name":"Gibt es nicht","equipped":true},{"name":"Helm","equipped":true}],
         "lifestyle":{"level":"UNBEKANNT","months":0}}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(1, loaded.getArmor().size());
    assertEquals(LifestyleLevel.STREET, loaded.getLifestyle().getLevel());
    assertEquals(1, loaded.getLifestyle().getMonths());
  }
}
