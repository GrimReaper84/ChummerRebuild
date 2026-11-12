package de.shalorian.chummer.model.character.spirit;

import org.jspecify.annotations.NonNull;

/** Geist (Zauberer, GRW S. 112, 219–222) oder Sprite (Technomancer, S. 113, 281/282), mit den Texten je Art. */
public enum SpiritKind {
  SPIRIT("spirit", "Geist", "Geister", "Kraft", "gebunden", "herbeigerufen", "Herbeirufen", "Magie"),
  SPRITE("sprite", "Sprite", "Sprites", "Stufe", "registriert", "kompiliert", "Kompilieren", "Resonanz");

  private final @NonNull String code;
  private final @NonNull String displayName;
  private final @NonNull String pluralName;
  private final @NonNull String powerName;
  private final @NonNull String boundName;
  private final @NonNull String unboundName;
  private final @NonNull String skillName;
  private final @NonNull String attributeName;

  SpiritKind(@NonNull String code, @NonNull String displayName, @NonNull String pluralName,
      @NonNull String powerName, @NonNull String boundName, @NonNull String unboundName, @NonNull String skillName,
      @NonNull String attributeName) {
    this.code = code;
    this.displayName = displayName;
    this.pluralName = pluralName;
    this.powerName = powerName;
    this.boundName = boundName;
    this.unboundName = unboundName;
    this.skillName = skillName;
    this.attributeName = attributeName;
  }

  /** „Magie“ bzw. „Resonanz“: bestimmt Kraft bzw. Stufe bei der Erschaffung. */
  public @NonNull String getAttributeName() {
    return attributeName;
  }

  /** „Geist“ bzw. „Sprite“. */
  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** „Geister“ bzw. „Sprites“. */
  public @NonNull String getPluralName() {
    return pluralName;
  }

  /** „Kraft“ bzw. „Stufe“. */
  public @NonNull String getPowerName() {
    return powerName;
  }

  /** „gebunden“ bzw. „registriert“. */
  public @NonNull String getBoundName() {
    return boundName;
  }

  /** „herbeigerufen“ bzw. „kompiliert“. */
  public @NonNull String getUnboundName() {
    return unboundName;
  }

  /** Fertigkeit, die bei der Erschaffung die Dienste begrenzt: Herbeirufen bzw. Kompilieren (S. 112/113). */
  public @NonNull String getSkillName() {
    return skillName;
  }

  /**
   * Art zum Code in {@code <kind>}.
   *
   * @throws IllegalArgumentException bei unbekanntem Code
   */
  public static @NonNull SpiritKind fromCode(@NonNull String code) {
    for (SpiritKind kind : values()) {
      if (kind.code.equals(code)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Unbekannte Art: " + code);
  }
}
