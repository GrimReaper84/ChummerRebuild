package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class SelectedQualityEntry {

  public @NonNull String name = "";
  public boolean gratis = false;

  public SelectedQualityEntry() {
  }

  public SelectedQualityEntry(@NonNull String name, boolean gratis) {
    this.name = name;
    this.gratis = gratis;
  }
}
