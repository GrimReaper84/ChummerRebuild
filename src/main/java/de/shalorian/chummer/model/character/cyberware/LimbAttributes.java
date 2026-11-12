package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import org.jspecify.annotations.NonNull;

/**
 * Konstitution, Stärke und Geschicklichkeit eines Glieds: als Grundwerte einer Maßanfertigung, als Gesamtwerte samt
 * Verbesserungen (GRW S. 390) oder als Durchschnitt über die fünf Körperteile ({@link Implants#getLimbAverages()}).
 */
public record LimbAttributes(int body, int strength, int agility) {

  /** Grundwerte eines Standardglieds (S. 390). */
  public static final int STANDARD_VALUE = 3;

  /** Standardglied: alle drei Werte 3. */
  public static final LimbAttributes STANDARD = new LimbAttributes(STANDARD_VALUE, STANDARD_VALUE, STANDARD_VALUE);

  /**
   * Wert für {@code type}.
   *
   * @throws IllegalArgumentException bei einem anderen als KON, STR oder GES
   */
  public int get(@NonNull AttributeType type) {
    return switch (type) {
      case BOD -> body;
      case STR -> strength;
      case AGI -> agility;
      default -> throw new IllegalArgumentException("Glieder haben kein " + type);
    };
  }

  /** Summe der Punkte über 3, die eine Maßanfertigung kostet (je Punkt Verfügbarkeit +1 und 1.500 ¥, S. 390). */
  public int pointsAboveStandard() {
    return Math.max(0, body - STANDARD_VALUE) + Math.max(0, strength - STANDARD_VALUE)
        + Math.max(0, agility - STANDARD_VALUE);
  }
}
