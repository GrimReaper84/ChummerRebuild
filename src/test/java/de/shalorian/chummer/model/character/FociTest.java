package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.magic.Foci;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Foki binden und aktivieren (GRW S. 112, 123, 233/234). */
class FociTest {

  private static Gear focus(String name, int force, String extra) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), force, 1, extra, null);
  }

  private static Gear buy(Character c, String name, int force, String extra) {
    return c.getEquipment().addGear(focus(name, force, extra));
  }

  @Test
  void bindingCostsPerType() {
    // Tabelle Fokus-Bindung S. 234: Kraft × Faktor
    Character c = withGift("Zauberer", 5);
    Map<String, Integer> karmaAtForceTwo = Map.of("Bindungsfokus", 6, "Herbeirufungsfokus", 8, "Kraftfokus", 16,
        "Spruchabwehrfokus", 6, "Verbannungsfokus", 6, "Waffenfokus", 6, "Zauberspeicher", 4, "Zauberspruchfokus", 8);
    karmaAtForceTwo.forEach((name, karma) -> {
      String extra = switch (name) {
        case "Kraftfokus" -> "";
        case "Waffenfokus" -> "Klingenwaffen";
        case "Bindungsfokus", "Herbeirufungsfokus", "Verbannungsfokus" -> "Feuergeist";
        default -> "Kampf";
      };
      assertEquals(karma, c.getFoci().getBindingKarma(focus(name, 2, extra)), name);
    });
  }

  @Test
  void creationCostsOneBpPerForce() {
    // S. 112/123: 1 GP je Kraftstufe
    Character c = withGift("Zauberer", 4);
    Gear power = buy(c, "Kraftfokus", 3, "");
    int before = c.getSpentBP();
    c.getFoci().bind(power);
    assertEquals(3, c.getSpentBP() - before);
    c.setCreated(true);
    assertEquals(0, c.getFoci().getBpCost());
  }

  @Test
  void unbindingDuringCreationFreesTheBp() {
    // BP rechnen sich live aus den gebundenen Foki: Lösen bei der Erschaffung gibt sie wieder frei
    Character c = withGift("Zauberer", 4);
    Gear power = buy(c, "Kraftfokus", 3, "");
    int before = c.getSpentBP();
    c.getFoci().bind(power);
    c.getFoci().unbind(power);
    assertEquals(before, c.getSpentBP());
  }

  @Test
  void limitsCountAndTotalForce() {
    // S. 233: höchstens Magie Foki, zusammen höchstens 5 × Magie Kraft
    Character c = withGift("Zauberer", 2);
    Foci foci = c.getFoci();
    foci.bind(buy(c, "Zauberspruchfokus", 6, "Kampf"));
    Gear tooStrong = buy(c, "Spruchabwehrfokus", 5, "Kampf");
    assertEquals("Gesamtkraft gebundener Foki höchstens 10 (5 × Magie)", foci.getBindProblem(tooStrong));
    foci.bind(buy(c, "Spruchabwehrfokus", 4, "Illusion"));
    assertEquals("Höchstens 2 gebundene Foki (Magie)", foci.getBindProblem(buy(c, "Kraftfokus", 1, "")));
    assertThrows(IllegalArgumentException.class, () -> foci.bind(tooStrong));
  }

  @Test
  void onlyAwakenedBind() {
    Character mundane = new Character("Test", MetatypeDataLoader.find("Mensch"));
    assertEquals("Nur Erwachte binden Foki", mundane.getFoci().getBindProblem(buy(mundane, "Kraftfokus", 1, "")));
  }

  @Test
  void activeAtMostLogic() {
    // S. 234: höchstens Logik aktive Foki
    Character c = withGift("Zauberer", 6);
    c.getAttribute(AttributeType.LOG).setValue(2);
    Foci foci = c.getFoci();
    Gear a = buy(c, "Kraftfokus", 1, "");
    Gear b = buy(c, "Zauberspruchfokus", 1, "Kampf");
    Gear d = buy(c, "Waffenfokus", 1, "Klingenwaffen");
    assertEquals("Erst binden", foci.getActivateProblem(a));
    foci.bind(a);
    foci.bind(b);
    foci.bind(d);
    foci.setActive(a, true);
    foci.setActive(b, true);
    assertEquals("Höchstens 2 aktive Foki (Logik)", foci.getActivateProblem(d));
    foci.setActive(a, false);
    assertNull(foci.getActivateProblem(d));
  }

  @Test
  void unbindingAlsoDeactivatesAndLaterLimitsShowProblems() {
    Character c = withGift("Zauberer", 2);
    Foci foci = c.getFoci();
    Gear power = buy(c, "Kraftfokus", 6, "");
    foci.bind(power);
    foci.setActive(power, true);
    assertTrue(foci.getProblems().isEmpty());
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(1);
    assertEquals(List.of("Gesamtkraft gebundener Foki 6, erlaubt 5 (5 × Magie)"), foci.getProblems());
    foci.unbind(power);
    assertFalse(power.isFocusActive());
  }
}
