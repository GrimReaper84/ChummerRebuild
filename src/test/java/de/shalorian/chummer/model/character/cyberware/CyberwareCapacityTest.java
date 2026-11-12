package de.shalorian.chummer.model.character.cyberware;

import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.add;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.data;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.implant;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.mensch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Basissysteme mit Kapazität und eingebautes Zubehör (GRW S. 386–388). */
class CyberwareCapacityTest {

  @Test
  void baseSystemHasFourCapacityPerRating() {
    Cyberware eyes = implant("Cyberaugen-Basissystem", 3);
    assertEquals(12, eyes.getCapacity());
    assertEquals(0, eyes.getUsedCapacity());
  }

  @Test
  void installedAccessoryCostsCapacityAndNoEssence() {
    Character c = mensch();
    Cyberware eyes = add(c, "Cyberaugen-Basissystem", 3);
    eyes.install(implant("Restlichtverstärkung", 0));
    eyes.install(implant("Sichtverbesserung", 3));

    assertEquals(5, eyes.getUsedCapacity());
    // Essenz nur vom Basissystem (Stufe 3: 0,4), das Zubehör kostet Kapazität (S. 386)
    assertEquals(0, new BigDecimal("0.4").compareTo(c.getEquipment().getCyberwareEssenceCost()));
    assertEquals(1000 + 1000 + 3 * 1500, eyes.getNuyenCost());
  }

  @Test
  void overfullCapacityIsRejected() {
    Cyberware eyes = implant("Cyberaugen-Basissystem", 1);
    eyes.install(implant("Smartlink", 0));

    String problem = eyes.installProblem(data("Restlichtverstärkung"), 0);
    assertNotNull(problem);
    assertTrue(problem.startsWith("Kapazität reicht nicht"), problem);
    assertThrows(IllegalArgumentException.class, () -> eyes.install(implant("Restlichtverstärkung", 0)));
  }

  @Test
  void includedPartsAndForeignCategoriesCannotBeInstalled() {
    Cyberware eyes = implant("Cyberaugen-Basissystem", 4);
    assertNotNull(eyes.installProblem(data("Bildverbindung"), 0));
    assertNotNull(eyes.installProblem(data("Dämpfer"), 0));
    assertNotNull(eyes.installProblem(data("Schutzgläser"), 0));
    assertNotNull(implant("Datenbuchse", 0).installProblem(data("Smartlink"), 0));
  }

  @Test
  void sameAccessoryOnlyOnce() {
    Cyberware ears = implant("Cyberohren", 4);
    ears.install(implant("Dämpfer", 0));
    assertNotNull(ears.installProblem(data("Dämpfer"), 0));
  }

  @Test
  void ocularDroneFitsOnlyIntoEyes() {
    Cyberware eyes = implant("Cyberaugen-Basissystem", 2);
    assertNull(eyes.installProblem(data("Okular-Drohne"), 0));
  }

  @Test
  void accessoryTakesGradeOfBaseSystem() {
    Cyberware eyes = new Cyberware(data("Cyberaugen-Basissystem"), CyberwareGrade.ALPHAWARE, 2);
    Cyberware flare = implant("Blitzkompensation", 0);
    eyes.install(flare);
    assertEquals(CyberwareGrade.ALPHAWARE, flare.getGrade());

    eyes.setGrade(CyberwareGrade.BETAWARE);
    assertEquals(CyberwareGrade.BETAWARE, flare.getGrade());
    // Betaware: Preis × 4 (S. 359) für Basissystem und Zubehör
    assertEquals((750 + 750) * 4, eyes.getNuyenCost());
  }

  @Test
  void uninstallRemovesAccessory() {
    Cyberware ears = implant("Cyberohren", 1);
    Cyberware damper = implant("Dämpfer", 0);
    ears.install(damper);
    assertTrue(ears.uninstall(damper));
    assertEquals(0, ears.getUsedCapacity());
  }
}
