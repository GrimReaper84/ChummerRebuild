package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Deckt die initiale, GRW-verifizierte Cyberware-/Bioware-Teilmenge ab (siehe Kommentarkopf in {@code
 * cyberware.xml}/{@code bioware.xml}).
 */
class CyberwareDataLoaderTest {

  @Test
  void loadsBothCatalogsWithCorrectKind() {
    Map<String, CyberwareData> all = CyberwareDataLoader.getAllCyberware();
    long cyber = all.values().stream().filter(d -> d.kind() == EquipmentKind.CYBERWARE).count();
    long bio = all.values().stream().filter(d -> d.kind() == EquipmentKind.BIOWARE).count();

    assertTrue(cyber >= 2, "cyberware.xml sollte mindestens 2 Einträge liefern");
    assertTrue(bio >= 2, "bioware.xml sollte mindestens 2 Einträge liefern");
  }

  @Test
  void kindFollowsSourceFile() {
    CyberwareData datenbuchse = CyberwareDataLoader.findByName("Datenbuchse");
    CyberwareData katzenaugen = CyberwareDataLoader.findByName("Katzenaugen");
    assertNotNull(datenbuchse);
    assertNotNull(katzenaugen);

    assertEquals(EquipmentKind.CYBERWARE, datenbuchse.kind());
    assertEquals(EquipmentKind.BIOWARE, katzenaugen.kind());
  }

  @Test
  void fieldsAreMapped() {
    CyberwareData dermal = CyberwareDataLoader.findByName("Dermalpanzerung");
    assertNotNull(dermal);

    assertEquals("Bodyware", dermal.category());
    assertEquals("Rating * 0.5", dermal.essence());
    assertEquals("Rating * 5000", dermal.cost());
    assertEquals(3, dermal.maxRating());
    assertTrue(dermal.hasRating());
    assertEquals("SR4", dermal.source());
    assertEquals("389", dermal.page());
  }

  @Test
  void itemWithoutRatingHasZeroMaxRating() {
    CyberwareData datenbuchse = CyberwareDataLoader.findByName("Datenbuchse");
    assertNotNull(datenbuchse);
    assertFalse(datenbuchse.hasRating());
  }

  @Test
  void specialCharactersInNamesAreLoaded() {
    assertNotNull(CyberwareDataLoader.findByName("Dämpfer"));
    assertNotNull(CyberwareDataLoader.findByName("Mnemoverstärker"));
  }

  @Test
  void allCatalogFormulasAreEvaluable() {
    for (CyberwareData data : CyberwareDataLoader.getAllCyberware().values()) {
      int rating = data.hasRating() ? data.maxRating() : 0;
      String choice = data.selection() == ImplantSelection.NONE ? "" : "Auswahl";
      Integer price = data.hasVariablePrice() ? data.priceMin() : null;
      Cyberware implant = new Cyberware(data, CyberwareGrade.STANDARD, rating, choice, price);
      // Essenz "–" im GRW (Cortexbomben, Zahnbehälter, Okular-Drohne, …) ist 0
      assertTrue(implant.getEssenceCost().signum() >= 0, data.name());
      assertTrue(implant.getNuyenCost() > 0, data.name());
    }
  }

  @Test
  void findByNameReturnsNullForUnknownItem() {
    assertNull(CyberwareDataLoader.findByName("Kein echtes Implantat"));
  }
}
