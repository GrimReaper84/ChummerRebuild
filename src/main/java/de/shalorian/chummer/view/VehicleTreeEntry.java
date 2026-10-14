package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleCategory;
import de.shalorian.chummer.model.character.vehicle.VehicleModification;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Knotenwert im Fahrzeugbaum des Tabs "Fahrzeuge &amp; Drohnen": eine Kategorie-Gruppe ({@code vehicle
 * == null}), ein Fahrzeug oder – als Kind des Fahrzeugs – der Riggeradapter, eine Waffenhalterung,
 * die Waffe in einer Halterung (Kind der Halterung), ein Ausrüstungsgegenstand oder ein Einbau
 * (Kind des Gegenstands).
 *
 * @param label          angezeigter Text
 * @param vehicle        das Fahrzeug des Knotens, {@code null} bei einem Kategorieknoten
 * @param mount          die Halterung (auch beim Waffenknoten), sonst {@code null}
 * @param weaponNode     ob der Knoten die Waffe in {@code mount} ist
 * @param gear           Ausrüstungsgegenstand oder Einbau, sonst {@code null}
 * @param riggerAdapter  ob der Knoten der Riggeradapter ist
 */
record VehicleTreeEntry(@NonNull String label, @Nullable Vehicle vehicle,
                        @Nullable WeaponMount mount, boolean weaponNode, @Nullable Gear gear,
                        boolean riggerAdapter) {

  private static final String INCLUDED = " (mitgeliefert)";

  static @NonNull VehicleTreeEntry ofCategory(@NonNull VehicleCategory category) {
    return new VehicleTreeEntry(category.getDisplayName(), null, null, false, null, false);
  }

  static @NonNull VehicleTreeEntry ofVehicle(@NonNull Vehicle vehicle) {
    return new VehicleTreeEntry(vehicle.getName(), vehicle, null, false, null, false);
  }

  static @NonNull VehicleTreeEntry ofRiggerAdapter(@NonNull Vehicle vehicle) {
    String label = VehicleModification.RIGGER_ADAPTER.getDisplayName()
        + (vehicle.isRiggerAdapterIncluded() ? INCLUDED : "");
    return new VehicleTreeEntry(label, vehicle, null, false, null, true);
  }

  static @NonNull VehicleTreeEntry ofMount(@NonNull Vehicle vehicle, @NonNull WeaponMount mount) {
    String label = VehicleModification.WEAPON_MOUNT.getDisplayName()
        + (mount.isIncluded() ? INCLUDED : "") + (mount.getWeapon() == null ? " – leer" : "");
    return new VehicleTreeEntry(label, vehicle, mount, false, null, false);
  }

  static @NonNull VehicleTreeEntry ofWeapon(@NonNull Vehicle vehicle, @NonNull WeaponMount mount,
      @NonNull Weapon weapon) {
    return new VehicleTreeEntry(weapon.getDisplayName(), vehicle, mount, true, null, false);
  }

  static @NonNull VehicleTreeEntry ofGear(@NonNull Vehicle vehicle, @NonNull Gear gear) {
    String label = gear.getDisplayName() + (vehicle.isIncluded(gear) ? INCLUDED : "");
    return new VehicleTreeEntry(label, vehicle, null, false, gear, false);
  }

  /** Ob der Knoten das Fahrzeug selbst ist. */
  boolean isVehicle() {
    return vehicle != null && mount == null && gear == null && !riggerAdapter;
  }

  /** Die Waffe des Knotens, oder {@code null}, wenn er keine Waffe ist. */
  @Nullable Weapon weapon() {
    return weaponNode && mount != null ? mount.getWeapon() : null;
  }

  /** Beschriftung im {@code TreeView} (Standard-Zellen zeigen {@code toString()}). */
  @Override
  public @NonNull String toString() {
    return label;
  }
}
