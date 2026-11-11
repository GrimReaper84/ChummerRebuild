package de.shalorian.chummer.model.character.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Fahrzeuge zählen gegen das Ressourcengeld (GRW S. 111), ihre Waffen gehören nicht zum Charakter. */
class EquipmentVehicleTest {

  private static Vehicle vehicle(String name) {
    return new Vehicle(Objects.requireNonNull(VehicleDataLoader.findByName(name), name));
  }

  @Test
  void vehiclesCountAgainstResourceNuyen() {
    // 10 BP = 50.000 ¥; Mercury Comet 14.000 ¥ + Waffenhalterung 2.500 ¥ → 33.500 ¥
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.setNuyenBP(10);
    Vehicle comet = vehicle("Mercury Comet (Mittelklassewagen)");
    comet.addMount();
    c.getEquipment().addVehicle(comet);

    assertEquals(16_500, c.getEquipment().getVehicleNuyenCost());
    assertEquals(16_500, c.getEquipment().getSpentNuyen());
    assertEquals(33_500, c.getRemainingNuyen());

    c.getEquipment().removeVehicle(comet);
    assertEquals(50_000, c.getRemainingNuyen());
  }

  @Test
  void mountedWeaponsAreNotCharacterWeapons() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Vehicle doberman = vehicle("GM-Nissan Doberman (Mittel)");
    Weapon lmg = new Weapon(Objects.requireNonNull(
        WeaponDataLoader.findByName("Ingram White Knight")));
    doberman.mountWeapon(doberman.getMounts().getFirst(), lmg);
    c.getEquipment().addVehicle(doberman);

    assertTrue(c.getEquipment().getWeapons().isEmpty());
    assertEquals(0, c.getEquipment().getWeaponNuyenCost());
    assertEquals(3_000 + lmg.getTotalNuyenCost(), c.getEquipment().getVehicleNuyenCost());
    assertFalse(c.getEquipment().getVehicles().isEmpty());
  }
}
