package de.shalorian.chummer.model.character;

import java.util.Set;
import org.jspecify.annotations.NonNull;

public record QualityData(
    @NonNull String name,
    int bp,
    @NonNull QualityType type,
    boolean contributeToLimit,
    @NonNull Set<QualityBonusType> bonuses
) implements CatalogEntry {}