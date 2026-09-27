package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class LifestyleEntry {

  /** {@code LifestyleLevel}-Enum-Name, z.B. {@code "MIDDLE"}. */
  public @NonNull String level = "STREET";
  /** Im Voraus bezahlte Monate, mindestens 1. */
  public int months = 1;

  public LifestyleEntry() {
  }

  public LifestyleEntry(@NonNull String level, int months) {
    this.level = level;
    this.months = months;
  }
}
