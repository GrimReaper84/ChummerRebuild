package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Ein Sonderattribut (Edge, Magic oder Resonance). Nutzt bewusst nicht {@link CharacterAttribute},
 * da die BP-Kostenformel je nach Sonderattribut abweicht: Edge ist vom Rassenmaximum-Bonus
 * ausgenommen (SR4-Grundregelwerk: "Edge: 10 GP pro Attributsstufe [ausgenommen Maximierung]"),
 * während Magic/Resonance denselben Rassenmax-Bonus wie Standardattribute erhalten (der sechste
 * Punkt kostet 25 GP). Die Karma-Kosten im Karrieremodus folgen dagegen für alle drei
 * Sonderattribute derselben Formel wie Standardattribute (GRW, Kapitel Attribut-Steigerung: "Die
 * Kosten für die Steigerung eines natürlichen Attributes betragen die neue Stufe × 5" — explizit
 * inklusive Edge/Magie/Resonanz, keine Ausnahme).
 */
public class SpecialAttribute {

  private static final int BP_PER_POINT = 10;
  private static final int RACIAL_MAX_BONUS = 15;
  private static final int KARMA_PER_POINT = 5;

  private final @NonNull SpecialAttributeType type;
  private final int min;
  private final int max;
  private final boolean racialMaxBonusApplies;
  private int value;

  /**
   * @param racialMaxBonusApplies ob das Erreichen des Maximalwerts einen Kostenaufschlag von
   *                              {@value #RACIAL_MAX_BONUS} BP auslöst (gilt für Magic/Resonance,
   *                              nicht für Edge)
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

  public int getBpCost() {
    return getBpCostForValue(value);
  }

  /**
   * BP-Kosten, die dieses Sonderattribut bei einem hypothetischen Wert hätte — für eine
   * Budget-Vorschau vor einer tatsächlichen Erhöhung.
   */
  public int getBpCostForValue(int hypotheticalValue) {
    int cost = (hypotheticalValue - min) * BP_PER_POINT;
    if (racialMaxBonusApplies && hypotheticalValue == max && min != max) {
      cost += RACIAL_MAX_BONUS;
    }
    return cost;
  }

  /**
   * Karma-Kosten um dieses Sonderattribut im Karrieremodus um 1 zu erhöhen (neuer Wert × 5) —
   * identische Formel für Edge/Magic/Resonance, keine Rassenmax-Ausnahme.
   */
  public int getKarmaCost() {
    return (value + 1) * KARMA_PER_POINT;
  }
}
