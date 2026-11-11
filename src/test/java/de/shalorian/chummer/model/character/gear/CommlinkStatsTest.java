package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Kommlink mit Betriebssystem als Einbau (GRW S. 260, Tabellen S. 374). */
class CommlinkStatsTest {

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
    CommlinkStats stats = Objects.requireNonNull(
        commlinkWith("Meta Link", "Iris Orb").getCommlinkStats());

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
    CommlinkStats stats = Objects.requireNonNull(
        commlinkWith("Hermes Ikon", "Novatech Navi").getCommlinkStats());

    assertEquals(4, stats.processor());
    assertEquals(3, stats.signal());
    assertEquals(3, stats.firewall());
    assertEquals(4, stats.system());
    assertFalse(stats.isSystemCapped());
  }

  @Test
  void commlinkWithoutOperatingSystemHasNoFirewallAndSystem() {
    CommlinkStats stats = Objects.requireNonNull(new Gear(find("CMT Clip"), 0).getCommlinkStats());

    assertEquals(0, stats.firewall());
    assertEquals(0, stats.system());
    assertFalse(stats.isSystemCapped());
  }

  @Test
  void otherGearHasNoCommlinkStats() {
    assertNull(new Gear(find("Survival-Kit"), 0).getCommlinkStats());
    assertNull(new Gear(find("Iris Orb"), 0).getCommlinkStats());
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
}
