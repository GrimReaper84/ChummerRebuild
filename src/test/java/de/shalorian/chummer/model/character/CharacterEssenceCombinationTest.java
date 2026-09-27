package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/**
 * GRW S. 111: Cyberware- und Bioware-Essenzverlust werden getrennt summiert, die höhere Summe wird voll,
 * die niedrigere zur Hälfte abgezogen.
 */
class CharacterEssenceCombinationTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware implant(EquipmentKind kind, String essence) {
    return new Cyberware(new CyberwareData("Test-" + kind + "-" + essence, "Test", kind, essence,
        "100", 0, Availability.NONE, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }

  @Test
  void characterWithoutImplantsHasEssenceSix() {
    assertEquals(6.0, mensch().getEssence());
  }

  @Test
  void rulebookExampleOnePointFiveCyberAndTwoPointFiveBio() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "1.5"));
    c.addCyberware(implant(EquipmentKind.BIOWARE, "2.5"));

    // 6 − 2,5 (volle Bioware) − 0,75 (halbe Cyberware) = 2,75
    assertEquals(2.75, c.getEssence());
  }

  @Test
  void onlyCyberwareIsDeductedFully() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "1.2"));
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "0.3"));

    assertEquals(4.5, c.getEssence());
  }

  @Test
  void onlyBiowareIsDeductedFully() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.BIOWARE, "0.4"));

    assertEquals(5.6, c.getEssence());
  }

  @Test
  void exactTieDeductsOneSumFullyAndTheOtherHalf() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "1.0"));
    c.addCyberware(implant(EquipmentKind.BIOWARE, "1.0"));

    assertEquals(4.5, c.getEssence());
  }

  @Test
  void separateLossesAreReportedBeforeCombination() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "1.5"));
    c.addCyberware(implant(EquipmentKind.BIOWARE, "2.5"));

    assertEquals(1.5, c.getCyberwareEssenceLoss());
    assertEquals(2.5, c.getBiowareEssenceLoss());
  }

  @Test
  void removingImplantRestoresEssence() {
    Character c = mensch();
    Cyberware implant = implant(EquipmentKind.CYBERWARE, "0.5");
    c.addCyberware(implant);

    c.removeCyberware(implant);

    assertEquals(6.0, c.getEssence());
  }

  @Test
  void gradeReducesEssenceLoss() {
    Character c = mensch();
    Cyberware implant = implant(EquipmentKind.CYBERWARE, "1.0");
    implant.setGrade(CyberwareGrade.ALPHAWARE);
    c.addCyberware(implant);

    assertEquals(5.2, c.getEssence());
  }
}
