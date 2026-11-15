package de.shalorian.chummer.model.character.situation;

import org.jspecify.annotations.NonNull;

/**
 * Konkreter Anlass eines Pools: Grundlage (Fertigkeit oder Probe) und Einschränkung, z.B. „Spruchzauberei“ /
 * „Kampfzauber“; leere Einschränkung = die Grundlage allgemein.
 */
record SituationKey(@NonNull String base, @NonNull String detail) {

  @NonNull String label() {
    return detail.isEmpty() ? base : base + " · " + detail;
  }

  /** Der allgemeine Anlass derselben Grundlage, dessen Boni auch hier gelten. */
  @NonNull SituationKey general() {
    return new SituationKey(base, "");
  }
}
