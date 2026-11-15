package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verheiztes Edge unter dem Minimum und ausgegebene Edgepunkte überstehen Speichern und Laden. */
class CharacterStoreEdgeRoundtripTest {

  @TempDir
  File tempDir;

  @Test
  void burnedAndSpentEdgeRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEdge().setValue(2);
    original.setCreated(true);
    original.burnEdge();
    original.spendEdge();

    File file = new File(tempDir, "edge.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    assertEquals(1, loaded.getEdge().getValue());
    assertEquals(1, loaded.getEdge().getBurned());
    assertEquals(0, loaded.getEdgePoints());
  }

  @Test
  void edgeBurnedToZeroRoundtrip() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEdge().setValue(2);
    original.setCreated(true);
    original.burnEdge();
    original.burnEdge();

    File file = new File(tempDir, "edge0.json");
    CharacterStore.save(original, file);
    Character loaded = CharacterStore.load(file);

    assertEquals(0, loaded.getEdge().getValue());
    assertEquals(2, loaded.getEdge().getBurned());
  }
}
