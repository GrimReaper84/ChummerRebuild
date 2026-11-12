package de.shalorian.chummer.model.character.condition;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import org.jspecify.annotations.NonNull;

/**
 * Proben nur aus zwei Attributen ohne Fertigkeit (SR4-Grundregelwerk S. 166/167). Die Spielleitung kann weitere
 * Kombinationen verlangen; der Bogen zeigt die vier im GRW benannten.
 */
public enum AttributeTest {
  COMPOSURE("Selbstbeherrschung", AttributeType.WIL, AttributeType.CHA, 167),
  JUDGE_INTENTIONS("Menschenkenntnis", AttributeType.INT, AttributeType.CHA, 167),
  MEMORY("Erinnerungsvermögen", AttributeType.LOG, AttributeType.WIL, 166),
  LIFT_CARRY("Heben und Tragen", AttributeType.STR, AttributeType.BOD, 167);

  private final @NonNull String displayName;
  private final @NonNull AttributeType first;
  private final @NonNull AttributeType second;
  private final int page;

  AttributeTest(@NonNull String displayName, @NonNull AttributeType first, @NonNull AttributeType second, int page) {
    this.displayName = displayName;
    this.first = first;
    this.second = second;
    this.page = page;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  public @NonNull AttributeType getFirst() {
    return first;
  }

  public @NonNull AttributeType getSecond() {
    return second;
  }

  /** Gedruckte GRW-Seite der Probe. */
  public int getPage() {
    return page;
  }
}
