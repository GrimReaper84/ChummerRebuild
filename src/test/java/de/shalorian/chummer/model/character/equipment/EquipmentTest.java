package de.shalorian.chummer.model.character.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Ausrüstung als Komponente des Charakters (docs/plaene/archiv/character_aufteilung_plan.md). */
class EquipmentTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Armor armor(String name) {
    return new Armor(Objects.requireNonNull(ArmorDataLoader.findByName(name)));
  }

  @Test
  void encumbranceFromEquipmentReducesAgilityAndReactionOfTheCharacter() {
    // GRW S. 190: KON 3, Actioneer-Geschäftskleidung 5/3 + Gefütterter Mantel 6/4 → −4
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getAttribute(AttributeType.AGI).setValue(5);
    c.getAttribute(AttributeType.STR).setValue(5);
    c.getEquipment().addArmor(armor("Actioneer-Geschäftskleidung"));
    c.getEquipment().addArmor(armor("Gefütterter Mantel"));

    assertEquals(-4, c.getEquipment().getArmorEncumbrancePenalty());
    assertEquals(1, c.getAugmentedAttributeValue(AttributeType.AGI));
    assertEquals(5, c.getAugmentedAttributeValue(AttributeType.STR));
  }

  @Test
  void spentNuyenSumsAllCategoriesAndReducesRemainingNuyen() {
    Character c = mensch();
    c.setNuyenBP(10);
    Equipment equipment = c.getEquipment();
    equipment.addArmor(armor("Panzerjacke"));
    equipment.addWeapon(
        new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName("Ares Predator IV"))));
    equipment.getLifestyle().setLevel(LifestyleLevel.LOW);

    // Panzerjacke 900 ¥ + Ares Predator IV 350 ¥ + Unterschicht 2.000 ¥ × 1 Monat
    assertEquals(3_250, equipment.getSpentNuyen());
    assertEquals(46_750, c.getRemainingNuyen());
  }
}
