package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.quality.QualityData;
import org.jspecify.annotations.NonNull;

/**
 * Ergebnis des Quality-Auswahldialogs.
 *
 * @param choice Auswahl laut {@link QualityData#selection()}, sonst leer
 */
public record QualitySelectResult(
    @NonNull QualityData data,
    boolean gratis,
    boolean addAnother,
    @NonNull String choice
) {}
