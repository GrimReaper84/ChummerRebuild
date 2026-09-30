package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;

/**
 * Waffenkategorie laut den Tabellenüberschriften des SR4-Grundregelwerks (Nahkampf-, Projektil- und
 * Wurfwaffen S. 361, Feuerwaffen S. 363 und S. 366), in Tabellenreihenfolge. Der Anzeigename ist
 * zugleich der Wert von {@code <category>} in {@code weapons.xml}.
 */
public enum WeaponCategory {
  BLADES("Klingenwaffen", true, "Klingenwaffen"),
  CLUBS("Knüppel", true, "Knüppel"),
  EXOTIC_MELEE("Exotische Nahkampfwaffen", true, "Exotische Nahkampfwaffe"),
  UNARMED("Waffenlos", true, "Waffenloser Kampf"),
  BOWS("Bögen", false, "Projektilwaffen"),
  CROSSBOWS("Armbrüste", false, "Projektilwaffen"),
  THROWING("Wurfwaffen", false, "Wurfwaffen"),
  TASERS("Taser", false, "Pistolen"),
  HOLDOUTS("Holdout-Pistolen", false, "Pistolen"),
  LIGHT_PISTOLS("Leichte Pistolen", false, "Pistolen"),
  HEAVY_PISTOLS("Schwere Pistolen", false, "Pistolen"),
  MACHINE_PISTOLS("Automatikpistolen", false, "Schnellfeuerwaffen"),
  SUBMACHINE_GUNS("Maschinenpistolen", false, "Schnellfeuerwaffen"),
  ASSAULT_RIFLES("Sturmgewehre", false, "Schnellfeuerwaffen"),
  SPORTS_RIFLES("Sportgewehre", false, "Gewehre"),
  SNIPER_RIFLES("Scharfschützengewehre", false, "Gewehre"),
  SHOTGUNS("Schrotflinten", false, "Gewehre"),
  SPECIAL("Spezielle Waffen", false, "Exotische Fernkampfwaffe"),
  LIGHT_MG("Leichte Maschinengewehre", false, "Schwere Waffen"),
  MEDIUM_MG("Mittlere Maschinengewehre", false, "Schwere Waffen"),
  HEAVY_MG("Schwere Maschinengewehre", false, "Schwere Waffen"),
  ASSAULT_CANNONS("Sturmkanonen", false, "Schwere Waffen"),
  GRENADE_LAUNCHERS("Granatwerfer", false, "Schwere Waffen"),
  MISSILE_LAUNCHERS("Raketenwerfer", false, "Schwere Waffen");

  private final @NonNull String displayName;
  private final boolean melee;
  private final @NonNull String skillName;

  WeaponCategory(@NonNull String displayName, boolean melee, @NonNull String skillName) {
    this.displayName = displayName;
    this.melee = melee;
    this.skillName = skillName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob Waffen dieser Kategorie Nahkampfwaffen mit Reichweite (statt Fernkampf-Reichweiten) sind. */
  public boolean isMelee() {
    return melee;
  }

  /**
   * Name der aktiven Kampffertigkeit, mit der Waffen dieser Kategorie geführt bzw. abgefeuert werden
   * (Fertigkeiten GRW S. 147/148, Zuordnung laut Kategorietexten S. 360–366, z.B. S. 362 "Taser …
   * werden mit der Fertigkeit Pistolen abgefeuert", S. 363 "Maschinenpistolen werden mit der
   * Fertigkeit Schnellfeuerwaffen abgefeuert", S. 365 "Schrotflinten werden mit der Fertigkeit
   * Gewehre abgefeuert", S. 365 "Diese Waffen werden mit der Fertigkeit Schwere Waffen abgefeuert").
   * Bögen und Armbrüste nutzen Projektilwaffen, Wurfwaffen die gleichnamige Fertigkeit (S. 148).
   *
   * <p><b>Einschränkung:</b> Laut GRW S. 365 benötigt jede Spezielle Waffe "eine auf sie
   * zugeschnittene Fertigkeit Exotische Fernkampfwaffe", und Exotische Nahkampfwaffen müssen ebenfalls
   * je Waffe gelernt werden (S. 148). Das Fertigkeitsmodell kennt bisher nur je eine gemeinsame
   * exotische Fertigkeit; deren Wert gilt hier für alle exotischen Waffen.
   */
  public @NonNull String skillName() {
    return skillName;
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
