package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Tabelle Waffenreichweiten, GRW S. 179 (am PDF-Bild verifiziert). */
class WeaponRangeTest {

  /** Stärke für Tabellenzeilen ohne Stärkebezug; der Wert darf keine Rolle spielen. */
  private static final int IRRELEVANT_STRENGTH = 1;

  private static WeaponData catalog(String name) {
    return Objects.requireNonNull(WeaponDataLoader.findByName(name));
  }

  private static String bands(Weapon weapon, int strength) {
    RangeBands bands = weapon.getRangeBands(strength);
    assertNotNull(bands, weapon.getName());
    return bands.displayText();
  }

  @Test
  void heavyPistol() {
    assertEquals("0–5/6–20/21–40/41–60",
        WeaponRange.HEAVY_PISTOL.bands(IRRELEVANT_STRENGTH).displayText());
  }

  @Test
  void assaultRifle() {
    assertEquals("0–50/51–150/151–350/351–550",
        WeaponRange.ASSAULT_RIFLE.bands(IRRELEVANT_STRENGTH).displayText());
  }

  @Test
  void grenadeLauncherHasMinimumRangeOfFive() {
    assertEquals("5–50/51–100/101–150/151–500",
        WeaponRange.GRENADE_LAUNCHER.bands(IRRELEVANT_STRENGTH).displayText());
  }

  @Test
  void missileLauncherStartsAtTwenty() {
    assertEquals("20–70/71–150/151–450/451–1500",
        WeaponRange.MISSILE_LAUNCHER.bands(IRRELEVANT_STRENGTH).displayText());
  }

  @Test
  void bowUsesItsMinimumStrength() {
    Weapon bow = new Weapon(catalog("Bogen"), 4);

    assertEquals("0–4/5–40/41–120/121–240", bands(bow, IRRELEVANT_STRENGTH));
  }

  @Test
  void throwingKnifeUsesStrength() {
    assertEquals("0–5/6–10/11–15/16–25", bands(new Weapon(catalog("Wurfmesser")), 5));
  }

  @Test
  void shurikenUsesStrength() {
    assertEquals("0–4/5–8/9–20/21–28", bands(new Weapon(catalog("Shuriken")), 4));
  }

  @Test
  void fixedRangesIgnoreStrength() {
    assertEquals(WeaponRange.HEAVY_PISTOL.bands(1), WeaponRange.HEAVY_PISTOL.bands(8));
  }

  @Test
  void crossbowsHaveFixedRanges() {
    assertEquals("0–15/16–45/46–120/121–180",
        bands(new Weapon(catalog("Schwere Armbrust")), IRRELEVANT_STRENGTH));
  }

  @Test
  void meleeWeaponsHaveNoRange() {
    assertNull(new Weapon(catalog("Katana")).getRangeBands(5));
  }

  @Test
  void fromDisplayNameReadsCatalogValues() {
    assertEquals(WeaponRange.SHOTGUN_SLUG,
        WeaponRange.fromDisplayName("Schrotflinte (Massivgeschosse)"));
    assertThrows(IllegalArgumentException.class, () -> WeaponRange.fromDisplayName("Laser"));
  }
}
