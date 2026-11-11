package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;

/**
 * Zeilen der Tabelle <i>Waffenreichweiten</i> (SR4-Grundregelwerk S. 179, gegen das PDF-Bild
 * verifiziert). Der Anzeigename ist zugleich der Wert von {@code <range>} in {@code weapons.xml};
 * welche Zeile eine Waffe nutzt, steht dort je Waffe, weil das GRW einzelnen Waffen eine andere Zeile
 * als ihrer Kategorie zuweist (z.B. S. 362 Hämmerli 620S mit den Reichweiten
 * schwerer Pistolen; S. 365 Parashield Pfeilgewehr mit denen eines Sportgewehrs).
 *
 * <p>Bei Bögen und Wurfwaffen sind die Werte Vielfache der Stärke ("bis STR x 10"), beim Bogen der
 * Mindeststärke (S. 180). Granatwerfer haben eine Minimalreichweite von 5 Metern (S. 184), Raketenwerfer beginnen laut Tabelle bei 20 Metern.
 *
 * <p>Nicht enthalten: die Zeilen für geworfene Granaten (Granaten kommen mit dem Gear-Feature).
 */
public enum WeaponRange {
  HOLDOUT_PISTOL("Holdout-Pistole", 0, 5, 15, 30, 50, false),
  LIGHT_PISTOL("Leichte Pistole", 0, 5, 15, 30, 50, false),
  HEAVY_PISTOL("Schwere Pistole", 0, 5, 20, 40, 60, false),
  TASER("Taser", 0, 5, 10, 15, 20, false),
  MACHINE_PISTOL("Automatikpistole", 0, 5, 15, 30, 50, false),
  SUBMACHINE_GUN("Maschinenpistole", 0, 10, 40, 80, 150, false),
  ASSAULT_RIFLE("Sturmgewehr", 0, 50, 150, 350, 550, false),
  SNIPER_RIFLE("Scharfschützengewehr", 0, 150, 350, 800, 1500, false),
  SHOTGUN_FLECHETTE("Schrotflinte (Flechette)", 0, 10, 25, 40, 60, false),
  SHOTGUN_SLUG("Schrotflinte (Massivgeschosse)", 0, 10, 40, 80, 150, false),
  SPORTS_RIFLE("Sportgewehr", 0, 100, 250, 500, 750, false),
  GRENADE_LAUNCHER("Granatwerfer", 5, 50, 100, 150, 500, false),
  LIGHT_MG("Leichtes Maschinengewehr", 0, 75, 200, 400, 800, false),
  MEDIUM_HEAVY_MG("Mittleres/Schweres Maschinengewehr", 0, 80, 250, 750, 1200, false),
  MISSILE_LAUNCHER("Raketenwerfer", 20, 70, 150, 450, 1500, false),
  ASSAULT_CANNON("Sturmkanone", 0, 100, 300, 750, 1500, false),
  BOW("Bogen", 0, 1, 10, 30, 60, true),
  LIGHT_CROSSBOW("Leichte Armbrust", 0, 6, 24, 60, 120, false),
  MEDIUM_CROSSBOW("Mittlere Armbrust", 0, 9, 36, 90, 150, false),
  HEAVY_CROSSBOW("Schwere Armbrust", 0, 15, 45, 120, 180, false),
  SHURIKEN("Shuriken", 0, 1, 2, 5, 7, true),
  THROWING_KNIFE("Geworfene Messer", 0, 1, 2, 3, 5, true);

  private final @NonNull String displayName;
  private final int minimum;
  private final int shortMax;
  private final int mediumMax;
  private final int longMax;
  private final int extremeMax;
  private final boolean strengthBased;

  /**
   * @param strengthBased ob die Obergrenzen Faktoren der Stärke sind ("bis STR x N") statt Meter
   */
  WeaponRange(@NonNull String displayName, int minimum, int shortMax, int mediumMax, int longMax,
      int extremeMax, boolean strengthBased) {
    this.displayName = displayName;
    this.minimum = minimum;
    this.shortMax = shortMax;
    this.mediumMax = mediumMax;
    this.longMax = longMax;
    this.extremeMax = extremeMax;
    this.strengthBased = strengthBased;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob die Reichweite von der Stärke (beim Bogen: Mindeststärke) abhängt. */
  public boolean isStrengthBased() {
    return strengthBased;
  }

  /**
   * Reichweitenstufen in Metern.
   *
   * @param strength Stärke des Werfers bzw. Mindeststärke des Bogens; nur bei {@link
   *                 #isStrengthBased()} relevant
   */
  public @NonNull RangeBands bands(int strength) {
    int factor = strengthBased ? strength : 1;
    return new RangeBands(minimum, shortMax * factor, mediumMax * factor, longMax * factor,
        extremeMax * factor);
  }

  /**
   * Liest die Tabellenzeile aus {@code <range>} in {@code weapons.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull WeaponRange fromDisplayName(@NonNull String displayName) {
    for (WeaponRange range : values()) {
      if (range.displayName.equals(displayName)) {
        return range;
      }
    }
    throw new IllegalArgumentException("Unbekannte Reichweitenzeile: \"" + displayName + "\"");
  }
}
