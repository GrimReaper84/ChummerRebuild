package de.shalorian.chummer.model.character.lifestyle;

import org.jspecify.annotations.NonNull;

/**
 * Ergebnis eines ausgesetzten Lebensstil-Monats (GRW S. 312).
 *
 * @param roll         geworfene Augenzahl (1–6)
 * @param missedMonths ausgesetzte Monate in Folge, dieser eingeschlossen
 * @param downgraded   ob der Lebensstil um eine Stufe gesunken ist
 * @param debtAdded    neue Schulden in Nuyen (ein Monat der früheren Stufe), sonst 0
 * @param level        Stufe nach dem Wurf
 */
public record MissedPaymentResult(int roll, int missedMonths, boolean downgraded, int debtAdded,
    @NonNull LifestyleLevel level) {
}
