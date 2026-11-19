package de.shalorian.chummer.model.character.initiation;

import org.jspecify.annotations.NonNull;

/**
 * Art einer Technik: Metamagie aus der Initiation (GRW S. 232/233) oder Echo aus der Wandlung (S. 284), mit allen
 * Texten, die von der Art abhängen.
 */
public enum TechniqueKind {
  METAMAGIC("metamagic", "Initiation", "Initiationsgrad", "Metamagie", "eine Metamagie", "Metamagien", "Magie", "232",
      "Zugang zu den Metaebenen des Astralraums (mit Astralprojektion, GRW S. 233)"),
  ECHO("echo", "Wandlung", "Wandlungsgrad", "Echo", "ein Echo", "Echos", "Resonanz", "284",
      "Zugang zu den Resonanzräumen der Matrix (GRW S. 284)");

  private final @NonNull String code;
  private final @NonNull String processName;
  private final @NonNull String gradeName;
  private final @NonNull String techniqueName;
  private final @NonNull String oneTechnique;
  private final @NonNull String pluralName;
  private final @NonNull String attributeName;
  private final @NonNull String page;
  private final @NonNull String accessHint;

  TechniqueKind(@NonNull String code, @NonNull String processName, @NonNull String gradeName,
      @NonNull String techniqueName, @NonNull String oneTechnique, @NonNull String pluralName,
      @NonNull String attributeName, @NonNull String page, @NonNull String accessHint) {
    this.code = code;
    this.processName = processName;
    this.gradeName = gradeName;
    this.techniqueName = techniqueName;
    this.oneTechnique = oneTechnique;
    this.pluralName = pluralName;
    this.attributeName = attributeName;
    this.page = page;
    this.accessHint = accessHint;
  }

  /** „Initiation“ bzw. „Wandlung“. */
  public @NonNull String getProcessName() {
    return processName;
  }

  /** „Initiationsgrad“ bzw. „Wandlungsgrad“. */
  public @NonNull String getGradeName() {
    return gradeName;
  }

  /** „Metamagie“ bzw. „Echo“. */
  public @NonNull String getTechniqueName() {
    return techniqueName;
  }

  /** Mit Artikel: „eine Metamagie“ bzw. „ein Echo“. */
  public @NonNull String getOneTechnique() {
    return oneTechnique;
  }

  /** „Metamagien“ bzw. „Echos“. */
  public @NonNull String getPluralName() {
    return pluralName;
  }

  /** „Magie“ bzw. „Resonanz“. */
  public @NonNull String getAttributeName() {
    return attributeName;
  }

  /** Gedruckte GRW-Seite der Regeln. */
  public @NonNull String getPage() {
    return page;
  }

  /** Was der erste Grad zusätzlich öffnet (S. 233 bzw. 284). */
  public @NonNull String getAccessHint() {
    return accessHint;
  }

  /**
   * Art zum Code in {@code <kind>}.
   *
   * @throws IllegalArgumentException bei unbekanntem Code
   */
  public static @NonNull TechniqueKind fromCode(@NonNull String code) {
    for (TechniqueKind kind : values()) {
      if (kind.code.equals(code)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Unbekannte Technik-Art: " + code);
  }
}
