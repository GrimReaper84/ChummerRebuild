package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.QualityData;
import org.jspecify.annotations.NonNull;

public record QualitySelectResult(
    @NonNull QualityData data,
    boolean gratis,
    boolean addAnother
) {}
