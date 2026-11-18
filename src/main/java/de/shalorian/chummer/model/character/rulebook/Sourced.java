package de.shalorian.chummer.model.character.rulebook;

import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag mit Quellenangabe: das Kürzel des Regelwerks aus {@code books.xml} (z.B. {@code "SR4"}). Ein leeres
 * Kürzel zählt als Grundregelwerk. Darüber filtern die Auswahldialoge nach den aktiven Regelwerken eines Charakters.
 */
public interface Sourced {

  @NonNull String source();
}
