package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tabellen S. 391 (am Seitenbild geprüft) und die eckigen Gliedkapazitäten der Head-/Bodyware (S. 386, 389). */
class CyberlimbDataLoaderGrwTableTest {

  private static CyberwareData data(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  private static void assertLimb(String name, LimbPart part, String essence, int capacity, int avail, int price) {
    CyberwareData d = data(name);
    assertEquals(part, d.limb().part(), name);
    assertEquals(0, new BigDecimal(essence).compareTo(RatingFormula.evaluate(d.essence(), 0)), name);
    assertEquals(capacity, d.capacityFor(0), name);
    assertEquals(avail, d.availability().evaluate(0), name);
    assertEquals(price, RatingFormula.evaluate(d.cost(), 0).intValueExact(), name);
    assertTrue(d.restrictions().multiple(), name);
  }

  @Test
  void obviousLimbs() {
    assertLimb("Ganzer Arm (offensichtlich)", LimbPart.FULL_ARM, "1", 15, 4, 15000);
    assertLimb("Ganzes Bein (offensichtlich)", LimbPart.FULL_LEG, "1", 20, 4, 15000);
    assertLimb("Hand/Fuß (offensichtlich)", LimbPart.HAND_FOOT, "0.25", 4, 2, 5000);
    assertLimb("Schädel (offensichtlich)", LimbPart.SKULL, "0.75", 4, 16, 10000);
    assertLimb("Torso (offensichtlich)", LimbPart.TORSO, "1.5", 10, 12, 20000);
    assertLimb("Unterarm (offensichtlich)", LimbPart.LOWER_ARM, "0.45", 10, 4, 10000);
    assertLimb("Unterschenkel (offensichtlich)", LimbPart.LOWER_LEG, "0.45", 12, 4, 10000);
  }

  @Test
  void syntheticLimbs() {
    assertLimb("Ganzer Arm (synthetisch)", LimbPart.FULL_ARM, "1", 8, 4, 20000);
    assertLimb("Ganzes Bein (synthetisch)", LimbPart.FULL_LEG, "1", 10, 4, 20000);
    assertLimb("Hand/Fuß (synthetisch)", LimbPart.HAND_FOOT, "0.25", 2, 2, 6000);
    assertLimb("Schädel (synthetisch)", LimbPart.SKULL, "0.75", 2, 16, 15000);
    assertLimb("Torso (synthetisch)", LimbPart.TORSO, "1.5", 5, 12, 25000);
    assertLimb("Unterarm (synthetisch)", LimbPart.LOWER_ARM, "0.45", 5, 4, 12000);
    assertLimb("Unterschenkel (synthetisch)", LimbPart.LOWER_LEG, "0.45", 6, 4, 12000);
  }

  @Test
  void enhancements() {
    CyberwareData body = data("Konstitutionsverbesserung");
    assertEquals(LimbEnhancement.BODY, body.limb().enhancement());
    assertEquals(7, body.maxRating());
    assertEquals(5, body.limbCapacityCostFor(5));
    assertEquals("15E", body.availability().displayText(5));
    assertEquals(1000, RatingFormula.evaluate(body.cost(), 5).intValueExact());
    assertEquals(1250, RatingFormula.evaluate(data("Geschicklichkeitsverbesserung").cost(), 5).intValueExact());
    assertEquals(1250, RatingFormula.evaluate(data("Stärkeverbesserung").cost(), 5).intValueExact());
    CyberwareData armor = data("Panzerungsverbesserung");
    assertEquals(LimbEnhancement.ARMOR, armor.limb().enhancement());
    assertEquals(4, armor.maxRating());
    assertEquals(6, armor.limbCapacityCostFor(3));
    assertEquals("15", armor.availability().displayText(3));
    assertEquals(900, RatingFormula.evaluate(armor.cost(), 3).intValueExact());
    assertTrue(armor.restrictions().installOnly());
  }

  @Test
  void accessories() {
    Map<String, String> table = Map.of(
        "Cyberarm-Gyrostabilisator", "4|12V|6000",
        "Cyber-Holster", "7|8E|2000",
        "Cyber-Springarm", "8|12E|3000",
        "Großer Schmuggelbehälter", "5|6|2000");
    table.forEach((name, row) -> {
      CyberwareData d = data(name);
      assertEquals(row, d.limbCapacityCostFor(0) + "|" + d.availability().displayText(0) + "|"
          + RatingFormula.evaluate(d.cost(), 0).intValueExact(), name);
    });
    assertEquals(Set.of(LimbPart.FULL_ARM, LimbPart.LOWER_ARM), data("Cyberarm-Gyrostabilisator").limb().fitsIn());
    CyberwareData jacks = data("Sprunghydraulik");
    assertTrue(jacks.limb().pairedLegs());
    assertEquals(4, jacks.limbCapacityCostFor(4));
    assertEquals(8000, RatingFormula.evaluate(jacks.cost(), 4).intValueExact());
    assertEquals("9", jacks.availability().displayText(4));
  }

  @Test
  void cyberMeleeWeapons() {
    Map<String, String> table = Map.of(
        "Handklinge (einziehbar)", "0.25|3|10V|1500|Handklinge",
        "Nagelmesser (einziehbar)", "0.2|2|10V|900|Nagelmesser",
        "Schockhand", "0.25|3|8E|1000|Schockhand",
        "Sporn (einziehbar)", "0.3|3|12V|1800|Sporn");
    table.forEach((name, row) -> {
      CyberwareData d = data(name);
      assertEquals(row, d.essence() + "|" + d.limbCapacityCostFor(0) + "|" + d.availability().displayText(0) + "|"
          + RatingFormula.evaluate(d.cost(), 0).intValueExact() + "|" + d.weapon(), name);
      assertFalse(d.restrictions().installOnly(), name);
    });
  }

  @Test
  void headAndBodywareWithLimbCapacity() {
    Map<String, Integer> table = Map.ofEntries(
        Map.entry("Cortexbombe (Spleenbombe)", 1), Map.entry("Cortexbombe (Mikrobombe)", 2),
        Map.entry("Cortexbombe (Flächenbombe)", 3), Map.entry("Datenbuchse", 1), Map.entry("Datenschloss", 1),
        Map.entry("Geruchsbooster", 2), Map.entry("Kommlink", 2), Map.entry("Sim-Modul", 2),
        Map.entry("Sim-Modul (modifiziert für heißes Sim)", 2), Map.entry("Ultraschallsensor", 2),
        Map.entry("Enterhakenkanone", 5), Map.entry("Fingerkuppenbehälter", 1), Map.entry("Interner Lufttank", 3),
        Map.entry("Schmuggelbehälter", 2));
    table.forEach((name, capacity) -> assertEquals(capacity, data(name).limbCapacityCostFor(1), name));
    assertFalse(data("Riggerkontrolle").limb().isInstallableInLimb());
    assertFalse(data("Kunstmuskeln").limb().isInstallableInLimb());
  }
}
