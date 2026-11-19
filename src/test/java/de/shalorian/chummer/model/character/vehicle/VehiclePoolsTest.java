package de.shalorian.chummer.model.character.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.DicePool;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Rigger- und Drohnenpools nach der Tabelle S. 287 und die Fahrzeugprobe S. 198. */
class VehiclePoolsTest {

  private static Vehicle vehicle(String name) {
    return new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName(name), name));
  }

  private static Character rigger() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.REA).setValue(3);
    Objects.requireNonNull(c.getSkill("Geschütze")).setRating(4);
    Objects.requireNonNull(c.getSkill("Bodenfahrzeuge")).setRating(3);
    return c;
  }

  private static VehiclePoolRow row(Character c, Vehicle v, String test) {
    List<VehiclePoolRow> rows = VehiclePools.of(c, v);
    return rows.stream().filter(r -> r.test().equals(test)).findFirst().orElseThrow();
  }

  private static int dice(VehiclePoolCell cell) {
    DicePool pool = cell.pool();
    assertNotNull(pool, cell.note());
    return pool.dice();
  }

  private static Gear gear(String name, int rating) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), rating);
  }

  @Test
  void autonomousDroneUsesPilotAndAutosofts() {
    // Steel Lynx: Pilot 3, Sensor 3, Abwehr 3, Zielerfassung 3 (S. 397/398)
    Character c = rigger();
    Vehicle lynx = vehicle("Steel Lynx (Groß)");
    assertEquals(6, dice(row(c, lynx, "Angriff").autonomous()));
    assertEquals(6, dice(row(c, lynx, "Nahkampfverteidigung").autonomous()));
    assertEquals(3, dice(row(c, lynx, "Wahrnehmung").autonomous()));
    assertEquals(3, dice(row(c, lynx, "Manövrieren").autonomous()));
    assertEquals(6, dice(row(c, lynx, "Initiative").autonomous()));
  }

  @Test
  void attackNeedsTargetingAutosoft() {
    // S. 202: ohne passende Zielerfassung kein autonomer Angriff
    Vehicle crawler = vehicle("Aztechnology Crawler (Klein)");
    VehiclePoolCell attack = row(rigger(), crawler, "Angriff").autonomous();
    assertNull(attack.pool());
    assertEquals("keine Zielerfassung-Autosoft (GRW S. 202)", attack.note());
  }

  @Test
  void jumpedInUsesRiggerSkillsWithVehicleAttributes() {
    // S. 286/287: Prozessor statt GES/REA, Sensor statt INT; Gerätestufe 3 der Drohne als Prozessor
    Character c = rigger();
    Vehicle lynx = vehicle("Steel Lynx (Groß)");
    assertEquals(7, dice(row(c, lynx, "Angriff").jumpedIn()));
    assertEquals(6, dice(row(c, lynx, "Manövrieren").jumpedIn()));
    assertEquals(3, dice(row(c, lynx, "Fernkampfverteidigung").jumpedIn()));
    // S. 385: Riggerkontrolle +2 auf Proben mit Fahrzeugfertigkeit
    c.getEquipment().addCyberware(new Cyberware(
        Objects.requireNonNull(CyberwareDataLoader.findByName("Riggerkontrolle")), CyberwareGrade.STANDARD, 0));
    assertEquals(9, dice(row(c, lynx, "Angriff").jumpedIn()));
    assertEquals(8, dice(row(c, lynx, "Manövrieren").jumpedIn()));
    assertEquals(3, dice(row(c, lynx, "Fernkampfverteidigung").jumpedIn()));
  }

  @Test
  void carsNeedARiggerAdapterAndDoNotActAutonomously() {
    Character c = rigger();
    Vehicle car = vehicle("Honda Spirit (Kleinwagen)");
    VehiclePoolRow attack = row(c, car, "Angriff");
    assertEquals("kein Riggeradapter (GRW S. 396)", attack.jumpedIn().note());
    assertEquals("nur Drohnen handeln autonom", attack.autonomous().note());
    assertNull(row(c, car, "Initiative").jumpedIn().pool());
    assertNull(row(c, car, "Schadenswiderstand").autonomous().pool());
  }

  @Test
  void incompetenceBlocksTheRiggerSkillToo() {
    // gleiche Sperren wie bei Fertigkeitsproben (S. 120)
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Vehicle lynx = vehicle("Steel Lynx (Groß)");
    assertEquals(2, dice(row(c, lynx, "Angriff").jumpedIn())); // Sensor 3 − 1 ohne Fertigkeitswert
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Inkompetenz")), false, "Geschütze");
    assertNull(row(c, lynx, "Angriff").jumpedIn().pool());
  }

  @Test
  void missingAutosoftIsNamedInTheBreakdown() {
    VehiclePoolCell maneuver = row(rigger(), vehicle("Steel Lynx (Groß)"), "Manövrieren").autonomous();
    assertEquals("Pilot 3 (ohne Autosoft Steuerung)", Objects.requireNonNull(maneuver.pool()).breakdown());
  }

  @Test
  void remoteControlUsesRunningCommandProgram() {
    // Befehl 6 auf Hermes Ikon mit Novatech Navi (System 4) läuft mit Stufe 4 (S. 271)
    Character c = rigger();
    Vehicle lynx = vehicle("Steel Lynx (Groß)");
    assertNull(row(c, lynx, "Angriff").remote().pool());
    Gear commlink = c.getEquipment().addGear(gear("Hermes Ikon", 0));
    commlink.install(gear("Novatech Navi", 0));
    Gear command = gear("Befehl", 6);
    commlink.install(command);
    assertNull(row(c, lynx, "Angriff").remote().pool());
    command.setRunning(true);
    assertEquals(8, dice(row(c, lynx, "Angriff").remote()));
    assertEquals(4, dice(row(c, lynx, "Fernkampfverteidigung").remote()));
  }

  @Test
  void damageResistanceIsBodyPlusArmorEverywhere() {
    VehiclePoolRow resistance = row(rigger(), vehicle("Steel Lynx (Groß)"), "Schadenswiderstand");
    assertEquals(13, dice(resistance.jumpedIn()));
    assertEquals(13, dice(resistance.autonomous()));
    assertEquals(13, dice(resistance.remote()));
  }

  @Test
  void woundsCountForTheRiggerButNotForTheDrone() {
    Character c = rigger();
    c.setCreated(true);
    c.setPhysicalDamage(3);
    Vehicle lynx = vehicle("Steel Lynx (Groß)");
    assertEquals(6, dice(row(c, lynx, "Angriff").jumpedIn()));
    assertEquals(6, dice(row(c, lynx, "Angriff").autonomous()));
  }

  @Test
  void driverRollsSkillPlusReactionPlusHandling() {
    // S. 198: Fahrzeugfertigkeit + Reaktion ± Handling
    Vehicle car = vehicle("Honda Spirit (Kleinwagen)");
    DicePool pool = VehiclePools.driverPool(rigger(), car).orElseThrow();
    assertEquals(6 + car.getData().handling(), pool.dice());
  }
}
