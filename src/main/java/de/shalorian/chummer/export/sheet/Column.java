package de.shalorian.chummer.export.sheet;

import org.jspecify.annotations.NonNull;

/** Tabellenspalte des Bogens: Überschrift, relative Breite (die Tabelle verteilt die Gewichte) und Ausrichtung. */
record Column(@NonNull String header, float weight, @NonNull ColumnKind kind) {

  static @NonNull Column text(@NonNull String header, float weight) {
    return new Column(header, weight, ColumnKind.TEXT);
  }

  static @NonNull Column centered(@NonNull String header, float weight) {
    return new Column(header, weight, ColumnKind.CENTERED);
  }
}
