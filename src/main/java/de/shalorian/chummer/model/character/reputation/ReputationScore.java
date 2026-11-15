package de.shalorian.chummer.model.character.reputation;

import org.jspecify.annotations.NonNull;

/**
 * Ein Wert der Reputation mit seiner Herleitung für den Tooltip.
 *
 * @param value     Wert
 * @param breakdown Rechenweg in Kurzform, z.B. „verdientes Karma 35 ÷ 10 = 4“
 */
public record ReputationScore(int value, @NonNull String breakdown) {
}
