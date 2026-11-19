package de.shalorian.chummer.model.critter;

/** Was das Modell an einer Critterkraft rechnet; alle übrigen Kräfte sind reine Beschreibung. */
public enum CritterPowerEffect {
  NONE,
  /** Angriff mit eigenem Schaden und PB aus dem Zusatz der Kraft (GRW S. 340). */
  NATURAL_WEAPON,
  /** Panzerung aus dem Zusatz, addiert sich zur getragenen (S. 340). */
  ARMOR,
  /** Wie {@link #ARMOR}; Schaden bis zur Panzerung prallt ganz ab (S. 342). */
  HARDENED_ARMOR
}
