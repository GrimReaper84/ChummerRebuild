package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

public record CharacterQuality(
    @NonNull QualityData data,
    @NonNull QualitySource source,
    /** Wenn true, zählt diese Quality nicht zu BP-Kosten und Limit (GM-Override). */
    boolean gratis
) {
  /** Convenience-Konstruktor ohne Gratis-Override (gratis = false). */
  public CharacterQuality(@NonNull QualityData data, @NonNull QualitySource source) {
    this(data, source, false);
  }
}