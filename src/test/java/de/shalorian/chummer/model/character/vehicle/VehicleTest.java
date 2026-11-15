package de.shalorian.chummer.model.character.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class VehicleTest {

  private static Vehicle vehicle(String name) {
    return new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName(name), name));
  }

  /** Synthetisches Fahrzeug mit beliebigem Rumpf, ohne mitgelieferte Teile. */
  private static Vehicle withBody(int body) {
    return new Vehicle(new VehicleData("Testwagen", VehicleCategory.CARS, "Bodenfahrzeuge", 0, 10, 20, 100, 1,
        body,
        6, 2, Availability.NONE, 10_000, 0, "", List.of(), "", "SR4", "397"));
  }

  private static WeaponData weapon(String name) {
    return Objects.requireNonNull(WeaponDataLoader.findByName(name), name);
  }

  private static Gear gear(String name, int rating) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), rating);
  }

  /** GRW-Beispiel S. 197: Rumpf 3 ergibt 10 Kästchen. */
  @Test
  void damageIsClampedAndFullMonitorMeansDestroyed() {
    Vehicle vehicle = withBody(3);
    vehicle.setDamage(-2);
    assertEquals(0, vehicle.getDamage());
    vehicle.setDamage(9);
    assertFalse(vehicle.isDestroyed());
    vehicle.setDamage(15);
    assertEquals(10, vehicle.getDamage());
    assertTrue(vehicle.isDestroyed());
  }

  @Test
  void conditionMonitorMatchesGrwExample() {
    assertEquals(10, withBody(3).getConditionMonitor());
    assertEquals(8, withBody(0).getConditionMonitor());
    assertEquals(10, withBody(4).getConditionMonitor());
    assertEquals(19, withBody(22).getConditionMonitor());
  }

  /** GRW S. 395: so viele Halterungen wie Rumpf ÷ 3 (abgerundet). */
  @Test
  void maxMountsIsBodyDividedByThree() {
    assertEquals(0, withBody(2).getMaxMounts());
    assertEquals(1, withBody(3).getMaxMounts());
    assertEquals(1, withBody(4).getMaxMounts());
    assertEquals(3, withBody(10).getMaxMounts());
    assertEquals(5, withBody(16).getMaxMounts());
  }

  @Test
  void includedMountsCountAgainstLimit() {
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    assertEquals(1, doberman.getMounts().size());
    assertTrue(doberman.getMounts().getFirst().isIncluded());
    assertNotNull(doberman.mountProblem());
    assertThrows(IllegalStateException.class, doberman::addMount);

    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    for (int i = 0; i < 3; i++) {
      assertNull(comet.mountProblem());
      assertFalse(comet.addMount().isIncluded());
    }
    assertNotNull(comet.mountProblem());
  }

  @Test
  void includedMountsCannotBeRemoved() {
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    WeaponMount included = doberman.getMounts().getFirst();
    assertThrows(IllegalArgumentException.class, () -> doberman.removeMount(included));

    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    WeaponMount bought = comet.addMount();
    comet.mountWeapon(bought, new Weapon(weapon("Ingram White Knight")));
    comet.removeMount(bought);
    assertTrue(comet.getMounts().isEmpty());
    assertTrue(comet.getMountedWeapons().isEmpty());
  }

  /** GRW S. 395: Waffen bis LMG; Granatwerfer laut Nutzerentscheidung 2026-10-09. */
  @Test
  void mountAcceptsUpToLmgAndGrenadeLaunchers() {
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    WeaponMount mount = comet.addMount();
    assertNull(comet.weaponProblem(mount, weapon("Ingram White Knight")));
    assertNull(comet.weaponProblem(mount, weapon("Ares Antioch-2")));
    assertNull(comet.weaponProblem(mount, weapon("AK-97")));
    assertNotNull(comet.weaponProblem(mount, weapon("Stoner-Ares M202")));
    assertNotNull(comet.weaponProblem(mount, weapon("Panther XXL")));
    assertNotNull(comet.weaponProblem(mount, weapon("Aztechnology Striker")));
    assertNotNull(comet.weaponProblem(mount, weapon("Katana")));
    assertNotNull(comet.weaponProblem(mount, weapon("Bogen")));

    comet.mountWeapon(mount, new Weapon(weapon("Ingram White Knight")));
    assertNotNull(comet.weaponProblem(mount, weapon("AK-97")));
    assertThrows(IllegalArgumentException.class,
        () -> comet.mountWeapon(mount, new Weapon(weapon("AK-97"))));
    comet.unmountWeapon(mount);
    assertNull(mount.getWeapon());
  }

  /** GRW S. 398: Doberman mit Halterung, Clearsight 3 und Zielerfassung 3 für 3.000 ¥. */
  @Test
  void includedPartsAreFree() {
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    assertEquals(3, doberman.getIncludedGear().size());
    assertEquals(3_000, doberman.getNuyenCost());
    assertEquals(4_000, vehicle("CrashCart AutoDoc (Groß)").getNuyenCost());
  }

  @Test
  void costSumsPurchasedParts() {
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    WeaponMount mount = comet.addMount();
    Weapon lmg = new Weapon(weapon("Ingram White Knight"));
    comet.mountWeapon(mount, lmg);
    comet.installRiggerAdapter();
    comet.addGear(gear("Medkit", 2));
    Gear sensor = gear("Bewegungsmelder", 0);
    Objects.requireNonNull(comet.getSensorPackage()).install(sensor);

    assertEquals(14_000 + 2_500 + lmg.getTotalNuyenCost() + 2_500 + 200 + 50,
        comet.getNuyenCost());

    comet.removeGear(sensor);
    assertEquals(14_000 + 2_500 + lmg.getTotalNuyenCost() + 2_500 + 200, comet.getNuyenCost());
  }

  @Test
  void includedGearCannotBeRemoved() {
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    Gear autosoft = doberman.getIncludedGear().get(1);
    assertTrue(doberman.isIncluded(autosoft));
    assertThrows(IllegalArgumentException.class, () -> doberman.removeGear(autosoft));
  }

  @Test
  void purchasedGearIsMerged() {
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    Gear first = comet.addGear(gear("Medkit", 2));
    assertSame(first, comet.addGear(gear("Medkit", 2)));
    assertEquals(1, comet.getGear().size());
    assertEquals(2, first.getQuantity());
  }

  /** GRW S. 398: Drohnen haben immer einen Riggeradapter. */
  @Test
  void dronesHaveRiggerAdapter() {
    Vehicle drone = vehicle("Lockheed Optic-X (Klein)");
    assertTrue(drone.hasRiggerAdapter());
    assertTrue(drone.isRiggerAdapterIncluded());
    assertNotNull(drone.riggerAdapterProblem());

    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    assertFalse(comet.hasRiggerAdapter());
    assertNull(comet.riggerAdapterProblem());
    comet.installRiggerAdapter();
    assertTrue(comet.hasRiggerAdapter());
    assertNotNull(comet.riggerAdapterProblem());
    assertThrows(IllegalStateException.class, comet::installRiggerAdapter);
    comet.removeRiggerAdapter();
    assertFalse(comet.hasRiggerAdapter());
  }

  /** GRW S. 380: Durchschnitt der Sensoren aufgerundet, stufenlose Sensoren mit der Paketstufe. */
  @Test
  void averageSensorRating() {
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)"); // Sensor 3
    Gear pack = Objects.requireNonNull(doberman.getSensorPackage());
    assertEquals(OptionalInt.empty(), doberman.getAverageSensorRating());

    pack.install(gear("Atmosphärensensor", 2));
    pack.install(gear("Geruchsscanner", 5));
    assertEquals(OptionalInt.of(4), doberman.getAverageSensorRating()); // ⌈3,5⌉

    pack.install(gear("Bewegungsmelder", 0)); // zählt mit Stufe 3 → (2 + 5 + 3) / 3 = 3,33…
    assertEquals(OptionalInt.of(4), doberman.getAverageSensorRating());
  }

  @Test
  void deviceRatingByCategory() {
    assertEquals(3, vehicle("Mercury Comet (Mittelklassewagen)").getDeviceRating());
    assertEquals(4, vehicle("Chrysler-Nissan Patrol-1 (Streifenwagen)").getDeviceRating());
  }
}
