package de.shalorian.chummer.model.character.lifestyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LifestyleTest {

  @Test
  void defaultIsStreetForOneMonth() {
    Lifestyle lifestyle = new Lifestyle();

    assertEquals(LifestyleLevel.STREET, lifestyle.getLevel());
    assertEquals(1, lifestyle.getMonths());
    assertEquals(0, lifestyle.getNuyenCost());
  }

  @Test
  void costIsMonthlyCostTimesMonths() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 3);

    assertEquals(15_000, lifestyle.getNuyenCost());
  }

  @Test
  void atLeastOneMonth() {
    Lifestyle lifestyle = new Lifestyle();

    assertThrows(IllegalArgumentException.class, () -> lifestyle.setMonths(0));
    assertThrows(IllegalArgumentException.class, () -> new Lifestyle(LifestyleLevel.LOW, 0));
  }

  @Test
  void firstMissedMonthRollOneDowngrades() {
    // S. 312: Wurf ≤ ausgesetzte Monate in Folge → eine Stufe tiefer, ein Monat der alten Stufe als Schuld
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 2);
    MissedPaymentResult result = lifestyle.skipMonth(1);

    assertTrue(result.downgraded());
    assertEquals(1, result.missedMonths());
    assertEquals(5_000, result.debtAdded());
    assertEquals(LifestyleLevel.LOW, lifestyle.getLevel());
    assertEquals(5_000, lifestyle.getDebt());
    assertEquals(0, lifestyle.getMissedMonths());
    assertEquals(2, lifestyle.getMonths());
  }

  @Test
  void firstMissedMonthRollTwoIsNoProblem() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 1);
    MissedPaymentResult result = lifestyle.skipMonth(2);

    assertFalse(result.downgraded());
    assertEquals(1, lifestyle.getMissedMonths());
    assertEquals(LifestyleLevel.MIDDLE, lifestyle.getLevel());
    assertEquals(0, lifestyle.getDebt());
  }

  @Test
  void thirdMissedMonthComparesWithThree() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.HIGH, 1);
    lifestyle.skipMonth(6);
    lifestyle.skipMonth(6);
    Lifestyle safe = new Lifestyle(LifestyleLevel.HIGH, 1);
    safe.restoreArrears(2, 0);

    assertFalse(safe.skipMonth(4).downgraded());
    MissedPaymentResult result = lifestyle.skipMonth(3);
    assertTrue(result.downgraded());
    assertEquals(3, result.missedMonths());
    assertEquals(LifestyleLevel.MIDDLE, lifestyle.getLevel());
    assertEquals(10_000, lifestyle.getDebt());
  }

  @Test
  void paymentAndLevelChangeClearArrearsButNotDebt() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 1);
    lifestyle.skipMonth(1);
    lifestyle.skipMonth(6);
    assertEquals(1, lifestyle.getMissedMonths());
    lifestyle.payMonth();
    assertEquals(0, lifestyle.getMissedMonths());
    lifestyle.skipMonth(6);
    lifestyle.changeLevel(LifestyleLevel.SQUATTER);
    assertEquals(0, lifestyle.getMissedMonths());
    assertEquals(5_000, lifestyle.getDebt());
    lifestyle.payDebt();
    assertEquals(0, lifestyle.getDebt());
  }

  @Test
  void secondDowngradeAddsDebtOfThenCurrentLevel() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 1);
    lifestyle.skipMonth(1);
    lifestyle.skipMonth(1);

    assertEquals(LifestyleLevel.SQUATTER, lifestyle.getLevel());
    assertEquals(5_000 + 2_000, lifestyle.getDebt());
  }

  @Test
  void outcomeDoesNotChangeLifestyle() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 1);
    MissedPaymentResult outcome = lifestyle.skipOutcome(1);

    assertTrue(outcome.downgraded());
    assertEquals(LifestyleLevel.LOW, outcome.level());
    assertEquals(LifestyleLevel.MIDDLE, lifestyle.getLevel());
    assertEquals(0, lifestyle.getMissedMonths());
    assertEquals(outcome, lifestyle.skipMonth(1));
  }

  @Test
  void streetCannotBeSkippedAndRollMustBeValid() {
    assertThrows(IllegalStateException.class, () -> new Lifestyle().skipMonth(1));
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.LOW, 1);
    assertThrows(IllegalArgumentException.class, () -> lifestyle.skipMonth(0));
    assertThrows(IllegalArgumentException.class, () -> lifestyle.skipMonth(7));
  }
}
