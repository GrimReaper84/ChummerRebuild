package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class WeaponEntry {

  public @NonNull String name = "";
  /** Gewählte Stufe, {@code 0} bei Waffen ohne Stufe. */
  public int rating = 0;

  public WeaponEntry() {
  }

  public WeaponEntry(@NonNull String name, int rating) {
    this.name = name;
    this.rating = rating;
  }
}
