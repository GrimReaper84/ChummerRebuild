package de.shalorian.chummer.view.grid;

import org.jspecify.annotations.NonNull;

/**
 * Anzeigedaten einer Attribut-Zeile ({@code de.shalorian.chummer.view.tab.AttributeTabBuilder.addAttributeGridRow}), unabhängig davon ob sie von
 * einem Standard- oder Sonderattribut stammen.
 */
public record AttributeRowLabel(
    @NonNull String displayName, @NonNull String abbreviation, int min, int max) {

}
