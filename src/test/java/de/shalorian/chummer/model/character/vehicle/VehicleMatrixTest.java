package de.shalorian.chummer.model.character.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Matrixwerte und Aufrüstungen von Fahrzeugen und Drohnen (GRW S. 260/261). */
class VehicleMatrixTest {

  private static GearData gear(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  private static Vehicle vehicleWithDeviceRating(int rating) {
    return VehicleDataLoader.getAllVehicles().values().stream()
        .filter(d -> d.category().deviceRating() == rating)
        .findFirst()
        .map(Vehicle::new)
        .orElseThrow();
  }

  @Test
  void deviceRatingAndUpgrades() {
    Vehicle drone = vehicleWithDeviceRating(3);
    assertEquals(3, drone.getMatrixStats().signal());
    int before = drone.getNuyenCost();

    assertNull(drone.matrixUpgradeProblem(gear("Signal-Modul"), 5));
    assertEquals("Signal höchstens 5 (Ausgangswert 3 + 2)", drone.matrixUpgradeProblem(gear("Signal-Modul"), 6));
    drone.addMatrixUpgrade(new Gear(gear("Signal-Modul"), 5));
    drone.addMatrixUpgrade(new Gear(gear("Firewall-Paket"), 6));

    assertEquals(5, drone.getMatrixStats().signal());
    assertEquals(6, drone.getMatrixStats().firewall());
    assertEquals(before + 1_000 + 3_000, drone.getNuyenCost());
    assertThrows(IllegalArgumentException.class, () -> drone.addMatrixUpgrade(new Gear(gear("Signal-Modul"), 4)));
    assertEquals("Analyse ist kein Hardware-Modul oder Softwarepaket", drone.matrixUpgradeProblem(gear("Analyse"), 2));
  }
}
