package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Gespeicherter Geist bzw. Sprite: Katalogname, Kraft bzw. Stufe, Dienste, gebunden bzw. registriert und die gewählten
 * optionalen Kräfte bzw. Komplexen Formen (fehlt in älteren Dateien, dann leer).
 */
class SpiritEntry {

  public @NonNull String name = "";
  public int force = 1;
  public int services;
  public boolean bound;
  public @NonNull List<String> options = new ArrayList<>();

  public SpiritEntry() {
  }

  public SpiritEntry(@NonNull String name, int force, int services, boolean bound, @NonNull List<String> options) {
    this.name = name;
    this.force = force;
    this.services = services;
    this.bound = bound;
    this.options = new ArrayList<>(options);
  }
}
