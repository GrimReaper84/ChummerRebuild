package de.shalorian.chummer.model.character.weapon;

import static de.shalorian.chummer.model.character.weapon.WeaponCategory.ASSAULT_CANNONS;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.ASSAULT_RIFLES;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.GRENADE_LAUNCHERS;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.HEAVY_MG;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.LIGHT_MG;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.MACHINE_PISTOLS;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.MEDIUM_MG;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.MISSILE_LAUNCHERS;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.SHOTGUNS;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.SNIPER_RIFLES;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.SPORTS_RIFLES;
import static de.shalorian.chummer.model.character.weapon.WeaponCategory.SUBMACHINE_GUNS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Sonderregel eines Zubehörteils laut den Zubehörtexten des SR4-Grundregelwerks (S. 367/368), als
 * Enum statt Namensvergleich. Jede Regel prüft, ob eine Waffe das Teil aufnehmen kann, und liefert
 * sonst einen Grund für die Anzeige. Der Name ist zugleich der Wert von {@code <rule>} in {@code
 * weapon_accessories.xml} bzw. des Attributs {@code rule} an {@code <included>} in {@code weapons.xml}.
 */
public enum AccessoryRule {

  /**
   * S. 367: "Gasventilsysteme können in Automatikpistolen, Maschinenpistolen, Sturmgewehre und
   * Maschinengewehre eingebaut werden. … Einmal installierte Gasventilsysteme können nicht mehr
   * entfernt werden. Waffen, die bereits ein eingebautes Gasventilsystem besitzen, können nicht mit
   * einem weiteren ausgerüstet werden."
   */
  GAS_VENT("nur Automatikpistolen, Maschinenpistolen, Sturmgewehre und Maschinengewehre",
      true, true),

  /**
   * S. 367: "Ein Schalldämpfer kann nur bei Einzelschuss- oder halbautomatischen Waffen (und nicht bei
   * Revolvern) benutzt werden." Revolver = Munition aus der Trommel {@code (tr)} (S. 368 Schnelllader).
   */
  SUPPRESSOR("nur Einzelschuss- oder halbautomatische Waffen, keine Revolver", false, false),

  /** S. 367: "speziell für Salvenmodus- und vollautomatische Waffen entwickelt". */
  SOUND_SUPPRESSOR("nur Waffen mit Salvenmodus oder Vollautomatik", false, false),

  /**
   * S. 368: "kann auf die feste Schulterstütze eines Gewehrs, einer Schrotflinte oder einer schweren
   * Waffe aufgebracht werden". Höchstens eines je Waffe, da es nur eine Schulterstütze gibt.
   */
  SHOCK_PAD("nur Gewehre, Schrotflinten und schwere Waffen", true, false),

  /**
   * S. 367: "ein beweglicher Gyrostabilisatorarm für ein Gewehr oder eine schwere Waffe". Anders als
   * beim Schockpolster nennt der Text keine Schrotflinten.
   */
  GYROSTABILIZER("nur Gewehre und schwere Waffen", false, false),

  /**
   * Smartgunsystem, intern oder extern (S. 368). Eine Waffe hat höchstens eines — analog zur
   * Gasventil-Regel S. 367 (Nutzerentscheidung 2026-09-30, {@code waffen_plan.md} Abschnitt 13).
   */
  SMARTGUN("", true, false),

  /** S. 367: "Dieses Smartgun-Zubehör für Granat-/Raketenwerfer". */
  AIRBURST("nur Granat- und Raketenwerfer", false, false),

  /** S. 367: "für das schnelle Nachladen eines Revolvers (Pistolen mit einer Trommel)". */
  SPEEDLOADER("nur Revolver (Munition aus der Trommel)", false, false),

  /** S. 367: zusätzliche Ladestreifen, also nur Waffen mit Ladestreifen {@code (s)} (S. 357). */
  SPARE_CLIP("nur Waffen mit Ladestreifen", false, false);

  private final @NonNull String restriction;
  private final boolean onePerWeapon;
  private final boolean permanent;

  AccessoryRule(@NonNull String restriction, boolean onePerWeapon, boolean permanent) {
    this.restriction = restriction;
    this.onePerWeapon = onePerWeapon;
    this.permanent = permanent;
  }

  /** Ob die Waffe dieses Zubehör laut Regeltext aufnehmen kann (Halterungen nicht berücksichtigt). */
  public boolean allows(@NonNull WeaponData weapon) {
    WeaponCategory category = weapon.category();
    return switch (this) {
      case GAS_VENT -> EnumSet.of(MACHINE_PISTOLS, SUBMACHINE_GUNS, ASSAULT_RIFLES, LIGHT_MG,
          MEDIUM_MG, HEAVY_MG).contains(category);
      case SUPPRESSOR -> hasMode(weapon, "EM", "HM") && !isRevolver(weapon);
      case SOUND_SUPPRESSOR -> hasMode(weapon, "SM", "AM");
      case SHOCK_PAD -> category == SHOTGUNS || isRifleOrHeavy(category);
      case GYROSTABILIZER -> isRifleOrHeavy(category);
      case SMARTGUN -> true;
      case AIRBURST -> category == GRENADE_LAUNCHERS || category == MISSILE_LAUNCHERS;
      case SPEEDLOADER -> isRevolver(weapon);
      case SPARE_CLIP -> weapon.ammo().contains("(s)");
    };
  }

  /** Einschränkung als Anzeigetext, z.B. "nur Granat- und Raketenwerfer". */
  public @NonNull String getRestriction() {
    return restriction;
  }

  /** Ob eine Waffe höchstens ein Teil mit dieser Regel haben kann, eingebautes Zubehör eingeschlossen. */
  public boolean isOnePerWeapon() {
    return onePerWeapon;
  }

  /**
   * Ob das Teil nach dem Einbau nicht mehr entfernt werden kann; seine RK zählt dann als fest
   * eingebaut (Wert außerhalb der Klammer, siehe {@link Recoil}).
   */
  public boolean isPermanent() {
    return permanent;
  }

  /** Sturm-, Sport- und Scharfschützengewehre sowie alle schweren Waffen (MGs bis Raketenwerfer). */
  private static boolean isRifleOrHeavy(@NonNull WeaponCategory category) {
    return EnumSet.of(ASSAULT_RIFLES, SPORTS_RIFLES, SNIPER_RIFLES, LIGHT_MG, MEDIUM_MG, HEAVY_MG,
        ASSAULT_CANNONS, GRENADE_LAUNCHERS, MISSILE_LAUNCHERS).contains(category);
  }

  /** Revolver = Munition aus der Trommel {@code (tr)} (GRW S. 357, 367). */
  private static boolean isRevolver(@NonNull WeaponData data) {
    return data.ammo().contains("(tr)");
  }

  /** Ob die Waffe einen der Feuermodi hat (Modus-Spalte z.B. "HM/SM/AM"). */
  private static boolean hasMode(@NonNull WeaponData data, String... modes) {
    List<String> weaponModes = Arrays.asList(data.mode().split("/"));
    return Arrays.stream(modes).anyMatch(weaponModes::contains);
  }
}
