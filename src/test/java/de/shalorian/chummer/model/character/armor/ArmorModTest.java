package de.shalorian.chummer.model.character.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Einbau von Zubehör und Modifikationen in ein Rüstungsteil (GRW S. 372/373). */
class ArmorModTest {

  static Armor armor(String name) {
    ArmorData data = Objects.requireNonNull(ArmorDataLoader.findByName(name), name);
    // Kleidung mit Preisspanne (Lederjacke) braucht einen gewählten Preis
    return new Armor(data, true, data.hasVariablePrice() ? data.priceMin() : null);
  }

  static ArmorModData mod(String name) {
    return Objects.requireNonNull(ArmorModDataLoader.findByName(name), name);
  }

  @Test
  void accessoryFitsOnlyItsArmor() {
    assertNull(armor("Ganzkörperpanzerung").installProblem(mod("Chemische Versiegelung"), 0));
    assertEquals("Chemische Versiegelung gehört zu Ganzkörperpanzerung",
        armor("Panzerjacke").installProblem(mod("Chemische Versiegelung"), 0));
    assertNotNull(armor("Ganzkörperpanzerung").installProblem(mod("Helm (Urban Explorer Overall)"), 0));
  }

  @Test
  void modificationsNeedAnArmorThatAcceptsThem() {
    assertNull(armor("Lederjacke/-mantel").installProblem(mod("Feuerresistenz"), 2));
    assertNotNull(armor("Helm").installProblem(mod("Feuerresistenz"), 2));
  }

  @Test
  void shieldsTakeOnlyThreeModifications() {
    Armor shield = armor("Schutzschild");
    assertNull(shield.installProblem(mod("Elektrische Isolierung"), 1));
    assertNotNull(shield.installProblem(mod("Wärmedämpfung"), 1));
    assertNotNull(shield.installProblem(mod("Schockstreifen"), 0));
    assertEquals("Wärmedämpfung passt nicht an Schilde", shield.fitProblem(mod("Wärmedämpfung")));
    assertTrue(shield.fits(mod("Feuerresistenz")));
    assertFalse(armor("Panzerjacke").fits(mod("Helm (Ganzkörperpanzerung)")));
  }

  @Test
  void shockFrillsNeedElectricalInsulationAndProtectIt() {
    Armor jacket = armor("Panzerjacke");
    assertEquals("Nur zusammen mit Elektrische Isolierung", jacket.installProblem(mod("Schockstreifen"), 0));
    ArmorMod insulation = new ArmorMod(mod("Elektrische Isolierung"), 2);
    jacket.install(insulation);
    jacket.install(new ArmorMod(mod("Schockstreifen"), 0));
    assertEquals("Schockstreifen braucht Elektrische Isolierung", jacket.uninstallProblem(insulation));
    assertFalse(jacket.uninstall(insulation));
  }

  @Test
  void eachModificationOnlyOnceAndWithValidRating() {
    Armor jacket = armor("Panzerjacke");
    jacket.install(new ArmorMod(mod("Feuerresistenz"), 3));
    assertEquals("Feuerresistenz ist bereits eingebaut", jacket.installProblem(mod("Feuerresistenz"), 2));
    assertThrows(IllegalArgumentException.class, () -> new ArmorMod(mod("Feuerresistenz"), 7));
  }

  @Test
  void priceAndValuesIncludeMods() {
    Armor fullBody = armor("Ganzkörperpanzerung");
    fullBody.install(new ArmorMod(mod("Helm (Ganzkörperpanzerung)"), 0));
    fullBody.install(new ArmorMod(mod("Thermische Isolierung"), 4));
    assertEquals(12, fullBody.getBallistic());
    assertEquals(10, fullBody.getImpact());
    assertEquals(6000 + 1000 + 4 * 150, fullBody.getNuyenCost());
    assertEquals(4, fullBody.getProtection(ArmorProtection.COLD));
    assertEquals(0, fullBody.getProtection(ArmorProtection.FIRE));
  }

  @Test
  void surchargeCountsOnTheArmorsAvailability() {
    // Ganzkörperpanzerung 14E, Chemische Versiegelung +6 → 20 (S. 373)
    assertEquals(20, armor("Ganzkörperpanzerung").availabilityWith(mod("Chemische Versiegelung"), 0));
    assertEquals(6, armor("Panzerjacke").availabilityWith(mod("Elektrische Isolierung"), 3));
    assertTrue(mod("Chemische Versiegelung").availabilitySurcharge());
  }
}
