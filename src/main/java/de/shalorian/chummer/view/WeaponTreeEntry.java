package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Knotenwert im Waffenbaum des Unter-Tabs "Waffen": entweder eine Kategorie-Gruppe ({@code weapon ==
 * null}) oder eine gekaufte Waffe. Zubehör (Waffen-Plan Teil C) wird später als weitere Knotenart unter
 * der Waffe ergänzt.
 *
 * @param label  angezeigter Text (Kategoriename bzw. Waffenname inkl. Stufe)
 * @param weapon die Waffe, oder {@code null} bei einem Kategorieknoten
 */
record WeaponTreeEntry(@NonNull String label, @Nullable Weapon weapon) {

  static @NonNull WeaponTreeEntry ofCategory(@NonNull WeaponCategory category) {
    return new WeaponTreeEntry(category.getDisplayName(), null);
  }

  static @NonNull WeaponTreeEntry ofWeapon(@NonNull Weapon weapon) {
    return new WeaponTreeEntry(weapon.getDisplayName(), weapon);
  }

  /** Beschriftung im {@code TreeView} (Standard-Zellen zeigen {@code toString()}). */
  @Override
  public @NonNull String toString() {
    return label;
  }
}
