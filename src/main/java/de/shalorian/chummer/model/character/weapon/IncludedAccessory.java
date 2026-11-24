package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Mit der Waffe geliefertes (eingebautes) Zubehör laut Waffentext, z.B. "Smartgunsystem" beim Ares
 * Predator IV (GRW S. 362). SR4-Grundregelwerk S. 357: Ab Werk eingebautes Zubehör belegt keine
 * Halterung. Seine RK steckt bereits im Tabellenwert der Waffe und wird nicht erneut addiert; wird das
 * Teil ersetzt oder ausgebaut ({@link Weapon#getIncluded()}), zieht die Waffe {@code rc} wieder ab.
 *
 * @param name        Bezeichnung wie im Waffentext (nicht zwingend ein Katalogname, z.B. das interne
 *                    Gasventilsystem der Ingram White Knight mit 5 RK)
 * @param rule        Sonderregel mit Wirkung (Gasventil, Smartgun, Schockpolster, Lasermarkierer), sonst
 *                    {@code null}
 * @param rc          RK des Teils im Tabellenwert: fest bei {@link AccessoryRule#isPermanent() fest
 *                    eingebauten} Teilen (Gasventil), sonst im Klammerwert; 0 ohne RK
 * @param replaceable ob ein Teil derselben Regel es ersetzen darf ({@code false} beim internen Gasventil der
 *                    White Knight, das laut Waffentext nicht aufwertbar ist)
 */
public record IncludedAccessory(@NonNull String name, @Nullable AccessoryRule rule, int rc, boolean replaceable) {

  /** Ob die RK fest eingebaut ist (Wert außerhalb der Klammer, siehe {@link Recoil}). */
  public boolean isPermanent() {
    return rule != null && rule.isPermanent();
  }
}
