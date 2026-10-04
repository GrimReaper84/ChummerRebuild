package de.shalorian.chummer.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class GearDetailsTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void ratedItemShowsRatingAvailabilityAndPrice() {
    GearDetails d = GearDetails.of(new Gear(find("Automatischer Dietrich"), 4));

    assertEquals("Automatischer Dietrich (Stufe 4)", d.name());
    assertEquals("Einbruchswerkzeug", d.category());
    assertEquals("4", d.rating());
    assertEquals("–", d.deviceRating());
    assertEquals("8E", d.availability());
    assertEquals("800 ¥ pro Stück", d.packPrice());
    assertEquals("800 ¥", d.totalCost());
    assertEquals("1 Stück", d.quantity());
    assertEquals("SR4 S. 382", d.source());
  }

  @Test
  void packagedItemShowsUnitPackPriceAndTotal() {
    GearDetails d = GearDetails.of(new Gear(find("Standard-RFID-Marker"), 0, 40, "", null));

    assertEquals("–", d.rating());
    assertEquals("1", d.deviceRating());
    assertEquals("40 Stück", d.quantity());
    assertEquals("1 ¥ pro 20 Stück", d.packPrice());
    assertEquals("2 ¥", d.totalCost());
    assertEquals("–", d.availability());
  }

  @Test
  void doseRopeAndExtraUseTheirUnitsAndLabels() {
    assertEquals("10 ¥ pro Dosis", GearDetails.of(new Gear(find("Cram"), 0)).packPrice());
    assertEquals("3 Dosen",
        GearDetails.of(new Gear(find("Cram"), 0, 3, "", null)).quantity());
    assertEquals("50 ¥ pro 100 m", GearDetails.of(new Gear(find("Standardseil"), 0)).packPrice());

    GearDetails license = GearDetails.of(new Gear(find("Gefälschte Lizenz"), 2, 1, "Führerschein",
        null));
    assertEquals("Lizenz", license.extraLabel());
    assertEquals("Führerschein", license.extra());
    assertEquals("6V", license.availability());
  }
}
