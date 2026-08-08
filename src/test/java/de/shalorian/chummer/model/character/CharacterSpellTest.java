package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;

class CharacterSpellTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private static @NonNull SpellData heilen() {
    SpellData data = SpellDataLoader.findByName("Heilen");
    assertNotNull(data);
    return data;
  }

  @Test
  void newCharacterHasNoSpells() {
    assertTrue(mensch("Test").getSpells().isEmpty());
  }

  @Test
  void addSpellAddsToList() {
    Character c = mensch("Test");
    c.addSpell(heilen());

    assertEquals(1, c.getSpells().size());
    assertEquals("Heilen", c.getSpells().get(0).name());
  }

  @Test
  void addingSameSpellTwiceThrows() {
    Character c = mensch("Test");
    c.addSpell(heilen());

    assertThrows(IllegalArgumentException.class, () -> c.addSpell(heilen()));
    assertEquals(1, c.getSpells().size());
  }

  @Test
  void removeSpellRemovesByName() {
    Character c = mensch("Test");
    c.addSpell(heilen());

    c.removeSpell("Heilen");

    assertTrue(c.getSpells().isEmpty());
  }

  @Test
  void spellCostsThreeBpEach() {
    Character c = mensch("Test");
    c.addSpell(heilen());

    assertEquals(3, c.getSpellBP());
  }

  @Test
  void spellBpIsIncludedInSpentBP() {
    Character c = mensch("Test");
    int baseline = c.getSpentBP();

    c.addSpell(heilen());

    assertEquals(baseline + 3, c.getSpentBP());
  }

  @Test
  void spellKarmaCostIsFixedAtFive() {
    Character c = mensch("Test");
    assertEquals(5, c.getSpellKarmaCost());
  }
}
