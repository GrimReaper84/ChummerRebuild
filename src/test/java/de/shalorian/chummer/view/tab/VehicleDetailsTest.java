package de.shalorian.chummer.view.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Anzeigewerte des Fahrzeug-Detailbereichs (GRW S. 197, 380, 397). */
class VehicleDetailsTest {

  private static Vehicle vehicle(String name) {
    return new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName(name), name));
  }

  @Test
  void carShowsTableValuesAndPurchasedEquipment() {
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    VehicleDetails plain = VehicleDetails.of(comet);
    assertEquals("Pkw", plain.category());
    assertEquals("0", plain.handling());
    assertEquals("15/30", plain.accel());
    assertEquals("–", plain.availability());
    assertEquals("13 Kästchen", plain.conditionMonitor()); // 8 + ⌈10/2⌉
    assertEquals("0 von max. 3 (0 belegt)", plain.mounts());
    assertEquals("–", plain.riggerAdapter());
    assertEquals("14000 ¥", plain.cost());
    assertEquals("Sensorpaket (Fahrzeug)", plain.included());
    assertEquals("SR4 S. 397", plain.source());

    comet.addMount();
    comet.installRiggerAdapter();
    Objects.requireNonNull(comet.getSensorPackage()).install(new Gear(
        Objects.requireNonNull(GearDataLoader.findByName("Geruchsscanner")), 3));
    VehicleDetails equipped = VehicleDetails.of(comet);
    assertEquals("1 von max. 3 (0 belegt)", equipped.mounts());
    assertEquals("eingebaut", equipped.riggerAdapter());
    assertEquals("1 (Ø Sensoren 3)", equipped.sensor());
    assertEquals("14000 ¥ (gesamt 20500 ¥)", equipped.cost()); // + 2.500 + 2.500 + 1.500
  }

  @Test
  void droneShowsIncludedEquipment() {
    VehicleDetails doberman = VehicleDetails.of(vehicle("GM-Nissan Doberman (Mittel)"));
    assertEquals("Drohnen", doberman.category());
    assertEquals("6", doberman.availability());
    assertEquals("mitgeliefert", doberman.riggerAdapter());
    assertEquals("1 von max. 1 (0 belegt)", doberman.mounts());
    assertEquals("Riggeradapter, Waffenhalterung, Sensorpaket (Mittlere Drohne), "
        + "Autosoft (Stufe 3, Clearsight), Autosoft (Stufe 3, Zielerfassung)", doberman.included());
    assertEquals("3000 ¥", doberman.cost());
  }

  @Test
  void handlingShowsSignLikeTheRulebook() {
    assertEquals("+3", VehicleDetails.of(vehicle("Eurocar Westwind 3K (Sportwagen)")).handling());
    assertEquals("-2", VehicleDetails.of(vehicle("Mitsubishi Nightsky (Limousine)")).handling());
  }
}
