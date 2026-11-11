package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class SelectedQualityEntry {

  public @NonNull String name = "";
  public boolean gratis = false;
  /** Auswahl der Quality (Fertigkeit, Attribut oder Text); fehlt in Dateien vor 2026-11-12. */
  public @NonNull String choice = "";

  public SelectedQualityEntry() {
  }

  public SelectedQualityEntry(@NonNull String name, boolean gratis, @NonNull String choice) {
    this.name = name;
    this.gratis = gratis;
    this.choice = choice;
  }
}
