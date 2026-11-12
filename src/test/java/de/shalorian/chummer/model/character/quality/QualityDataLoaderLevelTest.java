package de.shalorian.chummer.model.character.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Stufen und die Bonus-Tags für Zustandsmonitor und Astralraum. */
class QualityDataLoaderLevelTest {

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void levelsAreReadAndDefaultToOne() {
    assertEquals(3, q("Hohe Schmerztoleranz 3").level());
    assertEquals(2, q("Überlebenswille 2").level());
    assertEquals(4, q("Magieresistenz 4").level());
    assertEquals(1, q("Glück").level());
  }

  @Test
  void conditionMonitorAndAstralBonusesAreRead() {
    assertTrue(q("Hohe Schmerztoleranz 1").bonuses().contains(QualityBonusType.HIGH_PAIN_TOLERANCE));
    assertTrue(q("Niedrige Schmerztoleranz").bonuses().contains(QualityBonusType.LOW_PAIN_TOLERANCE));
    assertTrue(q("Überlebenswille 3").bonuses().contains(QualityBonusType.WILL_TO_LIVE));
    assertTrue(q("Zauberer").bonuses().contains(QualityBonusType.ASTRAL_ACCESS));
    assertTrue(q("Magieradept").bonuses().stream().noneMatch(b -> b == QualityBonusType.ASTRAL_ACCESS));
  }
}
