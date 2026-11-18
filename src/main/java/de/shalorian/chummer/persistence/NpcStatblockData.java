package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

/** Gespeicherter Werteblock einer Schergengruppe: der Charakter im üblichen Format und der Ausrüstungstext. */
class NpcStatblockData {

  public @NonNull CharacterData character = new CharacterData();
  public @NonNull String gear = "";

  public NpcStatblockData() {
  }

  NpcStatblockData(@NonNull CharacterData character, @NonNull String gear) {
    this.character = character;
    this.gear = gear;
  }
}
