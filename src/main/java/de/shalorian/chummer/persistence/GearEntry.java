package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
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
  /** Einbauten, z.B. das Betriebssystem eines Kommlinks; fehlt in Dateien vor Gear Teil B. */
  public @NonNull List<GearEntry> installed = new ArrayList<>();
  /** Ob ein Programm läuft (Prozessorlimit); fehlt in Dateien vor ADR-054. */
  public boolean running;
  /** Ob dies das ausdrücklich gewählte aktive Kommlink ist; fehlt in Dateien vor ADR-054. */
  public boolean active;

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
