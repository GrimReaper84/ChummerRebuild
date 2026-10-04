package de.shalorian.chummer.model.character.gear;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Art des Zusatztexts, den ein Gegenstand beim Kauf braucht (Fertigkeit eines Werkzeugs, Thema einer
 * Datasoft, Name einer gefälschten SIN …). Die Feldbezeichnung steht je Eintrag in {@link
 * GearData#extraLabel()}. Ist ein Zusatztext vorgesehen, ist er Pflicht.
 */
public enum ExtraKind {
  /** Kein Zusatztext. */
  NONE,
  /** Freitext. */
  TEXT,
  /**
   * Lizenz einer gefälschten Lizenz: Vorauswahl der GRW-Beispiele plus Freitext. SR4-Grundregelwerk
   * S. 377: Gefälschte Lizenzen gibt es "für den Kauf lizenzierter Waren … oder bestimmte Aktivitäten
   * (Jagd, verdecktes Tragen einer Waffe, Zauberei usw.)"; S. 131 nennt Detektiv-Lizenz, Führerschein
   * und Waffenlizenz. Nicht modelliert: S. 359 "Eine gefälschte Lizenz ist immer mit einer falschen
   * SIN verbunden".
   */
  LICENSE;

  private static final List<String> LICENSE_SUGGESTIONS = List.of("Waffenlizenz",
      "Verdecktes Tragen", "Jagdlizenz", "Zauberlizenz", "Detektivlizenz", "Führerschein");

  /** Vorschläge für den Zusatztext; leer, wenn nur Freitext vorgesehen ist. */
  public @NonNull List<String> suggestions() {
    return this == LICENSE ? LICENSE_SUGGESTIONS : List.of();
  }
}
