package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Ausrüstungskategorien und -bereiche in GRW-Reihenfolge (S. 368–395). */
class GearCategoryTest {

  @Test
  void areasOfCategoriesFollowAreaOrder() {
    GearCategory[] categories = GearCategory.values();

    for (int i = 1; i < categories.length; i++) {
      assertTrue(categories[i - 1].area().compareTo(categories[i].area()) <= 0,
          "Bereichsfolge bei " + categories[i]);
    }
  }

  @Test
  void categoriesBelongToTheirRulebookArea() {
    assertEquals(GearArea.ELECTRONICS, GearCategory.RFID_TAGS.area());
    assertEquals(GearArea.ELECTRONICS, GearCategory.COMMLINKS.area());
    assertEquals(GearArea.ELECTRONICS, GearCategory.OPERATING_SYSTEMS.area());
    assertEquals(GearArea.DATACHIPS_AND_SOFTWARE, GearCategory.MATRIX_PROGRAMS.area());
    assertEquals(GearArea.SENSORS, GearCategory.SENSOR_FUNCTIONS.area());
    assertEquals(GearArea.SECURITY, GearCategory.MAGLOCK_OPTIONS.area());
    assertEquals(GearArea.DATACHIPS_AND_SOFTWARE, GearCategory.SKILLSOFTS.area());
    assertEquals(GearArea.SECURITY, GearCategory.RESTRAINTS.area());
    assertEquals(GearArea.CHEMICALS_AND_DRUGS, GearCategory.TOXINS.area());
    assertEquals(GearArea.BIOTECH, GearCategory.DOCWAGON.area());
    assertEquals(GearArea.MAGICAL, GearCategory.SPELL_FORMULAE.area());
  }

  @Test
  void fromDisplayNameFindsEveryCategoryAndRejectsUnknown() {
    for (GearCategory category : GearCategory.values()) {
      assertEquals(category, GearCategory.fromDisplayName(category.getDisplayName()));
    }
    assertThrows(IllegalArgumentException.class, () -> GearCategory.fromDisplayName("Zubehör"));
  }

  @Test
  void installedOnlyCategories() {
    Set<GearCategory> installedOnly = EnumSet.of(GearCategory.OPERATING_SYSTEMS,
        GearCategory.VISION_ENHANCEMENTS, GearCategory.AUDIO_ENHANCEMENTS,
        GearCategory.SENSOR_FUNCTIONS, GearCategory.MAGLOCK_OPTIONS);
    for (GearCategory category : GearCategory.values()) {
      assertEquals(installedOnly.contains(category), category.isInstalledOnly(), category.name());
    }
  }

  @Test
  void onlyLicenseHasSuggestions() {
    assertEquals(6, ExtraKind.LICENSE.suggestions().size());
    assertTrue(ExtraKind.TEXT.suggestions().isEmpty());
    assertTrue(ExtraKind.NONE.suggestions().isEmpty());
  }
}
