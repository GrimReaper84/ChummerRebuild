package de.shalorian.chummer.model.character.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Zauberkatalog gegen das GRW (Strassengrimoire S. 238–249, am Seitenbild geprüft). */
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
  void containsAllRulebookSpellsPerCategory() {
    Map<String, SpellData> spells = SpellDataLoader.getAllSpells();
    assertEquals(84, spells.size());
    Map<String, Long> perCategory = spells.values().stream()
        .collect(java.util.stream.Collectors.groupingBy(SpellData::category, java.util.stream.Collectors.counting()));
    assertEquals(Map.of("Heilung", 12L, "Illusion", 15L, "Kampf", 18L, "Manipulation", 22L, "Wahrnehmung", 17L),
        perCategory);
    spells.values().forEach(s -> assertTrue(s.page().matches("2(39|4[0-9])"), s.name()));
  }

  @Test
  void drainUsesHalfForceAsInTheRulebook() {
    // GRW S. 238: Entzug = halbe Kraftstufe (abgerundet) plus Modifikator; Flammenwerfer S. 243
    assertEquals("(K÷2)+3", SpellDataLoader.findByName("Flammenwerfer").dv());
    assertEquals("(K÷2)", SpellDataLoader.findByName("Entzünden").dv());
    assertEquals("(K÷2)−1", SpellDataLoader.findByName("Energieschlag").dv());
    assertEquals("(Schaden)−2", SpellDataLoader.findByName("Heilen").dv());
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

  @Test
  void everySpellHasAShortNote() {
    SpellDataLoader.getAllSpells().values().forEach(spell -> assertFalse(spell.note().isBlank(), spell.name()));
  }
}
