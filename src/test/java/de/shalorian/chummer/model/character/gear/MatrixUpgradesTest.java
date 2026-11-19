package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Einbauregeln für Hardware-Module und Softwarepakete (GRW S. 260). */
class MatrixUpgradesTest {

  private static final MatrixBase BASE = new MatrixBase(3, 3, 3, 3);

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void hardwareAtMostTwoAboveOriginal() {
    GearData processor = find("Prozessor-Modul");
    assertNull(MatrixUpgrades.problem(processor, 5, BASE, List.of()));
    assertEquals("Prozessor höchstens 5 (Ausgangswert 3 + 2)", MatrixUpgrades.problem(processor, 6, BASE, List.of()));
    assertEquals("Prozessor-Modul 3 bringt nichts: Prozessor ist schon 3",
        MatrixUpgrades.problem(processor, 3, BASE, List.of()));
  }

  @Test
  void onlyOneModuleOrPackagePerAttribute() {
    List<Gear> installed = List.of(new Gear(find("Signal-Modul"), 4), new Gear(find("Firewall-Paket"), 4));
    assertEquals("Für Signal ist bereits ein Modul eingebaut",
        MatrixUpgrades.problem(find("Signal-Modul"), 5, BASE, installed));
    assertEquals("Für Firewall ist bereits ein Paket eingebaut",
        MatrixUpgrades.problem(find("Firewall-Paket"), 6, BASE, installed));
    assertNull(MatrixUpgrades.problem(find("Prozessor-Modul"), 4, BASE, installed));
  }

  @Test
  void softwareHasNoPlusTwoLimitButNeedsAnOperatingSystem() {
    assertNull(MatrixUpgrades.problem(find("System-Paket"), 6, BASE, List.of()));
    assertEquals("Ein Softwarepaket braucht ein Betriebssystem.",
        MatrixUpgrades.problem(find("Firewall-Paket"), 4, new MatrixBase(3, 3, 0, 0), List.of()));
  }

  @Test
  void otherGearIsNoUpgrade() {
    assertNull(MatrixUpgrades.problem(find("Analyse"), 3, BASE, List.of()));
  }
}
