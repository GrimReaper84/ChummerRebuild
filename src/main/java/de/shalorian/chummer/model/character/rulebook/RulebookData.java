package de.shalorian.chummer.model.character.rulebook;

import de.shalorian.chummer.model.character.CatalogEntry;
import org.jspecify.annotations.NonNull;

/**
 * Ein Regelwerk aus {@code books.xml}.
 *
 * @param code Kürzel, auf das die Kataloge mit {@code <source>} verweisen
 * @param name Anzeigename, z.B. „Grundregelwerk“
 * @param core das Grundregelwerk: immer aktiv, nicht abschaltbar
 */
public record RulebookData(@NonNull String code, @NonNull String name, boolean core) implements CatalogEntry {
}
