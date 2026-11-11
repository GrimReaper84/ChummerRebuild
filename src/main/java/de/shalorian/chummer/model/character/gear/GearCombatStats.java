package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Kampfwerte aus den Tabellen für Munition (S. 369) bzw. Granaten, Raketen und Lenkraketen (S. 371),
 * als GRW-Text, nur zur Anzeige. Die Wirkung der Munition auf die Waffenwerte und der Einsatz von
 * Granaten im Kampf sind nicht modelliert ({@code docs/plaene/archiv/gear_plan.md} Abschnitt 13).
 *
 * @param damage        Schaden (Granaten: "10K") bzw. Schadensmodifikator (Munition: "+1", "(−1)G")
 * @param armorPiercing PB bzw. PB-Modifikator, z.B. "−4", "−Hälfte"
 * @param blast         Sprengwirkung (Granaten, Raketen: "10 m Radius", "−1/m"), sonst leer
 * @param armorType     verwendete Panzerung (Munition: "Ballistisch", "Stoß"), sonst leer
 */
public record GearCombatStats(@NonNull String damage, @NonNull String armorPiercing,
                              @NonNull String blast, @NonNull String armorType) {
}
