package de.shalorian.chummer.model.character.lifestyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
