package de.shalorian.chummer.model.character.situation;

import org.jspecify.annotations.NonNull;

/**
 * Anlass, für den ein situativer Bonus gilt (Foki S. 233/234, Schutzpatrone S. 234–237, Adeptenkräfte S. 229–232).
 * Das Detail des Bonus nennt bei Bedarf Zauberkategorie, Geisterart oder Fertigkeit.
 */
public enum SituationKind {
  /** Spruch- und Ritualzauberei für eine Zauberkategorie. */
  SPELL_CATEGORY("Spruch- und Ritualzauberei"),
  /** Antimagie (Spruchabwehr) gegen eine Zauberkategorie. */
  COUNTERSPELL_CATEGORY("Antimagie"),
  /** Herbeirufen, Binden und Verbannen einer Geisterart. */
  SPIRIT_TYPE("Herbeirufen, Binden, Verbannen"),
  /** Herbeirufen einer Geisterart (Herbeirufungsfokus). */
  SPIRIT_SUMMON("Herbeirufen"),
  /** Binden, ggf. nur einer Geisterart (Bindungsfokus, Schlange). */
  SPIRIT_BIND("Binden"),
  /** Verbannen einer Geisterart (Verbannungsfokus). */
  SPIRIT_BANISH("Verbannen"),
  /** Eine bestimmte Fertigkeit (Detail = Name). */
  SKILL(""),
  /** Alle Proben mit dem Magieattribut außer Antimagie (Kraftfokus, S. 234). */
  MAGIC("Magiefertigkeiten außer Antimagie"),
  /** Schadenswiderstand gegen Körperlichen Schaden: Konstitution + Panzerung (S. 181). */
  DAMAGE_RESISTANCE("Schadenswiderstand"),
  /** Widerstand gegen Zauber mit Konstitution bzw. Willenskraft. */
  SPELL_RESISTANCE("Zauberwiderstand"),
  /** Widerstand gegen Toxine und Krankheiten: Konstitution (S. 297). */
  TOXIN_RESISTANCE("Widerstand gegen Toxine und Krankheiten"),
  /** Verteidigung gegen Angriffe: Reaktion (S. 181). */
  DEFENSE("Verteidigung"),
  /** Natürliche Heilung: Konstitution × 2 bzw. Konstitution + Willenskraft (S. 294). */
  HEALING("Natürliche Heilung"),
  /** Springen: Akrobatik + Geschicklichkeit (S. 163). */
  JUMP("Springen"),
  /** Soziale Proben allgemein; nur als Bonus ohne Grundpool. */
  SOCIAL("Soziale Proben");

  private final @NonNull String label;

  SituationKind(@NonNull String label) {
    this.label = label;
  }

  /** Anlass mit Detail zum Lesen, z.B. „Spruch- und Ritualzauberei (Kampf)“; bei {@link #SKILL} nur die Fertigkeit. */
  public @NonNull String describe(@NonNull String detail) {
    if (label.isEmpty()) {
      return detail;
    }
    return detail.isEmpty() ? label : label + " (" + detail + ")";
  }
}
