package de.shalorian.chummer.model.character.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Legality;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Kleidung und Panzerung laut GRW-Tabellen S. 373 (am Seitenbild verifiziert). */
class ArmorDataLoaderTest {

  private static ArmorData find(String name) {
    ArmorData data = ArmorDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  @Test
  void loadsAllSixteenEntries() {
    assertEquals(16, ArmorDataLoader.getAllArmor().size());
  }

  @Test
  void clothingHasPriceRangeAndSurcharge() {
    // GRW S. 373: Kleidung 20–100.000 ¥; Feedback-Kleidung Verfügbarkeit 8, +500 ¥; Lederjacke 2/2, +200 ¥
    ArmorData clothing = find("Kleidung");
    assertEquals(ArmorCategory.CLOTHING, clothing.category());
    assertTrue(clothing.hasVariablePrice());
    assertEquals("20–100.000 ¥", clothing.priceText());
    assertEquals("20–100.000 ¥ + 500 ¥", find("Feedback-Kleidung").priceText());
    assertEquals("8", find("Feedback-Kleidung").availabilityText());
    assertEquals("2/2", find("Lederjacke/-mantel").protectionText());
    assertFalse(find("Panzerjacke").hasVariablePrice());
    assertEquals("900 ¥", find("Panzerjacke").priceText());
  }

  @Test
  void clothingCostsChosenPricePlusSurcharge() {
    Armor jacket = new Armor(find("Lederjacke/-mantel"), true, 300);
    assertEquals(500, jacket.getNuyenCost());
    assertThrows(IllegalArgumentException.class,
        () -> new Armor(find("Kleidung"), true, 10));
    assertThrows(IllegalArgumentException.class,
        () -> new Armor(find("Kleidung")));
    assertThrows(IllegalArgumentException.class,
        () -> new Armor(find("Panzerjacke"), true, 100));
  }

  @Test
  void onlyHelmetsAndShieldsAreBonusItems() {
    Map<String, ArmorData> all = ArmorDataLoader.getAllArmor();
    Set<String> bonus = all.values().stream()
        .filter(d -> d.category().isBonus())
        .map(ArmorData::name)
        .collect(Collectors.toSet());

    assertEquals(Set.of("Helm", "Ballistischer Schild", "Schutzschild", "Taserschild"), bonus);
  }

  @Test
  void valuesMatchRulebook() {
    ArmorData jacke = find("Panzerjacke");
    assertEquals(ArmorCategory.ARMOR, jacke.category());
    assertEquals(8, jacke.ballistic());
    assertEquals(6, jacke.impact());
    assertEquals(900, jacke.cost());
    assertEquals("2", jacke.availabilityText());
    assertEquals("SR4", jacke.source());
    assertEquals("373", jacke.page());

    ArmorData helm = find("Helm");
    assertEquals(ArmorCategory.HELMETS_AND_SHIELDS, helm.category());
    assertEquals("+1/+2", helm.protectionText());

    assertEquals(0, find("Panzerkleidung").impact());
    assertEquals("6/6", find("Urban Explorer Overall").protectionText());
  }

  @Test
  void fullBodyArmorIsNotAvailableAtCreation() {
    ArmorData gkp = find("Ganzkörperpanzerung");

    assertEquals("14E", gkp.availabilityText());
    assertEquals(Legality.RESTRICTED, gkp.availability().legality());
    assertFalse(gkp.isAllowedAtCreation());
  }

  @Test
  void characterBlocksFullBodyArmorOnlyAtCreation() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    ArmorData gkp = find("Ganzkörperpanzerung");

    assertFalse(c.getEquipment().isAvailableForPurchase(gkp));
    assertTrue(c.getEquipment().isAvailableForPurchase(find("Panzerjacke")));

    c.setCreated(true);
    assertTrue(c.getEquipment().isAvailableForPurchase(gkp));
  }

  @Test
  void armorAccessoriesAndModificationsAreNotYetInCatalog() {
    assertNull(ArmorDataLoader.findByName("Chemische Versiegelung"));
    assertNull(ArmorDataLoader.findByName("Feuerresistenz"));
  }
}
