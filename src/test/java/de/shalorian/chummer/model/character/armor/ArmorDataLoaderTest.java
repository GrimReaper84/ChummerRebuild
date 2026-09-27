package de.shalorian.chummer.model.character.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Legality;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Deckt die GRW-verifizierte Rüstungs-Teilmenge ab (Tabelle Panzerung, GRW S. 373). */
class ArmorDataLoaderTest {

  private static ArmorData find(String name) {
    ArmorData data = ArmorDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  @Test
  void loadsAllThirteenEntries() {
    assertEquals(13, ArmorDataLoader.getAllArmor().size());
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

    assertFalse(c.isAvailableForPurchase(gkp));
    assertTrue(c.isAvailableForPurchase(find("Panzerjacke")));

    c.setCreated(true);
    assertTrue(c.isAvailableForPurchase(gkp));
  }

  @Test
  void clothingIsNotInCatalog() {
    assertNull(ArmorDataLoader.findByName("Lederjacke"));
    assertNull(ArmorDataLoader.findByName("Kleidung"));
  }
}
