package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.QualityTestSupport.selected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualityType;
import org.junit.jupiter.api.Test;

/**
 * GRW S. 120, Handicap "Immunabstoßung": doppelter Essenzverlust durch Cyberware, Bioware unverändert.
 */
class CharacterImmunabstossungEssenceTest {

  private static final String IMMUNABSTOSSUNG = "Immunabstoßung";

  private static Character withImmunabstossung() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(immunabstossung(), false);
    return c;
  }

  private static QualityData immunabstossung() {
    QualityData data = QualityDataLoader.findByName(IMMUNABSTOSSUNG);
    assertNotNull(data);
    return data;
  }

  private static Cyberware implant(EquipmentKind kind, String essence) {
    return new Cyberware(new CyberwareData("Test-" + kind + "-" + essence, "Test", kind, essence,
        "100", 0, Availability.NONE, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }

  @Test
  void qualityIsLoadedWithRulebookValues() {
    QualityData data = immunabstossung();
    assertEquals(-15, data.bp());
    assertEquals(QualityType.NEGATIVE, data.type());
    assertTrue(data.bonuses().contains(QualityBonusType.DOUBLES_CYBERWARE_ESSENCE));
  }

  @Test
  void cyberwareLossIsDoubled() {
    Character c = withImmunabstossung();
    c.getEquipment().addCyberware(implant(EquipmentKind.CYBERWARE, "1.0"));

    assertEquals(2.0, c.getCyberwareEssenceLoss());
    assertEquals(4.0, c.getEssence());
  }

  @Test
  void biowareLossIsNotAffected() {
    Character c = withImmunabstossung();
    c.getEquipment().addCyberware(implant(EquipmentKind.BIOWARE, "1.0"));

    assertEquals(1.0, c.getBiowareEssenceLoss());
    assertEquals(5.0, c.getEssence());
  }

  @Test
  void doubledCyberwareSumEntersCombinationRule() {
    Character c = withImmunabstossung();
    c.getEquipment().addCyberware(implant(EquipmentKind.CYBERWARE, "1.0"));
    c.getEquipment().addCyberware(implant(EquipmentKind.BIOWARE, "2.0"));

    // Cyberware verdoppelt 2,0, Bioware 2,0 → 6 − 2,0 − 2,0/2 = 3,0
    assertEquals(3.0, c.getEssence());
  }

  @Test
  void doubledCyberwareCanBecomeTheHigherSum() {
    Character c = withImmunabstossung();
    c.getEquipment().addCyberware(implant(EquipmentKind.CYBERWARE, "1.5"));
    c.getEquipment().addCyberware(implant(EquipmentKind.BIOWARE, "2.5"));

    // Ohne Handicap 2,75 — mit Handicap: Cyberware 3,0 (voll), Bioware 2,5 (halb) → 6 − 3 − 1,25 = 1,75
    assertEquals(1.75, c.getEssence());
  }

  @Test
  void removingQualityRestoresNormalEssence() {
    Character c = withImmunabstossung();
    c.getEquipment().addCyberware(implant(EquipmentKind.CYBERWARE, "1.0"));

    c.removeQuality(selected(c, IMMUNABSTOSSUNG));

    assertEquals(5.0, c.getEssence());
  }
}
