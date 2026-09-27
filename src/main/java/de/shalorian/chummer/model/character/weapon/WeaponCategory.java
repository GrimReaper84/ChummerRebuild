package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;

/**
 * Waffenkategorie laut den Tabellenüberschriften des SR4-Grundregelwerks (Nahkampf-, Projektil- und
 * Wurfwaffen S. 361, Feuerwaffen S. 363 und S. 366), in Tabellenreihenfolge. Der Anzeigename ist
 * zugleich der Wert von {@code <category>} in {@code weapons.xml}.
 */
public enum WeaponCategory {
  BLADES("Klingenwaffen", true),
  CLUBS("Knüppel", true),
  EXOTIC_MELEE("Exotische Nahkampfwaffen", true),
  UNARMED("Waffenlos", true),
  BOWS("Bögen", false),
  CROSSBOWS("Armbrüste", false),
  THROWING("Wurfwaffen", false),
  TASERS("Taser", false),
  HOLDOUTS("Holdout-Pistolen", false),
  LIGHT_PISTOLS("Leichte Pistolen", false),
  HEAVY_PISTOLS("Schwere Pistolen", false),
  MACHINE_PISTOLS("Automatikpistolen", false),
  SUBMACHINE_GUNS("Maschinenpistolen", false),
  ASSAULT_RIFLES("Sturmgewehre", false),
  SPORTS_RIFLES("Sportgewehre", false),
  SNIPER_RIFLES("Scharfschützengewehre", false),
  SHOTGUNS("Schrotflinten", false),
  SPECIAL("Spezielle Waffen", false),
  LIGHT_MG("Leichte Maschinengewehre", false),
  MEDIUM_MG("Mittlere Maschinengewehre", false),
  HEAVY_MG("Schwere Maschinengewehre", false),
  ASSAULT_CANNONS("Sturmkanonen", false),
  GRENADE_LAUNCHERS("Granatwerfer", false),
  MISSILE_LAUNCHERS("Raketenwerfer", false);

  private final @NonNull String displayName;
  private final boolean melee;

  WeaponCategory(@NonNull String displayName, boolean melee) {
    this.displayName = displayName;
    this.melee = melee;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob Waffen dieser Kategorie Nahkampfwaffen mit Reichweite (statt Fernkampf-Reichweiten) sind. */
  public boolean isMelee() {
    return melee;
  }

  /**
   * Liest die Kategorie aus {@code <category>} in {@code weapons.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull WeaponCategory fromDisplayName(@NonNull String displayName) {
    for (WeaponCategory category : values()) {
      if (category.displayName.equals(displayName)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unbekannte Waffenkategorie: \"" + displayName + "\"");
  }
}
