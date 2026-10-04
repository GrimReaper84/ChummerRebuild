package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;

/**
 * Halterung für Waffenzubehör. SR4-Grundregelwerk S. 357 (<i>Ausrüstungswerte</i>): "Gibt an, wo an
 * einer Waffe Zubehör angebracht werden kann: Unterlauf, Lauf oder aufmontiert. An jeder Halterung kann
 * nur jeweils ein Zubehörteil montiert werden." Der Anzeigename ist zugleich der Wert von {@code
 * <mount>} in {@code weapon_accessories.xml}.
 */
public enum Mount {
  BARREL("Lauf"),
  UNDERBARREL("Unterlauf"),
  TOP("Aufmontiert");

  private final @NonNull String displayName;

  Mount(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /**
   * Liest eine Halterung aus {@code weapon_accessories.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull Mount fromDisplayName(@NonNull String displayName) {
    for (Mount mount : values()) {
      if (mount.displayName.equals(displayName)) {
        return mount;
      }
    }
    throw new IllegalArgumentException("Unbekannte Halterung: \"" + displayName + "\"");
  }
}
