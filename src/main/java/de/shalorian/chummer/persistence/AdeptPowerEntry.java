package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

/** Gespeicherte Adeptenkraft: Katalogname, Auswahl (leer ohne) und Stufe. */
class AdeptPowerEntry {

  public @NonNull String name = "";
  public @NonNull String choice = "";
  public int level = 1;

  public AdeptPowerEntry() {
  }

  public AdeptPowerEntry(@NonNull String name, @NonNull String choice, int level) {
    this.name = name;
    this.choice = choice;
    this.level = level;
  }
}
