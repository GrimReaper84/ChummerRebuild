package de.shalorian.chummer.model.character;

public record AttributeRange(int min, int max) {

  public AttributeRange {
    if (min < 1 || max < min) {
      throw new IllegalArgumentException(
          "Ungültiger Bereich: [%d/%d]".formatted(min, max));
    }
  }

  public boolean contains(int value) {
    return value >= min && value <= max;
  }
}