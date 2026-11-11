package de.shalorian.chummer.model.character.quality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityDataLoaderMagicResonanceBonusTest {

  @Test
  void adeptMagicianAndMysticAdeptGrantMagic() {
    for (String name : new String[] {"Adept", "Zauberer", "Magieradept"}) {
      QualityData data = QualityDataLoader.findByName(name);
      assertNotNull(data, name);
      assertTrue(data.bonuses().contains(QualityBonusType.GRANTS_MAGIC), name);
      assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE), name);
    }
  }

  @Test
  void technomancerGrantsResonance() {
    QualityData data = QualityDataLoader.findByName("Technomancer");
    assertNotNull(data);
    assertTrue(data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE));
    assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_MAGIC));
  }

  @Test
  void spellcastersAreMagicianAndMysticAdept() {
    // GRW S. 116/118: Zauberer und Magieradepten dürfen magische Fertigkeiten lernen, Adepten nicht.
    for (String name : new String[] {"Zauberer", "Magieradept"}) {
      QualityData data = QualityDataLoader.findByName(name);
      assertNotNull(data, name);
      assertTrue(data.bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING), name);
    }

    for (String name : new String[] {"Adept", "Technomancer"}) {
      QualityData data = QualityDataLoader.findByName(name);
      assertNotNull(data, name);
      assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING), name);
    }
  }

  @Test
  void magicianQualityCostsFifteenBp() {
    // GRW S. 118 ("Zauberer — Kosten: 15 GP"), bildkorrigierter Bug-Fix aus dem Magie-Feature (war
    // fälschlich 5 BP, siehe docs/magie_analyse.md).
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    assertNotNull(magier);
    assertEquals(15, magier.bp());
  }
}
