package de.shalorian.chummer.model.character.lifestyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tabellenwerte (GRW S. 111/114) und Startkapital-Formel (GRW S. 114). */
class LifestyleLevelTest {

  @Test
  void tableValuesMatchRulebook() {
    assertLevel(LifestyleLevel.STREET, 0, 1, 10);
    assertLevel(LifestyleLevel.SQUATTER, 500, 2, 20);
    assertLevel(LifestyleLevel.LOW, 2_000, 3, 50);
    assertLevel(LifestyleLevel.MIDDLE, 5_000, 4, 100);
    assertLevel(LifestyleLevel.HIGH, 10_000, 4, 500);
    assertLevel(LifestyleLevel.LUXURY, 100_000, 4, 1_000);
  }

  private static void assertLevel(LifestyleLevel level, int monthly, int dice, int multiplier) {
    assertEquals(monthly, level.getMonthlyCost(), level.name());
    assertEquals(dice, level.getDice(), level.name());
    assertEquals(multiplier, level.getMultiplier(), level.name());
  }

  @Test
  void middleClassWithRemainingNuyen() {
    // Wurf 14, Rest 1.250 ¥ → Bonus 12 → (14 + 12) × 100
    assertEquals(2_600, LifestyleLevel.MIDDLE.startingCapital(14, 1_250));
  }

  @Test
  void bonusIsCappedAtThreePerDie() {
    // Straße: 1 Würfel → Bonus höchstens 3, obwohl 5.000 ¥ übrig sind
    assertEquals(3, LifestyleLevel.STREET.restBonus(5_000));
    assertEquals((4 + 3) * 10, LifestyleLevel.STREET.startingCapital(4, 5_000));
  }

  @Test
  void onlyFullHundredsCount() {
    assertEquals(0, LifestyleLevel.MIDDLE.restBonus(99));
    assertEquals(1, LifestyleLevel.MIDDLE.restBonus(199));
  }

  @Test
  void negativeRemainingGivesNoBonus() {
    assertEquals(0, LifestyleLevel.MIDDLE.restBonus(-500));
    assertEquals(1_000, LifestyleLevel.MIDDLE.startingCapital(10, -500));
  }

  @Test
  void impossibleDiceSumIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> LifestyleLevel.MIDDLE.startingCapital(3, 0));
    assertThrows(IllegalArgumentException.class,
        () -> LifestyleLevel.MIDDLE.startingCapital(25, 0));
    assertEquals(4 * 100, LifestyleLevel.MIDDLE.startingCapital(4, 0));
    assertEquals(24 * 100, LifestyleLevel.MIDDLE.startingCapital(24, 0));
  }

  @Test
  void luxuryMaximumFitsIntoInt() {
    assertEquals((24 + 12) * 1_000, LifestyleLevel.LUXURY.startingCapital(24, 250_000));
  }

  @Test
  void rolledSumStaysWithinDiceRange() {
    Random random = new Random(42);
    for (LifestyleLevel level : LifestyleLevel.values()) {
      for (int i = 0; i < 200; i++) {
        int sum = level.rollDiceSum(random);
        assertTrue(sum >= level.getMinDiceSum() && sum <= level.getMaxDiceSum(),
            level + ": " + sum);
      }
    }
  }

  @Test
  void rollIsReproducibleWithSameSeed() {
    assertEquals(LifestyleLevel.MIDDLE.rollDiceSum(new Random(7)),
        LifestyleLevel.MIDDLE.rollDiceSum(new Random(7)));
  }
}
