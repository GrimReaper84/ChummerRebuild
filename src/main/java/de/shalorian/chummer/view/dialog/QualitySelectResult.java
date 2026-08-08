package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.quality.QualityData;
import org.jspecify.annotations.NonNull;

public record QualitySelectResult(
    @NonNull QualityData data,
    boolean gratis,
    boolean addAnother
) {}
