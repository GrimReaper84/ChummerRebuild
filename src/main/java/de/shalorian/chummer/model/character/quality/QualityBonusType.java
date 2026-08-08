package de.shalorian.chummer.model.character.quality;

public enum QualityBonusType {
  UNEDUCATED,        // Technical Active Skills kosten doppelt
  UNCOUTH,           // Social Active Skills kosten doppelt
  INFIRM,            // Physical Active Skills kosten doppelt
  GRANTS_MAGIC,          // schaltet das Magic-Sonderattribut frei (Adept/Magier/Mystischer Adept)
  GRANTS_RESONANCE,      // schaltet das Resonance-Sonderattribut frei (Technomant)
  GRANTS_SPELLCASTING    // schaltet magische Aktionsfertigkeiten + Zauber frei (nur Magier, nicht Adept)
}