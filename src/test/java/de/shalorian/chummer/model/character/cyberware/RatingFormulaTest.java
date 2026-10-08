package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RatingFormulaTest {

  private static void assertValue(String expected, BigDecimal actual) {
    assertEquals(0, new BigDecimal(expected).compareTo(actual),
        () -> "erwartet " + expected + ", war " + actual);
  }

  @Test
  void fixedValueIgnoresRating() {
    assertValue("0.1", RatingFormula.evaluate("0.1", 0));
    assertValue("0.1", RatingFormula.evaluate("0.1", 5));
    assertValue("10000", RatingFormula.evaluate("10000", 3));
  }

  @Test
  void ratingAloneReturnsRating() {
    assertValue("4", RatingFormula.evaluate("Rating", 4));
  }

  @Test
  void ratingTimesFactor() {
    assertValue("1.5", RatingFormula.evaluate("Rating * 0.5", 3));
    assertValue("5000", RatingFormula.evaluate("Rating * 5000", 1));
  }

  @Test
  void ratingPlusAndMinus() {
    assertValue("5", RatingFormula.evaluate("Rating + 2", 3));
    assertValue("2", RatingFormula.evaluate("Rating - 1", 3));
  }

  @Test
  void whitespaceIsTolerated() {
    assertValue("0.6", RatingFormula.evaluate("  Rating*0.2 ", 3));
  }

  @Test
  void unsupportedExpressionThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> RatingFormula.evaluate("FixedValues(2,3,5)", 1));
    assertThrows(IllegalArgumentException.class,
        () -> RatingFormula.evaluate("Rating * 0.1 + 0.1", 1));
    assertThrows(IllegalArgumentException.class, () -> RatingFormula.evaluate("", 1));
    assertThrows(IllegalArgumentException.class, () -> RatingFormula.evaluate("0,5", 1));
  }

  @Test
  void basePlusRatingTimesFactor() {
    // Lenkrakete: Rakete 500 ¥ + (Sensorstufe x 500) ¥ (GRW S. 371)
    assertValue("2000", RatingFormula.evaluate("500 + Rating * 500", 3));
    assertValue("1500", RatingFormula.evaluate("1000+Rating*500", 1));
  }
}
