package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.quality.CharacterQuality;
import org.jspecify.annotations.NonNull;

/** Hilfen für Tests rund um gewählte Qualities. */
final class QualityTestSupport {

  private QualityTestSupport() {
  }

  /** Die erste gewählte Quality dieses Namens; der Test scheitert, wenn es keine gibt. */
  static @NonNull CharacterQuality selected(@NonNull Character c, @NonNull String name) {
    return c.getSelectedQualities().stream()
        .filter(q -> q.data().name().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Quality nicht gewählt: " + name));
  }
}
