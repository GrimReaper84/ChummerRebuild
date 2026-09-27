package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/**
 * Kategorie eines Rüstungsteils aus der GRW-Tabelle <i>Panzerung</i> (S. 373). Entscheidend ist der
 * Unterschied beim Schutzwert: SR4-Grundregelwerk S. 372: "Helme und Schilde werden nicht als
 * eigenständige Panzerungsstücke behandelt, sondern erhöhen den Wert der getragenen Panzerung um ihre
 * Stufe." Die Kategorie "Kleidung" ist vorerst nicht enthalten (siehe {@code
 * docs/plaene/nuyen_ruestung_plan.md} Abschnitt 13).
 */
public enum ArmorCategory {
  ARMOR("Panzerung", false),
  HELMETS_AND_SHIELDS("Helme und Schilde", true);

  private final @NonNull String displayName;
  private final boolean bonus;

  ArmorCategory(@NonNull String displayName, boolean bonus) {
    this.displayName = displayName;
    this.bonus = bonus;
  }

  /** Bezeichnung wie im GRW; zugleich der Wert von {@code <category>} in {@code armor.xml}. */
  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob Teile dieser Kategorie ihren Wert zum getragenen Schutz addieren, statt selbst zu zählen. */
  public boolean isBonus() {
    return bonus;
  }

  /**
   * Liest die Kategorie aus {@code <category>} in {@code armor.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull ArmorCategory fromDisplayName(@NonNull String displayName) {
    for (ArmorCategory category : values()) {
      if (category.displayName.equals(displayName)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unbekannte Rüstungskategorie: \"" + displayName + "\"");
  }
}
