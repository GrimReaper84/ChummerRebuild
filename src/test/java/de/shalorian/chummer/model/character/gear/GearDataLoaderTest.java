package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.shalorian.chummer.model.character.equipment.Legality;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Ausrüstungskatalog Teil A gegen die GRW-Tabellen (S. 368, 374–385, 395, am Bild gelesen). */
class GearDataLoaderTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void everyCategoryHasItsRulebookRowCount() {
    Map<GearCategory, Integer> expected = new EnumMap<>(GearCategory.class);
    expected.put(GearCategory.FIREARM_ACCESSORIES, 4);
    expected.put(GearCategory.COMMLINKS, 9);
    expected.put(GearCategory.OPERATING_SYSTEMS, 6);
    expected.put(GearCategory.ELECTRONIC_ACCESSORIES, 11);
    expected.put(GearCategory.OTHER_ELECTRONICS, 2);
    expected.put(GearCategory.RFID_TAGS, 4);
    expected.put(GearCategory.COMMUNICATION, 6);
    expected.put(GearCategory.MATRIX_PROGRAMS, 6);
    expected.put(GearCategory.DATA_STORAGE, 1);
    expected.put(GearCategory.SOFTWARE, 3);
    expected.put(GearCategory.SKILLSOFTS, 3);
    expected.put(GearCategory.SIMSENSE, 3);
    expected.put(GearCategory.ARE_PROGRAMS, 6);
    expected.put(GearCategory.IDS_AND_CREDSTICKS, 3);
    expected.put(GearCategory.TOOLS, 3);
    expected.put(GearCategory.SECURITY_DEVICES, 2);
    expected.put(GearCategory.RESTRAINTS, 4);
    expected.put(GearCategory.BREAKING_AND_ENTERING, 10);
    expected.put(GearCategory.DRUGS, 9);
    expected.put(GearCategory.CHEMICALS, 2);
    expected.put(GearCategory.TOXINS, 7);
    expected.put(GearCategory.SURVIVAL, 15);
    expected.put(GearCategory.GRAPPLE_GUN, 6);
    expected.put(GearCategory.BIOTECH, 4);
    expected.put(GearCategory.DOCWAGON, 4);
    expected.put(GearCategory.SLAP_PATCHES, 4);
    expected.put(GearCategory.DISGUISE, 3);
    expected.put(GearCategory.FOCI, 8);
    expected.put(GearCategory.MAGICAL_SUPPLIES, 3);
    expected.put(GearCategory.FETISHES, 5);
    expected.put(GearCategory.SPELL_FORMULAE, 5);

    Map<GearCategory, Integer> actual = new EnumMap<>(GearCategory.class);
    GearDataLoader.getAllGear().values()
        .forEach(g -> actual.merge(g.category(), 1, Integer::sum));
    assertEquals(expected, actual);
  }

  @Test
  void categoriesFollowRulebookPageOrder() {
    int previousCategoryMaxPage = 0;
    for (GearCategory category : GearCategory.values()) {
      IntSummaryStatistics pages = GearDataLoader.getAllGear().values().stream()
          .filter(g -> g.category() == category)
          .mapToInt(g -> Integer.parseInt(g.page()))
          .summaryStatistics();
      assertTrue(previousCategoryMaxPage <= pages.getMin(), "Seitenfolge bei " + category);
      previousCategoryMaxPage = pages.getMax();
    }
  }

  @Test
  void allFormulasAreEvaluableForEveryRating() {
    for (GearData data : GearDataLoader.getAllGear().values()) {
      int from = data.hasRating() ? data.minRating() : 0;
      int to = data.hasRating() ? data.maxRating() : 0;
      for (int rating = from; rating <= to; rating++) {
        try {
          assertTrue(data.packCostFor(rating) > 0, data.name());
          data.availability().evaluate(rating);
        } catch (IllegalArgumentException | ArithmeticException e) {
          fail(data.name() + " Stufe " + rating + ": " + e.getMessage());
        }
      }
    }
  }

  @Test
  void commlinksAndOperatingSystemsHaveTheirMatrixValues() {
    // GRW S. 374, am Bild gelesen
    GearData caliban = find("Fairlight Caliban");
    assertEquals(4, caliban.processor());
    assertEquals(5, caliban.signal());
    assertEquals(8_000, caliban.packCostFor(0));
    assertEquals("–", caliban.availability().displayText(0));

    GearData navi = find("Novatech Navi");
    assertEquals(3, navi.firewall());
    assertEquals(4, navi.system());
    assertEquals(1_500, navi.packCostFor(0));
    assertTrue(navi.isSingleItem());
  }

  @Test
  void onlyMatrixProgramsHaveASecondPriceBand() {
    for (GearData data : GearDataLoader.getAllGear().values()) {
      assertEquals(data.category() == GearCategory.MATRIX_PROGRAMS, !data.highCost().isEmpty(),
          data.name());
    }
  }

  @Test
  void automaticLockpickHasRatingFormula() {
    GearData lockpick = find("Automatischer Dietrich");

    assertEquals(1, lockpick.minRating());
    assertEquals(6, lockpick.maxRating());
    assertEquals("8E", lockpick.availability().displayText(4));
    assertEquals(600, lockpick.packCostFor(3));
  }

  @Test
  void fakeLicenseHasRatingBasedForbiddenAvailabilityAndLicenseExtra() {
    GearData license = find("Gefälschte Lizenz");

    assertEquals("9V", license.availability().displayText(3));
    assertEquals(300, license.packCostFor(3));
    assertEquals(ExtraKind.LICENSE, license.extraKind());
    assertEquals("Lizenz", license.extraLabel());
  }

  @Test
  void survivalKitAndStandardRfidTags() {
    GearData kit = find("Survival-Kit");
    assertEquals("4", kit.availability().displayText(0));
    assertEquals(100, kit.packCostFor(0));

    GearData rfid = find("Standard-RFID-Marker");
    assertEquals(1, rfid.packCostFor(0));
    assertEquals(20, rfid.packSize());
    assertEquals(1, rfid.deviceRating());
  }

  @Test
  void drugsAreSoldPerDose() {
    GearData cram = find("Cram");

    assertEquals("2E", cram.availability().displayText(0));
    assertEquals(10, cram.packCostFor(0));
    assertEquals(GearUnit.DOSE, cram.unit());
    assertEquals(1, cram.packSize());
  }

  @Test
  void powerFocusUsesForceAsRating() {
    GearData focus = find("Kraftfokus");

    assertEquals("20E", focus.availability().displayText(4));
    assertEquals(100_000, focus.packCostFor(4));
    assertEquals(ExtraKind.NONE, focus.extraKind());
  }

  @Test
  void ropesAreSoldPerHundredOrTenMeters() {
    assertEquals(100, find("Standardseil").packSize());
    assertEquals(GearUnit.METER, find("Standardseil").unit());
    assertEquals(10, find("Myomerisches Seil").packSize());
    assertEquals(10, find("Plastik-Handschellen").packSize());
    assertEquals(GearUnit.SQUARE_METER, find("Biofasern").unit());
  }

  @Test
  void priceRangeOnlyForTheFourRulebookEntries() {
    Set<String> variable = GearDataLoader.getAllGear().values().stream()
        .filter(GearData::variableCost).map(GearData::name).collect(Collectors.toSet());

    assertEquals(Set.of("Sim-Modul", "Sim-Modul (für BTL/heißes Sim modifiziert)",
        "Sim-Aufzeichnung", "BTL-Aufzeichnung"), variable);
    assertEquals(5, find("Sim-Aufzeichnung").packCostFor(0));
    assertEquals(250, find("Sim-Modul (für BTL/heißes Sim modifiziert)").packCostFor(0));
  }

  @Test
  void legalitiesAndRatingsFromTheTables() {
    GearData jammer = find("Störsender, Bereich");
    assertEquals(10, jammer.maxRating());
    assertEquals(Legality.FORBIDDEN, jammer.availability().legality());
    assertEquals("30V", jammer.availability().displayText(10));

    assertEquals("5", find("White-Noise-Generator").availability().displayText(4));
    assertEquals("–", find("Medkit").availability().displayText(6));
    assertEquals(10, find("Tranq-Patch").maxRating());
    assertEquals(4, find("Aktionssoft").maxRating());
  }

  @Test
  void extrasNameTheirField() {
    assertEquals("Fertigkeit", find("Werkzeugkiste").extraLabel());
    assertEquals("Sprache", find("Linguasoft").extraLabel());
    assertEquals("Name", find("Gefälschte SIN").extraLabel());
    assertEquals("Geisterart", find("Herbeirufungsfokus").extraLabel());
    assertEquals("Zauber", find("Zauberformel (Kampf)").extraLabel());
    assertFalse(find("Dietrich-Set").needsExtra());
  }
}
