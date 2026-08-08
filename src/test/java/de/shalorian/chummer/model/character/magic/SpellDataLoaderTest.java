package de.shalorian.chummer.model.character.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Deckt die initiale, verifizierte Zauber-Teilmenge ab (siehe {@code spells.xml}-Kommentar und
 * {@code docs/magie_plan.md} Abschnitt 13) — bewusst kein Hardcoding auf die vollen 84 GRW-Kern-
 * Zauber, da die Vervollständigung ein separater Folgeauftrag ist.
 */
class SpellDataLoaderTest {

  @Test
  void loadsAllExpectedCategories() {
    Map<String, SpellData> spells = SpellDataLoader.getAllSpells();
    Set<String> categories = spells.values().stream().map(SpellData::category)
        .collect(java.util.stream.Collectors.toSet());

    assertTrue(categories.containsAll(
        Set.of("Kampf", "Wahrnehmung", "Heilung", "Illusion", "Manipulation")));
  }

  @Test
  void everyCategoryHasAtLeastTwoSpells() {
    Map<String, SpellData> spells = SpellDataLoader.getAllSpells();
    for (String category : Set.of("Kampf", "Wahrnehmung", "Heilung", "Illusion", "Manipulation")) {
      long count = spells.values().stream().filter(s -> s.category().equals(category)).count();
      assertTrue(count >= 2, category + " sollte mindestens 2 Zauber haben, hat " + count);
    }
  }

  @Test
  void findByNameReturnsKnownSpell() {
    SpellData heilen = SpellDataLoader.findByName("Heilen");
    assertEquals("Heilen", heilen.name());
    assertEquals("Heilung", heilen.category());
    assertEquals("SR4", heilen.source());
  }

  @Test
  void findByNameReturnsNullForUnknownSpell() {
    assertNull(SpellDataLoader.findByName("Kein echter Zauber"));
  }
}
