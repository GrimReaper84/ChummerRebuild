package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Schadenscodes nach GRW S. 188 ("Stärke ÷ 2 (aufgerundet)") und S. 361 (Bogen, Wurfwaffen). */
class WeaponDamageTest {

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name)));
  }

  @Test
  void katanaRoundsHalfStrengthUp() {
    assertEquals("6K", weapon("Katana").getDamageText(5));
    assertEquals("5K", weapon("Katana").getDamageText(4));
  }

  @Test
  void knifeWithMinimumStrength() {
    assertEquals("2K", weapon("Messer").getDamageText(1));
  }

  @Test
  void sapKeepsStunDamage() {
    assertEquals("3G", weapon("Totschläger").getDamageText(3));
  }

  @Test
  void throwingKnifeUsesStrength() {
    assertEquals("4K", weapon("Wurfmesser").getDamageText(6));
  }

  @Test
  void shurikenWithoutModifier() {
    assertEquals("3K", WeaponDamage.evaluate("(STR÷2)K", 5));
  }

  @Test
  void bowUsesItsMinimumStrengthNotTheCharacters() {
    Weapon bow = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), 3);

    assertEquals("5K", bow.getDamageText(1));
    assertEquals("5K", bow.getDamageText(8));
  }

  @Test
  void fixedDamageKeepsSuffix() {
    assertEquals("5G(e)", weapon("Schockhandschuh").getDamageText(6));
    assertEquals("6K(f)", weapon("Raecor Sting").getDamageText(6));
  }

  @Test
  void specialCodesAreReturnedUnchanged() {
    assertEquals("Granate", weapon("Ares Antioch-2").getDamageText(4));
    assertEquals("Chemikalie", weapon("Ares S-III Super-Squirt").getDamageText(4));
    assertEquals("Droge/Toxin", WeaponDamage.evaluate("Droge/Toxin", 4));
  }

  @Test
  void suffixOnStrengthCodeIsKept() {
    assertEquals("4G(e)", WeaponDamage.evaluate("(STR÷2+1)G(e)", 5));
  }

  @Test
  void noCatalogCodeLeavesStrengthUnresolved() {
    for (WeaponData data : WeaponDataLoader.getAllWeapons().values()) {
      String evaluated = WeaponDamage.evaluate(data.damage(), 4);
      assertFalse(evaluated.contains("STR"), data.name() + ": " + evaluated);
    }
  }
}
