package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Die Art der Log-Einträge übersteht Speichern und Laden; alte Dateien ohne Art laden als „Manuell“. */
class CharacterStoreLogRoundtripTest {

  @TempDir
  File tempDir;

  @Test
  void categoryRoundtripAndOldFilesAreManual() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.addKarmaEntry(new LogEntry("Stärke 3 → 4", -20, LocalDate.of(2026, 11, 15), LogCategory.ATTRIBUTE));
    File file = new File(tempDir, "log.json");
    CharacterStore.save(original, file);

    assertEquals(LogCategory.ATTRIBUTE, CharacterStore.load(file).getKarmaLog().getFirst().category());

    String json = Files.readString(file.toPath(), StandardCharsets.UTF_8)
        .replaceAll(",\\s*\"category\"\\s*:\\s*\"ATTRIBUTE\"", "");
    Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    assertEquals(LogCategory.MANUAL, CharacterStore.load(file).getKarmaLog().getFirst().category());
  }
}
