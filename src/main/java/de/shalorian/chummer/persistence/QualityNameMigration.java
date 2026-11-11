package de.shalorian.chummer.persistence;

import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Übersetzt Quality-Namen aus Speicherdateien vor der Umstellung auf die GRW-Namen (2026-11-12, Nutzerentscheidung:
 * übersetzen statt verwerfen). Einträge ohne GRW-Gegenstück (z.B. Analytischer Verstand) bleiben unübersetzt und
 * werden beim Laden übersprungen.
 */
final class QualityNameMigration {

  private static final @NonNull Map<String, String> OLD_TO_GRW = Map.ofEntries(
      Map.entry("Magier", "Zauberer"),
      Map.entry("Gebrechlich", "Körperliche Schwäche"),
      Map.entry("Glückspilz", "Glück"),
      Map.entry("Pechvogel", "Unglück"),
      Map.entry("Beidhändig", "Beidhändigkeit"),
      Map.entry("Natürliche Abhärtung", "Resistenz gegen Signalspitzen"),
      Map.entry("SÜNder", "SIN-Mensch (normale SIN)"),
      Map.entry("SÜNder (National)", "SIN-Mensch (normale SIN)"),
      Map.entry("SÜNder (Global)", "SIN-Mensch (normale SIN)"),
      Map.entry("Abhängigkeit (Mild)", "Abhängigkeit (Leicht)"),
      Map.entry("Abhängigkeit (Mäßig)", "Abhängigkeit (Mittel)"),
      Map.entry("Abhängigkeit (Stark)", "Abhängigkeit (Schwer)"),
      Map.entry("Allergie (Ungewöhnlich, Mild)", "Allergie (Ungewöhnlich, Leicht)"),
      Map.entry("Allergie (Häufig, Mild)", "Allergie (Gewöhnlich, Leicht)"),
      Map.entry("Allergie (Häufig, Schwer)", "Allergie (Gewöhnlich, Schwer)"),
      Map.entry("Allergie (Verbreitet, Mild)", "Allergie (Gewöhnlich, Leicht)"),
      Map.entry("Allergie (Verbreitet, Schwer)", "Allergie (Gewöhnlich, Schwer)"));

  private QualityNameMigration() {
  }

  /** Aktueller Katalogname zu einem gespeicherten Namen; unbekannte Namen unverändert. */
  static @NonNull String currentName(@NonNull String storedName) {
    return OLD_TO_GRW.getOrDefault(storedName, storedName);
  }
}
