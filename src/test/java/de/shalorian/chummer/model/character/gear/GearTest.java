package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Menge, Preis und Validierung gekaufter Ausrüstung (GRW S. 374–395). */
class GearTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void fortyStandardRfidTagsCostTwoNuyen() {
    // GRW S. 374: Standard-RFID-Marker 1 ¥ pro 20 Stück
    Gear tags = new Gear(find("Standard-RFID-Marker"), 0, 40, "", null);

    assertEquals(2, tags.getNuyenCost());
    assertEquals(1, tags.getPackPrice());
  }

  @Test
  void quantityMustBeWholePacks() {
    GearData rfid = find("Standard-RFID-Marker");

    assertThrows(IllegalArgumentException.class, () -> new Gear(rfid, 0, 30, "", null));
    assertThrows(IllegalArgumentException.class, () -> new Gear(rfid, 0, 0, "", null));
    Gear tags = new Gear(rfid, 0);
    assertThrows(IllegalArgumentException.class, () -> tags.setQuantity(10));
    tags.setQuantity(60);
    assertEquals(3, tags.getNuyenCost());
  }

  @Test
  void packStepsKeepAtLeastOnePack() {
    // Standardseil: 50 ¥ pro 100 m (GRW S. 383)
    Gear rope = new Gear(find("Standardseil"), 0);
    assertFalse(rope.canRemovePack());
    assertThrows(IllegalStateException.class, rope::removePack);

    rope.addPack();
    assertEquals(200, rope.getQuantity());
    assertEquals(100, rope.getNuyenCost());
    assertTrue(rope.canRemovePack());

    rope.removePack();
    assertEquals(100, rope.getQuantity());
    assertEquals(50, rope.getPackPrice());
  }

  @Test
  void ratingMustBeInRange() {
    GearData lockpick = find("Automatischer Dietrich");

    assertThrows(IllegalArgumentException.class, () -> new Gear(lockpick, 0));
    assertThrows(IllegalArgumentException.class, () -> new Gear(lockpick, 7));
    assertThrows(IllegalArgumentException.class, () -> new Gear(find("Dietrich-Set"), 1));
    assertEquals(800, new Gear(lockpick, 4).getNuyenCost());
  }

  @Test
  void requiredExtraMustBeFilledAndOthersMustBeEmpty() {
    GearData toolkit = find("Werkzeugkiste");

    assertThrows(IllegalArgumentException.class, () -> new Gear(toolkit, 0, 1, " ", null));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("Brecheisen"), 0, 1, "Hebeln", null));
    assertEquals("Waffenbau", new Gear(toolkit, 0, 1, " Waffenbau ", null).getExtra());
  }

  @Test
  void priceRangeNeedsChosenPriceAtLeastTheMinimum() {
    GearData recording = find("Sim-Aufzeichnung");

    assertThrows(IllegalArgumentException.class, () -> new Gear(recording, 0));
    assertThrows(IllegalArgumentException.class, () -> new Gear(recording, 0, 1, "", 4));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("VR-Spiele"), 0, 1, "", 60));
    Gear sim = new Gear(recording, 0, 3, "", 150);
    assertEquals(450, sim.getNuyenCost());
  }

  @Test
  void chosenPriceAndQuantityCannotOverflowTheTotal() {
    GearData recording = find("Sim-Aufzeichnung");

    assertThrows(IllegalArgumentException.class,
        () -> new Gear(recording, 0, 1, "", Gear.MAX_CHOSEN_PACK_PRICE + 1));
    Gear expensive = new Gear(recording, 0, 1, "", Gear.MAX_CHOSEN_PACK_PRICE);
    assertThrows(IllegalArgumentException.class, () -> expensive.setQuantity(3_000));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(find("Werkstatt"), 0, 30_000, "Waffenbau", null));
    assertEquals(Gear.MAX_CHOSEN_PACK_PRICE, expensive.getNuyenCost());
  }

  @Test
  void sameKindRequiresSameEntryRatingExtraAndPrice() {
    GearData license = find("Gefälschte Lizenz");
    Gear weapons = new Gear(license, 3, 1, "Waffenlizenz", null);

    assertTrue(weapons.isSameKind(new Gear(license, 3, 1, "Waffenlizenz", null)));
    assertFalse(weapons.isSameKind(new Gear(license, 4, 1, "Waffenlizenz", null)));
    assertFalse(weapons.isSameKind(new Gear(license, 3, 1, "Führerschein", null)));
    GearData recording = find("Sim-Aufzeichnung");
    assertFalse(new Gear(recording, 0, 1, "", 5).isSameKind(new Gear(recording, 0, 1, "", 50)));
  }

  @Test
  void displayNameShowsRatingExtraAndQuantity() {
    assertEquals("Dietrich-Set", new Gear(find("Dietrich-Set"), 0).getDisplayName());
    assertEquals("Automatischer Dietrich (Stufe 4)",
        new Gear(find("Automatischer Dietrich"), 4).getDisplayName());
    assertEquals("Gefälschte Lizenz (Stufe 3, Waffenlizenz)",
        new Gear(find("Gefälschte Lizenz"), 3, 1, "Waffenlizenz", null).getDisplayName());
    assertEquals("Standard-RFID-Marker ×40",
        new Gear(find("Standard-RFID-Marker"), 0, 40, "", null).getDisplayName());
    assertEquals("Cram (3 Dosen)", new Gear(find("Cram"), 0, 3, "", null).getDisplayName());
    assertEquals("DocWagon-Vertrag (Gold, 1 Jahr)",
        new Gear(find("DocWagon-Vertrag (Gold)"), 0).getDisplayName());
  }
}
