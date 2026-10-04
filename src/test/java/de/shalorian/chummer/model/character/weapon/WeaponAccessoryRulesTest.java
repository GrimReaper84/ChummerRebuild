package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Montageregeln für Zubehör, GRW S. 357 (Halterungen) und S. 367/368 (Zubehörtexte). */
class WeaponAccessoryRulesTest {

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name)));
  }

  private static AccessoryData accessory(String name) {
    return Objects.requireNonNull(AccessoryDataLoader.findByName(name));
  }

  private static void mount(Weapon weapon, String accessory, Mount mount) {
    weapon.addAccessory(new WeaponAccessory(accessory(accessory), mount));
  }

  @Test
  void holdoutHasNoMount() {
    Weapon holdout = weapon("Streetline Special");

    assertNotNull(holdout.mountProblem(accessory("Lasermarkierer")));
    assertTrue(holdout.freeMounts(accessory("Zielfernrohr")).isEmpty());
  }

  @Test
  void pistolHasBarrelAndTopMounts() {
    Weapon pistol = weapon("Colt America L36");

    assertEquals(List.of(Mount.TOP), pistol.freeMounts(accessory("Lasermarkierer")));
    assertNull(pistol.mountProblem(accessory("Schalldämpfer")));
    assertNotNull(pistol.mountProblem(accessory("Zweibein")));
  }

  @Test
  void submachineGunRifleAndHeavyWeaponHaveAllThreeMounts() {
    for (String name : List.of("Uzi IV", "AK-97", "Ruger 100", "Stoner-Ares M202")) {
      assertEquals(List.of(Mount.TOP, Mount.UNDERBARREL),
          weapon(name).freeMounts(accessory("Lasermarkierer")), name);
      assertEquals(List.of(Mount.BARREL), weapon(name).freeMounts(accessory("Schalldämpfer")), name);
    }
  }

  @Test
  void taserOnlyHasTopMount() {
    Weapon taser = weapon("Yamaha Pulsar");

    assertEquals(List.of(Mount.TOP), taser.freeMounts(accessory("Lasermarkierer")));
    assertNotNull(taser.mountProblem(accessory("Schalldämpfer")));
  }

  @Test
  void onlyOneAccessoryPerMount() {
    Weapon rifle = weapon("AK-97");
    mount(rifle, "Zielfernrohr", Mount.TOP);

    assertEquals(List.of(Mount.UNDERBARREL), rifle.freeMounts(accessory("Lasermarkierer")));
    assertNotNull(rifle.mountProblem(accessory("Periskop")));
    assertThrows(IllegalArgumentException.class, () -> mount(rifle, "Lasermarkierer", Mount.TOP));
  }

  @Test
  void accessoryWithoutMountCanBeAddedRepeatedly() {
    Weapon pistol = weapon("Ares Predator IV");
    pistol.addAccessory(new WeaponAccessory(accessory("Ersatzladestreifen"), null));
    pistol.addAccessory(new WeaponAccessory(accessory("Ersatzladestreifen"), null));

    assertEquals(2, pistol.getAccessories().size());
  }

  @Test
  void mountMustMatchAccessory() {
    Weapon rifle = weapon("AK-97");

    assertThrows(IllegalArgumentException.class, () -> mount(rifle, "Zielfernrohr", null));
    assertThrows(IllegalArgumentException.class,
        () -> rifle.addAccessory(new WeaponAccessory(accessory("Schockpolster"), Mount.TOP)));
  }

  @Test
  void projectileWeaponsOnlyTakeLaserSightOrSmartgun() {
    for (String name : List.of("Bogen", "Leichte Armbrust")) {
      WeaponData data = Objects.requireNonNull(WeaponDataLoader.findByName(name));
      Weapon weapon = new Weapon(data, data.lowestRating());
      assertNull(weapon.mountProblem(accessory("Lasermarkierer")), name);
      assertNull(weapon.mountProblem(accessory("Smartgunsystem, extern")), name);
      assertNotNull(weapon.mountProblem(accessory("Zielfernrohr")), name);
      assertNotNull(weapon.mountProblem(accessory("Smartgunsystem, intern")), name);
    }
  }

  @Test
  void meleeAndThrowingWeaponsTakeNoAccessories() {
    assertNotNull(weapon("Katana").mountProblem(accessory("Smartgunsystem, intern")));
    assertNotNull(weapon("Shuriken").mountProblem(accessory("Lasermarkierer")));
  }

  @Test
  void gasVentOnlyForPermittedCategoriesAndNotRemovable() {
    // GRW S. 367: Automatikpistolen, Maschinenpistolen, Sturmgewehre, Maschinengewehre
    assertNull(weapon("Steyr TMP").mountProblem(accessory("Gasventilsystem 2")));
    assertNull(weapon("Uzi IV").mountProblem(accessory("Gasventilsystem 2")));
    assertNull(weapon("Stoner-Ares M202").mountProblem(accessory("Gasventilsystem 3")));
    assertNotNull(weapon("Colt America L36").mountProblem(accessory("Gasventilsystem 2")));
    assertNotNull(weapon("Ruger 100").mountProblem(accessory("Gasventilsystem 2")));

    Weapon uzi = weapon("Uzi IV");
    mount(uzi, "Gasventilsystem 2", Mount.BARREL);
    WeaponAccessory gasVent = uzi.getAccessories().get(0);
    assertThrows(IllegalStateException.class, () -> uzi.removeAccessory(gasVent));
  }

  @Test
  void noSecondGasVentOrSmartgunWhenOneIsIncluded() {
    // GRW S. 367: "Waffen, die bereits ein eingebautes Gasventilsystem besitzen, können nicht mit
    // einem weiteren ausgerüstet werden."
    assertNotNull(weapon("Ingram White Knight").mountProblem(accessory("Gasventilsystem 3")));
    assertNotNull(weapon("Ares Predator IV").mountProblem(accessory("Smartgunsystem, extern")));
    assertNotNull(weapon("Ares Predator IV").mountProblem(accessory("Smartgunsystem, intern")));
    assertNotNull(weapon("Ruger 100").mountProblem(accessory("Schockpolster")));
  }

  @Test
  void noSecondSmartgunOrShockPadWhenOneIsMounted() {
    Weapon rifle = weapon("AK-97");
    mount(rifle, "Smartgunsystem, extern", Mount.TOP);
    rifle.addAccessory(new WeaponAccessory(accessory("Schockpolster"), null));

    assertNotNull(rifle.mountProblem(accessory("Smartgunsystem, intern")));
    assertNotNull(rifle.mountProblem(accessory("Smartgunsystem, extern")));
    assertNotNull(rifle.mountProblem(accessory("Schockpolster")));
  }

  @Test
  void suppressorOnlyForSingleShotOrSemiAutoAndNotRevolvers() {
    assertNull(weapon("Colt America L36").mountProblem(accessory("Schalldämpfer")));
    assertNotNull(weapon("Ruger Super Warhawk").mountProblem(accessory("Schalldämpfer")));
    assertNotNull(weapon("Uzi IV").mountProblem(accessory("Schalldämpfer")));
    assertNull(weapon("Uzi IV").mountProblem(accessory("Schallunterdrücker")));
    assertNotNull(weapon("Colt America L36").mountProblem(accessory("Schallunterdrücker")));
  }

  @Test
  void furtherTextRestrictions() {
    assertNull(weapon("Ruger Super Warhawk").mountProblem(accessory("Schnelllader")));
    assertNotNull(weapon("Colt America L36").mountProblem(accessory("Schnelllader")));
    assertNotNull(weapon("Remington 990").mountProblem(accessory("Ersatzladestreifen")));
    assertNull(weapon("Ares Antioch-2").mountProblem(accessory("Airburst-Verbindung")));
    assertNotNull(weapon("AK-97").mountProblem(accessory("Airburst-Verbindung")));
    assertNull(weapon("Mossberg AM-CMDT").mountProblem(accessory("Schockpolster")));
    assertNotNull(weapon("Uzi IV").mountProblem(accessory("Schockpolster")));
    assertNull(weapon("AK-97").mountProblem(accessory("Gyrostabilisator")));
    assertNotNull(weapon("Mossberg AM-CMDT").mountProblem(accessory("Gyrostabilisator")));
  }

  @Test
  void recoilAddsAccessories() {
    Weapon uzi = weapon("Uzi IV"); // "(1)"
    mount(uzi, "Gasventilsystem 2", Mount.BARREL);
    assertEquals("2 (3)", uzi.getRecoil().displayText());

    mount(uzi, "Zweibein", Mount.UNDERBARREL);
    assertEquals("2 (5)", uzi.getRecoil().displayText());
  }

  @Test
  void includedAccessoriesAreNotAddedAgain() {
    assertEquals("3 (10)", weapon("Ultimax HMG-2").getRecoil().displayText());
  }

  @Test
  void internalSmartgunCostsTheWeaponPrice() {
    Weapon rifle = weapon("AK-97"); // 500 ¥
    rifle.addAccessory(new WeaponAccessory(accessory("Smartgunsystem, intern"), null));
    mount(rifle, "Zielfernrohr", Mount.TOP);

    assertEquals(500, rifle.getAccessoryNuyenCost(accessory("Smartgunsystem, intern")));
    assertEquals(500 + 500 + 300, rifle.getTotalNuyenCost());
    assertEquals(500, rifle.getNuyenCost());
  }
}
