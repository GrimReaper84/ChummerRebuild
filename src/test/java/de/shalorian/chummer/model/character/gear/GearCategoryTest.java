package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void onlyCommlinksTakeInstallsAndOnlyOperatingSystemsAreInstalledOnly() {
    for (GearCategory category : GearCategory.values()) {
      assertEquals(category == GearCategory.COMMLINKS, category.acceptsInstalls(), category.name());
      assertEquals(category == GearCategory.OPERATING_SYSTEMS, category.isInstalledOnly(),
          category.name());
    }
  }

  @Test
  void onlyLicenseHasSuggestions() {
    assertEquals(6, ExtraKind.LICENSE.suggestions().size());
    assertTrue(ExtraKind.TEXT.suggestions().isEmpty());
    assertTrue(ExtraKind.NONE.suggestions().isEmpty());
  }
}
