package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Tabelle Feuerwaffenzubehör, GRW S. 368. */
class AccessoryDataLoaderTest {

  private static AccessoryData find(String name) {
    AccessoryData data = AccessoryDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  @Test
  void loadsSixteenAccessoriesWithoutGearItems() {
    assertEquals(16, AccessoryDataLoader.getAllAccessories().size());
    for (String gear : List.of("Schnellziehholster", "Tarnholster", "Versteckte Armhalterung",
        "Smart-Feuerplattform")) {
      assertNull(AccessoryDataLoader.findByName(gear), gear);
    }
  }

  @Test
  void gasVentMatchesRulebook() {
    AccessoryData gasVent = find("Gasventilsystem 3");

    assertEquals(List.of(Mount.BARREL), gasVent.mounts());
    assertEquals(3, gasVent.rc());
    assertEquals("6E", gasVent.availability().displayText(0));
    assertEquals(400, gasVent.costFor(350));
    assertTrue(gasVent.isPermanent());
  }

  @Test
  void samplesFromTable() {
    assertEquals(List.of(Mount.TOP, Mount.UNDERBARREL), find("Lasermarkierer").mounts());
    assertEquals("Aufmontiert/Unterlauf", find("Smartgunsystem, extern").mountsText());
    assertEquals("12V", find("Schallunterdrücker").availability().displayText(0));
    assertEquals("8V", find("Schalldämpfer").availability().displayText(0));
    assertEquals(3000, find("Gyrostabilisator").costFor(0));
    assertEquals(0, find("Gyrostabilisator").rc());
    assertEquals(6, find("Dreibein").rc());
    assertEquals(2, find("Zweibein").rc());
    assertEquals(1, find("Schockpolster").rc());
    assertFalse(find("Schockpolster").needsMount());
    assertEquals(5, find("Ersatzladestreifen").costFor(0));
  }

  @Test
  void internalSmartgunCostsTheWeaponPrice() {
    AccessoryData internal = find("Smartgunsystem, intern");

    assertEquals(650, internal.costFor(650));
    assertEquals("Waffenpreis", internal.costText());
    assertEquals("6E", internal.availability().displayText(0));
  }

  @Test
  void onlyLaserSightAndExternalSmartgunFitProjectileWeapons() {
    Set<String> projectile = AccessoryDataLoader.getAllAccessories().values().stream()
        .filter(AccessoryData::projectile)
        .map(AccessoryData::name)
        .collect(Collectors.toSet());

    assertEquals(Set.of("Lasermarkierer", "Smartgunsystem, extern"), projectile);
  }
}
