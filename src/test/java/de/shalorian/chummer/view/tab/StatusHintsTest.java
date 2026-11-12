package de.shalorian.chummer.view.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Hinweise der Status-Übersicht. */
class StatusHintsTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static void implant(Character c, String name, int rating) {
    c.getEquipment().addCyberware(new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName(name)),
        CyberwareGrade.STANDARD, rating));
  }

  @Test
  void freshCharacterHasNoHints() {
    assertTrue(StatusHints.of(mensch()).isEmpty());
  }

  @Test
  void overspentResourcesAtCreation() {
    Character c = mensch();
    implant(c, "Datenbuchse", 0);
    assertEquals(List.of("Ressourcengeld um 500 ¥ überzogen"), StatusHints.of(c));
  }

  @Test
  void essenceAtZeroAtCreation() {
    Character c = mensch();
    c.setNuyenBP(50);
    implant(c, "Kunstmuskeln", 4);
    implant(c, "Reflexbooster", 1);
    assertEquals(List.of("Essenz darf bei der Erschaffung nicht auf 0 oder darunter fallen"), StatusHints.of(c));
  }

  @Test
  void careerIgnoresCreationLimits() {
    Character c = mensch();
    c.setCreated(true);
    implant(c, "Datenbuchse", 0);
    assertTrue(StatusHints.of(c).isEmpty());
  }

  @Test
  void overspentBuildPoints() {
    Character c = mensch();
    for (AttributeType type : AttributeType.values()) {
      c.getAttribute(type).setValue(c.getAttribute(type).getMax());
    }
    assertEquals(List.of("BP um " + -c.getRemainingBP() + " überzogen"), StatusHints.of(c));
  }

  @Test
  void overspentPowerPointsInBothModes() {
    Character c = mensch();
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Adept")), false);
    SpecialAttribute magic = Objects.requireNonNull(c.getMagicOrResonance());
    magic.setValue(2);
    AdeptPower reflexes = c.getAdeptPowers().add(
        Objects.requireNonNull(AdeptPowerDataLoader.findByName("Gesteigerte Reflexe")), "");
    assertEquals(1, reflexes.getLevel());
    magic.setValue(1);
    String problem = Objects.requireNonNull(c.getAdeptPowers().getProblem());
    assertEquals(List.of(problem), StatusHints.of(c));
    c.setCreated(true);
    assertEquals(List.of(problem), StatusHints.of(c));
  }
}
