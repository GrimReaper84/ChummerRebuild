package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.Nullable;

/**
 * Ob und mit welcher Stufe eine Talentsoft gerade wirkt (GRW S. 377, 389).
 *
 * @param rating  nutzbare Stufe, 0 wenn sie nicht wirkt
 * @param problem warum sie nicht wirkt (z.B. „nicht geladen“), sonst {@code null}
 */
public record SkillsoftStatus(int rating, @Nullable String problem) {
}
