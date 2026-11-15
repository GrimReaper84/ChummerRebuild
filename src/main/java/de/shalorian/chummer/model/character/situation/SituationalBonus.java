package de.shalorian.chummer.model.character.situation;

import org.jspecify.annotations.NonNull;

/**
 * Bonus (oder Malus), der nur in einer bestimmten Lage gilt.
 *
 * @param source Herkunft für die Aufschlüsselung, z.B. „Hai“, „Zauberspruchfokus 3“, „Kampfsinn“
 * @param kind   Anlass
 * @param detail Zauberkategorie, Geisterart oder Fertigkeit; leer, wenn der Anlass keins braucht
 * @param dice   Würfel, negativ bei einem Nachteil
 * @param focus  ob der Bonus von einem Fokus stammt; je Probe zählt nur der stärkste Fokus (S. 233)
 */
public record SituationalBonus(@NonNull String source, @NonNull SituationKind kind, @NonNull String detail, int dice,
                               boolean focus) {
}
