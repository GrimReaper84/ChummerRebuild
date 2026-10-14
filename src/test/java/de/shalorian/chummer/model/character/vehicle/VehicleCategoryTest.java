package de.shalorian.chummer.model.character.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VehicleCategoryTest {

  @Test
  void hasTheTenRulebookHeadings() {
    assertEquals(10, VehicleCategory.values().length);
    assertEquals(VehicleCategory.MILITARY_AND_SECURITY,
        VehicleCategory.fromDisplayName("Militär- und Sicherheitsfahrzeuge"));
    assertThrows(IllegalArgumentException.class, () -> VehicleCategory.fromDisplayName("Panzer"));
  }

  @Test
  void onlyDronesAreDrones() {
    assertTrue(VehicleCategory.DRONES.isDrone());
    assertFalse(VehicleCategory.CARS.isDrone());
  }

  /** Tabelle Gerätestufen, GRW S. 261: Fahrzeuge/Drohnen 3, Sicherheitsfahrzeuge 4. */
  @Test
  void deviceRatingFollowsRulebookTable() {
    assertEquals(3, VehicleCategory.CARS.deviceRating());
    assertEquals(3, VehicleCategory.DRONES.deviceRating());
    assertEquals(4, VehicleCategory.MILITARY_AND_SECURITY.deviceRating());
  }
}
