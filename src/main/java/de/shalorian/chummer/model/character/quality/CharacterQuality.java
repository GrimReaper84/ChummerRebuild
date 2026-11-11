package de.shalorian.chummer.model.character.quality;

import de.shalorian.chummer.model.character.attribute.AttributeType;
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

  /** Name mit Auswahl, z.B. "Talentiert (Pistolen)" oder "Außergewöhnliches Attribut (Stärke)". */
  public @NonNull String displayName() {
    if (choice.isEmpty()) {
      return data.name();
    }
    String shown = data.selection() == QualitySelection.ATTRIBUTE
        ? AttributeType.valueOf(choice).getDisplayName()
        : choice;
    return "%s (%s)".formatted(data.name(), shown);
  }
}
