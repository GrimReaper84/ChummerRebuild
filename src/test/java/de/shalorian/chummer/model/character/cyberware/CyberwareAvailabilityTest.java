package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Verfügbarkeit der Implantate gegen die Erschaffungsgrenze 12 (GRW S. 111, Tabellen S. 386–394). */
class CyberwareAvailabilityTest {

  private static Availability availability(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data, name);
    return data.availability();
  }

  @Test
  void dermalArmorRatingTwoAllowedRatingThreeNot() {
    Availability dermal = availability("Dermalpanzerung");

    assertEquals("10E", dermal.displayText(2));
    assertTrue(dermal.isAllowedAtCreation(2));
    assertEquals("15E", dermal.displayText(3));
    assertFalse(dermal.isAllowedAtCreation(3));
  }

  @Test
  void skillwiresRatingThreeAllowedFourNot() {
    Availability skillwires = availability("Talentleitungen");

    assertTrue(skillwires.isAllowedAtCreation(3));
    assertFalse(skillwires.isAllowedAtCreation(4));
  }

  @Test
  void painEditorIsForbiddenAndTooHigh() {
    Availability painEditor = availability("Schmerzeditor");

    assertEquals("18V", painEditor.displayText(0));
    assertFalse(painEditor.isAllowedAtCreation(0));
  }

  @Test
  void entriesWithoutAvailabilityShowDash() {
    assertEquals("–", availability("Datenbuchse").displayText(0));
    assertEquals("–", availability("Sim-Modul").displayText(0));
  }

  @Test
  void everyEntryHasEvaluableAvailability() {
    for (CyberwareData data : CyberwareDataLoader.getAllCyberware().values()) {
      int maxRating = data.hasRating() ? data.maxRating() : 0;
      data.availability().evaluate(maxRating);
    }
  }

  @Test
  void characterBlocksHighAvailabilityOnlyAtCreation() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Availability dermal = availability("Dermalpanzerung");

    assertFalse(c.isAvailableForPurchase(dermal, 3));
    assertTrue(c.isAvailableForPurchase(dermal, 2));

    c.setCreated(true);
    assertTrue(c.isAvailableForPurchase(dermal, 3));
  }
}
