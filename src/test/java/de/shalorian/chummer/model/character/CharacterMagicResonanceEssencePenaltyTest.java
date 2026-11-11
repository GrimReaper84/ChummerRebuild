package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import org.junit.jupiter.api.Test;

/**
 * GRW S. 208: "Für jeden angefangenen Punkt Essenzverlust werden das Magieattribut des Charakters und
 * sein maximaler Magiewert um jeweils 1 Punkt reduziert" — gilt ebenso für Resonanz (S. 111).
 */
class CharacterMagicResonanceEssencePenaltyTest {

  private static Character withQuality(String qualityName) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    QualityData quality = QualityDataLoader.findByName(qualityName);
    assertNotNull(quality);
    c.addQuality(quality, false);
    return c;
  }

  private static Cyberware cyberware(String essence) {
    return new Cyberware(new CyberwareData("Test-" + essence, "Test", EquipmentKind.CYBERWARE,
        essence, "100", 0, Availability.NONE, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }

  private static SpecialAttribute magicOrResonance(Character c) {
    SpecialAttribute attr = c.getMagicOrResonance();
    assertNotNull(attr);
    return attr;
  }

  @Test
  void rulebookExampleMagicFourEssenceFivePointEight() {
    Character c = withQuality("Magier");
    magicOrResonance(c).setValue(4);

    c.getEquipment().addCyberware(cyberware("0.2"));

    assertEquals(5.8, c.getEssence());
    assertEquals(1, c.getEssencePenalty());
    assertEquals(3, c.getEffectiveMagicOrResonanceValue());
    assertEquals(5, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void investedValueIsNotMutated() {
    Character c = withQuality("Magier");
    magicOrResonance(c).setValue(4);
    int bpBefore = c.getSpecialAttributeBP();

    c.getEquipment().addCyberware(cyberware("0.2"));

    assertEquals(4, magicOrResonance(c).getValue());
    assertEquals(6, magicOrResonance(c).getMax());
    assertEquals(bpBefore, c.getSpecialAttributeBP());
  }

  @Test
  void noLossMeansNoPenalty() {
    Character c = withQuality("Magier");
    magicOrResonance(c).setValue(4);

    assertEquals(0, c.getEssencePenalty());
    assertEquals(4, c.getEffectiveMagicOrResonanceValue());
    assertEquals(6, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void smallestLossAlreadyCostsAFullPoint() {
    Character c = withQuality("Magier");
    c.getEquipment().addCyberware(cyberware("0.01"));

    assertEquals(1, c.getEssencePenalty());
  }

  @Test
  void furtherLossOnlyCountsOnceNextEssencePointIsStarted() {
    Character c = withQuality("Magier");
    c.getEquipment().addCyberware(cyberware("1.0"));
    assertEquals(1, c.getEssencePenalty());

    c.getEquipment().addCyberware(cyberware("0.01"));
    assertEquals(2, c.getEssencePenalty());
  }

  @Test
  void valueAtMaximumStaysAtReducedMaximum() {
    Character c = withQuality("Magier");
    magicOrResonance(c).setValue(6);

    c.getEquipment().addCyberware(cyberware("0.5"));

    assertEquals(5, c.getEffectiveMagicOrResonanceValue());
    assertEquals(5, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void effectiveValueNeverDropsBelowZero() {
    Character c = withQuality("Magier");
    c.getEquipment().addCyberware(cyberware("2.5"));

    assertEquals(3, c.getEssencePenalty());
    assertEquals(0, c.getEffectiveMagicOrResonanceValue());
  }

  @Test
  void penaltyAppliesIdenticallyInCareerMode() {
    Character c = withQuality("Magier");
    magicOrResonance(c).setValue(4);
    c.setCreated(true);

    c.getEquipment().addCyberware(cyberware("0.2"));

    assertEquals(3, c.getEffectiveMagicOrResonanceValue());
    assertEquals(5, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void resonanceIsReducedLikeMagic() {
    Character c = withQuality("Technomant");
    magicOrResonance(c).setValue(4);

    c.getEquipment().addCyberware(cyberware("0.2"));

    assertEquals(3, c.getEffectiveMagicOrResonanceValue());
    assertEquals(5, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void complexFormCapUsesReducedResonance() {
    Character c = withQuality("Technomant");
    magicOrResonance(c).setValue(2);
    var analyse = ComplexFormDataLoader.findByName("Analyse");
    assertNotNull(analyse);
    c.addComplexForm(analyse);
    assertTrue(c.canRaiseComplexForm(c.getComplexForms().getFirst()));

    c.getEquipment().addCyberware(cyberware("0.2"));

    assertFalse(c.canRaiseComplexForm(c.getComplexForms().getFirst()));
  }

  @Test
  void characterWithoutMagicOrResonanceHasZeroEffectiveValues() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getEquipment().addCyberware(cyberware("0.2"));

    assertEquals(0, c.getEffectiveMagicOrResonanceValue());
    assertEquals(0, c.getEffectiveMagicOrResonanceMax());
  }

  @Test
  void removingMagicQualityWithImplantsInstalledIsConsistent() {
    Character c = withQuality("Magier");
    c.getEquipment().addCyberware(cyberware("0.2"));

    c.removeQuality("Magier");

    assertFalse(c.hasMagic());
    assertEquals(0, c.getEffectiveMagicOrResonanceValue());
    assertEquals(5.8, c.getEssence());
  }
}
