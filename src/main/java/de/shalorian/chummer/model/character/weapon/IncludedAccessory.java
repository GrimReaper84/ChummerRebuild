package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Mit der Waffe geliefertes (eingebautes) Zubehör laut Waffentext, z.B. "Smartgunsystem" beim Ares
 * Predator IV (GRW S. 362). SR4-Grundregelwerk S. 357: Ab Werk eingebautes Zubehör belegt keine
 * Halterung. Seine RK steckt bereits im Tabellenwert der Waffe und
 * wird nicht erneut addiert. Nur zur Anzeige und für die Regel "höchstens eines je Waffe".
 *
 * @param name Bezeichnung wie im Waffentext (nicht zwingend ein Katalogname, z.B. das interne
 *             Gasventilsystem der Ingram White Knight mit 5 RK)
 * @param rule Sonderregel, wenn sie für weiteres Zubehör zählt (Gasventil, Smartgun, Schockpolster),
 *             sonst {@code null}
 */
public record IncludedAccessory(@NonNull String name, @Nullable AccessoryRule rule) {
}
