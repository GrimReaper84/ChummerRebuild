package de.shalorian.chummer.model.character.quality;

import org.jspecify.annotations.NonNull;

/**
 * Eine Quality eines Charakters.
 *
 * @param choice gewählte Fertigkeit, Attribut ({@code AttributeType}-Name) oder Freitext laut {@link
 *               QualityData#selection()}, sonst leer
 */
public record CharacterQuality(
    @NonNull QualityData data,
    @NonNull QualitySource source,
    boolean gratis,
    @NonNull String choice
) {

  public CharacterQuality(@NonNull QualityData data, @NonNull QualitySource source) {
    this(data, source, false, "");
  }
}
