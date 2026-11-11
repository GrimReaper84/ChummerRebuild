package de.shalorian.chummer.model.character.quality;

/** Rechnerische Wirkungen von Qualities, die der Charakter auswertet (Tags in {@code <bonus>}). */
public enum QualityBonusType {
  /** Technische Fertigkeiten (und akademische/berufliche Wissensfertigkeiten) kosten doppelt, keine Technischen Gruppen (GRW S. 121). */
  UNEDUCATED,
  /** Soziale Fertigkeiten kosten doppelt, keine Sozialen Gruppen (GRW S. 122). */
  UNCOUTH,
  /** Körperliche Fertigkeiten kosten doppelt, keine Körperlichen Gruppen (GRW S. 120, Körperliche Schwäche). */
  INFIRM,
  /** Schaltet das Sonderattribut Magie frei (Adept, Zauberer, Magieradept). */
  GRANTS_MAGIC,
  /** Schaltet das Sonderattribut Resonanz frei (Technomancer). */
  GRANTS_RESONANCE,
  /** Schaltet magische Aktionsfertigkeiten und Zauber frei (Zauberer, Magieradept). */
  GRANTS_SPELLCASTING,
  /**
   * Handicap "Immunabstoßung" (SR4-Grundregelwerk S. 120): doppelter Essenzverlust durch Cyberware,
   * Bioware bleibt unverändert. Angewendet in {@code Character#getEssence()} auf die Cyberware-Summe, bevor
   * die Kombinationsregel (höhere Summe voll, niedrigere halb) greift.
   */
  DOUBLES_CYBERWARE_ESSENCE,
  /** Außergewöhnliches Attribut (GRW S. 115): Maximum des gewählten Attributs +1. */
  EXCEPTIONAL_ATTRIBUTE,
  /** Glück (GRW S. 115): Edge-Maximum +1. */
  LUCKY,
  /** Talentiert (GRW S. 117): gewählte Aktionsfertigkeit bis 7. */
  APTITUDE,
  /** Inkompetenz (GRW S. 120): gewählte Aktionsfertigkeit nicht erlernbar. */
  INCOMPETENT,
  /** Abhängigkeit (Ausgebrannt) (GRW S. 118): Essenz −1. */
  BURNOUT_ESSENCE
}
