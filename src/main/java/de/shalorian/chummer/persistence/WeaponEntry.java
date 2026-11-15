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
  /** Pfeilstapel eines Bogens; fehlt in Dateien vor Gear Teil D. */
  public @NonNull List<GearEntry> arrows = new ArrayList<>();
  /** Name der geladenen Munition (Stapel der Ausrüstung), leer für Standardmunition. */
  public @NonNull String ammo = "";

  public WeaponEntry() {
  }

  public WeaponEntry(@NonNull String name, int rating, @NonNull List<AccessoryEntry> accessories) {
    this.name = name;
    this.rating = rating;
    this.accessories = accessories;
  }
}
