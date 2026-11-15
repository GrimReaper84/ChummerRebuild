package de.shalorian.chummer.persistence;

import java.util.Arrays;
import org.jspecify.annotations.NonNull;

/**
 * Gemerkter Stand eines Charakters, z.B. vor einer Karma- oder Nuyen-Buchung für „Rückgängig“ (ADR-058). Enthält das
 * Speicherformat als Bytes, damit spätere Änderungen am Charakter den Stand nicht berühren; wiederhergestellt über
 * {@link CharacterStore#restore(CharacterSnapshot)}.
 */
public final class CharacterSnapshot {

  private final byte @NonNull [] json;

  CharacterSnapshot(byte @NonNull [] json) {
    this.json = Arrays.copyOf(json, json.length);
  }

  byte @NonNull [] json() {
    return Arrays.copyOf(json, json.length);
  }
}
