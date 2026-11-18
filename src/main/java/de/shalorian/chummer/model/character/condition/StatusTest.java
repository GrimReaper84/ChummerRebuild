package de.shalorian.chummer.model.character.condition;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Proben, die das Status-Panel auf Wunsch zeigt (Anzeige-Einstellung, ADR-061): die vier Attributproben (GRW
 * S. 166/167), Sprinten (S. 177) sowie Wahrnehmung und Ausweichen als häufige Fertigkeitsproben. Die Reihenfolge ist
 * die Anzeigereihenfolge.
 */
public enum StatusTest {
  PERCEPTION("Wahrnehmung", null, "Wahrnehmung"),
  DODGE("Ausweichen", null, "Ausweichen"),
  SPRINT("Sprinten", null, null),
  COMPOSURE(AttributeTest.COMPOSURE),
  JUDGE_INTENTIONS(AttributeTest.JUDGE_INTENTIONS),
  MEMORY(AttributeTest.MEMORY),
  LIFT_CARRY(AttributeTest.LIFT_CARRY);

  private final @NonNull String displayName;
  private final @Nullable AttributeTest attributeTest;
  private final @Nullable String skillName;

  StatusTest(@NonNull AttributeTest attributeTest) {
    this(attributeTest.getDisplayName(), attributeTest, null);
  }

  StatusTest(@NonNull String displayName, @Nullable AttributeTest attributeTest, @Nullable String skillName) {
    this.displayName = displayName;
    this.attributeTest = attributeTest;
    this.skillName = skillName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Die Attributprobe dahinter, sonst {@code null}. */
  public @Nullable AttributeTest getAttributeTest() {
    return attributeTest;
  }

  /** Name der aktiven Fertigkeit in {@code skills.xml}, sonst {@code null}. */
  public @Nullable String getSkillName() {
    return skillName;
  }
}
