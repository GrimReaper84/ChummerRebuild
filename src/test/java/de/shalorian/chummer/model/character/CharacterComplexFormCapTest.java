package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * SR4-Grundregelwerk (Kapitel Komplexe Formen): "Die Grundstufe einer Komplexen Form kann das
 * Resonanzattribut des Technomancers nicht überschreiten." — anders als der reine Erstellungs-Cap
 * bei Fertigkeiten/Zaubern gilt dieser Cap laut Regeltext durchgehend, nicht nur bei der
 * Charaktererschaffung.
 */
class CharacterComplexFormCapTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private static Character technomancer(String name, int resonance) {
    Character c = mensch(name);
    QualityData technomant = QualityDataLoader.findByName("Technomant");
    assertNotNull(technomant);
    c.addQuality(technomant, false);
    var attr = c.getMagicOrResonance();
    assertNotNull(attr);
    attr.setValue(resonance);
    return c;
  }

  private static ComplexFormData analyse() {
    ComplexFormData data = ComplexFormDataLoader.findByName("Analyse");
    assertNotNull(data);
    return data;
  }

  @Test
  void cannotRaiseComplexFormWithoutResonance() {
    Character c = mensch("Test");
    ComplexForm cf = new ComplexForm(analyse());
    assertFalse(c.canRaiseComplexForm(cf));
  }

  @Test
  void canAddComplexFormRequiresAtLeastResonanceOne() {
    Character c = technomancer("Test", 1);
    assertTrue(c.canAddComplexForm());
  }

  @Test
  void ratingCapEqualsResonanceValueAtCreation() {
    Character c = technomancer("Test", 2);
    c.addComplexForm(analyse());
    ComplexForm cf = c.getComplexForms().get(0);

    assertTrue(c.canRaiseComplexForm(cf)); // 1 -> 2 erlaubt (Cap = 2)
    cf.setRating(2);
    assertFalse(c.canRaiseComplexForm(cf)); // 2 -> 3 nicht erlaubt (Cap = 2)
  }

  @Test
  void ratingCapAppliesInCareerModeToo() {
    Character c = technomancer("Test", 2);
    c.setCreated(true);
    c.addComplexForm(analyse());
    ComplexForm cf = c.getComplexForms().get(0);
    cf.setRating(2);

    assertFalse(c.canRaiseComplexForm(cf));
  }

  @Test
  void raisingResonanceRaisesTheCap() {
    Character c = technomancer("Test", 2);
    c.addComplexForm(analyse());
    ComplexForm cf = c.getComplexForms().get(0);
    cf.setRating(2);
    assertFalse(c.canRaiseComplexForm(cf));

    var resonance = c.getMagicOrResonance();
    assertNotNull(resonance);
    resonance.setValue(3);

    assertTrue(c.canRaiseComplexForm(cf));
  }
}
