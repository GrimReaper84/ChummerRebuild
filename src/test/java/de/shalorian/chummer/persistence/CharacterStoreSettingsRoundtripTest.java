package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.option.RuleOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Regel-Einstellungen sowie Rückstand und Schulden des Lebensstils überstehen Speichern und Laden (ADR-061). */
class CharacterStoreSettingsRoundtripTest {

  @Test
  void ruleOptionsAndArrearsRoundtrip() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getRuleOptions().setLimbArmorAverage(true);
    c.getRuleOptions().setLateAwakening(true);
    c.getRuleOptions().setMissedLifestylePayments(true);
    c.getRuleOptions().setDecoupleAtCreation(true);
    c.getEquipment().getLifestyle().changeLevel(LifestyleLevel.MIDDLE);
    c.getEquipment().getLifestyle().skipMonth(1);
    c.getEquipment().getLifestyle().skipMonth(6);

    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    RuleOptions rules = loaded.getRuleOptions();
    assertTrue(rules.isLimbArmorAverage());
    assertTrue(rules.isLateAwakening());
    assertTrue(rules.isMissedLifestylePayments());
    assertTrue(rules.isDecoupleAtCreation());
    Lifestyle lifestyle = loaded.getEquipment().getLifestyle();
    assertEquals(LifestyleLevel.LOW, lifestyle.getLevel());
    assertEquals(1, lifestyle.getMissedMonths());
    assertEquals(5_000, lifestyle.getDebt());
  }

  @Test
  void extraBooksRoundtripIncludingUnknownCodes() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getRuleOptions().setBookEnabled("AR", true);
    c.getRuleOptions().setBookEnabled("XYZ", true);
    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    assertEquals(Set.of("AR", "XYZ"), loaded.getRuleOptions().getExtraBooks());
    assertTrue(loaded.getRuleOptions().isBookEnabled("SR4"));
  }

  @Test
  void oldFileWithoutFieldsLoadsDefaults(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("alt.json");
    Files.writeString(file, """
        {"name":"Alt","metatype":"Mensch","lifestyle":{"level":"LOW","months":2}}
        """);
    Character loaded = CharacterStore.load(file.toFile());
    assertFalse(loaded.getRuleOptions().isLimbArmorAverage());
    assertFalse(loaded.getRuleOptions().isLateAwakening());
    assertFalse(loaded.getRuleOptions().isMissedLifestylePayments());
    assertFalse(loaded.getRuleOptions().isDecoupleAtCreation());
    assertTrue(loaded.getRuleOptions().getExtraBooks().isEmpty());
    assertEquals(0, loaded.getEquipment().getLifestyle().getMissedMonths());
    assertEquals(0, loaded.getEquipment().getLifestyle().getDebt());
  }
}
