package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** GRW S. 359, Tabelle "Implantatkategorien". */
class CyberwareGradeTest {

  @Test
  void onlyTheFourCoreGradesExist() {
    assertEquals(4, CyberwareGrade.values().length);
  }

  @Test
  void essenceMultipliersMatchRulebook() {
    assertEquals(0, BigDecimal.ONE.compareTo(CyberwareGrade.STANDARD.getEssenceMultiplier()));
    assertEquals(0, new BigDecimal("0.8").compareTo(CyberwareGrade.ALPHAWARE.getEssenceMultiplier()));
    assertEquals(0, new BigDecimal("0.7").compareTo(CyberwareGrade.BETAWARE.getEssenceMultiplier()));
    assertEquals(0, new BigDecimal("0.5").compareTo(CyberwareGrade.DELTAWARE.getEssenceMultiplier()));
  }

  @Test
  void priceMultipliersMatchRulebook() {
    assertEquals(1, CyberwareGrade.STANDARD.getPriceMultiplier());
    assertEquals(2, CyberwareGrade.ALPHAWARE.getPriceMultiplier());
    assertEquals(4, CyberwareGrade.BETAWARE.getPriceMultiplier());
    assertEquals(10, CyberwareGrade.DELTAWARE.getPriceMultiplier());
  }

  @Test
  void onlyStandardAndAlphaAreAvailableAtCreation() {
    assertTrue(CyberwareGrade.STANDARD.isAvailableAtCreation());
    assertTrue(CyberwareGrade.ALPHAWARE.isAvailableAtCreation());
    assertFalse(CyberwareGrade.BETAWARE.isAvailableAtCreation());
    assertFalse(CyberwareGrade.DELTAWARE.isAvailableAtCreation());
  }
}
