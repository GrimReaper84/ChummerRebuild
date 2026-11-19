package de.shalorian.chummer.model.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.npc.NpcGroup;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CritterCharacterTest {

  private static Character critter(String name) {
    return CritterTemplate.statblock(Objects.requireNonNull(CritterDataLoader.findByName(name)));
  }

  @Test
  void valuesAsInTheCatalog() {
    Character barghest = critter("Barghest");
    assertEquals(7, barghest.getAttribute(AttributeType.BOD).getValue());
    assertEquals(4, barghest.getEdge().getValue());
    assertEquals(2, barghest.getInitiativePasses());
    assertEquals(3, barghest.getSkill("Waffenloser Kampf").getRating());
  }

  /** R5: Magie ohne Gabe; R10: Essenz aus dem Katalog ohne Abzug auf die Magie. */
  @Test
  void magicWithoutQualityAndNoEssencePenalty() {
    Character ghoul = critter("Ghul");
    assertTrue(ghoul.hasMagic());
    assertEquals(1, ghoul.getMagicOrResonance().getValue());
    assertEquals(5.0, ghoul.getEssence());
    assertEquals(0, ghoul.getEssencePenalty());
    assertEquals(6.0, critter("Vampir").getEssence());
    assertFalse(critter("Hund").hasMagic());
  }

  /** R7: Panzer addiert sich; „(8/8)“ und „(2/3)“. */
  @Test
  void naturalArmorCounts() {
    Character naga = critter("Naga");
    assertEquals(2, naga.getArmorBallistic());
    assertEquals(3, naga.getArmorImpact());
    Character dragon = critter("Westlicher Drache");
    assertEquals(8, dragon.getArmorBallistic());
    assertTrue(dragon.getCritterTraits().isHardened());
  }

  /** R6: Natürliche Waffen aus dem Zusatz. */
  @Test
  void naturalWeapons() {
    assertEquals(List.of(new NaturalWeapon("Biss", "5K", 0, "")), critter("Barghest").getCritterTraits().getNaturalWeapons());
    assertEquals(6, critter("Westlicher Drache").getCritterTraits().getSpecialSkills().get("Fliegen"));
  }

  /** R2: gelistete Fertigkeiten bis Katalog + 3, andere bis 3. */
  @Test
  void skillMaxima() {
    Character wolf = critter("Wolf");
    assertEquals(7, wolf.getSkill("Waffenloser Kampf").getMaxRating());
    assertEquals(3, wolf.getSkill("Pistolen").getMaxRating());
  }

  /** Review-Befund 1: Gaben-Änderungen setzen die Critter-Maxima nicht auf 6 zurück. */
  @Test
  void skillMaximaSurviveQualityChanges() {
    Character dragon = critter("Westlicher Drache");
    assertTrue(dragon.isMagician());
    assertEquals(8, dragon.getSkill("Askennen").getRating());
    assertEquals(11, dragon.getSkill("Askennen").getMaxRating());
    Character wolf = critter("Wolf");
    wolf.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Mut")), false);
    assertEquals(4, wolf.getSkill("Waffenloser Kampf").getRating());
    assertEquals(7, wolf.getSkill("Waffenloser Kampf").getMaxRating());
    assertEquals(3, wolf.getSkill("Pistolen").getMaxRating());
  }

  /** Magische Fertigkeiten haben auch ohne Gabe einen Pool (S. 337, wie im C#-Original). */
  @Test
  void magicSkillsHaveAPoolWithoutTheMagicianQuality() {
    Character ghoul = critter("Ghul");
    assertFalse(ghoul.isMagician());
    assertTrue(ghoul.getSkillDicePool(Objects.requireNonNull(ghoul.getSkill("Askennen"))).isPresent());
    Character dog = critter("Hund");
    assertTrue(dog.getSkillDicePool(Objects.requireNonNull(dog.getSkill("Askennen"))).isEmpty());
  }

  @Test
  void templateCreatesAGroupWithOneMember() {
    NpcGroup group = new CritterTemplate(Objects.requireNonNull(CritterDataLoader.findByName("Wolf"))).create();
    assertEquals(1, group.getMemberCount());
    assertEquals(0, group.getProfessionalRating());
  }
}
