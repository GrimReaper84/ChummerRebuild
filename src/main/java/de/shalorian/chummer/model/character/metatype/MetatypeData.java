package de.shalorian.chummer.model.character.metatype;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Metatyp oder Metavariante aus {@code metatypes.xml}.
 *
 * @param baseName Grundmetatyp (bei Varianten der übergeordnete Metatyp, sonst der eigene Name)
 * @param movement Bewegungsweiten Gehen/Laufen (GRW S. 177)
 */
public record MetatypeData(
    @NonNull String name,
    int bpCost,
    boolean isVariant,
    @NonNull String baseName,
    @NonNull Map<AttributeType, AttributeRange> ranges,
    @NonNull AttributeRange edgeRange,
    @NonNull List<String> grantedPositiveQualityNames,
    @NonNull List<String> grantedNegativeQualityNames,
    @NonNull Movement movement
) {

  public @NonNull AttributeRange getRangeFor(@NonNull AttributeType type) {
    AttributeRange range = ranges.get(type);
    if (range == null) {
      throw new IllegalStateException(
          "Kein Attributbereich für " + type + " in Metatyp \"" + name + "\"");
    }
    return range;
  }
}