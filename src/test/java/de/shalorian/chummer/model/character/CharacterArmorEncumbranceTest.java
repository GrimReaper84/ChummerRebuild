package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorCategory;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Panzerungsbehinderung auf GES/REA (GRW S. 190). */
class CharacterArmorEncumbranceTest {

  private static Character withBody(int body) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.BOD).setValue(body);
    return c;
  }

  private static Armor catalog(String name) {
    return new Armor(Objects.requireNonNull(ArmorDataLoader.findByName(name)));
  }

  private static Armor synthetic(int ballistic, int impact) {
    return new Armor(new ArmorData("Test " + ballistic + "/" + impact, ArmorCategory.ARMOR,
        ballistic, impact, Availability.NONE, 0, "SR4", "0"));
  }

  @Test
  void rulebookExampleGivesMinusFour() {
    Character c = withBody(3);
    c.getEquipment().addArmor(catalog("Actioneer-Geschäftskleidung")); // 5/3
    c.getEquipment().addArmor(catalog("Gefütterter Mantel"));          // 6/4

    assertEquals(-4, c.getEquipment().getArmorEncumbrancePenalty());
  }

  @Test
  void everyStartedTwoPointsOverLimitCostOne() {
    // KON 3 → Grenze 6; Stoß 0, damit nur Ballistik wirkt
    int[] excess = {0, 1, 2, 3};
    int[] expected = {0, -1, -1, -2};
    for (int i = 0; i < excess.length; i++) {
      Character c = withBody(3);
      c.getEquipment().addArmor(synthetic(6 + excess[i], 0));
      assertEquals(expected[i], c.getEquipment().getArmorEncumbrancePenalty(),
          "Überschuss " + excess[i]);
    }
  }

  @Test
  void helmetCountsTowardsTheSum() {
    Character c = withBody(2); // Grenze 4
    c.getEquipment().addArmor(synthetic(4, 4));
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());

    c.getEquipment().addArmor(catalog("Helm")); // +1/+2 → 5/6
    assertEquals(-2, c.getEquipment().getArmorEncumbrancePenalty());
  }

  @Test
  void unequippedArmorDoesNotEncumber() {
    Character c = withBody(1);
    Armor jacke = catalog("Panzerjacke");
    c.getEquipment().addArmor(jacke);
    jacke.setEquipped(false);

    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());
  }

  @Test
  void penaltyReducesAgilityAndReactionOnly() {
    Character c = withBody(3);
    c.getAttribute(AttributeType.AGI).setValue(5);
    c.getAttribute(AttributeType.REA).setValue(4);
    c.getAttribute(AttributeType.STR).setValue(4);
    c.getEquipment().addArmor(catalog("Actioneer-Geschäftskleidung"));
    c.getEquipment().addArmor(catalog("Gefütterter Mantel"));

    assertEquals(1, c.getAugmentedAttributeValue(AttributeType.AGI));
    assertEquals(1, c.getAugmentedAttributeValue(AttributeType.REA)); // 4 − 4 = 0 → Untergrenze 1
    assertEquals(4, c.getAugmentedAttributeValue(AttributeType.STR));
    assertEquals(3, c.getAugmentedAttributeValue(AttributeType.BOD));
    assertEquals(5, c.getAttribute(AttributeType.AGI).getValue());
  }

  @Test
  void withoutPenaltyAugmentedEqualsBase() {
    Character c = withBody(3);
    c.getAttribute(AttributeType.AGI).setValue(4);

    assertEquals(4, c.getAugmentedAttributeValue(AttributeType.AGI));
  }
}
