package de.shalorian.chummer.samples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.persistence.DocumentKind;
import de.shalorian.chummer.persistence.NpcGroupStore;
import java.io.File;
import java.io.IOException;
import de.shalorian.chummer.model.character.LogCategory;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Die eingecheckten Testfälle des Beta-Menüs laden und entsprechen dem, was {@link GenerateSamples} erzeugt. */
class SamplesTest {

  private static final File DIR = new File("src/test/resources/samples");

  private static Character character(String name) throws IOException {
    return CharacterStore.load(new File(DIR, name + ".json"));
  }

  @Test
  void everySampleFileLoadsAndFileNamesMatchTheGenerator() throws IOException {
    List<String> expected = new ArrayList<>(GenerateSamples.characters().keySet());
    expected.add("schergengruppe-im-kampf");
    List<String> files = new ArrayList<>();
    for (File file : Objects.requireNonNull(DIR.listFiles((_, name) -> name.endsWith(".json")))) {
      String name = file.getName().substring(0, file.getName().length() - 5);
      files.add(name);
      if (DocumentKind.detect(file) == DocumentKind.NPC_GROUP) {
        NpcGroupStore.load(file);
      } else {
        CharacterStore.load(file);
      }
    }
    assertEquals(expected.stream().sorted().toList(), files.stream().sorted().toList());
  }

  @Test
  void samplesKeepTheirState() throws IOException {
    assertTrue(character("karriere-mit-log").isCreated());
    assertEquals(1, character("initiierter-magier").getInitiation().getGrade());
    assertEquals(1, character("technomancer-mit-wandlung").getInitiation().getGrade());
    Character overspent = character("ueberzogene-erschaffung");
    assertTrue(overspent.getRemainingBP() < 0);
    assertTrue(overspent.getRemainingNuyen() < 0);
    assertTrue(overspent.isEssenceBelowCreationMinimum());
    Character career = character("karriere-mit-log");
    Set<LogCategory> categories = EnumSet.noneOf(LogCategory.class);
    Stream.concat(career.getKarmaLog().stream(), career.getNuyenLog().stream()).forEach(e -> {
      categories.add(e.category());
      assertEquals(LocalDate.of(2072, 3, 1), e.date(), e.description());
    });
    assertEquals(EnumSet.complementOf(EnumSet.of(LogCategory.MAGIC_RESONANCE)), categories);
    assertTrue(character("alle-regel-einstellungen").getRuleOptions().isLateAwakening());
    NpcGroup group = NpcGroupStore.load(new File(DIR, "schergengruppe-im-kampf.json"));
    assertEquals(3, group.getMembers().getFirst().getDamage());
  }
}
