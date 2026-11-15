package de.shalorian.chummer.model.character.situation;

import org.jspecify.annotations.NonNull;

/**
 * Würfelpool für einen Anlass, z.B. „Spruchzauberei · Kampfzauber“.
 *
 * @param label     Anlass
 * @param dice      Würfel, Verletzungsmodifikator eingerechnet (außer beim Schadenswiderstand, S. 192); bei
 *                  {@code bonusOnly} nur die Summe der Boni
 * @param breakdown Aufschlüsselung für den Tooltip
 * @param bonusOnly ob es keinen Grundpool gibt und nur der Bonus angezeigt wird
 */
public record SituationalPool(@NonNull String label, int dice, @NonNull String breakdown, boolean bonusOnly) {

  /** Würfel für die Anzeige, bei {@code bonusOnly} mit Vorzeichen: „+2“. */
  public @NonNull String diceText() {
    return (bonusOnly ? "+" : "") + dice;
  }
}
