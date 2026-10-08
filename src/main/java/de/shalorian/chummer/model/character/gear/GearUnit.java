package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Einheit, in der die Menge eines Gegenstands gezählt wird, mit Einzahl und Mehrzahl für die Anzeige
 * ("1 Dosis", "3 Dosen"). Die Einheiten stammen aus den Preisangaben der GRW-Tabellen: "pro Dosis"
 * (S. 382), "pro 100 m" (S. 383), "pro m²" (S. 395), "pro Jahr" (S. 384), "pro Simchip" (S. 377),
 * "Munition pro 10 Schuss" (S. 369), "Sprengstoffe, pro Kilogramm" (S. 371).
 */
public enum GearUnit {
  PIECE("Stück", "Stück"),
  DOSE("Dosis", "Dosen"),
  METER("m", "m"),
  SQUARE_METER("m²", "m²"),
  YEAR("Jahr", "Jahre"),
  SIMCHIP("Simchip", "Simchips"),
  ROUND("Schuss", "Schuss"),
  KILOGRAM("kg", "kg");

  private final @NonNull String singular;
  private final @NonNull String plural;

  GearUnit(@NonNull String singular, @NonNull String plural) {
    this.singular = singular;
    this.plural = plural;
  }

  /** Einzahl, z.B. "Dosis" (für "pro Dosis"). */
  public @NonNull String getSingular() {
    return singular;
  }

  /** Mehrzahl, z.B. "Dosen" (Beschriftung neben einer Mengenauswahl). */
  public @NonNull String getPlural() {
    return plural;
  }

  /** Menge mit Einheit, z.B. "1 Dosis", "3 Dosen", "200 m". */
  public @NonNull String format(int quantity) {
    return quantity + " " + (quantity == 1 ? singular : plural);
  }

  /**
   * Liest die Einheit aus {@code <unit>} in {@code gear.xml} (Einzahl); leer bedeutet Stück.
   *
   * @throws IllegalArgumentException bei unbekannter Einheit (Datenfehler im Katalog)
   */
  public static @NonNull GearUnit fromSingular(@NonNull String singular) {
    if (singular.isEmpty()) {
      return PIECE;
    }
    for (GearUnit unit : values()) {
      if (unit.singular.equals(singular)) {
        return unit;
      }
    }
    throw new IllegalArgumentException("Unbekannte Einheit: \"" + singular + "\"");
  }
}
