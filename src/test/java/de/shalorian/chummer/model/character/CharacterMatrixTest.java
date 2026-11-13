package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.technomancer.LivingPersona;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Aktives Kommlink, VR-Zugang, Matrixinitiative und Lebende Persona (GRW S. 258, 265, 276, 279). */
class CharacterMatrixTest {

  private static GearData gear(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  private static Character human(int intuition) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.INT).setValue(intuition);
    c.getAttribute(AttributeType.REA).setValue(3);
    return c;
  }

  /** Hermes Ikon (Prozessor 4) mit Novatech Navi (System 4). */
  private static Gear commlink() {
    Gear device = new Gear(gear("Hermes Ikon"), 0);
    device.install(new Gear(gear("Novatech Navi"), 0));
    return device;
  }

  private static Gear simModule(boolean hot) {
    return new Gear(gear(hot ? "Sim-Modul (für BTL/heißes Sim modifiziert)" : "Sim-Modul"), 0, 1, "",
        hot ? 250 : 100);
  }

  private static Cyberware implant(String name) {
    return new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName(name), name), CyberwareGrade.STANDARD,
        0);
  }

  @Test
  void withoutCommlinkNoAccessAndArWithoutSimModule() {
    Character c = human(4);
    assertEquals(MatrixAccess.NONE, c.getMatrixAccess());
    assertEquals("Kein Kommlink", c.getMatrixAccessHint());

    c.getEquipment().addGear(commlink());
    assertEquals(MatrixAccess.AR, c.getMatrixAccess());
    assertEquals("Für VR fehlt: Sim-Modul (im aktiven Kommlink, implantiert oder im Simrig), Direktes Neuralinterface",
        c.getMatrixAccessHint());
    // S. 265: in AR gilt die Körperliche Initiative
    assertEquals(c.getInitiative(), c.getMatrixInitiative());
    assertEquals(c.getInitiativePasses(), c.getMatrixInitiativePasses());
  }

  @Test
  void vrNeedsSimModuleAndNeuralInterface() {
    Character c = human(4);
    Gear device = commlink();
    device.install(simModule(false));
    c.getEquipment().addGear(device);
    assertEquals("Für VR fehlt: Direktes Neuralinterface", c.getMatrixAccessHint());

    c.getEquipment().addGear(new Gear(gear("Elektroden"), 0));
    assertEquals(MatrixAccess.VR_COLD, c.getMatrixAccess());
    assertNull(c.getMatrixAccessHint());
  }

  @Test
  void matrixInitiativeColdAndHot() {
    // S. 276: kalt Prozessor + Intuition, 2 Durchgänge; heiß + 1, 3 Durchgänge
    Character cold = human(4);
    Gear coldLink = commlink();
    coldLink.install(simModule(false));
    cold.getEquipment().addGear(coldLink);
    cold.getEquipment().addCyberware(implant("Datenbuchse"));
    assertEquals(8, cold.getMatrixInitiative());
    assertEquals(2, cold.getMatrixInitiativePasses());

    Character hot = human(4);
    Gear hotLink = commlink();
    hotLink.install(simModule(true));
    hot.getEquipment().addGear(hotLink);
    hot.getEquipment().addCyberware(implant("Datenbuchse"));
    assertEquals(MatrixAccess.VR_HOT, hot.getMatrixAccess());
    assertEquals(9, hot.getMatrixInitiative());
    assertEquals(3, hot.getMatrixInitiativePasses());

    hot.setStunDamage(3); // Verletzungsmodifikator −1
    assertEquals(8, hot.getMatrixInitiative());
  }

  @Test
  void implantedSimModuleWorksWithAnyCommlink() {
    Character c = human(3);
    c.getEquipment().addGear(commlink());
    c.getEquipment().addCyberware(implant("Sim-Modul (modifiziert für heißes Sim)"));
    assertEquals(MatrixAccess.VR_HOT, c.getMatrixAccess());
    assertEquals(4 + 3 + 1, c.getMatrixInitiative());
  }

  @Test
  void processorLimitLowersMatrixInitiative() {
    Character c = human(4);
    Gear device = commlink();
    device.install(simModule(false));
    for (String name : new String[] {"Analyse", "Befehl", "Editieren", "Scannen"}) {
      Gear program = new Gear(gear(name), 2);
      device.install(program);
      program.setRunning(true);
    }
    c.getEquipment().addGear(device);
    c.getEquipment().addGear(new Gear(gear("Simrig"), 0));
    // System 4, 4 laufende Programme → Prozessor 3 (S. 261)
    assertEquals(3 + 4, c.getMatrixInitiative());
  }

  @Test
  void activeCommlinkFromHeadwareAndChoiceAmongSeveral() {
    Character c = human(4);
    Cyberware headware = implant("Kommlink");
    Gear implanted = commlink();
    implanted.install(simModule(false));
    headware.setCommlink(implanted);
    c.getEquipment().addCyberware(headware);
    assertSame(implanted, c.getEquipment().getActiveCommlink());
    assertEquals(MatrixAccess.VR_COLD, c.getMatrixAccess()); // implantiertes Kommlink ist auch das Interface

    Gear carried = new Gear(gear("Meta Link"), 0);
    c.getEquipment().addGear(carried);
    assertNull(c.getEquipment().getActiveCommlink());
    assertEquals("Aktives Kommlink wählen", c.getMatrixAccessHint());

    c.getEquipment().setActiveCommlink(carried);
    assertEquals(MatrixAccess.AR, c.getMatrixAccess());
    c.getEquipment().removeGear(carried);
    assertSame(implanted, c.getEquipment().getActiveCommlink());
    assertThrows(IllegalArgumentException.class, () -> c.getEquipment().setActiveCommlink(carried));
  }

  @Test
  void livingPersonaCappedByResonance() {
    // S. 279: Resonanz 3, Willenskraft 5 → Firewall 3; Signal = halbe Resonanz aufgerundet
    Character c = withGift("Technomancer", 3);
    c.getAttribute(AttributeType.WIL).setValue(5);
    c.getAttribute(AttributeType.INT).setValue(3);
    c.getAttribute(AttributeType.LOG).setValue(2);
    c.getAttribute(AttributeType.CHA).setValue(4);
    LivingPersona persona = Objects.requireNonNull(c.getLivingPersona());
    assertEquals(3, persona.firewall());
    assertEquals(3, persona.processor());
    assertEquals(3, persona.vrProcessor());
    assertEquals(2, persona.signal());
    assertEquals(2, persona.system());
    assertEquals(4, persona.biofeedbackFilter());

    assertEquals(MatrixAccess.VR_HOT, c.getMatrixAccess());
    assertEquals(3 * 2 + 1, c.getMatrixInitiative());
    assertEquals(3, c.getMatrixInitiativePasses());
    assertNull(human(3).getLivingPersona());
  }
}
