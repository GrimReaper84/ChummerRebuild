package de.shalorian.chummer.view.dialog;

import org.jspecify.annotations.NonNull;

/**
 * Ergebnis des Dialogs „Exotische Fertigkeit hinzufügen“.
 *
 * @param base   exotische Grundfertigkeit
 * @param target Waffe bzw. Fahrzeug
 */
public record ExoticSkillAddResult(@NonNull String base, @NonNull String target) {
}
