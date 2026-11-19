package de.shalorian.chummer.model.character.magic;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag eines Zaubers aus {@code spells.xml} (GRW-Kern-Teilmenge, siehe {@code
 * docs/magie_analyse.md}). Reines Referenz-/Anzeigeobjekt ohne Berechnung — insbesondere wird der
 * Force-Wert (Kraftstufe) hier nicht abgebildet, da er erst beim tatsächlichen Wirken eines Zaubers
 * relevant wird (kein Bestandteil der Charakterverwaltung). {@code dv} (Drain Value) ist daher
 * ebenfalls reiner Anzeigetext, keine berechenbare Zahl (z.B. {@code "(K/2)+3"}, K = Kraftstufe). {@code note} ist
 * die Kurzwirkung in eigenen Worten für Kaufdialog und Tooltip.
 */
public record SpellData(
    @NonNull String name,
    @NonNull String descriptors,
    @NonNull String category,
    @NonNull String type,
    @NonNull String range,
    @NonNull String damage,
    @NonNull String duration,
    @NonNull String dv,
    @NonNull String source,
    @NonNull String page,
    @NonNull String note
) implements CatalogEntry, Sourced {}
