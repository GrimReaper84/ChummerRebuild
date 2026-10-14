package de.shalorian.chummer.persistence;

import org.jspecify.annotations.Nullable;

/** Waffenhalterung eines Fahrzeugs in der Speicherdatei. */
class MountEntry {

  /** Ob die Halterung mitgeliefert ist (wird beim Laden aus dem Katalog erzeugt, nicht gekauft). */
  public boolean included = false;
  /** Eingesetzte Waffe (ohne Zubehör), oder {@code null} bei leerer Halterung. */
  public @Nullable WeaponEntry weapon;

  public MountEntry() {
    // Für Jackson-Deserialisierung benötigt.
  }

  public MountEntry(boolean included, @Nullable WeaponEntry weapon) {
    this.included = included;
    this.weapon = weapon;
  }
}
