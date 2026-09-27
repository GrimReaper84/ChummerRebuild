package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

class CharacterCyberwareTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware implant(String essence, String cost) {
    return new Cyberware(new CyberwareData("Test", "Test", EquipmentKind.CYBERWARE, essence, cost,
        0, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }

  @Test
  void newCharacterHasNoImplants() {
    assertTrue(mensch().getCyberware().isEmpty());
  }

  @Test
  void sameImplantCanBeInstalledTwiceAndRemovedIndividually() {
    Character c = mensch();
    Cyberware first = implant("0.2", "1500");
    Cyberware second = implant("0.2", "1500");
    c.addCyberware(first);
    c.addCyberware(second);

    c.removeCyberware(first);

    assertEquals(1, c.getCyberware().size());
    assertTrue(c.getCyberware().get(0) == second);
  }

  @Test
  void implantsDoNotCostBuildPoints() {
    Character c = mensch();
    int spentBefore = c.getSpentBP();

    c.addCyberware(implant("0.5", "10000"));

    assertEquals(spentBefore, c.getSpentBP());
  }

  @Test
  void nuyenCostSumsAllImplantsIncludingGrade() {
    Character c = mensch();
    c.addCyberware(implant("0.1", "500"));
    Cyberware alpha = implant("0.1", "1000");
    alpha.setGrade(CyberwareGrade.ALPHAWARE);
    c.addCyberware(alpha);

    assertEquals(2500, c.getCyberwareNuyenCost());
  }

  @Test
  void onlyStandardAndAlphaAreAvailableAtCreation() {
    Character c = mensch();

    assertTrue(c.isCyberwareGradeAvailable(CyberwareGrade.STANDARD));
    assertTrue(c.isCyberwareGradeAvailable(CyberwareGrade.ALPHAWARE));
    assertFalse(c.isCyberwareGradeAvailable(CyberwareGrade.BETAWARE));
    assertFalse(c.isCyberwareGradeAvailable(CyberwareGrade.DELTAWARE));
  }

  @Test
  void allGradesAreAvailableInCareerMode() {
    Character c = mensch();
    c.setCreated(true);

    for (CyberwareGrade grade : CyberwareGrade.values()) {
      assertTrue(c.isCyberwareGradeAvailable(grade), grade.name());
    }
  }

  @Test
  void essenceAtOrBelowZeroIsFlaggedOnlyDuringCreation() {
    Character c = mensch();
    c.addCyberware(implant("5.9", "100"));
    assertFalse(c.isEssenceBelowCreationMinimum());

    c.addCyberware(implant("0.1", "100"));
    assertTrue(c.isEssenceBelowCreationMinimum());

    c.setCreated(true);
    assertFalse(c.isEssenceBelowCreationMinimum());
  }
}
