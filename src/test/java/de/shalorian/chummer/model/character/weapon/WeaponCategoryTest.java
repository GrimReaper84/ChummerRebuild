package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillDataLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Kategorien laut Tabellenüberschriften GRW S. 361/363/366. */
class WeaponCategoryTest {

  @Test
  void hasTwentyFourRulebookCategories() {
    assertEquals(24, WeaponCategory.values().length);
  }

  @Test
  void onlyTheFourMeleeTablesAreMelee() {
    Set<WeaponCategory> melee = Arrays.stream(WeaponCategory.values())
        .filter(WeaponCategory::isMelee)
        .collect(Collectors.toSet());

    assertEquals(Set.of(WeaponCategory.BLADES, WeaponCategory.CLUBS, WeaponCategory.EXOTIC_MELEE,
        WeaponCategory.UNARMED), melee);
  }

  @Test
  void fromDisplayNameReadsCatalogValues() {
    assertEquals(WeaponCategory.HOLDOUTS, WeaponCategory.fromDisplayName("Holdout-Pistolen"));
    assertEquals(WeaponCategory.MACHINE_PISTOLS,
        WeaponCategory.fromDisplayName("Automatikpistolen"));
    assertThrows(IllegalArgumentException.class,
        () -> WeaponCategory.fromDisplayName("Laserwaffen"));
  }

  @Test
  void everyCategoryUsesAnExistingActiveSkill() {
    Set<String> skillNames = SkillDataLoader.createFreshSkills().stream()
        .map(Skill::getName)
        .collect(Collectors.toSet());

    for (WeaponCategory category : WeaponCategory.values()) {
      assertTrue(skillNames.contains(category.skillName()), category.name());
    }
  }

  @Test
  void skillAssignmentFollowsRulebookTexts() {
    assertEquals("Schnellfeuerwaffen", WeaponCategory.SUBMACHINE_GUNS.skillName());
    assertEquals("Schnellfeuerwaffen", WeaponCategory.MACHINE_PISTOLS.skillName());
    assertEquals("Gewehre", WeaponCategory.SHOTGUNS.skillName());
    assertEquals("Projektilwaffen", WeaponCategory.CROSSBOWS.skillName());
    assertEquals("Pistolen", WeaponCategory.TASERS.skillName());
    assertEquals("Schwere Waffen", WeaponCategory.MISSILE_LAUNCHERS.skillName());
    assertEquals("Exotische Fernkampfwaffe", WeaponCategory.SPECIAL.skillName());
    assertEquals("Exotische Nahkampfwaffe", WeaponCategory.EXOTIC_MELEE.skillName());
  }

  @Test
  void mountsFollowRulebookPage357() {
    assertTrue(WeaponCategory.HOLDOUTS.mounts().isEmpty());
    assertEquals(Set.of(Mount.BARREL, Mount.TOP), WeaponCategory.LIGHT_PISTOLS.mounts());
    assertEquals(Set.of(Mount.BARREL, Mount.TOP), WeaponCategory.MACHINE_PISTOLS.mounts());
    assertEquals(Set.of(Mount.TOP), WeaponCategory.TASERS.mounts());
    for (WeaponCategory category : List.of(WeaponCategory.SUBMACHINE_GUNS,
        WeaponCategory.ASSAULT_RIFLES, WeaponCategory.SNIPER_RIFLES, WeaponCategory.SHOTGUNS,
        WeaponCategory.HEAVY_MG, WeaponCategory.MISSILE_LAUNCHERS)) {
      assertEquals(Set.of(Mount.values()), category.mounts(), category.name());
    }
    assertTrue(WeaponCategory.BLADES.mounts().isEmpty());
    assertFalse(WeaponCategory.THROWING.acceptsAccessories());
    assertTrue(WeaponCategory.BOWS.isProjectile());
  }
}
