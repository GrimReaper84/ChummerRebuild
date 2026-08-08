package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

class CharacterComplexFormTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private static @NonNull Character technomancer(String name) {
    Character c = mensch(name);
    QualityData technomant = QualityDataLoader.findByName("Technomant");
    assertNotNull(technomant);
    c.addQuality(technomant, false);
    return c;
  }

  private static @NonNull ComplexFormData analyse() {
    ComplexFormData data = ComplexFormDataLoader.findByName("Analyse");
    assertNotNull(data);
    return data;
  }

  @Test
  void newCharacterHasNoComplexForms() {
    assertTrue(mensch("Test").getComplexForms().isEmpty());
  }

  @Test
  void addComplexFormStartsAtRatingOne() {
    Character c = technomancer("Test");
    c.addComplexForm(analyse());

    assertEquals(1, c.getComplexForms().size());
    assertEquals("Analyse", c.getComplexForms().get(0).getName());
    assertEquals(1, c.getComplexForms().get(0).getRating());
  }

  @Test
  void addingSameComplexFormTwiceThrows() {
    Character c = technomancer("Test");
    c.addComplexForm(analyse());

    assertThrows(IllegalArgumentException.class, () -> c.addComplexForm(analyse()));
    assertEquals(1, c.getComplexForms().size());
  }

  @Test
  void removeComplexFormRemovesByName() {
    Character c = technomancer("Test");
    c.addComplexForm(analyse());

    c.removeComplexForm("Analyse");

    assertTrue(c.getComplexForms().isEmpty());
  }

  @Test
  void complexFormBpIsIncludedInSpentBP() {
    Character c = technomancer("Test");
    int baseline = c.getSpentBP();

    c.addComplexForm(analyse());

    assertEquals(baseline + 1, c.getSpentBP());
  }

  @Test
  void raisingComplexFormRatingIncreasesBpCost() {
    Character c = technomancer("Test");
    var resonance = c.getMagicOrResonance();
    assertNotNull(resonance);
    resonance.setValue(4);
    c.addComplexForm(analyse());
    int baseline = c.getSpentBP();

    c.getComplexForms().get(0).setRating(3);

    assertEquals(baseline + 2, c.getSpentBP());
  }

  @Test
  void cannotAddComplexFormWithoutResonance() {
    assertFalse(mensch("Test").canAddComplexForm());
  }
}
