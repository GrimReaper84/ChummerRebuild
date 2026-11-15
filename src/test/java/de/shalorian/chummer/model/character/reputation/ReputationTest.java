package de.shalorian.chummer.model.character.reputation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Straßenruf, Schlechter Ruf und Prominenz (GRW S. 308/309, Liste am Bild geprüft). */
class ReputationTest {

  private static Character character() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static void karma(Character c, int amount) {
    c.addKarmaEntry(new LogEntry("Karma", amount, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));
  }

  private static QualityData quality(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void streetCredRoundsNormally() {
    // S. 308: 35 verdientes Karma → 3,5 → aufgerundet 4
    Character c = character();
    karma(c, 35);
    assertEquals(4, c.getStreetCred().value());
    assertEquals("verdientes Karma 35 ÷ 10 = 4", c.getStreetCred().breakdown());
    Character d = character();
    karma(d, 34);
    assertEquals(3, d.getStreetCred().value());
  }

  @Test
  void onlyPositiveKarmaCounts() {
    Character c = character();
    karma(c, 20);
    karma(c, -15);
    karma(c, 15);
    assertEquals(35, c.getEarnedKarma());
    assertEquals(4, c.getStreetCred().value());
  }

  @Test
  void notorietyFromQualities() {
    Character c = character();
    assertEquals(0, c.getNotoriety().value());
    c.addQuality(quality("Ungebildet"), false);
    c.addQuality(quality("Gremlins 2"), false);
    c.addQuality(quality("Glück"), false);
    assertEquals(1, c.getNotoriety().value()); // +1 +1 −1
    c.getReputation().setNotorietyBonus(2);
    assertEquals(3, c.getNotoriety().value());
  }

  @Test
  void giftsCanMakeNotorietyNegative() {
    Character c = character();
    c.addQuality(quality("Unauffälligkeit"), false);
    assertEquals(-1, c.getNotoriety().value());
    assertEquals(0, c.getPublicAwareness().value());
  }

  @Test
  void publicAwarenessExample() {
    // S. 309: Straßenruf 7 und Schlechter Ruf 2 → (7 + 2) ÷ 3 = 3
    Character c = character();
    karma(c, 70);
    c.getReputation().setNotorietyBonus(2);
    assertEquals(3, c.getPublicAwareness().value());
    c.getReputation().setAwarenessAdjustment(-5);
    assertEquals(0, c.getPublicAwareness().value());
  }

  @Test
  void burningStreetCred() {
    // S. 308: je 2 geopferte Punkte Straßenruf 1 Punkt weniger Schlechter Ruf
    Character c = character();
    karma(c, 40);
    c.getReputation().setNotorietyBonus(1);
    assertTrue(c.canBurnStreetCred());
    c.burnStreetCred();
    assertEquals(2, c.getStreetCred().value());
    assertEquals(0, c.getNotoriety().value());
    assertFalse(c.canBurnStreetCred());
    assertThrows(IllegalStateException.class, c::burnStreetCred);
  }
}
