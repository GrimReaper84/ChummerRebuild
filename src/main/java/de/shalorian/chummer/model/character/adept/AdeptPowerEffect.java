package de.shalorian.chummer.model.character.adept;

/**
 * Rechnerische Wirkung einer Adeptenkraft, die der Charakter auswertet (Tag {@code <effect>} in
 * {@code adept_powers.xml}). Kräfte mit situativer Wirkung tragen {@link #NONE} und nur einen Hinweis.
 */
public enum AdeptPowerEffect {
  /** Keine Rechnung, Wirkung nur als Hinweis. */
  NONE,
  /** Gesteigerte Reflexe (GRW S. 230): je Stufe Reaktion +1 und ein Initiativedurchgang mehr. */
  IMPROVED_REFLEXES,
  /** Gesteigertes körperliches Attribut (GRW S. 230): je Stufe das gewählte Attribut +1. */
  IMPROVED_ATTRIBUTE,
  /** Verbesserte Fertigkeit (GRW S. 232): je Stufe +1 Würfel auf die gewählte Fertigkeit. */
  IMPROVED_SKILL,
  /** Verbesserte Wahrnehmung (GRW S. 232): je Stufe +1 Würfel auf Wahrnehmung und Askennen. */
  IMPROVED_PERCEPTION,
  /** Schmerzresistenz (GRW S. 231): je Stufe ein Kästchen je Spalte ohne Verletzungsmodifikator. */
  PAIN_RESISTANCE,
  /** Mystischer Panzer (GRW S. 231): je Stufe +1 Ballistik und Stoß. */
  MYSTIC_ARMOR,
  /** Astrale Wahrnehmung (GRW S. 230): Zugang zu Askennen und Astralkampf. */
  ASTRAL_PERCEPTION
}
