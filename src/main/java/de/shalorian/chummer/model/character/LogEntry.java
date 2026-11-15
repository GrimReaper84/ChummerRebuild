package de.shalorian.chummer.model.character;

import java.time.LocalDate;
import org.jspecify.annotations.NonNull;

/**
 * Ein Eintrag im Karma- bzw. Nuyen-Log: Beschreibung, Betrag (Ausgabe negativ), Datum und Art.
 */
public record LogEntry(@NonNull String description, int amount, @NonNull LocalDate date,
                       @NonNull LogCategory category) {
}
