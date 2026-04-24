package de.shalorian.chummer.model.character;

import java.time.LocalDate;

public record LogEntry(String description, int amount, LocalDate date) {}