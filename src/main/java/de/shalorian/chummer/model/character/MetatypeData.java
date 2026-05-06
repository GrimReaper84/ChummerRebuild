package de.shalorian.chummer.model.character;

import java.util.Map;
import org.jspecify.annotations.NonNull;

public record MetatypeData(
    @NonNull String name,
    int bpCost,
    boolean isVariant,
    @NonNull Map<AttributeType, AttributeRange> ranges
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