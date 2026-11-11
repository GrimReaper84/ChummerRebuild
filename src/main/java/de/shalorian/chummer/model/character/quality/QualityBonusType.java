package de.shalorian.chummer.model.character.quality;

public enum QualityBonusType {
  UNEDUCATED,        // Technical Active Skills kosten doppelt
  UNCOUTH,           // Social Active Skills kosten doppelt
  INFIRM,            // Physical Active Skills kosten doppelt
  GRANTS_MAGIC,          // schaltet das Magic-Sonderattribut frei (Adept/Magier/Mystischer Adept)
  GRANTS_RESONANCE,      // schaltet das Resonance-Sonderattribut frei (Technomant)
  GRANTS_SPELLCASTING,   // schaltet magische Aktionsfertigkeiten + Zauber frei (nur Magier, nicht Adept)
  /**
   * Handicap "Immunabstoßung" (SR4-Grundregelwerk S. 120): doppelter Essenzverlust durch Cyberware,
   * Bioware bleibt unverändert. Angewendet in {@code Character#getEssence()} auf die Cyberware-Summe, bevor
   * die Kombinationsregel (höhere Summe voll, niedrigere halb) greift.
   */
  DOUBLES_CYBERWARE_ESSENCE
}
