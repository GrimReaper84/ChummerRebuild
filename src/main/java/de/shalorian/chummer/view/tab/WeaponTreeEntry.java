package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.weapon.IncludedAccessory;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Knotenwert im Waffenbaum des Unter-Tabs "Waffen": eine Kategorie-Gruppe ({@code weapon == null}),
 * eine gekaufte Waffe oder — als Kind der Waffe — ein gekauftes bzw. mitgeliefertes Zubehörteil oder
 * ein Pfeilstapel eines Bogens.
 *
 * @param label     angezeigter Text
 * @param weapon    die Waffe des Knotens bzw. die Waffe, an der das Zubehör sitzt; {@code null} bei
 *                  einem Kategorieknoten
 * @param accessory gekauftes Zubehör, sonst {@code null}
 * @param included  mitgeliefertes Zubehör, sonst {@code null}
 * @param arrows    Pfeilstapel eines Bogens, sonst {@code null}
 */
record WeaponTreeEntry(@NonNull String label, @Nullable Weapon weapon,
                       @Nullable WeaponAccessory accessory, @Nullable IncludedAccessory included,
                       @Nullable Gear arrows) {

  static @NonNull WeaponTreeEntry ofCategory(@NonNull WeaponCategory category) {
    return new WeaponTreeEntry(category.getDisplayName(), null, null, null, null);
  }

  static @NonNull WeaponTreeEntry ofWeapon(@NonNull Weapon weapon) {
    String label = weapon.getData().fromArmor() ? weapon.getDisplayName() + " (aus Rüstung)" : weapon.getDisplayName();
    return new WeaponTreeEntry(label, weapon, null, null, null);
  }

  static @NonNull WeaponTreeEntry ofAccessory(@NonNull Weapon weapon,
      @NonNull WeaponAccessory accessory) {
    return new WeaponTreeEntry(accessory.getDisplayName(), weapon, accessory, null, null);
  }

  static @NonNull WeaponTreeEntry ofIncluded(@NonNull Weapon weapon,
      @NonNull IncludedAccessory included) {
    return new WeaponTreeEntry(included.name() + " (mitgeliefert)", weapon, null, included, null);
  }

  static @NonNull WeaponTreeEntry ofArrows(@NonNull Weapon weapon, @NonNull Gear arrows) {
    return new WeaponTreeEntry(arrows.getDisplayName(), weapon, null, null, arrows);
  }

  /** Ob der Knoten die Waffe selbst ist (nicht Kategorie, nicht Zubehör, nicht Pfeile). */
  boolean isWeapon() {
    return weapon != null && accessory == null && included == null && arrows == null;
  }

  /** Beschriftung im {@code TreeView} (Standard-Zellen zeigen {@code toString()}). */
  @Override
  public @NonNull String toString() {
    return label;
  }
}
