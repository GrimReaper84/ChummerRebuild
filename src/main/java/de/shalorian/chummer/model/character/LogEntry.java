package de.shalorian.chummer.model.character;

import java.time.LocalDate;
import org.jspecify.annotations.NonNull;

public record LogEntry(@NonNull String description, int amount, @NonNull LocalDate date) {

}