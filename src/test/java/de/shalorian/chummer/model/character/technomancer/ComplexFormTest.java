package de.shalorian.chummer.model.character.technomancer;

import de.shalorian.chummer.model.character.skill.Skill;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SR4-Grundregelwerk (Karma-Kosten-Kapitel): "Eine neue Komplexe Form zu erlernen oder eine
 * bestehende zu steigern, kostet das gleiche, wie eine Wissensfertigkeit zu erlernen bzw. zu
 * steigern: 2 Karmapunkte für eine neue Komplexe Form, Karmapunkte in Höhe der neuen Stufe für eine
 * gesteigerte Komplexe Form." — deckungsgleich mit
 * {@link de.shalorian.chummer.model.character.Character#getKnowledgeSkillKarmaCost}.
 */
class ComplexFormTest {

  private static ComplexFormData analyseData() {
    ComplexFormData data = ComplexFormDataLoader.findByName("Analyse");
    return data;
  }

  @Test
  void startsAtMinRating() {
    ComplexForm cf = new ComplexForm(analyseData());
    assertEquals(1, cf.getRating());
  }

  @Test
  void ratingBelowMinThrows() {
    ComplexForm cf = new ComplexForm(analyseData());
    assertThrows(IllegalArgumentException.class, () -> cf.setRating(0));
  }

  @Test
  void ratingAboveSkillMaxThrows() {
    ComplexForm cf = new ComplexForm(analyseData());
    assertThrows(IllegalArgumentException.class, () -> cf.setRating(Skill.MAX_RATING + 1));
  }

  @Test
  void bpCostPerRatingIsOne() {
    ComplexForm cf = new ComplexForm(analyseData());
    assertEquals(1, cf.getBpCostPerRating());
  }

  @Test
  void karmaCostForNewFormIsTwo() {
    ComplexForm cf = new ComplexForm(analyseData());
    assertEquals(2, cf.getKarmaCost(0));
  }

  @Test
  void karmaCostStaggersByNewRating() {
    ComplexForm cf = new ComplexForm(analyseData());
    // GRW-Beispiel (analog C#-Referenzformel): Rating 3 kostet insgesamt 2+2+3=7 Karma.
    assertEquals(2, cf.getKarmaCost(0)); // 0 -> 1
    assertEquals(2, cf.getKarmaCost(1)); // 1 -> 2
    assertEquals(3, cf.getKarmaCost(2)); // 2 -> 3
  }
}
