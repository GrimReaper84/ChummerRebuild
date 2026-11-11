package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertEquals("Heilen", c.getSpells().getFirst().name());
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
