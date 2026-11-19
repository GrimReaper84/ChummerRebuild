package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Stabilität von {@code serialize}, Grundlage für den Stern bei ungespeicherten Änderungen (ADR-066). */
class DocumentSerializationTest {

  private static Character runner() {
    Character c = new Character("Kira", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.AGI).setValue(4);
    return c;
  }

  @Test
  void characterSerializationIsStable(@TempDir Path dir) throws IOException {
    Character c = runner();
    assertArrayEquals(CharacterStore.serialize(c), CharacterStore.serialize(c));
    File file = dir.resolve("kira.json").toFile();
    CharacterStore.save(c, file);
    Character loaded = CharacterStore.load(file);
    assertArrayEquals(CharacterStore.serialize(loaded), CharacterStore.serialize(loaded));
    assertArrayEquals(CharacterStore.serialize(c), CharacterStore.serialize(loaded));
  }

  @Test
  void characterChangeChangesSerialization() {
    Character c = runner();
    byte[] before = CharacterStore.serialize(c);
    c.getAttribute(AttributeType.AGI).setValue(5);
    assertFalse(Arrays.equals(before, CharacterStore.serialize(c)));
  }

  @Test
  void revertedChangeRestoresSerialization() {
    Character c = runner();
    byte[] before = CharacterStore.serialize(c);
    c.getAttribute(AttributeType.AGI).setValue(5);
    c.getAttribute(AttributeType.AGI).setValue(4);
    assertArrayEquals(before, CharacterStore.serialize(c));
  }

  @Test
  void gmModeDoesNotChangeSerialization() {
    Character c = runner();
    byte[] before = CharacterStore.serialize(c);
    c.setGmMode(true);
    assertArrayEquals(before, CharacterStore.serialize(c));
  }

  @Test
  void groupSerializationIsStableAndFollowsDamage(@TempDir Path dir) throws IOException {
    NpcGroup group = Objects.requireNonNull(NpcTemplateLoader.findByName("Konzernsicherheitseinheit")).create();
    byte[] before = NpcGroupStore.serialize(group);
    assertArrayEquals(before, NpcGroupStore.serialize(group));
    File file = dir.resolve("gruppe.json").toFile();
    NpcGroupStore.save(group, file);
    NpcGroup loaded = NpcGroupStore.load(file);
    assertArrayEquals(NpcGroupStore.serialize(loaded), NpcGroupStore.serialize(loaded));
    group.getMembers().getFirst().setDamage(3, group.getGrunt().getMonitorBoxes());
    assertFalse(Arrays.equals(before, NpcGroupStore.serialize(group)));
  }
}
