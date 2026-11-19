package de.shalorian.chummer.model.character.spirit;

import org.jspecify.annotations.NonNull;

/** Geist (Zauberer, GRW S. 112, 219–222) oder Sprite (Technomancer, S. 113, 281/282), mit den Texten je Art. */
public enum SpiritKind {
  SPIRIT("spirit", "Geist", "Geister", "Kraft", "gebunden", "herbeigerufen", "Binden", "Herbeirufen", "Magie",
      "Nur für Zauberer und Magieradepten", "Optionale Kräfte"),
  SPRITE("sprite", "Sprite", "Sprites", "Stufe", "registriert", "kompiliert", "Registrieren", "Kompilieren", "Resonanz",
      "Nur für Technomancer", "Optionale Komplexe Formen");

  private final @NonNull String code;
  private final @NonNull String displayName;
  private final @NonNull String pluralName;
  private final @NonNull String powerName;
  private final @NonNull String boundName;
  private final @NonNull String unboundName;
  private final @NonNull String bindVerb;
  private final @NonNull String skillName;
  private final @NonNull String attributeName;
  private final @NonNull String unavailableText;
  private final @NonNull String optionsName;

  SpiritKind(@NonNull String code, @NonNull String displayName, @NonNull String pluralName,
      @NonNull String powerName, @NonNull String boundName, @NonNull String unboundName, @NonNull String bindVerb,
      @NonNull String skillName, @NonNull String attributeName, @NonNull String unavailableText,
      @NonNull String optionsName) {
    this.code = code;
    this.displayName = displayName;
    this.pluralName = pluralName;
    this.powerName = powerName;
    this.boundName = boundName;
    this.unboundName = unboundName;
    this.bindVerb = bindVerb;
    this.skillName = skillName;
    this.attributeName = attributeName;
    this.unavailableText = unavailableText;
    this.optionsName = optionsName;
  }

  /** „Optionale Kräfte“ bzw. „Optionale Komplexe Formen“ (GRW S. 347 bzw. 282). */
  public @NonNull String getOptionsName() {
    return optionsName;
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

  /** „Binden“ bzw. „Registrieren“. */
  public @NonNull String getBindVerb() {
    return bindVerb;
  }

  /** Grund, warum ein Charakter ohne passende Gabe diese Art nicht haben kann. */
  public @NonNull String getUnavailableText() {
    return unavailableText;
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
