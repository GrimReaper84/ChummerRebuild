package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class ContactEntry {

  public @NonNull String name = "";
  public @Nullable String description = null;
  public int connection = 1;
  public int loyalty = 1;

  public ContactEntry() {
  }

  public ContactEntry(@NonNull String name, @Nullable String description, int connection,
      int loyalty) {
    this.name = name;
    this.description = description;
    this.connection = connection;
    this.loyalty = loyalty;
  }
}
