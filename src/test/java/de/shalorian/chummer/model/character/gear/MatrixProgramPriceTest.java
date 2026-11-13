package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Matrixprogramme mit den Preisspalten "bis Stufe 3" und "bis Stufe 6" (GRW S. 272/376, am Bild). */
class MatrixProgramPriceTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void hackingSwitchesPriceBandAfterRatingThree() {
    GearData hacking = find("Angriff");

    assertEquals(1_500, hacking.packCostFor(3)); // Stufe x 500 ¥
    assertEquals(4_000, hacking.packCostFor(4)); // Stufe x 1.000 ¥
    assertEquals("8E", hacking.availability().displayText(4)); // (Stufe x 2)E
  }

  @Test
  void standardProgramPrices() {
    GearData standard = find("Analyse");

    assertEquals(100, standard.packCostFor(2)); // Stufe x 50 ¥
    assertEquals(500, standard.packCostFor(5)); // Stufe x 100 ¥
    assertEquals("–", standard.availability().displayText(5));
  }

  @Test
  void agentIcPilotUsesPilotRating() {
    GearData agent = find("Agent");

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
  void everyProgramFromTheRulebookIsListed() {
    // GRW S. 271-273, am Bild: 7 Standard-, 15 Hackingprogramme, dazu Agent und Autosoft
    long standard = GearDataLoader.getAllGear().values().stream()
        .filter(d -> d.category() == GearCategory.MATRIX_PROGRAMS && d.cost().equals("Rating * 50")).count();
    long hacking = GearDataLoader.getAllGear().values().stream()
        .filter(d -> d.category() == GearCategory.MATRIX_PROGRAMS && d.cost().equals("Rating * 500")
            && !d.name().equals("Autosoft")).count();
    assertEquals(7, standard);
    assertEquals(15, hacking);
    assertEquals("4E", find("Schwarzer Hammer").availability().displayText(2));
  }

  @Test
  void hardwareModulePricesFromTheTable() {
    // GRW S. 261, Tabelle Preise für Hardware-Upgrades, am Bild
    GearData processor = find("Prozessor-Modul");
    GearData signal = find("Signal-Modul");
    int[] processorPrices = {250, 750, 1_250, 2_000, 4_000, 8_000};
    int[] signalPrices = {10, 50, 150, 500, 1_000, 3_000};
    int[] availability = {4, 4, 8, 8, 12, 16};
    for (int rating = 1; rating <= 6; rating++) {
      assertEquals(processorPrices[rating - 1], processor.packCostFor(rating));
      assertEquals(signalPrices[rating - 1], signal.packCostFor(rating));
      assertEquals(availability[rating - 1], processor.availability().evaluate(rating));
      assertEquals(availability[rating - 1], signal.availability().evaluate(rating));
    }
    assertEquals(MatrixAttribute.PROCESSOR, processor.matrixAttribute());
    assertEquals(MatrixAttribute.SIGNAL, signal.matrixAttribute());
  }

  @Test
  void softwarePackagesCostLikeSystemPrograms() {
    GearData firewall = find("Firewall-Paket");
    assertEquals(600, firewall.packCostFor(3)); // Stufe x 200 ¥
    assertEquals(2_500, firewall.packCostFor(5)); // Stufe x 500 ¥
    assertEquals(MatrixAttribute.FIREWALL, firewall.matrixAttribute());
    assertEquals(MatrixAttribute.SYSTEM, find("System-Paket").matrixAttribute());
  }

  @Test
  void singleBandItemsIgnoreTheThreshold() {
    // Automatischer Dietrich hat nur eine Preisspalte: Stufe x 200 ¥ auch ab Stufe 4
    assertEquals(1_000, find("Automatischer Dietrich").packCostFor(5));
  }
}
