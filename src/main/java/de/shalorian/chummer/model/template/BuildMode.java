package de.shalorian.chummer.model.template;

/** Wie {@link CharacterSpec#build} einen Charakter anlegt. */
public enum BuildMode {
  /** Werteblock der Spielleitung: fertig erschaffen, Spielleiter-Modus, Kosten spielen keine Rolle (Schergen). */
  GM,
  /** Normale Erschaffung: Käufe kosten BP bzw. Ressourcen-Nuyen wie von Hand (Archetypen). */
  CREATION
}
