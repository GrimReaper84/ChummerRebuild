package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

/** Gespeicherter Geist bzw. Sprite: Katalogname, Kraft bzw. Stufe, Dienste, gebunden bzw. registriert. */
class SpiritEntry {

  public @NonNull String name = "";
  public int force = 1;
  public int services;
  public boolean bound = true;

  public SpiritEntry() {
  }

  public SpiritEntry(@NonNull String name, int force, int services, boolean bound) {
    this.name = name;
    this.force = force;
    this.services = services;
    this.bound = bound;
  }
}
