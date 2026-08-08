package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class ComplexFormEntry {

  public @NonNull String name = "";
  public int rating = 1;

  public ComplexFormEntry() {
  }

  public ComplexFormEntry(@NonNull String name, int rating) {
    this.name = name;
    this.rating = rating;
  }
}
