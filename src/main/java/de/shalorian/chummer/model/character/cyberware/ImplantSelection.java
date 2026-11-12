package de.shalorian.chummer.model.character.cyberware;

/** Was beim Kauf eines Implantats gewählt wird (Reflexrekorder, GRW S. 394). */
public enum ImplantSelection {
  NONE,
  /** Eine aktive Fertigkeit aus den Kampf- oder körperlichen Fertigkeiten. */
  SKILL,
  /** Eine Fertigkeitsgruppe aus Kampf- oder körperlichen Fertigkeiten. */
  SKILL_GROUP
}
