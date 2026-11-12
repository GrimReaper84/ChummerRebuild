package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.gear.GearCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Prüft die Implantat-Kataloge gegen die am PDF-Bild gelesenen GRW-Tabellen (Headware S. 386, Augen-/Ohren-Cyberware
 * S. 388, Bodyware S. 389, Bioware S. 394): Anzahl je Kategorie, alle Stufenlisten und Stichproben je Tabelle.
 */
class CyberwareDataLoaderGrwTableTest {

  private static CyberwareData find(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  private static Cyberware standard(String name, int rating) {
    return new Cyberware(find(name), CyberwareGrade.STANDARD, rating);
  }

  @Test
  void countsPerCategoryMatchTables() {
    Map<String, Long> counts = CyberwareDataLoader.getAllCyberware().values().stream()
        .collect(Collectors.groupingBy(CyberwareData::category, Collectors.counting()));

    assertEquals(16, counts.get("Headware"));
    assertEquals(12, counts.get("Augen-Cyberware"));
    assertEquals(8, counts.get("Ohren-Cyberware"));
    assertEquals(15, counts.get("Bodyware"));
    assertEquals(17, counts.get("Standard-Bioware"));
    assertEquals(8, counts.get("Kultivierte Bioware"));
  }

  @Test
  void everyEntryHasSourceAndPage() {
    for (CyberwareData data : CyberwareDataLoader.getAllCyberware().values()) {
      assertEquals("SR4", data.source(), data.name());
      assertFalse(data.page().isEmpty(), data.name());
    }
  }

  @Test
  void reflexBoosterUsesTableValuesPerRating() {
    List<String> essence = List.of("2", "3", "5");
    List<Integer> cost = List.of(11_000, 32_000, 100_000);
    List<String> avail = List.of("8E", "12E", "20E");
    for (int rating = 1; rating <= 3; rating++) {
      Cyberware booster = standard("Reflexbooster", rating);
      assertEquals(new BigDecimal(essence.get(rating - 1)), booster.getEssenceCost());
      assertEquals(cost.get(rating - 1), booster.getNuyenCost());
      assertEquals(avail.get(rating - 1), booster.getData().availability().displayText(rating));
    }
  }

  @Test
  void eyeAndEarBaseSystemsUseTableValuesPerRating() {
    List<String> essence = List.of("0.2", "0.3", "0.4", "0.5");
    List<Integer> cost = List.of(500, 750, 1000, 1500);
    List<String> avail = List.of("–", "4", "6", "8");
    for (String name : List.of("Cyberaugen-Basissystem", "Cyberohren")) {
      for (int rating = 1; rating <= 4; rating++) {
        Cyberware base = standard(name, rating);
        assertEquals(0, new BigDecimal(essence.get(rating - 1)).compareTo(base.getEssenceCost()), name + rating);
        assertEquals(cost.get(rating - 1), base.getNuyenCost(), name + rating);
        assertEquals(avail.get(rating - 1), base.getData().availability().displayText(rating), name + rating);
        assertEquals(rating * 4, base.getData().capacityFor(rating), name + rating);
      }
    }
  }

  @Test
  void headwareSamples() {
    assertEquals("20V", find("Cortexbombe (Flächenbombe)").availability().displayText(0));
    assertEquals(10_000, standard("Cortexbombe (Flächenbombe)", 0).getNuyenCost());
    assertEquals(0, standard("Cortexbombe (Spleenbombe)", 0).getEssenceCost().signum());
    assertEquals(new BigDecimal("0.3"), standard("Ultraschallsensor", 0).getEssenceCost());
    assertEquals(9_000, standard("Geschmacksbooster", 6).getNuyenCost());
    assertEquals("12V", find("Sim-Modul (modifiziert für heißes Sim)").availability().displayText(0));
    assertEquals("18V", find("Zweitmuster").availability().displayText(6));
    assertEquals(30_000, standard("Zweitmuster", 6).getNuyenCost());
    assertEquals(GearCategory.COMMLINKS, find("Kommlink").acceptedGear());
    assertNull(find("Datenbuchse").acceptedGear());
  }

  @Test
  void eyeAndEarAccessorySamples() {
    assertEquals("*", find("Augen-Rekorder").capacityCost());
    assertFalse(find("Bildverbindung").isInstallable());
    assertEquals(6, find("Okular-Drohne").capacityCostFor(0));
    assertTrue(find("Okular-Drohne").restrictions().installOnly());
    assertEquals(3, find("Sichtverbesserung").capacityCostFor(3));
    assertEquals("16V", find("Retinaduplikat").availability().displayText(6));
    assertEquals(90_000, standard("Retinaduplikat", 6).getNuyenCost());
    assertEquals(4, find("Balanceverstärker").capacityCostFor(0));
    assertEquals("10", find("Balanceverstärker").availability().displayText(0));
    assertEquals(6_000, standard("Selektiver Geräuschfilter", 6).getNuyenCost());
  }

  @Test
  void bodywareSamples() {
    assertEquals(new BigDecimal("1.5"), standard("Kompositknochen (Titan)", 0).getEssenceCost());
    assertEquals("16V", find("Kompositknochen (Titan)").availability().displayText(0));
    assertEquals(new BigDecimal("0.9"), standard("Reaktionsverbesserung", 3).getEssenceCost());
    assertEquals("15E", find("Reaktionsverbesserung").availability().displayText(3));
    assertEquals(20_000, standard("Kunstmuskeln", 4).getNuyenCost());
    CyberwareData cosmetic = find("Kosmetische Modifikation");
    assertTrue(cosmetic.isValidPrice(200));
    assertTrue(cosmetic.isValidPrice(10_000));
    assertFalse(cosmetic.isValidPrice(10_001));
  }

  @Test
  void biowareSamples() {
    assertEquals(new BigDecimal("2.25"), standard("Adrenalinpumpe", 3).getEssenceCost());
    assertEquals("18V", find("Adrenalinpumpe").availability().displayText(3));
    assertEquals(new BigDecimal("0.7"), standard("Hyperschilddrüse", 0).getEssenceCost());
    assertEquals(new BigDecimal("0.75"), standard("Orthoskin", 3).getEssenceCost());
    assertEquals("12E", find("Orthoskin").availability().displayText(3));
    assertEquals(24_000, standard("Muskelstraffung", 3).getNuyenCost());
    assertEquals(new BigDecimal("1.2"), standard("Schadenskompensator", 12).getEssenceCost());
    assertEquals("36V", find("Schadenskompensator").availability().displayText(12));
    assertEquals(new BigDecimal("0.2"), new Cyberware(find("Reflexrekorder (Fertigkeitsgruppe)"),
        CyberwareGrade.STANDARD, 0, "Athletik", null).getEssenceCost());
    assertEquals(240_000, standard("Synapsenbeschleuniger", 3).getNuyenCost());
    assertEquals("18E", find("Synapsenbeschleuniger").availability().displayText(3));
  }

  @Test
  void effectsAndRestrictionsAreLoaded() {
    CyberwareData muscles = find("Kunstmuskeln");
    assertEquals(3, muscles.effectValue(ImplantEffectType.ATTRIBUTE, "STR", 3));
    assertEquals(3, muscles.effectValue(ImplantEffectType.ATTRIBUTE, "AGI", 3));
    assertEquals(List.of("Muskelverstärkung", "Muskelstraffung"), muscles.restrictions().excludes());
    assertEquals(2, find("Reflexbooster").effectValue(ImplantEffectType.INITIATIVE_PASSES, null, 2));
    assertTrue(find("Reflexbooster").restrictions().initiativeBoost());
    assertEquals(1, find("Hyperschilddrüse").effectValue(ImplantEffectType.ATTRIBUTE, "BOD", 0));
    assertEquals(List.of("Augen-Cyberware"), find("Katzenaugen").restrictions().excludedCategories());
    assertEquals("Stimmenmodulator", find("Zweitmuster").restrictions().requires());
    assertEquals(ImplantSelection.SKILL_GROUP, find("Reflexrekorder (Fertigkeitsgruppe)").selection());
    assertTrue(find("Schmuggelbehälter").restrictions().multiple());
    assertFalse(find("Dermalpanzerung").restrictions().multiple());
  }
}
