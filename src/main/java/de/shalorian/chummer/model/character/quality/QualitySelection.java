package de.shalorian.chummer.model.character.quality;

/** Was beim Kauf einer Quality zusätzlich gewählt werden muss (Tag {@code <select>} in {@code qualities.xml}). */
public enum QualitySelection {
  /** Keine Auswahl. */
  NONE,
  /** Eine aktive Fertigkeit (z.B. Talentiert, Inkompetenz). */
  SKILL,
  /** Ein Körperliches oder Geistiges Attribut (Außergewöhnliches Attribut). */
  ATTRIBUTE,
  /** Freitext (z.B. Substanz einer Allergie, Geisterart, Gebiet). */
  TEXT
}
