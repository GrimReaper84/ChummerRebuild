package de.shalorian.chummer.model.character.attribute;

import org.jspecify.annotations.NonNull;

public class SpecialAttribute {

  private static final int BP_PER_POINT = 10;
  private static final int RACIAL_MAX_BONUS = 15;
  private static final int KARMA_PER_POINT = 5;

  private final @NonNull SpecialAttributeType type;
  private final int min;
  private final int max;
  private final boolean racialMaxBonusApplies;
  private int maxBonus;
  private int value;
  /** Verheizte Punkte (Edge, GRW S. 99); senken die Untergrenze des Werts. */
  private int burned;

  /**
   * @param racialMaxBonusApplies ob das Erreichen des Maximalwerts einen Kostenaufschlag von {@value #RACIAL_MAX_BONUS} BP auslöst (gilt für
   *                              Magic/Resonance, nicht für Edge)
   */
  public SpecialAttribute(@NonNull SpecialAttributeType type, int min, int max,
      boolean racialMaxBonusApplies) {
    if (min < 0 || max < min) {
      throw new IllegalArgumentException("Ungültiger Bereich: [%d/%d]".formatted(min, max));
    }
    this.type = type;
    this.min = min;
    this.max = max;
    this.racialMaxBonusApplies = racialMaxBonusApplies;
    this.value = min;
  }

  public @NonNull SpecialAttributeType getType() {
    return type;
  }

  public int getMin() {
    return min;
  }

  /** Maximum, inklusive Bonus durch die Gabe Glück (Edge, GRW S. 115). */
  public int getMax() {
    return max + maxBonus;
  }

  /** Setzt den Maximum-Bonus; sinkt das Maximum unter den Wert, wird der Wert darauf gesenkt. */
  public void setMaxBonus(int maxBonus) {
    this.maxBonus = maxBonus;
    value = Math.min(value, getMax());
  }

  public int getValue() {
    return value;
  }

  public void setValue(int newValue) {
    if (newValue < getLowerBound() || newValue > getMax()) {
      throw new IllegalArgumentException(
          "%s: Wert %d liegt außerhalb [%d/%d]".formatted(type, newValue, getLowerBound(), getMax()));
    }
    this.value = newValue;
  }

  /** Untergrenze des Werts: Minimum, gesenkt um verheizte Punkte (Edge, S. 99), nicht unter 0. */
  public int getLowerBound() {
    return Math.max(0, min - burned);
  }

  /**
   * Senkt den Wert dauerhaft um 1 (Edge verheizen, GRW S. 99), auch unter das Metatyp-Minimum bis 0 (Nutzerentscheidung
   * 2026-11-15). Später lässt sich der Wert wie jede Steigerung mit Karma wieder erhöhen.
   *
   * @throws IllegalStateException beim Wert 0
   */
  public void burn() {
    if (value == 0) {
      throw new IllegalStateException(type + ": nichts mehr zu verheizen");
    }
    value--;
    burned++;
  }

  public int getBurned() {
    return burned;
  }

  /** Stellt Wert und verheizte Punkte beim Laden her; die Untergrenze gilt schon gesenkt. */
  public void load(int value, int burned) {
    this.burned = Math.max(0, burned);
    setValue(value);
  }

  public int getBpCost() {
    return getBpCostForValue(value);
  }

  public int getBpCostForValue(int hypotheticalValue) {
    int cost = Math.max(0, hypotheticalValue - min) * BP_PER_POINT;
    if (racialMaxBonusApplies && hypotheticalValue == getMax() && min != getMax()) {
      cost += RACIAL_MAX_BONUS;
    }
    return cost;
  }

  public int getKarmaCost() {
    return (value + 1) * KARMA_PER_POINT;
  }
}
