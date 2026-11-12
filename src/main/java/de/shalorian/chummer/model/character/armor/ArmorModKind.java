package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/** Art eines Einbaus an einem Rüstungsteil (GRW S. 372/373). */
public enum ArmorModKind {
  /** Zubehör genau eines Teils (Ganzkörperpanzerung, Urban Explorer Overall). */
  ACCESSORY("Zubehör"),
  /** Panzerungsmodifikation, an jedes Teil, das Modifikationen aufnimmt (an Schilde nur einige), meist mit Stufe 1–6. */
  MODIFICATION("Modifikation");

  private final @NonNull String displayName;

  ArmorModKind(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}
