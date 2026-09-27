package de.shalorian.chummer.model.character.cyberware;

import org.jspecify.annotations.NonNull;

/**
 * Unterscheidet Cyberware von Bioware. Fachlich einzig relevant für die Essenz-Kombinationsregel des
 * SR4-Grundregelwerks: "Essenzverlust durch Cyberware und Bioware wird getrennt berechnet und notiert.
 * Der höhere der beiden Werte wird vollständig von der Essenz abgezogen, der andere Wert nur zur
 * Hälfte" (siehe {@code Character#getEssence()}). Das übrige Domänenverhalten (Grade, Essenz-/
 * Kostenformel) ist für beide Arten identisch, daher bewusst keine getrennten Klassen.
 */
public enum EquipmentKind {
  CYBERWARE("Cyberware"),
  BIOWARE("Bioware");

  private final @NonNull String displayName;

  EquipmentKind(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}
