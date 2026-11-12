package de.shalorian.chummer.model.character.skill;

import org.jspecify.annotations.NonNull;

/**
 * Ergebnis einer Würfelpool-Berechnung.
 *
 * @param dice      Anzahl Würfel, nie negativ; Verletzungsmodifikator bereits eingerechnet
 * @param breakdown Aufschlüsselung für den Tooltip, z.B. "Pistolen 4 + Geschicklichkeit 5 − 1 Verletzung"
 */
public record DicePool(int dice, @NonNull String breakdown) {}
