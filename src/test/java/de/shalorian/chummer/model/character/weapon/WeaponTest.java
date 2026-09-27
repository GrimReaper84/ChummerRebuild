package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.equipment.Availability;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class WeaponTest {

  private static WeaponData bow() {
    return Objects.requireNonNull(WeaponDataLoader.findByName("Bogen"));
  }

  @Test
  void bowPriceAndAvailabilityFollowRating() {
    Weapon bow = new Weapon(bow(), 4);

    assertEquals(400, bow.getNuyenCost());
    assertEquals("8", bow.getAvailabilityText());
    assertEquals("Bogen (Stufe 4)", bow.getDisplayName());
  }

  @Test
  void bowRatingMustBeOneToEight() {
    assertThrows(IllegalArgumentException.class, () -> new Weapon(bow(), 0));
    assertThrows(IllegalArgumentException.class, () -> new Weapon(bow(), 9));
  }

  @Test
  void weaponWithoutRatingRejectsRating() {
    WeaponData knife = new WeaponData("Test", WeaponCategory.BLADES, "–", "(STR÷2+1)K", "–", "", "",
        "", Availability.NONE, "20", 0, "", "SR4", "0");

    assertEquals("Test", new Weapon(knife).getDisplayName());
    assertEquals(20, new Weapon(knife).getNuyenCost());
    assertThrows(IllegalArgumentException.class, () -> new Weapon(knife, 1));
  }
}
