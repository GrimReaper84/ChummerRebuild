package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.persistence.CharacterSnapshot;
import org.jspecify.annotations.NonNull;

/**
 * Ein Schritt im Rückgängig-Verlauf: Stand vor der Buchung und der gebuchte Eintrag.
 *
 * @param currency in welchem Log der Eintrag steht
 * @param created  ob der Charakter bei der Buchung schon im Karrieremodus war
 */
record UndoStep(@NonNull CharacterSnapshot before, @NonNull LogEntry entry, @NonNull SpendCurrency currency,
                boolean created) {
}
