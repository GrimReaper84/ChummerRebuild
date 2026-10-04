package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

class WeaponEntry {

  public @NonNull String name = "";
  /** Gewählte Stufe, {@code 0} bei Waffen ohne Stufe. */
  public int rating = 0;
  /** Gekauftes Zubehör; eingebautes Zubehör ergibt sich aus dem Katalog. */
  public @NonNull List<AccessoryEntry> accessories = new ArrayList<>();

  public WeaponEntry() {
  }

  public WeaponEntry(@NonNull String name, int rating, @NonNull List<AccessoryEntry> accessories) {
    this.name = name;
    this.rating = rating;
    this.accessories = accessories;
  }
}
