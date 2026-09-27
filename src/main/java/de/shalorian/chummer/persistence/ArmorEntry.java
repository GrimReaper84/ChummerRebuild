package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class ArmorEntry {

  public @NonNull String name = "";
  public boolean equipped = true;

  public ArmorEntry() {
  }

  public ArmorEntry(@NonNull String name, boolean equipped) {
    this.name = name;
    this.equipped = equipped;
  }
}
