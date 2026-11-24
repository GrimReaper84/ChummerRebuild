package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.option.RuleOptions;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Mitgeliefertes Zubehör ersetzen und ausbauen (ADR-071): GRW S. 357 (RK im Tabellenwert), S. 367 (Gasventil) und
 * der Archetyp Straßensamurai (Ingram Smartgun X mit Gasventilsystem 3 statt des mitgelieferten Gasventils 2).
 */
class WeaponIncludedTest {

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name)));
  }

  private static AccessoryData accessory(String name) {
    return Objects.requireNonNull(AccessoryDataLoader.findByName(name));
  }

  private static List<String> includedNames(Weapon weapon) {
    return weapon.getIncluded().stream().map(IncludedAccessory::name).toList();
  }

  @Test
  void gasVent3ReplacesIncludedGasVent2() {
    Weapon ingram = weapon("Ingram Smartgun X");
    assertEquals(new Recoil(2, 3), ingram.getRecoil());

    ingram.addAccessory(new WeaponAccessory(accessory("Gasventilsystem 3"), Mount.BARREL));

    // 2 (3) − Gasventil 2 + Gasventil 3 = 3 (4); fest eingebaut, ohne Halterung, Preis 400 ¥
    assertEquals(new Recoil(3, 4), ingram.getRecoil());
    WeaponAccessory vent = ingram.getAccessories().getFirst();
    assertNull(vent.mount());
    assertEquals("Gasventilsystem 2", vent.replaces());
    assertFalse(includedNames(ingram).contains("Gasventilsystem 2"));
    assertEquals(650 + 400, ingram.getTotalNuyenCost());
    assertTrue(ingram.freeMounts(accessory("Schalldämpfer")).contains(Mount.BARREL));
  }

  @Test
  void gasVentCannotBeReplacedByEqualOrWeaker() {
    Weapon ingram = weapon("Ingram Smartgun X");
    assertNull(ingram.replacementFor(accessory("Gasventilsystem 2")));
    Weapon ultimax = weapon("Ultimax HMG-2");
    assertNull(ultimax.replacementFor(accessory("Gasventilsystem 3")));
    assertNotNull(ultimax.mountProblem(accessory("Gasventilsystem 2")));
  }

  @Test
  void internalWhiteKnightVentNotUpgradeable() {
    Weapon whiteKnight = weapon("Ingram White Knight");
    assertNull(whiteKnight.replacementFor(accessory("Gasventilsystem 3")));
    String problem = whiteKnight.mountProblem(accessory("Gasventilsystem 3"));
    assertNotNull(problem);
    assertFalse(problem.contains("stärkeres"));
  }

  @Test
  void smartgunReplacesIncludedSmartgun() {
    Weapon predator = weapon("Ares Predator IV");
    predator.addAccessory(new WeaponAccessory(accessory("Smartgunsystem, intern"), null));

    assertTrue(predator.hasSmartgun());
    assertTrue(predator.getIncluded().isEmpty());
    assertNotNull(predator.mountProblem(accessory("Smartgunsystem, extern")));
  }

  @Test
  void removingIncludedGasVentLowersRecoilAndFreesRule() {
    Weapon ingram = weapon("Ingram Smartgun X");
    IncludedAccessory vent = ingram.getIncluded().getFirst();

    ingram.removeIncluded(vent.name());

    assertEquals(new Recoil(0, 1), ingram.getRecoil());
    assertEquals(List.of("Gasventilsystem 2"), ingram.getRetiredIncluded());
    // Danach ist ein Gasventil wieder ein normaler Kauf mit Laufhalterung.
    assertNull(ingram.mountProblem(accessory("Gasventilsystem 2")));
    assertNull(ingram.replacementFor(accessory("Gasventilsystem 2")));
  }

  @Test
  void removingIncludedShockPadLowersOnlyRemovableRecoil() {
    Weapon ruger = weapon("Ruger 100");
    IncludedAccessory pad = ruger.getIncluded().stream().filter(i -> i.name().equals("Schockpolster")).findFirst()
        .orElseThrow();

    ruger.removeIncluded(pad.name());

    assertEquals(Recoil.NONE, ruger.getRecoil());
  }

  @Test
  void removingReplacementBringsIncludedPartBack() {
    // Ohne Regel-Option darf ein mitgeliefertes Teil nicht verloren gehen (ADR-071).
    Weapon predator = weapon("Ares Predator IV");
    predator.addAccessory(new WeaponAccessory(accessory("Smartgunsystem, intern"), null));

    predator.removeAccessory(predator.getAccessories().getFirst());

    assertEquals(List.of("Smartgunsystem"), includedNames(predator));
    assertTrue(predator.getRetiredIncluded().isEmpty());
    assertTrue(predator.hasSmartgun());
  }

  @Test
  void removingIncludedNeedsRuleOption() {
    RuleOptions options = new RuleOptions();
    assertNotNull(Weapon.includedRemovalProblem(options));
    options.setRemoveIncludedParts(true);
    assertNull(Weapon.includedRemovalProblem(options));
  }

  @Test
  void includedDuplicateIsReportedByName() {
    Weapon ingram = weapon("Ingram Smartgun X");
    assertEquals("Schallunterdrücker",
        Objects.requireNonNull(ingram.includedDuplicate(accessory("Schallunterdrücker"))).name());
    assertNull(ingram.includedDuplicate(accessory("Schalldämpfer")));
  }

  @Test
  void includedLaserSightCounts() {
    // Uzi IV mit eingebautem Lasermarkierer (GRW S. 363); die Regel gibt +1 im Pool (S. 368).
    assertTrue(weapon("Uzi IV").hasLaserSight());
  }
}
