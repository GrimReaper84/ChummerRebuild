package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekauftes, an einer Waffe montiertes Zubehörteil.
 *
 * @param mount die belegte Halterung, bzw. {@code null} bei Zubehör ohne Halterung
 */
public record WeaponAccessory(@NonNull AccessoryData data, @Nullable Mount mount) {

  public @NonNull String getName() {
    return data.name();
  }

  /** Anzeigename inkl. Halterung, z.B. "Lasermarkierer (Unterlauf)". */
  public @NonNull String getDisplayName() {
    return mount == null ? data.name() : data.name() + " (" + mount.getDisplayName() + ")";
  }
}
