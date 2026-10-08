package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Matrixprogramme mit den Preisspalten "bis Stufe 3" und "bis Stufe 6" (GRW S. 376, am Bild). */
class MatrixProgramPriceTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void hackingSwitchesPriceBandAfterRatingThree() {
    GearData hacking = find("Matrixprogramm (Hacking)");

    assertEquals(1_500, hacking.packCostFor(3)); // Stufe x 500 ¥
    assertEquals(4_000, hacking.packCostFor(4)); // Stufe x 1.000 ¥
    assertEquals("8E", hacking.availability().displayText(4)); // (Stufe x 2)E
  }

  @Test
  void standardProgramPrices() {
    GearData standard = find("Matrixprogramm (Standard)");

    assertEquals(100, standard.packCostFor(2)); // Stufe x 50 ¥
    assertEquals(500, standard.packCostFor(5)); // Stufe x 100 ¥
    assertEquals("–", standard.availability().displayText(5));
  }

  @Test
  void agentIcPilotUsesPilotRating() {
    GearData agent = find("Matrixprogramm (Agent/IC/Pilot)");

    assertEquals(3_000, agent.packCostFor(3)); // Pilotstufe x 1.000 ¥
    assertEquals(15_000, agent.packCostFor(6)); // Pilotstufe x 2.500 ¥
    assertEquals("12", agent.availability().displayText(4)); // Stufe x 3
  }

  @Test
  void autosoftGoesUpToRatingFour() {
    GearData autosoft = find("Autosoft");

    assertEquals(1, autosoft.minRating());
    assertEquals(4, autosoft.maxRating());
    assertEquals(1_500, autosoft.packCostFor(3)); // Stufe x 500 ¥
    assertEquals(4_000, autosoft.packCostFor(4)); // Stufe x 1.000 ¥
    assertEquals("8", autosoft.availability().displayText(4));
  }

  @Test
  void singleBandItemsIgnoreTheThreshold() {
    // Automatischer Dietrich hat nur eine Preisspalte: Stufe x 200 ¥ auch ab Stufe 4
    assertEquals(1_000, find("Automatischer Dietrich").packCostFor(5));
  }
}
