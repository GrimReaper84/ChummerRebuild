package de.shalorian.chummer.model.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NaturalWeaponTest {

  @Test
  void readsNameDamageAndArmorPiercing() {
    assertEquals(new NaturalWeapon("Krallen/Biss", "5K", 0, ""), NaturalWeapon.parse("Krallen/Biss: Schaden 5K, PB 0"));
    assertEquals(new NaturalWeapon("Biss/Klauen", "10K", -2, ""), NaturalWeapon.parse("Biss/Klauen: Schaden 10K, PB −2"));
  }

  @Test
  void keepsTheRestAsExtra() {
    assertEquals(new NaturalWeapon("Biss", "1K", 0, "−1 Reichweite"),
        NaturalWeapon.parse("Biss: Schaden 1K, PB 0, −1 Reichweite"));
  }

  @Test
  void rejectsOtherText() {
    assertThrows(IllegalArgumentException.class, () -> NaturalWeapon.parse("Feuer"));
  }
}
