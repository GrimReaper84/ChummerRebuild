package de.shalorian.chummer.model.character.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Einbauten an Rüstungen gegen die am Bild gelesene Tabelle S. 373 (Zubehör und Panzerungsmodifikationen). */
class ArmorModDataLoaderGrwTableTest {

  private static ArmorModData mod(String name) {
    return Objects.requireNonNull(ArmorModDataLoader.findByName(name), name);
  }

  @Test
  void catalogHasFourAccessoriesAndSixModifications() {
    List<ArmorModData> all = List.copyOf(ArmorModDataLoader.getAllMods().values());
    assertEquals(4, all.stream().filter(m -> m.kind() == ArmorModKind.ACCESSORY).count());
    assertEquals(6, all.stream().filter(m -> m.kind() == ArmorModKind.MODIFICATION).count());
  }

  @Test
  void accessoriesBelongToTheirArmor() {
    ArmorModData helmet = mod("Helm (Ganzkörperpanzerung)");
    assertEquals("Ganzkörperpanzerung", helmet.forArmor());
    assertEquals(2, helmet.ballistic());
    assertEquals(2, helmet.impact());
    assertEquals(1000, helmet.costFor(0));

    ArmorModData seal = mod("Chemische Versiegelung");
    assertTrue(seal.availabilitySurcharge());
    assertEquals(6, seal.availability().evaluate(0));
    assertEquals(5000, seal.costFor(0));

    assertEquals(3, mod("Umweltanpassung").availability().evaluate(0));
    ArmorModData explorerHelmet = mod("Helm (Urban Explorer Overall)");
    assertEquals(0, explorerHelmet.ballistic());
    assertEquals(2, explorerHelmet.impact());
    assertEquals(50, explorerHelmet.costFor(0));
  }

  @Test
  void modificationValuesMatchTable() {
    assertEquals(1500, mod("Chemische Isolierung").costFor(6));
    assertEquals("8", mod("Chemische Isolierung").availability().displayText(6));
    assertEquals(1200, mod("Elektrische Isolierung").costFor(6));
    assertEquals(600, mod("Feuerresistenz").costFor(6));
    assertEquals(900, mod("Thermische Isolierung").costFor(6));
    assertEquals(3000, mod("Wärmedämpfung").costFor(6));
    assertEquals("10V", mod("Wärmedämpfung").availability().displayText(6));
    ArmorModData frills = mod("Schockstreifen");
    assertFalse(frills.hasRating());
    assertEquals("6E", frills.availability().displayText(0));
    assertEquals(200, frills.costFor(0));
    assertEquals("Elektrische Isolierung", frills.requires());
  }

  @Test
  void onlyThreeModificationsFitShields() {
    List<String> shieldMods = ArmorModDataLoader.getAllMods().values().stream()
        .filter(ArmorModData::shieldAllowed).map(ArmorModData::name).toList();
    assertEquals(List.of("Chemische Isolierung", "Elektrische Isolierung", "Feuerresistenz"), shieldMods);
  }

  @Test
  void armorFlagsAndShieldWeapons() {
    ArmorData shield = Objects.requireNonNull(ArmorDataLoader.findByName("Schutzschild"));
    assertTrue(shield.shield());
    assertEquals("Schutzschild", shield.weapon());
    assertTrue(Objects.requireNonNull(ArmorDataLoader.findByName("Lederjacke/-mantel")).acceptsMods());
    assertFalse(Objects.requireNonNull(ArmorDataLoader.findByName("Kleidung")).acceptsMods());
    assertFalse(Objects.requireNonNull(ArmorDataLoader.findByName("Helm")).acceptsMods());
    assertNotNull(de.shalorian.chummer.model.character.weapon.WeaponDataLoader.findByName("Schockstreifen"));
  }
}
