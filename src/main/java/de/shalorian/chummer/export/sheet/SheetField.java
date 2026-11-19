package de.shalorian.chummer.export.sheet;

import org.jspecify.annotations.NonNull;

/** Beschriftetes Feld des Bogens; ein leerer Wert ist ein Bleistiftfeld. */
record SheetField(@NonNull String label, @NonNull String value) {
}
