package de.shalorian.chummer.model.character.quality;

import de.shalorian.chummer.model.character.CatalogEntry;
import java.util.Set;
import org.jspecify.annotations.NonNull;

public record QualityData(
    @NonNull String name,
    int bp,
    @NonNull QualityType type,
    boolean contributeToLimit,
    @NonNull Set<QualityBonusType> bonuses
) implements CatalogEntry {}