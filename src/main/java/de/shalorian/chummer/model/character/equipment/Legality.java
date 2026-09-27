package de.shalorian.chummer.model.character.equipment;

import org.jspecify.annotations.NonNull;

/**
 * Legalitätsstufe eines Ausrüstungsgegenstands, im GRW als Suffix an der Verfügbarkeit notiert
 * (z.B. "8E" = eingeschränkt, "18V" = verboten; SR4-Grundregelwerk S. 357ff., Verfügbarkeit und
 * Einkauf). Wirkt sich im aktuellen Feature-Schnitt nur auf die Anzeige aus.
 */
public enum Legality {
  LEGAL(""),
  RESTRICTED("E"),
  FORBIDDEN("V");

  private final @NonNull String code;

  Legality(@NonNull String code) {
    this.code = code;
  }

  /** GRW-Kürzel ({@code "E"}, {@code "V"}), bzw. leer bei legalen Gegenständen. */
  public @NonNull String getCode() {
    return code;
  }

  /**
   * Liest das Kürzel aus den Katalog-XMLs ({@code <legality>}). Leer bzw. fehlend bedeutet legal.
   *
   * @throws IllegalArgumentException bei unbekanntem Kürzel (Datenfehler im Katalog)
   */
  public static @NonNull Legality fromCode(@NonNull String code) {
    String trimmed = code.trim();
    for (Legality legality : values()) {
      if (legality.code.equals(trimmed)) {
        return legality;
      }
    }
    throw new IllegalArgumentException("Unbekannte Legalität: \"" + code + "\"");
  }
}
