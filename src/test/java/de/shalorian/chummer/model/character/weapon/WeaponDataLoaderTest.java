package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.equipment.Legality;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** GRW-Kern-Waffenkatalog (Tabellen S. 361, 363, 366). */
class WeaponDataLoaderTest {

  private static WeaponData find(String name) {
    WeaponData data = WeaponDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  private static Collection<WeaponData> all() {
    return WeaponDataLoader.getAllWeapons().values();
  }

  @Test
  void loadsSixtyThreeWeaponsInAllCategories() {
    // 63 kaufbare GRW-Waffen; dazu eingebaute aus Rüstung (S. 373) und Implantaten (S. 391), die nicht gekauft werden
    assertEquals(63, all().stream().filter(w -> !w.builtIn()).count());
    assertEquals(List.of("Schutzschild", "Taserschild", "Handklinge", "Nagelmesser", "Schockhand", "Sporn",
        "Schockstreifen"),
        all().stream().filter(WeaponData::builtIn).map(WeaponData::name).toList());
    Set<WeaponCategory> used = all().stream().map(WeaponData::category).collect(Collectors.toSet());
    assertEquals(Set.of(WeaponCategory.values()), used);
  }

  @Test
  void aresPredatorMatchesRulebook() {
    WeaponData predator = find("Ares Predator IV");

    assertEquals(WeaponCategory.HEAVY_PISTOLS, predator.category());
    assertEquals("5K", predator.damage());
    assertEquals("–1", predator.armorPiercing());
    assertEquals("HM", predator.mode());
    assertEquals("–", predator.recoil().displayText());
    assertEquals("15 (s)", predator.ammo());
    assertEquals("4E", predator.availability().displayText(0));
    assertEquals(350, predator.costFor(0));
    assertEquals("363", predator.page());
  }

  @Test
  void katanaMatchesRulebook() {
    WeaponData katana = find("Katana");

    assertEquals(WeaponCategory.BLADES, katana.category());
    assertEquals("1", katana.reach());
    assertEquals("(STR÷2+3)K", katana.damage());
    assertEquals("–1", katana.armorPiercing());
    assertEquals("4E", katana.availability().displayText(0));
    assertEquals(1000, katana.costFor(0));
    assertEquals("", katana.mode());
  }

  @Test
  void samplesFromEachTable() {
    assertEquals("12V", find("Monofilamentpeitsche").availability().displayText(0));
    assertEquals("2 (3)", find("Ingram Smartgun X").recoil().displayText());
    assertEquals("5 (6)", find("Ingram White Knight").recoil().displayText());
    assertEquals("50 (s) oder 100 (gurt)", find("Ingram White Knight").ammo());
    assertEquals("6 (tr)", find("Ruger Super Warhawk").ammo());
    assertEquals("–", find("Messer").availability().displayText(0));
    assertEquals("–", find("Defiance EX Schocker").availability().displayText(0));
    assertEquals(Legality.LEGAL, find("Ares S-III Super-Squirt").availability().legality());
    assertEquals(12000, find("Mitsubishi Yakusoku MRL").costFor(0));
  }

  @Test
  void integratedUnderbarrelIsOnlyANote() {
    assertTrue(find("Ares Alpha").note().contains("Unterlauf-Granatwerfer"));
    assertNull(WeaponDataLoader.findByName("Unterlauf-Granatwerfer"));
    assertNull(WeaponDataLoader.findByName("Pfeil"));
    assertNull(WeaponDataLoader.findByName("Bolzen"));
  }

  @Test
  void bowHasMinimumStrengthAsRating() {
    WeaponData bow = find("Bogen");

    assertTrue(bow.hasRating());
    assertEquals(8, bow.maxRating());
    assertEquals(400, bow.costFor(4));
    assertEquals("8", bow.availability().displayText(4));
    assertEquals("(STR Min + 2)K", bow.damage());
  }

  @Test
  void onlyTheBowHasARating() {
    assertEquals(Set.of("Bogen"), all().stream()
        .filter(WeaponData::hasRating)
        .map(WeaponData::name)
        .collect(Collectors.toSet()));
  }

  @Test
  void everyAvailabilityAndPriceIsEvaluable() {
    for (WeaponData data : all()) {
      for (int rating = data.lowestRating(); rating <= Math.max(0, data.maxRating()); rating++) {
        data.availability().evaluate(rating);
        // Waffen aus Rüstung kosten nichts extra, ihr Preis steckt im Rüstungsteil
        assertTrue(data.costFor(rating) > 0 || data.builtIn(), data.name());
      }
    }
  }

  @Test
  void rangedWeaponsHaveNoMeleeReach() {
    for (WeaponData data : all()) {
      assertEquals(data.category().isMelee(), !data.reach().isEmpty(), data.name());
    }
    assertFalse(find("Shuriken").category().isMelee());
  }

  @Test
  void exactlyTheRangedWeaponsHaveARangeRow() {
    for (WeaponData data : all()) {
      assertEquals(!data.category().isMelee(), data.range() != null, data.name());
    }
  }

  @Test
  void rangeRowsFollowTheCategory() {
    assertEquals(WeaponRange.HEAVY_PISTOL, find("Ares Predator IV").range());
    assertEquals(WeaponRange.SHOTGUN_SLUG, find("Remington 990").range());
    assertEquals(WeaponRange.MEDIUM_HEAVY_MG, find("Ultimax HMG-2").range());
    assertEquals(WeaponRange.BOW, find("Bogen").range());
    assertEquals(WeaponRange.THROWING_KNIFE, find("Wurfmesser").range());
  }

  @Test
  void weaponTextsOverrideTheCategoryRange() {
    // GRW S. 362: nutzt die Reichweiten schwerer Pistolen
    assertEquals(WeaponRange.HEAVY_PISTOL, find("Hämmerli 620S").range());
    // GRW S. 365: "Sie verschießt ausschließlich Flechettemunition"
    assertEquals(WeaponRange.SHOTGUN_FLECHETTE, find("Mossberg AM-CMDT").range());
    // GRW S. 365: Spezielle Waffen
    assertEquals(WeaponRange.LIGHT_PISTOL, find("Ares S-III Super-Squirt").range());
    assertEquals(WeaponRange.LIGHT_PISTOL, find("Fichetti Pain Inducer").range());
    assertEquals(WeaponRange.HEAVY_PISTOL, find("Parashield Pfeilpistole").range());
    assertEquals(WeaponRange.SPORTS_RIFLE, find("Parashield Pfeilgewehr").range());
  }

  @Test
  void includedAccessoriesFollowWeaponTexts() {
    // GRW S. 363: "Die TX beinhaltet ein Gasventilsystem 2, einen Lasermarkierer …"
    assertEquals(List.of(new IncludedAccessory("Gasventilsystem 2", AccessoryRule.GAS_VENT),
            new IncludedAccessory("Lasermarkierer", null)),
        find("HK MP-5 TX").included());
    // GRW S. 362: "Enthält ein Smartgunsystem."
    assertEquals(List.of(new IncludedAccessory("Smartgunsystem", AccessoryRule.SMARTGUN)),
        find("Ares Predator IV").included());
    assertEquals(3, find("Ultimax HMG-2").included().size());
    assertTrue(find("Katana").included().isEmpty());
  }

  @Test
  void recoilWithIncludedAccessoriesMatchesTable() {
    // Tabellen-RK enthält eingebautes Zubehör bereits (GRW S. 357): Gasventil 3 fest,
    // Schockpolster 1 + Dreibein 6 im Klammerwert.
    assertEquals(new Recoil(3, 10), find("Ultimax HMG-2").recoil());
    assertEquals(new Recoil(0, 1), find("Ruger 100").recoil());
  }
}
