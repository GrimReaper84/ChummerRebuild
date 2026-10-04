package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Abschluss der Erschaffung: Hinderungsgründe und Startkapital nach GRW S. 111/114. */
class CharacterFinalizeTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware implant(String essence, String cost) {
    return new Cyberware(new CyberwareData("Test", "Test", EquipmentKind.CYBERWARE, essence, cost,
        0, Availability.NONE, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }

  @Test
  void negativeRemainingNuyenBlocksFinalization() {
    Character c = mensch();
    c.setNuyenBP(1);
    c.getEquipment().addCyberware(implant("0.1", "6000"));

    assertEquals(1, c.getFinalizeProblems().size());
    assertThrows(IllegalStateException.class, () -> c.finalizeCreation(3));
    assertFalse(c.isCreated());
    assertTrue(c.getNuyenLog().isEmpty());
  }

  @Test
  void essenceAtZeroIsNoFinalizeProblem() {
    Character c = mensch();
    c.setNuyenBP(10);
    c.getEquipment().addCyberware(implant("6", "100"));

    assertTrue(c.isEssenceBelowCreationMinimum());
    assertTrue(c.getFinalizeProblems().isEmpty());
  }

  @Test
  void finalizationBooksOnlyLifestyleStartingCapital() {
    Character c = mensch();
    c.setNuyenBP(10);
    c.getEquipment().getLifestyle().setLevel(LifestyleLevel.MIDDLE);
    c.getEquipment().addCyberware(implant("0.1", "43750"));
    // Rest: 50.000 − 5.000 (Lebensstil) − 43.750 = 1.250 ¥ → Bonus 12

    int capital = c.finalizeCreation(14);

    assertEquals(2_600, capital);
    assertEquals(1, c.getNuyenLog().size());
    assertEquals(2_600, c.getNuyenTotal());
    assertEquals("Startkapital (Lebensstil Mittelschicht)", c.getNuyenLog().get(0).description());
    assertTrue(c.isCreated());
    assertEquals(0, c.getNuyenBP());
  }

  @Test
  void unspentResourcesAreForfeited() {
    Character c = mensch();
    c.setNuyenBP(50);

    int capital = c.finalizeCreation(6);

    // Straße: (6 + max. 3) × 10 — nicht 250.000 ¥ wie beim früheren Abschluss
    assertEquals(90, capital);
    assertEquals(90, c.getNuyenTotal());
  }

  @Test
  void cannotFinalizeTwice() {
    Character c = mensch();
    c.finalizeCreation(1);

    assertThrows(IllegalStateException.class, () -> c.finalizeCreation(1));
  }

  @Test
  void impossibleDiceSumIsRejectedWithoutSideEffects() {
    Character c = mensch();

    assertThrows(IllegalArgumentException.class, () -> c.finalizeCreation(7));
    assertFalse(c.isCreated());
  }
}
