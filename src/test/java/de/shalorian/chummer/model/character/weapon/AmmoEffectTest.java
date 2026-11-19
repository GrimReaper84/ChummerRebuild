package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Munitionswirkung auf Schaden und PB (GRW S. 369, Tabelle am Bild geprüft). */
class AmmoEffectTest {

  private static AmmoEffect effect(String name, String damage, String ap, String armor) {
    return Objects.requireNonNull(AmmoEffect.fromTable(name, damage, ap, armor));
  }

  @Test
  void predatorWithApdsFromTheRulebookExample() {
    // S. 369: Ares Predator IV (Schaden 5, PB −1) mit APDS (PB −4) → Schaden 5, PB −5
    AmmoEffect apds = effect("APDS-Geschosse", "–", "−4", "Ballistisch");
    assertEquals("5K", apds.applyToDamage("5K"));
    assertEquals("−5", apds.applyToArmorPiercing("−1", "5K"));
  }

  @Test
  void explosiveAndExExplosive() {
    assertEquals("6K", effect("Explosivgeschosse", "+1", "–", "Ballistisch").applyToDamage("5K"));
    AmmoEffect ex = effect("EX-Explosivgeschosse", "+1", "−1", "Ballistisch");
    assertEquals("6K", ex.applyToDamage("5K"));
    assertEquals("−2", ex.applyToArmorPiercing("−1", "5K"));
    assertEquals("−1", ex.applyToArmorPiercing("–", "5K"));
  }

  @Test
  void gelRoundsDealStunDamage() {
    AmmoEffect gel = effect("Gelgeschosse", "(−1)G", "+2", "Stoß");
    assertEquals("4G", gel.applyToDamage("5K"));
    assertEquals("+1", gel.applyToArmorPiercing("−1", "5K"));
    assertEquals("Stoß", gel.armorType());
  }

  @Test
  void stickNShockReplacesDamageAndAp() {
    AmmoEffect shock = effect("Schocker", "6G(e)", "−Hälfte", "Stoß");
    assertEquals("6G(e)", shock.applyToDamage("5K"));
    assertEquals("−Hälfte", shock.applyToArmorPiercing("−1", "5K"));
  }

  @Test
  void flechetteIsIgnoredWhenAlreadyIncluded() {
    // S. 369: bei Waffen mit (f) ist Flechette schon eingerechnet
    AmmoEffect flechette = effect("Flechettegeschosse", "+2", "+5", "Stoß");
    assertEquals("7K", flechette.applyToDamage("5K"));
    assertEquals("+5", flechette.applyToArmorPiercing("–", "5K"));
    assertEquals("8K(f)", flechette.applyToDamage("8K(f)"));
    assertEquals("+5", flechette.applyToArmorPiercing("+5", "8K(f)"));
  }

  @Test
  void nonNumericValuesStayAndFixedWeaponAmmoIsNotLoadable() {
    assertEquals("−Hälfte",
        effect("APDS-Geschosse", "–", "−4", "Ballistisch").applyToArmorPiercing("−Hälfte", "6G(e)"));
    assertNull(AmmoEffect.fromTable("Taserpfeil", "wie Taser", "−Hälfte", "Stoß"));
    assertNull(AmmoEffect.fromTable("Sturmkanonen-Geschosse", "wie Kanone", "wie Kanone", "Ballistisch"));
  }
}
