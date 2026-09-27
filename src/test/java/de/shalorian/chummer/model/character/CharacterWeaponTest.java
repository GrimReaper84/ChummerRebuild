package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Waffen als Kaufkategorie des Ressourcengelds (GRW S. 110) und Verfügbarkeit (GRW S. 111). */
class CharacterWeaponTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static WeaponData catalog(String name) {
    return Objects.requireNonNull(WeaponDataLoader.findByName(name));
  }

  @Test
  void purchaseAndRemovalChangeRemainingNuyen() {
    Character c = mensch();
    c.setNuyenBP(10);
    Weapon predator = new Weapon(catalog("Ares Predator IV"));

    c.addWeapon(predator);
    assertEquals(49_650, c.getRemainingNuyen());

    c.removeWeapon(predator);
    assertEquals(50_000, c.getRemainingNuyen());
  }

  @Test
  void sameWeaponCanBeBoughtTwiceAndRemovedIndividually() {
    Character c = mensch();
    Weapon first = new Weapon(catalog("Messer"));
    Weapon second = new Weapon(catalog("Messer"));
    c.addWeapon(first);
    c.addWeapon(second);

    c.removeWeapon(first);

    assertEquals(1, c.getWeapons().size());
    assertSame(second, c.getWeapons().get(0));
  }

  @Test
  void weaponCostsSumIncludingBowRating() {
    Character c = mensch();
    c.addWeapon(new Weapon(catalog("Katana")));
    c.addWeapon(new Weapon(catalog("Bogen"), 3));

    assertEquals(1_300, c.getWeaponNuyenCost());
    assertEquals(1_300, c.getSpentNuyen());
  }

  @Test
  void weaponsDoNotCostBuildPoints() {
    Character c = mensch();
    int before = c.getSpentBP();

    c.addWeapon(new Weapon(catalog("Ares Alpha")));

    assertEquals(before, c.getSpentBP());
  }

  @Test
  void availabilityLimitAtCreation() {
    Character c = mensch();
    WeaponData bow = catalog("Bogen");

    assertTrue(c.isAvailableForPurchase(catalog("Ares Alpha").availability(), 0));   // 12V
    assertFalse(c.isAvailableForPurchase(catalog("HK XM30").availability(), 0));     // 15V
    assertTrue(c.isAvailableForPurchase(bow.availability(), 6));                       // 12
    assertFalse(c.isAvailableForPurchase(bow.availability(), 7));                      // 14

    c.setCreated(true);
    assertTrue(c.isAvailableForPurchase(catalog("HK XM30").availability(), 0));
    assertTrue(c.isAvailableForPurchase(bow.availability(), 8));
  }
}
