package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Essenz wird an der Berechnungsstelle auf 2 Nachkommastellen gerundet (kaufmännisch). */
class CharacterEssenceRoundingTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware implant(EquipmentKind kind, String essence, CyberwareGrade grade) {
    return new Cyberware(new CyberwareData("Test-" + essence, "Test", kind, essence, "100", 0,
        Availability.NONE, "SR4", "0"), grade, 0);
  }

  @Test
  void threeDecimalGradeResultIsRoundedToTwo() {
    Character c = mensch();
    // Schlafregulator als Betaware: 0,15 × 0,7 = 0,105 → 6 − 0,105 = 5,895 → 5,90
    c.addCyberware(implant(EquipmentKind.BIOWARE, "0.15", CyberwareGrade.BETAWARE));

    assertEquals(5.9, c.getEssence());
  }

  @Test
  void halvedLowerSumIsRoundedToTwo() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "0.1", CyberwareGrade.STANDARD));
    c.addCyberware(implant(EquipmentKind.BIOWARE, "0.05", CyberwareGrade.STANDARD));

    // 6 − 0,1 − 0,025 = 5,875 → 5,88
    assertEquals(5.88, c.getEssence());
  }

  @Test
  void sumOfDecimalsHasNoFloatingPointArtefacts() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "0.1", CyberwareGrade.STANDARD));
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "0.2", CyberwareGrade.STANDARD));

    assertEquals(5.7, c.getEssence());
    assertEquals(0.3, c.getCyberwareEssenceLoss());
  }

  @Test
  void lossBelowRoundingThresholdCausesNoPenalty() {
    Character c = mensch();
    c.addCyberware(implant(EquipmentKind.CYBERWARE, "0.004", CyberwareGrade.STANDARD));

    // Angezeigte Essenz und Penalty beruhen auf demselben gerundeten Wert.
    assertEquals(6.0, c.getEssence());
    assertEquals(0, c.getEssencePenalty());
  }
}
