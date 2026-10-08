package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Pfeile am Bogen. GRW S. 361: "Berechnen Sie den Schaden anhand der niedrigsten Stufe des Pfeils
 * oder Bogens"; Bogenschaden (STR Min + 2)K; Pfeil 5 ¥, Injektionspfeil 50 ¥ (Preis fest).
 */
class ArrowDamageTest {

  private static Weapon bow(int rating) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), rating);
  }

  private static GearData arrow() {
    return Objects.requireNonNull(GearDataLoader.findByName("Pfeil"));
  }

  @Test
  void damageUsesLowerRatingOfArrowAndBow() {
    Weapon bow = bow(6);
    Gear weakArrows = bow.addArrows(new Gear(arrow(), 4, 10, "", null));
    Gear strongArrows = bow.addArrows(new Gear(arrow(), 8, 10, "", null));

    assertEquals("6K", bow.getArrowDamageText(weakArrows)); // (4 + 2)K
    assertEquals("8K", bow.getArrowDamageText(strongArrows)); // min(8, 6) = 6 → (6 + 2)K
  }

  @Test
  void arrowPriceIsFixedAndCountsIntoTheBow() {
    Weapon bow = bow(4); // 400 ¥
    bow.addArrows(new Gear(arrow(), 1, 10, "", null));
    bow.addArrows(new Gear(arrow(), 6, 10, "", null));

    assertEquals(400 + 50 + 50, bow.getTotalNuyenCost());
  }

  @Test
  void sameArrowsAreMerged() {
    Weapon bow = bow(4);
    Gear first = bow.addArrows(new Gear(arrow(), 3, 10, "", null));
    Gear merged = bow.addArrows(new Gear(arrow(), 3, 5, "", null));

    assertSame(first, merged);
    assertEquals(15, first.getQuantity());
    assertEquals(1, bow.getArrows().size());
    bow.removeArrows(first);
    assertTrue(bow.getArrows().isEmpty());
  }

  @Test
  void onlyBowsTakeArrows() {
    Weapon crossbow = new Weapon(
        Objects.requireNonNull(WeaponDataLoader.findByName("Leichte Armbrust")));
    Weapon bow = bow(4);

    assertFalse(crossbow.takesArrows());
    assertThrows(IllegalArgumentException.class,
        () -> crossbow.addArrows(new Gear(arrow(), 3)));
    assertThrows(IllegalArgumentException.class, () -> bow.addArrows(
        new Gear(Objects.requireNonNull(GearDataLoader.findByName("Survival-Kit")), 0)));
  }
}
