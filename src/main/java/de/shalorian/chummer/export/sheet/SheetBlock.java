package de.shalorian.chummer.export.sheet;

import java.util.List;
import org.jspecify.annotations.NonNull;

/** Ein Attributblock des Bogens mit Titel und Feldern (z.B. „Körperliche Attribute“). */
record SheetBlock(@NonNull String title, @NonNull List<SheetField> fields) {
}
