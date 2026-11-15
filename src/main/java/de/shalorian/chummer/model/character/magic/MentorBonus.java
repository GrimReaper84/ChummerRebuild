package de.shalorian.chummer.model.character.magic;

import de.shalorian.chummer.model.character.situation.SituationKind;
import org.jspecify.annotations.NonNull;

/**
 * Rechnender Vorteil (Würfel positiv) oder Nachteil (negativ) eines Schutzpatrons (GRW S. 234–237).
 *
 * @param detail Zauberkategorie, Geisterart oder Fertigkeit; leer, wenn der Anlass keins braucht
 */
public record MentorBonus(@NonNull SituationKind kind, @NonNull String detail, int dice) {
}
