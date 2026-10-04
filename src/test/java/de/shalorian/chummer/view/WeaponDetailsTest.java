package de.shalorian.chummer.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Anzeigewerte des Waffen-Detailbereichs (Werte aus Waffen Teil B, GRW S. 179/188). */
class WeaponDetailsTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Weapon weapon(String name) {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(name)));
  }

  @Test
  void meleeWeaponShowsCalculatedDamageWithFormulaAndNoRanges() {
    Character c = mensch();
    c.getAttribute(AttributeType.STR).setValue(5);

    WeaponDetails katana = WeaponDetails.of(weapon("Katana"), c);

    assertEquals("6K", katana.damage());
    assertEquals("(STR÷2+3)K", katana.damageFormula());
    assertEquals("1", katana.reach());
    assertTrue(katana.rangeBands().isEmpty());
    assertEquals("", katana.rangeNote());
    assertEquals("Klingenwaffen 0", katana.skill());
  }

  @Test
  void firearmShowsFixedDamageRangesAndSkill() {
    WeaponDetails predator = WeaponDetails.of(weapon("Ares Predator IV"), mensch());

    assertEquals("5K", predator.damage());
    assertEquals("", predator.damageFormula());
    assertEquals(List.of("0–5", "6–20", "21–40", "41–60"), predator.rangeBands());
    assertEquals("Reichweiten wie Schwere Pistole (GRW S. 179)", predator.rangeNote());
    assertEquals("Pistolen 0", predator.skill());
    assertEquals("–", predator.reach());
    assertEquals("350 ¥", predator.cost());
  }

  @Test
  void throwingWeaponRangeFollowsStrength() {
    WeaponDetails knife = WeaponDetails.of(weapon("Wurfmesser"), mensch());

    assertEquals("Reichweiten wie Geworfene Messer (GRW S. 179), nach Stärke", knife.rangeNote());
  }

  @Test
  void bowRangeFollowsMinimumStrength() {
    Weapon bow = new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Bogen")), 4);

    assertEquals("Reichweiten wie Bogen (GRW S. 179), nach Mindeststärke",
        WeaponDetails.of(bow, mensch()).rangeNote());
  }

  @Test
  void tableNoteIsPassedThrough() {
    WeaponDetails roomsweeper = WeaponDetails.of(weapon("Remington Roomsweeper"), mensch());

    assertEquals("Mit Flechette: 7K(f), PB +5", roomsweeper.note());
  }

  @Test
  void recoilAndCostIncludeBoughtAccessories() {
    Weapon uzi = weapon("Uzi IV"); // RK "(1)", 500 ¥
    uzi.addAccessory(new WeaponAccessory(
        Objects.requireNonNull(AccessoryDataLoader.findByName("Gasventilsystem 2")), Mount.BARREL));

    WeaponDetails details = WeaponDetails.of(uzi, mensch());

    assertEquals("2 (3)", details.recoil());
    assertEquals("500 ¥ (mit Zubehör 700 ¥)", details.cost());
    assertEquals("500 ¥", WeaponDetails.of(weapon("Uzi IV"), mensch()).cost());
  }
}
