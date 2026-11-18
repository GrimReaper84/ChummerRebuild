package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Schergengruppen speichern und laden, Dateiart erkennen (ADR-062). */
class NpcGroupStoreTest {

  @Test
  void templateWithCombatStateRoundtrip(@TempDir Path dir) throws IOException {
    NpcGroup group = Objects.requireNonNull(NpcTemplateLoader.findByName("Konzernsicherheitseinheit")).create();
    group.setMemberCount(5);
    group.getMembers().get(1).setDamage(4, group.getGrunt().getMonitorBoxes());
    NpcStatblock leader = Objects.requireNonNull(group.getLeader());
    group.getLeaderState().setDamage(2, leader.getMonitorBoxes());
    group.setEdgeSpent(1);
    group.getGrunt().setGearText("Kommlink (Gerätestufe 4)");
    File file = dir.resolve("gruppe.json").toFile();

    NpcGroupStore.save(group, file);
    assertEquals(DocumentKind.NPC_GROUP, DocumentKind.detect(file));
    NpcGroup loaded = NpcGroupStore.load(file);
    assertEquals("Konzernsicherheitseinheit", loaded.getName());
    assertEquals(2, loaded.getProfessionalRating());
    assertEquals(5, loaded.getMemberCount());
    assertEquals(4, loaded.getMembers().get(1).getDamage());
    assertEquals(2, loaded.getLeaderState().getDamage());
    assertEquals(1, loaded.getEdgeSpent());
    assertEquals("Kommlink (Gerätestufe 4)", loaded.getGrunt().getGearText());
    NpcStatblock loadedLeader = loaded.getLeader();
    assertNotNull(loadedLeader);
    assertEquals(6, loadedLeader.getCharacter().getSpells().size());
    assertTrue(loaded.getGrunt().getCharacter().isGmMode());
  }

  @Test
  void groupWithoutLeader(@TempDir Path dir) throws IOException {
    NpcGroup group = Objects.requireNonNull(NpcTemplateLoader.findByName("Tir Ghosts")).create();
    group.setLeader(null);
    File file = dir.resolve("ohne.json").toFile();
    NpcGroupStore.save(group, file);
    assertNull(NpcGroupStore.load(file).getLeader());
  }

  @Test
  void characterFilesAreDetectedAsCharacters(@TempDir Path dir) throws IOException {
    File file = dir.resolve("char.json").toFile();
    CharacterStore.save(new Character("Test", MetatypeDataLoader.find("Mensch")), file);
    assertEquals(DocumentKind.CHARACTER, DocumentKind.detect(file));
    Path old = dir.resolve("alt.json");
    Files.writeString(old, "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}");
    assertEquals(DocumentKind.CHARACTER, DocumentKind.detect(old.toFile()));
  }
}
