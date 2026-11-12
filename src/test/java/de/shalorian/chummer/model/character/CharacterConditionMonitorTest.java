package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Zustandsmonitor und Verletzungsmodifikator (GRW S. 94, 116, 117, 120, 192/193). */
class CharacterConditionMonitorTest {

  private static Character mensch() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getAttribute(AttributeType.WIL).setValue(4);
    return c;
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void boxesAreEightPlusHalfAttributeRoundedUp() {
    Character c = mensch();
    assertEquals(10, c.getPhysicalBoxes());
    assertEquals(10, c.getStunBoxes());
    c.getAttribute(AttributeType.WIL).setValue(5);
    assertEquals(11, c.getStunBoxes());
  }

  @Test
  void woundModifierCountsBothColumns() {
    // GRW S. 193: 6 körperliche und 3 geistige Kästchen ergeben −3
    Character c = mensch();
    c.setPhysicalDamage(6);
    c.setStunDamage(3);
    assertEquals(-3, c.getWoundModifier());
    c.setStunDamage(2);
    assertEquals(-2, c.getWoundModifier());
  }

  @Test
  void highPainToleranceIgnoresBoxesPerColumn() {
    // GRW S. 116: Stufe 2 erlaubt je Spalte 2 Kästchen ohne Modifikator
    Character c = mensch();
    c.addQuality(q("Hohe Schmerztoleranz 2"), false);
    c.setPhysicalDamage(2);
    c.setStunDamage(2);
    assertEquals(0, c.getWoundModifier());
    c.setPhysicalDamage(5);
    assertEquals(-1, c.getWoundModifier());
  }

  @Test
  void lowPainToleranceCountsEveryTwoBoxes() {
    // GRW S. 120
    Character c = mensch();
    c.addQuality(q("Niedrige Schmerztoleranz"), false);
    c.setPhysicalDamage(4);
    assertEquals(-2, c.getWoundModifier());
  }

  @Test
  void willToLiveAddsOverflowBoxesOnly() {
    // GRW S. 117/192: Überzahl bis Konstitution, plus Stufe von Überlebenswille
    Character c = mensch();
    assertEquals(3, c.getPhysicalOverflowBoxes());
    c.addQuality(q("Überlebenswille 2"), false);
    assertEquals(5, c.getPhysicalOverflowBoxes());
    c.setPhysicalDamage(14);
    assertEquals(14, c.getPhysicalDamage());
    assertEquals(-3, c.getWoundModifier()); // nur die 10 Kästchen der Spalte zählen
  }

  @Test
  void damageIsClampedToTheMonitor() {
    Character c = mensch();
    c.setPhysicalDamage(99);
    assertEquals(13, c.getPhysicalDamage());
    c.setStunDamage(-2);
    assertEquals(0, c.getStunDamage());
    c.setStunDamage(10);
    c.getAttribute(AttributeType.WIL).setValue(1);
    assertEquals(9, c.getStunDamage());
  }

  @Test
  void woundModifierLowersInitiative() {
    Character c = mensch();
    c.getAttribute(AttributeType.REA).setValue(3);
    c.getAttribute(AttributeType.INT).setValue(4);
    c.setStunDamage(6);
    assertEquals(7, c.getInitiativeAttribute());
    assertEquals(5, c.getInitiative());
  }
}
