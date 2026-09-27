package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Kategorien laut Tabellenüberschriften GRW S. 361/363/366. */
class WeaponCategoryTest {

  @Test
  void hasTwentyFourRulebookCategories() {
    assertEquals(24, WeaponCategory.values().length);
  }

  @Test
  void onlyTheFourMeleeTablesAreMelee() {
    Set<WeaponCategory> melee = Arrays.stream(WeaponCategory.values())
        .filter(WeaponCategory::isMelee)
        .collect(Collectors.toSet());

    assertEquals(Set.of(WeaponCategory.BLADES, WeaponCategory.CLUBS, WeaponCategory.EXOTIC_MELEE,
        WeaponCategory.UNARMED), melee);
  }

  @Test
  void fromDisplayNameReadsCatalogValues() {
    assertEquals(WeaponCategory.HOLDOUTS, WeaponCategory.fromDisplayName("Holdout-Pistolen"));
    assertEquals(WeaponCategory.MACHINE_PISTOLS,
        WeaponCategory.fromDisplayName("Automatikpistolen"));
    assertThrows(IllegalArgumentException.class,
        () -> WeaponCategory.fromDisplayName("Laserwaffen"));
  }
}
