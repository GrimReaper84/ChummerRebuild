package de.shalorian.chummer.model.character.quality;

import org.jspecify.annotations.NonNull;

public record CharacterQuality(
    @NonNull QualityData data,
    @NonNull QualitySource source,
    boolean gratis
) {

  public CharacterQuality(@NonNull QualityData data, @NonNull QualitySource source) {
    this(data, source, false);
  }
}