package de.shalorian.chummer.model.character.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Adept, Zauberer, Magieradept und Technomancer gibt es nur bei der Erschaffung (GRW S. 313). */
class QualityCareerTest {

  private static QualityData quality(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void awakeningBlockedInCareer() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    for (String name : List.of("Adept", "Zauberer", "Magieradept", "Technomancer")) {
      assertNull(c.getQualityBlockReason(quality(name), false), name);
    }
    c.setCreated(true);
    for (String name : List.of("Adept", "Zauberer", "Magieradept", "Technomancer")) {
      assertEquals("Nur bei der Erschaffung wählbar (S. 313)", c.getQualityBlockReason(quality(name), false), name);
    }
    assertNull(c.getQualityBlockReason(quality("Beidhändigkeit"), false));
  }
}
