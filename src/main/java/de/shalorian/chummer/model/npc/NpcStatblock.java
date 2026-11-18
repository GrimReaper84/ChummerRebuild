package de.shalorian.chummer.model.npc;

import de.shalorian.chummer.model.character.Character;
import org.jspecify.annotations.NonNull;

/**
 * Werteblock eines Schergen oder Anführers. Die Werte liegen in einem {@link Character} im Spielleiter-Modus, damit
 * Würfelpools, Initiative und Panzerung aus dem vorhandenen Modell kommen (ADR-062); dazu die sonstige Ausrüstung als
 * Text (Kommlinks, Drogen, Molotows …), die der Bogen nicht weiter auswertet.
 */
public class NpcStatblock {

  private final @NonNull Character character;
  private @NonNull String gearText;

  public NpcStatblock(@NonNull Character character, @NonNull String gearText) {
    this.character = character;
    this.gearText = gearText;
    character.setCreated(true);
    character.setGmMode(true);
  }

  public @NonNull Character getCharacter() {
    return character;
  }

  public @NonNull String getGearText() {
    return gearText;
  }

  public void setGearText(@NonNull String gearText) {
    this.gearText = gearText;
  }

  /** Kästchen des gemeinsamen Zustandsmonitors ({@link NpcRules#monitorBoxes}). */
  public int getMonitorBoxes() {
    return NpcRules.monitorBoxes(character);
  }
}
