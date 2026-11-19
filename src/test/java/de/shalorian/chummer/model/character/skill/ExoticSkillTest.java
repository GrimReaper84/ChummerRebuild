package de.shalorian.chummer.model.character.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Exotische Fertigkeiten je Waffe bzw. Fahrzeug (GRW S. 148, 155, 365). */
class ExoticSkillTest {

  private static Character character() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  @Test
  void addAndCost() {
    Character c = character();
    int before = c.getSpentBP();
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    assertEquals("Exotische Nahkampfwaffe (Monofilamentpeitsche)", whip.getName());
    assertEquals(ExoticSkills.MELEE, whip.getBaseName());
    assertEquals(before, c.getSpentBP());
    whip.setRating(2);
    assertEquals(before + 8, c.getSpentBP()); // 4 GP je Stufe wie jede Einzelfertigkeit
    assertEquals(4, new Skill(Objects.requireNonNull(c.getSkill(ExoticSkills.VEHICLE)), "Jetpack").getKarmaCost(false));
    assertEquals("Für „monofilamentpeitsche“ schon vorhanden",
        c.getExoticSkillProblem(ExoticSkills.MELEE, "monofilamentpeitsche"));
    assertThrows(IllegalArgumentException.class, () -> c.addExoticSkill(ExoticSkills.MELEE, " "));
    assertThrows(IllegalArgumentException.class, () -> c.addExoticSkill("Pistolen", "Ares Predator IV"));
  }

  @Test
  void templateIsNotRaisedAndInstancesFollowIt() {
    Character c = character();
    Skill template = Objects.requireNonNull(c.getSkill(ExoticSkills.MELEE));
    assertFalse(c.canRaiseSkill(template));
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    Skill halberd = c.addExoticSkill(ExoticSkills.MELEE, "Hellebarde");
    assertTrue(c.canRaiseSkill(whip));
    List<Skill> skills = c.getSkills();
    int index = skills.indexOf(template);
    assertSame(halberd, skills.get(index + 1));
    assertSame(whip, skills.get(index + 2));
    c.removeExoticSkill(halberd);
    assertNull(c.getSkill(halberd.getName()));
  }

  @Test
  void weaponUsesMatchingInstance() {
    Character c = character();
    c.getAttribute(AttributeType.AGI).setValue(4);
    Weapon whip = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Monofilamentpeitsche")));
    c.getEquipment().addWeapon(whip);
    assertEquals(ExoticSkills.MELEE, c.getEquipment().getWeaponSkill(whip).getName());
    assertEquals(3, c.getEquipment().getWeaponDicePool(whip).orElseThrow().dice()); // ohne Wert: GES − 1
    c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche").setRating(3);
    assertEquals(7, c.getEquipment().getWeaponDicePool(whip).orElseThrow().dice());
    assertFalse(c.getEquipment().isWeaponSpecialized(whip));
  }

  @Test
  void removalBlockedByQualityChoiceAndInCareer() {
    // Review-Befund 2: Talentiert bzw. Inkompetenz dürfen nicht ins Leere zeigen
    Character c = character();
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Talentiert")), false, whip.getName());
    assertEquals("Zuerst „Talentiert“ entfernen, es bezieht sich auf diese Fertigkeit",
        c.getExoticSkillRemovalProblem(whip));
    assertThrows(IllegalStateException.class, () -> c.removeExoticSkill(whip));
    Skill halberd = c.addExoticSkill(ExoticSkills.MELEE, "Hellebarde");
    c.setCreated(true);
    assertEquals("Im Karrieremodus wird nur gesteigert.", c.getExoticSkillRemovalProblem(halberd));
  }

  @Test
  void noSpecialization() {
    Character c = character();
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    whip.setRating(3);
    assertFalse(c.canSpecializeSkill(whip));
  }
}
