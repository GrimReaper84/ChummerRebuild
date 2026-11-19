package de.shalorian.chummer.view.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Bereiche und Gruppen der Seitenleiste wie im abgenommenen Entwurf (Plan UI-Rework, Abschnitt 7). */
class CharacterSectionTest {

  private static List<String> titles(SectionGroup group) {
    return CharacterSection.of(group).stream().map(CharacterSection::getTitle).toList();
  }

  @Test
  void characterGroupStartsWithStatus() {
    assertEquals(List.of("Status", "Biografie", "Attribute", "Fertigkeiten", "Gaben & Handicaps", "Cheat-Sheet"),
        titles(SectionGroup.CHARACTER));
  }

  @Test
  void otherGroupsInOrder() {
    assertEquals(List.of("Magie", "Adeptenkräfte", "Technomancer", "Initiation"), titles(SectionGroup.MAGIC_RESONANCE));
    assertEquals(List.of("Cyber- & Bioware", "Lebensstil", "Rüstung", "Waffen", "Ausrüstung", "Fahrzeuge & Drohnen"),
        titles(SectionGroup.EQUIPMENT));
    assertEquals(List.of("Kontakte", "Protokoll"), titles(SectionGroup.NETWORK));
  }

  @Test
  void everySectionBelongsToExactlyOneGroup() {
    long grouped = Arrays.stream(SectionGroup.values()).mapToLong(g -> CharacterSection.of(g).size()).sum();
    assertEquals(CharacterSection.values().length, grouped);
  }

  @Test
  void defaultIsAttributes() {
    assertEquals(CharacterSection.ATTRIBUTES, CharacterSection.DEFAULT);
  }
}
