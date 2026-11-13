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
    expected.put(GearCategory.ARROWS, 2);
    expected.put(GearCategory.BOLTS, 2);
    expected.put(GearCategory.FIREARM_ACCESSORIES, 4);
    expected.put(GearCategory.AMMUNITION, 11);
    expected.put(GearCategory.GRENADES, 7);
    expected.put(GearCategory.ROCKETS, 3);
    expected.put(GearCategory.MISSILES, 3);
    expected.put(GearCategory.EXPLOSIVES, 3);
    expected.put(GearCategory.EXPLOSIVE_ACCESSORIES, 1);
    expected.put(GearCategory.COMMLINKS, 9);
    expected.put(GearCategory.OPERATING_SYSTEMS, 6);
    expected.put(GearCategory.HARDWARE_UPGRADES, 2);
    expected.put(GearCategory.ELECTRONIC_ACCESSORIES, 11);
    expected.put(GearCategory.OTHER_ELECTRONICS, 2);
    expected.put(GearCategory.RFID_TAGS, 4);
    expected.put(GearCategory.COMMUNICATION, 6);
    expected.put(GearCategory.MATRIX_PROGRAMS, 24);
    expected.put(GearCategory.SOFTWARE_PACKAGES, 2);
    expected.put(GearCategory.DATA_STORAGE, 1);
    expected.put(GearCategory.SOFTWARE, 3);
    expected.put(GearCategory.SKILLSOFTS, 3);
    expected.put(GearCategory.SIMSENSE, 3);
    expected.put(GearCategory.ARE_PROGRAMS, 6);
    expected.put(GearCategory.IDS_AND_CREDSTICKS, 3);
    expected.put(GearCategory.TOOLS, 3);
    expected.put(GearCategory.VISUAL_DEVICES, 10);
    expected.put(GearCategory.VISION_ENHANCEMENTS, 8);
    expected.put(GearCategory.AUDIO_DEVICES, 3);
    expected.put(GearCategory.AUDIO_ENHANCEMENTS, 3);
    expected.put(GearCategory.SENSOR_PACKAGES, 7);
    expected.put(GearCategory.SENSOR_FUNCTIONS, 12);
    expected.put(GearCategory.SECURITY_DEVICES, 2);
    expected.put(GearCategory.MAGLOCK_OPTIONS, 3);
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
      if (category == GearCategory.HARDWARE_UPGRADES) {
        continue; // Tabelle bei den Matrixregeln (S. 261), nicht in der Ausrüstungsliste
      }
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
          // Sensorpakete haben im GRW keinen Preis (Nutzerentscheidung 2026-10-08: 0 ¥)
          assertEquals(data.category() != GearCategory.SENSOR_PACKAGES,
              data.packCostFor(rating) > 0, data.name());
          data.capacityFor(rating);
          data.capacityCostFor(rating);
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
  void installedOnlyEntriesFitIntoSomeDeviceAndDevicesAcceptInstalledOnlyCategories() {
    // Einzeln kaufbar und zugleich einbaubar sind nur Programme und Sim-Module (Kommlink, GRW S. 271, 374)
    Set<GearCategory> standaloneButInstallable = Set.of(GearCategory.MATRIX_PROGRAMS,
        GearCategory.ELECTRONIC_ACCESSORIES);
    Set<GearCategory> accepted = GearDataLoader.getAllGear().values().stream()
        .flatMap(d -> d.accepts().stream()).collect(Collectors.toSet());
    for (GearData data : GearDataLoader.getAllGear().values()) {
      if (data.category().isInstalledOnly()) {
        assertTrue(accepted.contains(data.category()), data.name());
      }
      for (GearCategory accepts : data.accepts()) {
        assertTrue(accepts.isInstalledOnly() || standaloneButInstallable.contains(accepts), data.name());
      }
    }
  }

  @Test
  void visionEnhancementsAreSurchargesWithCapacityCost() {
    // GRW S. 379, am Bild gelesen
    GearData ultrasound = find("Ultraschall");
    assertTrue(ultrasound.availabilitySurcharge());
    assertEquals("8", ultrasound.availability().displayText(0));
    assertEquals(1_000, ultrasound.packCostFor(0));
    assertEquals(2, ultrasound.capacityCostFor(0));

    GearData smartlink = find("Smartlink");
    assertEquals("4E", smartlink.availability().displayText(0));
    assertEquals(500, smartlink.packCostFor(0));

    assertEquals(3, find("Selektiver Geräuschfilter").capacityCostFor(3)); // pro Stufe
    assertEquals(4, find("Brille").capacityFor(4));
    assertEquals(3, find("Sensorpaket (Hand/Minidrohne)").capacityFor(0));
    assertEquals(2, find("Lasermikrofon").capacityCostFor(5));
    assertFalse(find("Fernglas, optisch").hasCapacity());
  }

  @Test
  void onlyMatrixProgramsHaveASecondPriceBand() {
    for (GearData data : GearDataLoader.getAllGear().values()) {
      assertEquals(data.category() == GearCategory.MATRIX_PROGRAMS
              || data.category() == GearCategory.SOFTWARE_PACKAGES, !data.highCost().isEmpty(),
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

  /** Tabelle "Sensorpakete" (GRW S. 381): Kapazität je Paket, Signal steht im Hinweis. */
  @Test
  void vehicleAndDroneSensorPackagesMatchRulebookTable() {
    Map<String, Integer> capacities = Map.of(
        "Sensorpaket (RFID)", 1,
        "Sensorpaket (Mikro)", 1,
        "Sensorpaket (Hand/Minidrohne)", 3,
        "Sensorpaket (Stationär/kleine Drohne)", 5,
        "Sensorpaket (Mittlere Drohne)", 6,
        "Sensorpaket (Große Drohne)", 8,
        "Sensorpaket (Fahrzeug)", 12);
    capacities.forEach((name, capacity) -> {
      GearData pack = find(name);
      assertEquals(capacity, pack.capacityFor(0), name);
      assertEquals(Set.of(GearCategory.SENSOR_FUNCTIONS), pack.accepts(), name);
      assertEquals(0, pack.packCostFor(0), name);
    });
    assertTrue(find("Sensorpaket (Fahrzeug)").note().startsWith("Signal 5."));
    assertTrue(find("Sensorpaket (Große Drohne)").note().startsWith("Signal 4."));
  }

  /** Autosoft: Typ als Auswahl der fünf Autosofts von GRW S. 287 (Preise in MatrixProgramPriceTest). */
  @Test
  void autosoftOffersRulebookTypes() {
    assertEquals(ExtraKind.AUTOSOFT, find("Autosoft").extraKind());
  }
}
