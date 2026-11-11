package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import static de.shalorian.chummer.model.character.QualityTestSupport.selected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Zugangsvoraussetzungen für magische Fertigkeiten (SR4-Grundregelwerk S. 148, Ausnahme Arcana) und
 * Resonanzfertigkeiten (S. 151, nur Technomancer), jeweils für Einzelfertigkeiten und Gruppen.
 */
class CharacterSkillAccessTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static @NonNull Character withQuality(@NonNull String qualityName) {
    Character c = mensch();
    QualityData quality = QualityDataLoader.findByName(qualityName);
    assertNotNull(quality);
    c.addQuality(quality, false);
    return c;
  }

  private static @NonNull Skill skill(@NonNull Character c, @NonNull String name) {
    Skill skill = c.getSkill(name);
    assertNotNull(skill);
    return skill;
  }

  @Test
  void mundaneCharacterCanLearnArcana() {
    Character c = mensch();

    assertTrue(c.canRaiseSkill(skill(c, "Arcana")));
  }

  @Test
  void mundaneCharacterCannotLearnOtherMagicalSkills() {
    Character c = mensch();

    assertFalse(c.canRaiseSkill(skill(c, "Askennen")));
    assertFalse(c.canRaiseSkill(skill(c, "Astralkampf")));
    assertFalse(c.canRaiseSkill(skill(c, "Verzaubern")));
    assertFalse(c.canRaiseSkill(skill(c, "Antimagie")));
  }

  @Test
  void mundaneCharacterCannotRaiseMagicalGroups() {
    Character c = mensch();

    assertFalse(c.canRaiseGroup("Hexerei"));
    assertFalse(c.canRaiseGroup("Beschwören"));
  }

  @Test
  void magicianCanRaiseMagicalSkillsAndGroups() {
    Character c = withQuality("Zauberer");

    assertTrue(c.canRaiseSkill(skill(c, "Askennen")));
    assertTrue(c.canRaiseGroup("Hexerei"));
    assertTrue(c.canRaiseGroup("Beschwören"));
  }

  @Test
  void onlyTechnomancerCanRaiseResonanceSkillsAndGroup() {
    Character mundane = mensch();
    assertFalse(mundane.canRaiseSkill(skill(mundane, "Kompilieren")));
    assertFalse(mundane.canRaiseGroup("Tasken"));

    Character technomancer = withQuality("Technomancer");
    assertTrue(technomancer.canRaiseSkill(skill(technomancer, "Kompilieren")));
    assertTrue(technomancer.canRaiseGroup("Tasken"));
  }

  @Test
  void accessRestrictionAlsoAppliesInCareerMode() {
    Character c = mensch();
    c.setCreated(true);

    assertFalse(c.canRaiseGroup("Tasken"));
    assertFalse(c.canRaiseSkill(skill(c, "Binden")));
  }

  @Test
  void ordinaryGroupsStayRaisable() {
    Character c = mensch();

    assertTrue(c.canRaiseGroup("Einfluss"));
    assertTrue(c.canRaiseGroup("Mechanik"));
  }

  @Test
  void removingMagicianResetsLockedSkillsButKeepsArcana() {
    Character c = withQuality("Zauberer");
    c.raiseGroup("Hexerei");
    c.raiseGroup("Hexerei");
    skill(c, "Askennen").setRating(1);
    skill(c, "Askennen").setSpecialization("Auren");
    skill(c, "Arcana").setRating(2);
    int bpWithSkills = c.getRemainingBP();

    assertEquals(4, c.getSkillsLockedByRemovingQuality(selected(c, "Zauberer")).size());

    c.removeQuality(selected(c, "Zauberer"));

    assertEquals(0, c.getGroupRating("Hexerei"));
    assertEquals(0, skill(c, "Askennen").getRating());
    assertNull(skill(c, "Askennen").getSpecialization());
    assertEquals(2, skill(c, "Arcana").getRating(), "Arcana bleibt auch für Mundane erlernbar");
    assertTrue(c.getRemainingBP() > bpWithSkills);
  }

  @Test
  void removingTechnomancerResetsTaskingGroup() {
    Character c = withQuality("Technomancer");
    c.raiseGroup("Tasken");

    assertEquals(3, c.getSkillsLockedByRemovingQuality(selected(c, "Technomancer")).size());

    c.removeQuality(selected(c, "Technomancer"));

    assertEquals(0, c.getGroupRating("Tasken"));
  }

  @Test
  void removingUnrelatedQualityLocksNoSkills() {
    Character c = withQuality("Zauberer");
    c.raiseGroup("Hexerei");
    QualityData beidhaendig = QualityDataLoader.findByName("Beidhändigkeit");
    assertNotNull(beidhaendig);
    c.addQuality(beidhaendig, false);

    assertTrue(c.getSkillsLockedByRemovingQuality(selected(c, "Beidhändigkeit")).isEmpty());
  }

  @Test
  void noLockedSkillsWithoutRatings() {
    Character c = withQuality("Zauberer");

    assertTrue(c.getSkillsLockedByRemovingQuality(selected(c, "Zauberer")).isEmpty());
  }
}
