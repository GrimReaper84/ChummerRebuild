package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
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

    c.getEquipment().addWeapon(predator);
    assertEquals(49_650, c.getRemainingNuyen());

    c.getEquipment().removeWeapon(predator);
    assertEquals(50_000, c.getRemainingNuyen());
  }

  @Test
  void sameWeaponCanBeBoughtTwiceAndRemovedIndividually() {
    Character c = mensch();
    Weapon first = new Weapon(catalog("Messer"));
    Weapon second = new Weapon(catalog("Messer"));
    c.getEquipment().addWeapon(first);
    c.getEquipment().addWeapon(second);

    c.getEquipment().removeWeapon(first);

    assertEquals(1, c.getEquipment().getWeapons().size());
    assertSame(second, c.getEquipment().getWeapons().getFirst());
  }

  @Test
  void weaponCostsSumIncludingBowRating() {
    Character c = mensch();
    c.getEquipment().addWeapon(new Weapon(catalog("Katana")));
    c.getEquipment().addWeapon(new Weapon(catalog("Bogen"), 3));

    assertEquals(1_300, c.getEquipment().getWeaponNuyenCost());
    assertEquals(1_300, c.getEquipment().getSpentNuyen());
  }

  @Test
  void weaponsDoNotCostBuildPoints() {
    Character c = mensch();
    int before = c.getSpentBP();

    c.getEquipment().addWeapon(new Weapon(catalog("Ares Alpha")));

    assertEquals(before, c.getSpentBP());
  }

  @Test
  void availabilityLimitAtCreation() {
    Character c = mensch();
    WeaponData bow = catalog("Bogen");

    assertTrue(c.getEquipment().isAvailableForPurchase(catalog("Ares Alpha").availability(), 0)); // 12V
    assertFalse(c.getEquipment().isAvailableForPurchase(catalog("HK XM30").availability(), 0)); // 15V
    assertTrue(c.getEquipment().isAvailableForPurchase(bow.availability(), 6)); // 12
    assertFalse(c.getEquipment().isAvailableForPurchase(bow.availability(), 7)); // 14

    c.setCreated(true);
    assertTrue(c.getEquipment().isAvailableForPurchase(catalog("HK XM30").availability(), 0));
    assertTrue(c.getEquipment().isAvailableForPurchase(bow.availability(), 8));
  }

  @Test
  void accessoriesCountAgainstResourceBudget() {
    Character c = mensch();
    c.setNuyenBP(10);
    Weapon predator = new Weapon(catalog("Ares Predator IV"));
    predator.addAccessory(new WeaponAccessory(
        Objects.requireNonNull(AccessoryDataLoader.findByName("Schalldämpfer")), Mount.BARREL));
    c.getEquipment().addWeapon(predator);

    assertEquals(550, c.getEquipment().getWeaponNuyenCost());
    assertEquals(49_450, c.getRemainingNuyen());
  }
}
