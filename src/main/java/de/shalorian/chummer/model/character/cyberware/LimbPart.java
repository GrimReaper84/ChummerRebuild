package de.shalorian.chummer.model.character.cyberware;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Art einer Cybergliedmaße aus der Tabelle S. 391. Ganze Arme und Beine sowie der Torso zählen zum Durchschnitt der
 * Körperattribute: Das Rechenbeispiel S. 390 geht nur mit genau diesen fünf Teilen auf. Teilglieder (Hand/Fuß,
 * Unterarm, Unterschenkel) gelten nur für Proben mit diesem Teil (S. 389/390), der Schädel ist eine Hülle ohne eigene
 * Attribute.
 */
public enum LimbPart {
  FULL_ARM("arm", true),
  FULL_LEG("leg", true),
  HAND_FOOT("handfoot", false),
  LOWER_ARM("lowerarm", false),
  LOWER_LEG("lowerleg", false),
  TORSO("torso", true),
  SKULL("skull", false);

  private static final List<BodyLocation> ARMS = List.of(BodyLocation.ARM_LEFT, BodyLocation.ARM_RIGHT);
  private static final List<BodyLocation> LEGS = List.of(BodyLocation.LEG_LEFT, BodyLocation.LEG_RIGHT);

  private final @NonNull String code;
  private final boolean countsForAverage;

  LimbPart(@NonNull String code, boolean countsForAverage) {
    this.code = code;
    this.countsForAverage = countsForAverage;
  }

  /** Stellen, an denen diese Gliedart sitzen kann; die erste ist die Vorgabe. */
  public @NonNull List<BodyLocation> locations() {
    return switch (this) {
      case FULL_ARM, LOWER_ARM -> ARMS;
      case FULL_LEG, LOWER_LEG -> LEGS;
      case HAND_FOOT -> List.of(BodyLocation.ARM_LEFT, BodyLocation.ARM_RIGHT, BodyLocation.LEG_LEFT,
          BodyLocation.LEG_RIGHT);
      case TORSO -> List.of(BodyLocation.TORSO);
      case SKULL -> List.of(BodyLocation.SKULL);
    };
  }

  /** Ob das Glied mit seinen Werten in den Durchschnitt der fünf Körperteile eingeht. */
  public boolean countsForAverage() {
    return countsForAverage;
  }

  /** Ob das Glied eigene KON/STR/GES hat (alle außer dem Schädel). */
  public boolean hasAttributes() {
    return this != SKULL;
  }

  /** Ob das Glied als Cyberbein zählt (Sprunghydraulik, S. 391): ganzes Bein oder Unterschenkel. */
  public boolean isLeg() {
    return this == FULL_LEG || this == LOWER_LEG;
  }

  /**
   * Gliedart zum Code in {@code cyberware.xml} ({@code <limb part="arm"/>}, {@code <fitsin>}).
   *
   * @throws IllegalArgumentException bei unbekanntem Code
   */
  public static @NonNull LimbPart fromCode(@NonNull String code) {
    return Arrays.stream(values())
        .filter(p -> p.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unbekannte Gliedart: " + code));
  }
}
