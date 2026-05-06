package de.shalorian.chummer.model.character;

import java.util.Map;

public record MetatypeData(
    String name,
    int bpCost,
    boolean isVariant,
    Map<AttributeType, AttributeRange> ranges
) {

  public AttributeRange getRangeFor(AttributeType type) {
    AttributeRange range = ranges.get(type);
    if (range == null) {
      throw new IllegalStateException(
          "Kein Attributbereich für " + type + " in Metatyp \"" + name + "\"");
    }
    return range;
  }
}