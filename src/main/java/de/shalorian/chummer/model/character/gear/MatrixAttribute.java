package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Matrixattribut eines Geräts (GRW S. 260): Prozessor und Signal stammen aus der Hardware, Firewall und System aus der
 * Software. Hardware-Module und Softwarepakete nennen das Attribut, das sie aufrüsten.
 */
public enum MatrixAttribute {
  PROCESSOR("Prozessor", true),
  SIGNAL("Signal", true),
  FIREWALL("Firewall", false),
  SYSTEM("System", false);

  private final @NonNull String displayName;
  private final boolean hardware;

  MatrixAttribute(@NonNull String displayName, boolean hardware) {
    this.displayName = displayName;
    this.hardware = hardware;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob das Attribut zur Hardware gehört; nur Hardware ist auf +2 über den Ausgangswert begrenzt (S. 260). */
  public boolean isHardware() {
    return hardware;
  }
}
