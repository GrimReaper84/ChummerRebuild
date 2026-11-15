package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Grundpreis beim Verkauf: 30 % des Originalpreises, abgerundet (GRW S. 358). */
class CharacterSaleTest {

  @Test
  void thirtyPercentRoundedDown() {
    assertEquals(105, Equipment.saleBasePrice(350));
    assertEquals(0, Equipment.saleBasePrice(3));
    assertEquals(29, Equipment.saleBasePrice(99));
  }

  @Test
  void weaponWithAccessoryUsesTheTotalPrice() {
    Weapon predator = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Ares Predator IV")));
    predator.addAccessory(new WeaponAccessory(
        Objects.requireNonNull(AccessoryDataLoader.findByName("Schalldämpfer")), Mount.BARREL));
    assertEquals(550, predator.getTotalNuyenCost());
    assertEquals(165, Equipment.saleBasePrice(predator.getTotalNuyenCost()));
  }
}
