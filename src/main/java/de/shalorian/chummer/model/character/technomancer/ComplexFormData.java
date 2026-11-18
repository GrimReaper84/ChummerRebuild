package de.shalorian.chummer.model.character.technomancer;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag einer Komplexen Form aus {@code complexforms.xml} (GRW-Kern-Teilmenge, siehe {@code
 * docs/analysen/archiv/technomancer_analyse.md}). Reines Referenz-/Anzeigeobjekt ohne Berechnung und ohne
 * Rating — anders als bei Zaubern ist das Rating einer Komplexen Form Charakter-spezifisch, nicht Teil
 * des Katalogeintrags (siehe {@link ComplexForm}).
 */
public record ComplexFormData(
    @NonNull String name,
    @NonNull String category,
    @NonNull String skill,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry, Sourced {}
