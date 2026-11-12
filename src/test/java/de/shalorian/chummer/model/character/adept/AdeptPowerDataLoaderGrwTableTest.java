package de.shalorian.chummer.model.character.adept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.quality.QualitySelection;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Katalog der Adeptenkräfte gegen das GRW (S. 229–232, am Seitenbild geprüft). */
class AdeptPowerDataLoaderGrwTableTest {

  private static AdeptPowerData power(String name) {
    AdeptPowerData data = AdeptPowerDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  @Test
  void loadsAllNineteenCorePowers() {
    Map<String, AdeptPowerData> powers = AdeptPowerDataLoader.getAllPowers();
    assertEquals(19, powers.size());
    powers.values().forEach(p -> assertFalse(p.note().isBlank(), p.name()));
  }

  @Test
  void pagesMatchRulebook() {
    assertEquals(229, power("Adrenalinkick").page());
    assertEquals(230, power("Astrale Wahrnehmung").page());
    assertEquals(230, power("Kampfsinn").page());
    assertEquals(231, power("Körpersprache").page());
    assertEquals(231, power("Schmerzresistenz").page());
    assertEquals(232, power("Stimmkontrolle").page());
    assertEquals(232, power("Verbesserte Wahrnehmung").page());
  }

  @Test
  void improvedReflexesUseCostTableUpToLevelThree() {
    AdeptPowerData reflexes = power("Gesteigerte Reflexe");
    assertEquals(List.of(new BigDecimal("1.5"), new BigDecimal("2.5"), new BigDecimal("4")), reflexes.costTable());
    assertEquals(3, reflexes.maxLevel());
    assertEquals(AdeptPowerEffect.IMPROVED_REFLEXES, reflexes.effect());
  }

  @Test
  void costsAndLevelsMatchRulebook() {
    assertEquals(0, new BigDecimal("1").compareTo(power("Astrale Wahrnehmung").cost()));
    assertFalse(power("Astrale Wahrnehmung").leveled());
    assertEquals(0, new BigDecimal("0.75").compareTo(power("Gesteigertes körperliches Attribut").cost()));
    assertEquals(0, new BigDecimal("0.5").compareTo(power("Mystischer Panzer").cost()));
    assertEquals(0, new BigDecimal("0.5").compareTo(power("Todeskralle").cost()));
    assertFalse(power("Todeskralle").leveled());
    assertEquals(3, power("Körpersprache").maxLevel());
    assertTrue(power("Kampfsinn").leveled());
    assertEquals(0, power("Kampfsinn").maxLevel());
  }

  @Test
  void selectionsAndMultiples() {
    AdeptPowerData sense = power("Geschärfter Sinn");
    assertEquals(QualitySelection.TEXT, sense.selection());
    assertTrue(sense.multiple());
    assertFalse(sense.leveled());
    assertEquals(QualitySelection.ATTRIBUTE, power("Gesteigertes körperliches Attribut").selection());
    assertEquals(QualitySelection.ATTRIBUTE, power("Adrenalinkick").selection());
    assertEquals(QualitySelection.SKILL, power("Verbesserte Fertigkeit").selection());
    assertTrue(power("Verbesserte Fertigkeit").multiple());
  }

  @Test
  void sevenPowersHaveCalculatedEffects() {
    long calculated = AdeptPowerDataLoader.getAllPowers().values().stream()
        .filter(p -> p.effect() != AdeptPowerEffect.NONE)
        .count();
    assertEquals(7, calculated);
  }
}
