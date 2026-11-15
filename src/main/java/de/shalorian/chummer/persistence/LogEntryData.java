package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class LogEntryData {

  public @NonNull String description = "";
  public int amount = 0;
  public @NonNull String date = ""; // ISO-Format: "2026-04-24"
  /** Name der {@code LogCategory}; fehlt in Dateien vor ADR-058 (dann „Manuell“). */
  public @NonNull String category = "MANUAL";

  @SuppressWarnings("unused") // Für Jackson-Deserialisierung benötigt
  public LogEntryData() {
  }

  public LogEntryData(@NonNull String description, int amount, @NonNull String date, @NonNull String category) {
    this.description = description;
    this.amount = amount;
    this.date = date;
    this.category = category;
  }
}
