package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Kommlink mit Betriebssystem, Modulen, Paketen, Programmen und Sim-Modul als Einbau (GRW S. 260/261, 271, 374). */
class CommlinkMatrixTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  private static Gear commlinkWith(String commlink, String os) {
    Gear device = new Gear(find(commlink), 0);
    device.install(new Gear(find(os), 0));
    return device;
  }

  @Test
  void systemIsCappedByProcessor() {
    // Meta Link: Prozessor 1; Iris Orb: Firewall 3, System 3 → System 1 (GRW S. 260)
    MatrixStats stats = Objects.requireNonNull(
        commlinkWith("Meta Link", "Iris Orb").getMatrixStats());

    assertEquals(1, stats.processor());
    assertEquals(2, stats.signal());
    assertEquals(3, stats.firewall());
    assertEquals(1, stats.system());
    assertEquals(3, stats.nominalSystem());
    assertTrue(stats.isSystemCapped());
  }

  @Test
  void systemWithinProcessorStaysUnchanged() {
    // Hermes Ikon: Prozessor 4, Signal 3; Novatech Navi: Firewall 3, System 4
    MatrixStats stats = Objects.requireNonNull(
        commlinkWith("Hermes Ikon", "Novatech Navi").getMatrixStats());

    assertEquals(4, stats.processor());
    assertEquals(3, stats.signal());
    assertEquals(3, stats.firewall());
    assertEquals(4, stats.system());
    assertFalse(stats.isSystemCapped());
  }

  @Test
  void commlinkWithoutOperatingSystemHasNoFirewallAndSystem() {
    MatrixStats stats = Objects.requireNonNull(new Gear(find("CMT Clip"), 0).getMatrixStats());

    assertEquals(0, stats.firewall());
    assertEquals(0, stats.system());
    assertFalse(stats.isSystemCapped());
  }

  @Test
  void otherGearHasNoMatrixStats() {
    assertNull(new Gear(find("Survival-Kit"), 0).getMatrixStats());
    assertNull(new Gear(find("Iris Orb"), 0).getMatrixStats());
  }

  @Test
  void commlinkTakesExactlyOneOperatingSystem() {
    Gear device = commlinkWith("Sony Emperor", "Vector Xim");

    assertEquals("Sony Emperor hat bereits ein Betriebssystem",
        device.installProblem(find("Redcap Nix"), 0));
    assertThrows(IllegalArgumentException.class,
        () -> device.install(new Gear(find("Redcap Nix"), 0)));
    assertEquals(1, device.getInstalled().size());
  }

  @Test
  void onlyOperatingSystemsFitOnlyIntoCommlinks() {
    Gear commlink = new Gear(find("Meta Link"), 0);
    Gear kit = new Gear(find("Survival-Kit"), 0);

    assertTrue(commlink.installProblem(find("Survival-Kit"), 0) != null);
    assertTrue(kit.installProblem(find("Iris Orb"), 0) != null);
    assertNull(commlink.installProblem(find("Iris Orb"), 0));
  }

  @Test
  void operatingSystemCountsIntoCommlinkPriceAndCanBeUninstalled() {
    // Erika Elite 2.500 ¥ + Mangadyne Deva 800 ¥
    Gear device = commlinkWith("Erika Elite", "Mangadyne Deva");
    assertEquals(3_300, device.getNuyenCost());

    Gear os = device.getInstalled().getFirst();
    assertTrue(device.uninstall(os));
    assertFalse(device.uninstall(os));
    assertEquals(2_500, device.getNuyenCost());
  }

  @Test
  void commlinksAreSingleItemsAndNeverMerged() {
    GearData metaLink = find("Meta Link");

    assertThrows(IllegalArgumentException.class, () -> new Gear(metaLink, 0, 2, "", null));
    assertFalse(new Gear(metaLink, 0).isSameKind(new Gear(metaLink, 0)));
    assertFalse(new Gear(metaLink, 0).canRemovePack());
  }

  @Test
  void commlinkTakesModulesPackagesProgramsAndOneSimModule() {
    // Hermes Ikon: Prozessor 4, Signal 3; Novatech Navi: Firewall 3, System 4 (S. 374)
    Gear device = commlinkWith("Hermes Ikon", "Novatech Navi");
    device.install(new Gear(find("Prozessor-Modul"), 6));
    device.install(new Gear(find("System-Paket"), 6));
    Gear analyse = new Gear(find("Analyse"), 6);
    device.install(analyse);
    device.install(new Gear(find("Sim-Modul"), 0, 1, "", 100));
    assertEquals("Hermes Ikon hat bereits ein Sim-Modul",
        device.installProblem(find("Sim-Modul (für BTL/heißes Sim modifiziert)"), 0));
    assertEquals("AR-Handschuhe lässt sich nicht in Hermes Ikon einbauen",
        device.installProblem(find("AR-Handschuhe"), 0));
    assertEquals("Analyse ist bereits eingebaut", device.installProblem(find("Analyse"), 2));

    MatrixStats stats = Objects.requireNonNull(device.getMatrixStats());
    assertEquals(6, stats.processor());
    assertEquals(6, stats.system());
    assertEquals(6, stats.programRating(analyse.getRating()));
  }

  @Test
  void removingTheOperatingSystemAlsoRemovesSoftwarePackages() {
    Gear device = commlinkWith("Hermes Ikon", "Novatech Navi");
    Gear firewall = new Gear(find("Firewall-Paket"), 5);
    device.install(firewall);
    device.install(new Gear(find("Signal-Modul"), 5));
    device.uninstall(Objects.requireNonNull(device.getOperatingSystem()));

    assertEquals(1, device.getInstalled().size());
    assertEquals(0, Objects.requireNonNull(device.getMatrixStats()).firewall());
  }

  @Test
  void onlyProgramsCanRun() {
    assertThrows(IllegalStateException.class, () -> new Gear(find("Survival-Kit"), 0).setRunning(true));
  }
}
