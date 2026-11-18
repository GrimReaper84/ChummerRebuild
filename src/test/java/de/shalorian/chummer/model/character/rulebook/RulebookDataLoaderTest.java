package de.shalorian.chummer.model.character.rulebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Regelwerk-Katalog (ADR-061). */
class RulebookDataLoaderTest {

  private static final Pattern SOURCE = Pattern.compile("<source>([^<]*)</source>");

  @Test
  void coreBookIsGrundregelwerk() {
    RulebookData core = RulebookDataLoader.getCore();
    assertEquals("SR4", core.code());
    assertEquals("Grundregelwerk", core.name());
    assertTrue(core.core());
    assertEquals(1, RulebookDataLoader.getAll().stream().filter(RulebookData::core).count());
  }

  /** Jedes Quellkürzel der Kataloge muss bekannt sein, sonst verschwände der Eintrag durch einen Tippfehler. */
  @Test
  void everyCatalogSourceIsAKnownBook() throws IOException, IllegalAccessException {
    Set<String> known = RulebookDataLoader.getAll().stream().map(RulebookData::code).collect(Collectors.toSet());
    Set<String> unknown = new TreeSet<>();
    int checked = 0;
    for (Field field : DataPaths.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.get(null) instanceof String path && path.endsWith(".xml")) {
        try (InputStream in = DataPaths.class.getResourceAsStream(path)) {
          assertNotNull(in, path);
          Matcher m = SOURCE.matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
          while (m.find()) {
            checked++;
            if (!m.group(1).isBlank() && !known.contains(m.group(1).trim())) {
              unknown.add(path + ": " + m.group(1));
            }
          }
        }
      }
    }
    assertTrue(checked > 700, "zu wenige Quellenangaben gefunden: " + checked);
    assertEquals(Set.of(), unknown);
  }
}
