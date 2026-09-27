package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Ressourcengeld und Restgeld bei der Erschaffung (GRW S. 110–112). */
class CharacterNuyenBudgetTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware cyberware(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data);
    return new Cyberware(data, CyberwareGrade.STANDARD, 0);
  }

  private static Armor armor(String name) {
    ArmorData data = ArmorDataLoader.findByName(name);
    assertNotNull(data);
    return new Armor(data);
  }

  @Test
  void rulebookExampleTwentyTwoBpGive110000Nuyen() {
    Character c = mensch();
    c.setNuyenBP(22);

    assertEquals(110_000, c.getResourceNuyen());
    assertEquals(110_000, c.getRemainingNuyen());
  }

  @Test
  void purchaseAndRemovalChangeRemainingNuyen() {
    Character c = mensch();
    c.setNuyenBP(10);
    Cyberware smartlink = cyberware("Smartlink");

    c.addCyberware(smartlink);
    assertEquals(1_000, c.getSpentNuyen());
    assertEquals(49_000, c.getRemainingNuyen());

    c.removeCyberware(smartlink);
    assertEquals(50_000, c.getRemainingNuyen());
  }

  @Test
  void lifestyleCostTimesMonthsCounts() {
    Character c = mensch();
    c.setNuyenBP(10);
    c.getLifestyle().setLevel(LifestyleLevel.LOW);
    c.getLifestyle().setMonths(3);

    assertEquals(6_000, c.getSpentNuyen());
    assertEquals(44_000, c.getRemainingNuyen());
  }

  @Test
  void armorCountsWhetherEquippedOrNot() {
    Character c = mensch();
    c.setNuyenBP(1);
    Armor jacke = armor("Panzerjacke");
    Armor helm = armor("Helm");
    helm.setEquipped(false);

    c.addArmor(jacke);
    c.addArmor(helm);

    assertEquals(1_000, c.getArmorNuyenCost());
    assertEquals(4_000, c.getRemainingNuyen());
  }

  @Test
  void allCategoriesAddUpAndMayGoNegative() {
    Character c = mensch();
    c.setNuyenBP(1);
    c.addCyberware(cyberware("Smartlink"));
    c.addArmor(armor("Panzerjacke"));
    c.getLifestyle().setLevel(LifestyleLevel.MIDDLE);

    assertEquals(1_000 + 900 + 5_000, c.getSpentNuyen());
    assertEquals(5_000 - 6_900, c.getRemainingNuyen());
  }

  @Test
  void resourcesDoNotChangeBpBeyondResourceBp() {
    Character c = mensch();
    int before = c.getSpentBP();
    c.setNuyenBP(5);
    c.addArmor(armor("Panzerjacke"));

    assertEquals(before + 5, c.getSpentBP());
  }
}
