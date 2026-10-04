package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearArea;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Knotenwert im Ausrüstungsbaum des Unter-Tabs "Ausrüstung": ein Bereich der Ausrüstungsliste ({@code
 * gear == null}) oder ein gekaufter Gegenstand.
 *
 * @param label angezeigter Text
 * @param gear  der Gegenstand, bzw. {@code null} bei einem Bereichsknoten
 */
record GearTreeEntry(@NonNull String label, @Nullable Gear gear) {

  static @NonNull GearTreeEntry ofArea(@NonNull GearArea area) {
    return new GearTreeEntry(area.getDisplayName(), null);
  }

  static @NonNull GearTreeEntry ofGear(@NonNull Gear gear) {
    return new GearTreeEntry(gear.getDisplayName(), gear);
  }

  /** Beschriftung im {@code TreeView} (Standard-Zellen zeigen {@code toString()}). */
  @Override
  public @NonNull String toString() {
    return label;
  }
}
