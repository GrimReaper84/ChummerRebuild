package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
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
    assertEquals(GearArea.PROJECTILES, GearCategory.BOLTS.area());
    assertEquals(GearArea.AMMUNITION, GearCategory.AMMUNITION.area());
    assertEquals(GearArea.EXPLOSIVES, GearCategory.MISSILES.area());
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
    Set<GearCategory> installedOnly = EnumSet.of(GearCategory.OPERATING_SYSTEMS, GearCategory.HARDWARE_UPGRADES,
        GearCategory.SOFTWARE_PACKAGES,
        GearCategory.VISION_ENHANCEMENTS, GearCategory.AUDIO_ENHANCEMENTS,
        GearCategory.SENSOR_FUNCTIONS, GearCategory.MAGLOCK_OPTIONS);
    for (GearCategory category : GearCategory.values()) {
      assertEquals(installedOnly.contains(category), category.isInstalledOnly(), category.name());
    }
  }

  @Test
  void suggestionsAndChoices() {
    assertEquals(6, ExtraKind.LICENSE.suggestions().size());
    assertEquals(15, ExtraKind.WEAPON_CLASS.suggestions().size());
    assertEquals(2, ExtraKind.GRENADE_FORM.suggestions().size());
    assertTrue(ExtraKind.TEXT.suggestions().isEmpty());
    assertTrue(ExtraKind.NONE.suggestions().isEmpty());
    assertFalse(ExtraKind.LICENSE.isChoice());
    assertTrue(ExtraKind.WEAPON_CLASS.isChoice());
    assertTrue(ExtraKind.GRENADE_FORM.isChoice());
    assertEquals(List.of("Abwehr", "Clearsight", "Elektronische Kriegsführung",
        "Steuerung", "Zielerfassung"), ExtraKind.AUTOSOFT.suggestions());
    assertTrue(ExtraKind.AUTOSOFT.isChoice());
  }
}
