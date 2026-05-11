package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

public class CharacterAttribute {

  private static final int BP_PER_POINT = 10;

  private final @NonNull AttributeType type;
  private final int min;
  private final int max;
  private int value;

  public CharacterAttribute(@NonNull AttributeType type, @NonNull AttributeRange range) {
    this.type = type;
    this.min = range.min();
    this.max = range.max();
    this.value = range.min();
  }

  public @NonNull AttributeType getType() {
    return type;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int newValue) {
    if (newValue < min || newValue > max) {
      throw new IllegalArgumentException(
          "%s: Wert %d liegt außerhalb [%d/%d]".formatted(type, newValue, min, max));
    }
    this.value = newValue;
  }

  /**
   * BP-Kosten für dieses Attribut (Punkte über Rassenminimum × 10, +15 BP auf Rassenmaximum).
   */
  public int getBpCost() {
    int cost = (value - min) * BP_PER_POINT;
    if (value == max && min != max) {
      cost += 15;
    }
    return cost;
  }

  /**
   * Karma-Kosten um dieses Attribut um 1 zu erhöhen (neuer Wert × 5).
   */
  public int getKarmaCost() {
    return (value + 1) * 5;
  }
}