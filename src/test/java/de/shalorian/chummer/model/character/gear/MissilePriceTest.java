package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Granaten, Raketen, Lenkraketen und Sprengstoffe (GRW S. 370/371, am Bild gelesen). */
class MissilePriceTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void guidedFragmentationMissileAddsSensorRating() {
    // Splitterrakete 500 ¥, 16V; Lenkrakete: Verf. +5, + (Sensorstufe x 500) ¥
    GearData missile = find("Splitterlenkrakete");

    assertEquals(2_000, missile.packCostFor(3));
    assertEquals("21V", missile.availability().displayText(3));
    assertEquals("16K(f)", Objects.requireNonNull(missile.combat()).damage());
  }

  @Test
  void plasticExplosivePerKilogram() {
    // Plastik Stufe 4–15, 16V, Stufe x 100 ¥ pro kg
    Gear plastic = new Gear(find("Plastiksprengstoff"), 6, 2, "", null);

    assertEquals(1_200, plastic.getNuyenCost());
    assertEquals("Plastiksprengstoff (Stufe 6, 2 kg)", plastic.getDisplayName());
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("Plastiksprengstoff"), 3, 1, "", null));
  }

  @Test
  void explosivesAboveRatingSixAreBlockedAtCreation() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    GearData foam = find("Sprengschaum"); // 12V

    assertTrue(c.getEquipment().isAvailableForPurchase(foam.availability(), 6));
    assertFalse(c.getEquipment().isAvailableForPurchase(foam.availability(), 7));
  }

  @Test
  void commercialExplosiveHasFixedRatingThree() {
    // Kommerziell: Stufe 3, 8E, 100 ¥ pro kg
    Gear commercial = new Gear(find("Kommerzieller Sprengstoff"), 3, 3, "", null);

    assertEquals(300, commercial.getNuyenCost());
    assertEquals("8E", commercial.getAvailabilityText());
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("Kommerzieller Sprengstoff"), 4, 1, "", null));
  }

  @Test
  void miniGrenadeHasTheSamePrice() {
    // GRW S. 370: "Minigranaten haben den gleichen Preis und die gleiche Wirkung"
    Gear grenade = new Gear(find("Splittergranate"), 0, 2, "Standard", null);
    Gear mini = new Gear(find("Splittergranate"), 0, 2, "Minigranate", null);

    assertEquals(70, grenade.getNuyenCost());
    assertEquals("Splittergranate (Minigranate) ×2", mini.getDisplayName());
    assertEquals(grenade.getNuyenCost(), mini.getNuyenCost());
    assertFalse(grenade.isSameKind(mini));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("Splittergranate"), 0, 1, "Riesengranate", null));
    assertEquals("10 m Radius", Objects.requireNonNull(find("Rauchgranate").combat()).blast());
  }
}
