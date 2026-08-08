package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SR4-Grundregelwerk: Bei der Charaktererschaffung darf ein Charakter höchstens
 * {@code 2 × höchstes Rating(Spruchzauberei, Ritualzauberei)} Zauber lernen.
 */
class CharacterSpellCapTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private static void learnDistinctSpells(Character c, int count) {
    String[] names = {"Heilen", "Krankheit Heilen", "Unsichtbarkeit", "Maske", "Flammenwerfer",
        "Energieball", "Entzünden", "Schatten", "Geistessonde", "Kampfsinn"};
    for (int i = 0; i < count; i++) {
      SpellData data = SpellDataLoader.findByName(names[i]);
      assertNotNull(data);
      c.addSpell(data);
    }
  }

  @Test
  void cannotAddSpellAtCreationWithoutSpellcastingSkill() {
    Character c = mensch("Test");
    assertFalse(c.canAddSpellAtCreation());
  }

  @Test
  void capAllowsTwiceTheSpellcastingRating() {
    Character c = mensch("Test");
    c.getSkill("Spruchzauberei").setRating(2);

    learnDistinctSpells(c, 4); // 2 * 2 = 4 erlaubt

    assertFalse(c.canAddSpellAtCreation());
  }

  @Test
  void capUsesHigherOfBothMagicalSkills() {
    Character c = mensch("Test");
    c.getSkill("Spruchzauberei").setRating(1);
    c.getSkill("Ritualzauberei").setRating(3);

    learnDistinctSpells(c, 5); // 2 * 3 = 6 erlaubt, 5 gelernt

    assertTrue(c.canAddSpellAtCreation());
  }

  @Test
  void noCapInCareerMode() {
    Character c = mensch("Test");
    c.setCreated(true);

    assertTrue(c.canAddSpellAtCreation());
  }
}
