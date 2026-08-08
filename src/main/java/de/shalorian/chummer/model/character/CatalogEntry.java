package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Gemeinsamer Nenner für aus XML geladene Katalog-Einträge (z.B. {@link QualityData}, {@link
 * SpellData}), die zur Auswahl in einem Katalog-Dialog angeboten werden. {@link #name()} dient
 * dort als eindeutiger Anzeige- und Suchschlüssel.
 */
public interface CatalogEntry {

  @NonNull String name();
}
