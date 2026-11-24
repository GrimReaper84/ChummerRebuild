package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekauftes, an einer Waffe montiertes Zubehörteil.
 *
 * @param mount    die belegte Halterung, bzw. {@code null} bei Zubehör ohne Halterung und bei einem Ersatzteil
 * @param replaces Name des mitgelieferten Teils, an dessen Stelle es sitzt ({@link Weapon#replacementFor}), sonst
 *                 {@code null}
 */
public record WeaponAccessory(@NonNull AccessoryData data, @Nullable Mount mount, @Nullable String replaces) {

  /** Zubehörteil, das kein mitgeliefertes ersetzt. */
  public WeaponAccessory(@NonNull AccessoryData data, @Nullable Mount mount) {
    this(data, mount, null);
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Anzeigename inkl. Halterung, z.B. "Lasermarkierer (Unterlauf)". */
  public @NonNull String getDisplayName() {
    return mount == null ? data.name() : data.name() + " (" + mount.getDisplayName() + ")";
  }
}
