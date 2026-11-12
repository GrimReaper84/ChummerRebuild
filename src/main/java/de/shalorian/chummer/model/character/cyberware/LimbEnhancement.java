package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import java.util.Arrays;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Was eine Cyberglied-Verbesserung erhöht (GRW S. 390): ein Körperattribut des Glieds um ihre Stufe oder seine
 * Panzerung (ballistisch und Stoß).
 */
public enum LimbEnhancement {
  BODY("body", AttributeType.BOD),
  STRENGTH("strength", AttributeType.STR),
  AGILITY("agility", AttributeType.AGI),
  ARMOR("armor", null);

  private final @NonNull String code;
  private final @Nullable AttributeType attribute;

  LimbEnhancement(@NonNull String code, @Nullable AttributeType attribute) {
    this.code = code;
    this.attribute = attribute;
  }

  /** Das verbesserte Attribut, {@code null} bei Panzerung. */
  public @Nullable AttributeType attribute() {
    return attribute;
  }

  /**
   * Verbesserung für ein Körperattribut.
   *
   * @throws IllegalArgumentException bei einem anderen als KON, STR oder GES
   */
  public static @NonNull LimbEnhancement forAttribute(@NonNull AttributeType type) {
    return Arrays.stream(values())
        .filter(e -> e.attribute == type)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Keine Cyberglied-Verbesserung für " + type));
  }

  /**
   * Verbesserung zum Code in {@code <enhances>}.
   *
   * @throws IllegalArgumentException bei unbekanntem Code
   */
  public static @NonNull LimbEnhancement fromCode(@NonNull String code) {
    return Arrays.stream(values())
        .filter(e -> e.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unbekannte Cyberglied-Verbesserung: " + code));
  }
}
