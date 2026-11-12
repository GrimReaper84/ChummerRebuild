package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.QualityTestSupport.selected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Regeln und Wirkungen der Gaben und Handicaps (GRW S. 114–122). */
class CharacterQualityRulesTest {

  private static Character of(String metatype) {
    return new Character("Test", MetatypeDataLoader.find(metatype));
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name), name);
  }

  @Test
  void exceptionalAttributeRaisesTheChosenMaximumOnly() {
    Character c = of("Mensch");
    c.addQuality(q("Außergewöhnliches Attribut"), false, "STR");
    assertEquals(7, c.getAttribute(AttributeType.STR).getMax());
    assertEquals(6, c.getAttribute(AttributeType.AGI).getMax());

    c.getAttribute(AttributeType.STR).setValue(7);
    c.removeQuality(selected(c, "Außergewöhnliches Attribut"));
    assertEquals(6, c.getAttribute(AttributeType.STR).getValue());
  }

  @Test
  void luckRaisesTheEdgeMaximum() {
    Character c = of("Mensch");
    c.addQuality(q("Glück"), false);
    assertEquals(8, c.getEdge().getMax());
  }

  @Test
  void aptitudeAllowsSevenWithDoubleCostForTheLastStep() {
    Character c = of("Mensch");
    c.addQuality(q("Talentiert"), false, "Pistolen");
    Skill pistols = skill(c, "Pistolen");
    assertEquals(7, pistols.getMaxRating());
    assertEquals(6, skill(c, "Gewehre").getMaxRating());

    pistols.setRating(6);
    assertTrue(c.canRaiseSkill(pistols));
    assertEquals(8, pistols.getBpCostPerRating(false));
    pistols.setRating(7);
    assertEquals(32, pistols.getBpCost(false)); // 6 × 4 + 8 (GRW S. 117)
  }

  @Test
  void incompetenceLocksTheSkillAndItsGroup() {
    Character c = of("Mensch");
    c.addQuality(q("Inkompetenz"), false, "Pistolen");
    assertFalse(c.canRaiseSkill(skill(c, "Pistolen")));
    assertFalse(c.canRaiseGroup("Feuerwaffen"));
    assertTrue(c.canRaiseSkill(skill(c, "Gewehre")));
  }

  @Test
  void incompetenceIsMultipleButNotForALearnedSkill() {
    Character c = of("Mensch");
    c.addQuality(q("Inkompetenz"), false, "Pistolen");
    assertNull(c.getQualityBlockReason(q("Inkompetenz"), false));
    assertNotNull(c.getQualityChoiceProblem(q("Inkompetenz"), "Pistolen"));
    skill(c, "Gewehre").setRating(2);
    assertNotNull(c.getQualityChoiceProblem(q("Inkompetenz"), "Gewehre"));
  }

  @Test
  void uneducatedBlocksTechnicalGroups() {
    Character c = of("Mensch");
    assertTrue(c.canRaiseGroup("Elektronik"));
    c.addQuality(q("Ungebildet"), false);
    assertFalse(c.canRaiseGroup("Elektronik"));
    assertTrue(c.canRaiseGroup("Athletik"));
  }

  @Test
  void burnoutCostsOnePointOfEssence() {
    Character c = of("Mensch");
    c.addQuality(q("Abhängigkeit (Ausgebrannt)"), false, "Novacoke");
    assertEquals(5.0, c.getEssence());
  }

  @Test
  void requirementsAndDependentRemoval() {
    Character c = of("Mensch");
    assertNotNull(c.getQualityBlockReason(q("Schutzpatron"), false));
    c.addQuality(q("Zauberer"), false);
    c.addQuality(q("Schutzpatron"), false, "Drache");
    c.removeQuality(selected(c, "Zauberer"));
    assertTrue(c.getSelectedQualities().isEmpty());
  }

  @Test
  void exclusionsWorkInBothDirections() {
    Character c = of("Mensch");
    c.addQuality(q("Adept"), false);
    assertNotNull(c.getQualityBlockReason(q("Magieresistenz 1"), false));
    assertNotNull(c.getQualityBlockReason(q("Technomancer"), false));

    Character d = of("Mensch");
    d.addQuality(q("Magieresistenz 2"), false);
    assertNotNull(d.getQualityBlockReason(q("Zauberer"), false));
  }

  @Test
  void metatypeRestrictionUsesTheBaseMetatype() {
    assertNull(of("Mensch").getQualityBlockReason(q("Elfenposer"), false));
    assertNotNull(of("Elf").getQualityBlockReason(q("Elfenposer"), false));
    assertNotNull(of("Troll").getQualityBlockReason(q("Menschliches Aussehen"), false));
  }

  @Test
  void onlyOneLevelPerGroup() {
    Character c = of("Mensch");
    c.addQuality(q("Hohe Schmerztoleranz 1"), false);
    assertNotNull(c.getQualityBlockReason(q("Hohe Schmerztoleranz 2"), false));
  }

  @Test
  void limitOf35GpAtCreation() {
    Character c = of("Mensch");
    c.addQuality(q("Glück"), false);
    c.addQuality(q("Zauberer"), false);
    assertNotNull(c.getQualityBlockReason(q("Mut"), false));
    assertNull(c.getQualityBlockReason(q("Mut"), true));
  }

  @Test
  void metatypeAbilitiesAreNotSelectableAndChoiceIsRequired() {
    Character c = of("Mensch");
    assertNotNull(c.getQualityBlockReason(q("Restlichtverstärkung"), false));
    assertThrows(IllegalArgumentException.class, () -> c.addQuality(q("Talentiert"), false));
  }

  @Test
  void uneducatedIsBlockedWhileATechnicalGroupHasARating() {
    Character c = of("Mensch");
    c.raiseGroup("Elektronik");
    assertNotNull(c.getQualityBlockReason(q("Ungebildet"), false));
    c.lowerGroup("Elektronik");
    assertNull(c.getQualityBlockReason(q("Ungebildet"), false));
  }

  @Test
  void metatypeChangeRemovesQualitiesThatNoLongerFit() {
    Character c = of("Mensch");
    c.addQuality(q("Elfenposer"), false);
    c.addQuality(q("Glück"), false);

    List<CharacterQuality> removed = c.setMetatype(MetatypeDataLoader.find("Elf"));

    assertEquals(List.of("Elfenposer"), removed.stream().map(r -> r.data().name()).toList());
    assertEquals(List.of("Glück"), c.getSelectedQualities().stream().map(r -> r.data().name()).toList());
  }

  @Test
  void removalConsequencesNameDependentsAndLoweredValues() {
    Character c = of("Mensch");
    c.addQuality(q("Zauberer"), false);
    c.addQuality(q("Schutzpatron"), false, "Drache");
    assertEquals(List.of("Schutzpatron (Drache) wird mit entfernt"),
        c.getQualityRemovalConsequences(selected(c, "Zauberer")));

    c.addQuality(q("Talentiert"), true, "Pistolen");
    skill(c, "Pistolen").setRating(7);
    assertEquals(List.of("Pistolen 7 → 6"), c.getQualityRemovalConsequences(selected(c, "Talentiert")));

    c.addQuality(q("Außergewöhnliches Attribut"), true, "STR");
    c.getAttribute(AttributeType.STR).setValue(7);
    assertEquals(1, c.getQualityRemovalConsequences(selected(c, "Außergewöhnliches Attribut")).size());
    assertTrue(c.getQualityRemovalConsequences(selected(c, "Schutzpatron")).isEmpty());
  }
}
