package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.weapon.Weapon;
import org.jspecify.annotations.Nullable;

/**
 * Eine Waffenhalterung eines Fahrzeugs mit höchstens einer eingesetzten Waffe. SR4-Grundregelwerk
 * S. 395: eine Waffe bis LMG und 250 Schuss. Mitgelieferte Halterungen (laut Fahrzeugtext, z.B. beim GM-Nissan Doberman) sind im
 * Fahrzeugpreis enthalten und lassen sich nicht ausbauen; Waffen setzt {@link Vehicle} ein, das die
 * Regeln prüft.
 */
public class WeaponMount {

  private final boolean included;
  private @Nullable Weapon weapon;

  WeaponMount(boolean included) {
    this.included = included;
  }

  /** Ob die Halterung mit dem Fahrzeug geliefert wurde (kostenlos, nicht ausbaubar). */
  public boolean isIncluded() {
    return included;
  }

  /** Die eingesetzte Waffe, oder {@code null} bei einer leeren Halterung. */
  public @Nullable Weapon getWeapon() {
    return weapon;
  }

  void setWeapon(@Nullable Weapon weapon) {
    this.weapon = weapon;
  }
}
