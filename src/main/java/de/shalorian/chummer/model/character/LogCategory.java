package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Art eines Karma- bzw. Nuyen-Log-Eintrags, gröber als im C#-Original (dort je Kauf- und Steigerungsart ein Wert): die
 * Beschreibung nennt den Gegenstand, die Art erlaubt den Überblick im Log.
 */
public enum LogCategory {
  ATTRIBUTE("Attribut"),
  SKILL("Fertigkeit"),
  QUALITY("Gabe/Handicap"),
  CONTACT("Kontakt"),
  MAGIC_RESONANCE("Magie/Resonanz"),
  PURCHASE("Kauf"),
  SALE("Verkauf"),
  LIFESTYLE("Lebensstil"),
  START("Startkapital"),
  /** Von Hand eingetragen; auch alle Einträge aus Dateien ohne Art. */
  MANUAL("Manuell");

  private final @NonNull String displayName;

  LogCategory(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}
