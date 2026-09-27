package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Schutzwert: höchstes getragenes Teil + Helm-/Schild-Boni (GRW S. 190/372). */
class CharacterArmorProtectionTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Armor armor(String name) {
    ArmorData data = ArmorDataLoader.findByName(name);
    assertNotNull(data);
    return new Armor(data);
  }

  @Test
  void noArmorGivesZero() {
    Character c = mensch();

    assertEquals(0, c.getArmorBallistic());
    assertEquals(0, c.getArmorImpact());
  }

  @Test
  void onlyHighestPieceCountsPerValue() {
    Character c = mensch();
    c.addArmor(armor("Panzerjacke"));
    c.addArmor(armor("Panzerweste"));

    assertEquals(8, c.getArmorBallistic());
    assertEquals(6, c.getArmorImpact());
  }

  @Test
  void highestIsDeterminedSeparatelyForBallisticAndImpact() {
    Character c = mensch();
    c.addArmor(armor("Panzerkleidung"));          // 4/0
    c.addArmor(armor("Urban Explorer Overall"));  // 6/6
    c.addArmor(armor("Actioneer-Geschäftskleidung")); // 5/3

    assertEquals(6, c.getArmorBallistic());
    assertEquals(6, c.getArmorImpact());
  }

  @Test
  void helmetAddsItsBonus() {
    Character c = mensch();
    c.addArmor(armor("Panzerjacke"));
    c.addArmor(armor("Panzerweste"));
    c.addArmor(armor("Helm"));

    assertEquals(9, c.getArmorBallistic());
    assertEquals(8, c.getArmorImpact());
  }

  @Test
  void unequippedPiecesDoNotCount() {
    Character c = mensch();
    Armor jacke = armor("Panzerjacke");
    Armor helm = armor("Helm");
    c.addArmor(jacke);
    c.addArmor(helm);
    c.addArmor(armor("Panzerweste"));

    jacke.setEquipped(false);
    helm.setEquipped(false);

    assertEquals(6, c.getArmorBallistic());
    assertEquals(4, c.getArmorImpact());
  }

  @Test
  void helmetAloneGivesItsBonus() {
    Character c = mensch();
    c.addArmor(armor("Helm"));

    assertEquals(1, c.getArmorBallistic());
    assertEquals(2, c.getArmorImpact());
  }
}
