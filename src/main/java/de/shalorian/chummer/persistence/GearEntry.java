package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class GearEntry {

  public @NonNull String name = "";
  /** Gewählte Stufe, {@code 0} bei Gegenständen ohne Stufe. */
  public int rating = 0;
  /** Menge in Einheiten (z.B. 40 RFID-Marker), nicht in Packungen. */
  public int quantity = 1;
  /** Zusatztext (Fertigkeit, Lizenz …), leer ohne Zusatztext. */
  public @NonNull String extra = "";
  /** Gewählter Packungspreis bei Preisspanne, sonst {@code null}. */
  public @Nullable Integer packPrice;

  public GearEntry() {
  }

  public GearEntry(@NonNull String name, int rating, int quantity, @NonNull String extra,
      @Nullable Integer packPrice) {
    this.name = name;
    this.rating = rating;
    this.quantity = quantity;
    this.extra = extra;
    this.packPrice = packPrice;
  }
}
