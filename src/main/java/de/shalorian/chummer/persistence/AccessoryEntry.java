package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class AccessoryEntry {

  public @NonNull String name = "";
  /** {@code Mount}-Enum-Name, z.B. {@code "TOP"}; {@code null} bei Zubehör ohne Halterung. */
  public @Nullable String mount;
  /** Name des mitgelieferten Teils, das es ersetzt (ADR-071), sonst {@code null}. */
  public @Nullable String replaces;

  public AccessoryEntry() {
  }

  public AccessoryEntry(@NonNull String name, @Nullable String mount) {
    this.name = name;
    this.mount = mount;
  }
}
