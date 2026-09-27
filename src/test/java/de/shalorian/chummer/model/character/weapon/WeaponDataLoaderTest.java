package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.equipment.Legality;
import java.util.Arrays;
import java.util.Collection;
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
    assertEquals(63, all().size());
    Set<WeaponCategory> used = all().stream().map(WeaponData::category).collect(Collectors.toSet());
    assertEquals(Set.copyOf(Arrays.asList(WeaponCategory.values())), used);
  }

  @Test
  void aresPredatorMatchesRulebook() {
    WeaponData predator = find("Ares Predator IV");

    assertEquals(WeaponCategory.HEAVY_PISTOLS, predator.category());
    assertEquals("5K", predator.damage());
    assertEquals("–1", predator.armorPiercing());
    assertEquals("HM", predator.mode());
    assertEquals("–", predator.recoil());
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
    assertEquals("2 (3)", find("Ingram Smartgun X").recoil());
    assertEquals("5 (6)", find("Ingram White Knight").recoil());
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
        assertTrue(data.costFor(rating) > 0, data.name());
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
}
