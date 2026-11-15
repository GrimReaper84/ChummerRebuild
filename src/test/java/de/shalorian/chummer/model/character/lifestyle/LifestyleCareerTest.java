package de.shalorian.chummer.model.character.lifestyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Lebensstil im Karrieremodus: Monat bezahlen und Stufe wechseln (GRW S. 310). */
class LifestyleCareerTest {

  @Test
  void payMonthAddsAMonth() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 2);
    lifestyle.payMonth();
    assertEquals(3, lifestyle.getMonths());
    assertEquals(5_000, lifestyle.getLevel().getMonthlyCost());
  }

  @Test
  void changeLevelPaysTheFirstMonthOfTheNewLevel() {
    Lifestyle lifestyle = new Lifestyle(LifestyleLevel.MIDDLE, 4);
    lifestyle.changeLevel(LifestyleLevel.LOW);
    assertEquals(LifestyleLevel.LOW, lifestyle.getLevel());
    assertEquals(1, lifestyle.getMonths());
  }
}
