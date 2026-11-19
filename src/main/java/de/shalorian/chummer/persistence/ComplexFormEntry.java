package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class ComplexFormEntry {

  /** Gratis-Anteil aus dem Spielleiter-Modus (ADR-070); fehlt in älteren Dateien, dann 0. */
  public int gmFreeBp;

  public @NonNull String name = "";
  public int rating = 1;

  public ComplexFormEntry() {
  }

  public ComplexFormEntry(@NonNull String name, int rating) {
    this.name = name;
    this.rating = rating;
  }
}
