package de.shalorian.chummer.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BetaMenuTest {

  @Test
  void labelTurnsFileNameIntoWords() {
    assertEquals("Strassen Samurai", BetaMenu.label(Path.of("strassen-samurai.json")));
    assertEquals("Karriere Mit Log", BetaMenu.label(Path.of("karriere_mit_log.json")));
  }

  @Test
  void filesListsOnlyJsonSortedByName(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("b.json"), "{}");
    Files.writeString(dir.resolve("a.json"), "{}");
    Files.writeString(dir.resolve("notiz.txt"), "");

    assertEquals(List.of(dir.resolve("a.json"), dir.resolve("b.json")), BetaMenu.files(dir));
  }

  @Test
  void missingFolderGivesNoFiles(@TempDir Path dir) throws IOException {
    assertTrue(BetaMenu.files(dir.resolve("fehlt")).isEmpty());
  }

  @Test
  void noFolderWithoutProperty() {
    String before = System.getProperty(BetaMenu.PROPERTY);
    System.clearProperty(BetaMenu.PROPERTY);
    try {
      assertTrue(BetaMenu.folder().isEmpty());
    } finally {
      if (before != null) {
        System.setProperty(BetaMenu.PROPERTY, before);
      }
    }
  }
}
