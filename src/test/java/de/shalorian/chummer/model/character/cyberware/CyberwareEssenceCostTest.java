package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.equipment.Availability;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CyberwareEssenceCostTest {

  private static final CyberwareData DATENBUCHSE = new CyberwareData("Datenbuchse", "Headware",
      EquipmentKind.CYBERWARE, "0.1", "500", 0, Availability.NONE, "SR4", "386");
  private static final CyberwareData DERMALPANZERUNG = new CyberwareData("Dermalpanzerung",
      "Bodyware", EquipmentKind.CYBERWARE, "Rating * 0.5", "Rating * 5000", 3,
      Availability.NONE, "SR4", "389");

  private static void assertEssence(String expected, Cyberware implant) {
    assertEquals(0, new BigDecimal(expected).compareTo(implant.getEssenceCost()),
        () -> "erwartet " + expected + ", war " + implant.getEssenceCost());
  }

  @Test
  void fixedEssenceItem() {
    Cyberware implant = new Cyberware(DATENBUCHSE, CyberwareGrade.STANDARD, 0);
    assertEssence("0.1", implant);
    assertEquals(500, implant.getNuyenCost());
  }

  @Test
  void ratingDependentEssenceItem() {
    assertEssence("0.5", new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 1));
    assertEssence("1.5", new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 3));
    assertEquals(15000, new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 3).getNuyenCost());
  }

  @Test
  void gradeChangeOfSameItemAppliesMultipliers() {
    Cyberware implant = new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 2);
    assertEssence("1.0", implant);

    implant.setGrade(CyberwareGrade.ALPHAWARE);
    assertEssence("0.8", implant);
    assertEquals(20000, implant.getNuyenCost());

    implant.setGrade(CyberwareGrade.BETAWARE);
    assertEssence("0.7", implant);
    assertEquals(40000, implant.getNuyenCost());

    implant.setGrade(CyberwareGrade.DELTAWARE);
    assertEssence("0.5", implant);
    assertEquals(100000, implant.getNuyenCost());
  }

  @Test
  void essenceCostIsNotRoundedPerItem() {
    CyberwareData schlafregulator = new CyberwareData("Schlafregulator", "Kultivierte Bioware",
        EquipmentKind.BIOWARE, "0.15", "10000", 0, Availability.NONE, "SR4", "394");
    assertEssence("0.105", new Cyberware(schlafregulator, CyberwareGrade.BETAWARE, 0));
  }

  @Test
  void invalidRatingIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 0));
    assertThrows(IllegalArgumentException.class,
        () -> new Cyberware(DERMALPANZERUNG, CyberwareGrade.STANDARD, 4));
    assertThrows(IllegalArgumentException.class,
        () -> new Cyberware(DATENBUCHSE, CyberwareGrade.STANDARD, 1));
  }
}
