package de.shalorian.chummer.model.character.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Verfügbarkeit und Erschaffungsgrenze 12 (SR4-Grundregelwerk S. 111). */
class AvailabilityTest {

  @Test
  void fixedValueIgnoresRating() {
    Availability availability = new Availability("8", Legality.LEGAL);

    assertEquals(8, availability.evaluate(0));
    assertEquals(8, availability.evaluate(3));
    assertEquals("8", availability.displayText(0));
  }

  @Test
  void ratingFormulaWithRestrictedSuffix() {
    Availability dermal = new Availability("Rating * 5", Legality.RESTRICTED);

    assertEquals(15, dermal.evaluate(3));
    assertEquals("15E", dermal.displayText(3));
    assertFalse(dermal.isAllowedAtCreation(3));
    assertTrue(dermal.isAllowedAtCreation(2));
  }

  @Test
  void twelveIsAllowedThirteenIsNot() {
    assertTrue(new Availability("12", Legality.LEGAL).isAllowedAtCreation(0));
    assertFalse(new Availability("13", Legality.LEGAL).isAllowedAtCreation(0));
  }

  @Test
  void forbiddenSuffix() {
    assertEquals("18V", new Availability("18", Legality.FORBIDDEN).displayText(0));
  }

  @Test
  void noneIsDisplayedAsDash() {
    assertEquals("–", Availability.NONE.displayText(0));
    assertTrue(Availability.NONE.isAllowedAtCreation(0));
  }

  @Test
  void legalityCodes() {
    assertEquals(Legality.LEGAL, Legality.fromCode(""));
    assertEquals(Legality.RESTRICTED, Legality.fromCode("E"));
    assertEquals(Legality.FORBIDDEN, Legality.fromCode(" V "));
    assertThrows(IllegalArgumentException.class, () -> Legality.fromCode("X"));
  }

  @Test
  void parseReadsCatalogFields() {
    assertEquals(new Availability("Rating * 5", Legality.RESTRICTED),
        Availability.parse(" Rating * 5 ", "E"));
    assertEquals(new Availability("8", Legality.LEGAL), Availability.parse("8", ""));
  }

  @Test
  void parseTreatsMissingAvailabilityAsNone() {
    assertEquals(Availability.NONE, Availability.parse("", ""));
  }
}
