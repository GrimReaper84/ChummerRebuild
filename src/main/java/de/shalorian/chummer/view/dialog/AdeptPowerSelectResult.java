package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import org.jspecify.annotations.NonNull;

/**
 * Ergebnis der Kraftauswahl.
 *
 * @param choice     Auswahl laut {@link AdeptPowerData#selection()}, sonst leer
 * @param addAnother ob der Dialog nach dem Kauf erneut geöffnet werden soll
 */
public record AdeptPowerSelectResult(
    @NonNull AdeptPowerData data,
    @NonNull String choice,
    boolean addAnother
) {}
