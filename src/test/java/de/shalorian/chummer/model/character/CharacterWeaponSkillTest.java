package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.RangeBands;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Berechnete Waffenwerte am Charakter: Fertigkeit (GRW S. 147/148, 360–366), Schaden aus Stärke
 * (GRW S. 188) und Reichweiten (GRW S. 179).
 */
class CharacterWeaponSkillTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name)));
  }

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name));
  }

  @Test
  void pistolUsesPistolsSkillWithRating() {
    Character c = mensch();
    skill(c, "Pistolen").setRating(4);

    Skill used = c.getWeaponSkill(weapon("Ares Predator IV"));

    assertEquals("Pistolen", used.getName());
    assertEquals(4, used.getRating());
  }

  @Test
  void groupRatingAppliesToRifles() {
    Character c = mensch();
    c.raiseGroup("Feuerwaffen");
    c.raiseGroup("Feuerwaffen");
    c.raiseGroup("Feuerwaffen");

    Skill used = c.getWeaponSkill(weapon("Remington 990"));

    assertEquals("Gewehre", used.getName());
    assertEquals(3, used.getRating());
  }

  @Test
  void specialWeaponsUseExoticRangedWeapon() {
    assertEquals("Exotische Fernkampfwaffe",
        mensch().getWeaponSkill(weapon("Parashield Pfeilpistole")).getName());
  }

  @Test
  void meleeDamageFollowsStrength() {
    Character c = mensch();
    Weapon katana = weapon("Katana");
    c.getAttribute(AttributeType.STR).setValue(5);
    assertEquals("6K", c.getWeaponDamageText(katana));

    c.getAttribute(AttributeType.STR).setValue(4);
    assertEquals("5K", c.getWeaponDamageText(katana));
  }

  @Test
  void throwingRangeFollowsStrength() {
    Character c = mensch();
    c.getAttribute(AttributeType.STR).setValue(5);

    RangeBands bands = c.getWeaponRangeBands(weapon("Wurfmesser"));

    assertNotNull(bands);
    assertEquals(List.of("0–5", "6–10", "11–15", "16–25"), bands.bandTexts());
  }

  @Test
  void meleeWeaponHasNoRangeBands() {
    assertNull(mensch().getWeaponRangeBands(weapon("Katana")));
  }
}
