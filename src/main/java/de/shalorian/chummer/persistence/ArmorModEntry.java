package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

/** Gespeicherter Einbau eines Rüstungsteils: Name aus {@code armor_mods.xml} und Stufe ({@code 0} ohne Stufe). */
class ArmorModEntry {

  public @NonNull String name = "";
  public int rating = 0;

  public ArmorModEntry() {
  }

  public ArmorModEntry(@NonNull String name, int rating) {
    this.name = name;
    this.rating = rating;
  }
}
