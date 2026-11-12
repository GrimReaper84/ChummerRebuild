package de.shalorian.chummer.model.character.cyberware;

import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.add;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.data;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.implant;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.mensch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.Character;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Kaufsperren der Implantate: Unverträglichkeiten, Mehrfachkauf, Voraussetzungen, Auswahl. */
class ImplantsBlockReasonTest {

  private static String reason(Character c, String name) {
    return c.getImplants().getBlockReason(data(name));
  }

  /** Unverträgliche Paare (S. 386–394): jeweils erstes gegen zweites. */
  private static final List<List<String>> INCOMPATIBLE = List.of(
      List.of("Reflexbooster", "Synapsenbeschleuniger"),
      List.of("Reaktionsverbesserung", "Synapsenbeschleuniger"),
      List.of("Kunstmuskeln", "Muskelverstärkung"),
      List.of("Kunstmuskeln", "Muskelstraffung"),
      List.of("Dermalpanzerung", "Orthoskin"),
      List.of("Knochenverstärkung", "Kompositknochen (Titan)"),
      List.of("Kompositknochen (Plastik)", "Kompositknochen (Aluminium)"),
      List.of("Katzenaugen", "Smartlink"),
      List.of("Katzenaugen", "Cyberaugen-Basissystem"),
      List.of("Sim-Modul", "Sim-Modul (modifiziert für heißes Sim)"),
      List.of("Cyberaugen-Basissystem", "Restlichtverstärkung"),
      List.of("Cyberohren", "Dämpfer"),
      List.of("Cyberaugen-Basissystem", "Augen-Rekorder"));

  @Test
  void incompatibleImplantsBlockEachOther() {
    for (List<String> pair : INCOMPATIBLE) {
      assertBlocks(pair.get(0), pair.get(1));
      assertBlocks(pair.get(1), pair.get(0));
    }
  }

  private static void assertBlocks(String present, String candidate) {
    Character c = mensch();
    add(c, present, data(present).hasRating() ? 1 : 0);
    assertNotNull(reason(c, candidate), present + " → " + candidate);
  }

  @Test
  void compatibleCombinationsStayAllowed() {
    Character c = mensch();
    add(c, "Reflexbooster", 1);
    assertNull(reason(c, "Reaktionsverbesserung"));
    add(c, "Muskelverstärkung", 1);
    assertNull(reason(c, "Muskelstraffung"));
    add(c, "Cyberaugen-Basissystem", 1);
    assertNull(reason(c, "Schutzgläser"));
    assertNull(reason(c, "Dämpfer"));
  }

  @Test
  void implantOnlyOnceUnlessMultiple() {
    Character c = mensch();
    add(c, "Datenbuchse", 0);
    add(c, "Schmuggelbehälter", 0);
    assertEquals("Bereits eingebaut", reason(c, "Datenbuchse"));
    assertNull(reason(c, "Schmuggelbehälter"));
  }

  @Test
  void requirementMustBePresent() {
    Character c = mensch();
    assertEquals("Nur zusammen mit Stimmenmodulator", reason(c, "Zweitmuster"));
    add(c, "Stimmenmodulator", 0);
    assertNull(reason(c, "Zweitmuster"));
  }

  @Test
  void ocularDroneOnlyAsInstallation() {
    assertEquals("Nur in ein Basissystem einbaubar", reason(mensch(), "Okular-Drohne"));
  }

  @Test
  void reflexRecorderNeedsCombatOrPhysicalChoice() {
    Character c = mensch();
    Implants implants = c.getImplants();
    CyberwareData skill = data("Reflexrekorder (Fertigkeit)");
    CyberwareData group = data("Reflexrekorder (Fertigkeitsgruppe)");

    assertNull(implants.getAddProblem(skill, "Pistolen"));
    assertEquals("Kampf- oder körperliche Fertigkeit wählen", implants.getAddProblem(skill, "Hacking"));
    assertNull(implants.getAddProblem(group, "Athletik"));
    assertEquals("Fertigkeitsgruppe wählen", implants.getAddProblem(group, "Elektronik"));

    c.getEquipment().addCyberware(new Cyberware(skill, CyberwareGrade.STANDARD, 0, "Pistolen", null));
    assertEquals("Mit dieser Auswahl bereits eingebaut", implants.getAddProblem(skill, "Pistolen"));
    assertNull(implants.getAddProblem(skill, "Gewehre"));
  }


  @Test
  void catsEyesAllowProtectiveCovers() {
    // Schutzgläser sind keine Augenmodifikation (S. 388) und bleiben neben Katzenaugen erlaubt (S. 393)
    Character c = mensch();
    add(c, "Katzenaugen", 0);
    assertNull(reason(c, "Schutzgläser"));
  }

  @Test
  void addEnforcesPurchaseRules() {
    Character c = mensch();
    c.getImplants().add(implant("Dermalpanzerung", 1));
    assertThrows(IllegalArgumentException.class, () -> c.getImplants().add(implant("Orthoskin", 1)));
    assertEquals(1, c.getEquipment().getCyberware().size());
  }
}
