package de.shalorian.chummer.model.character.quality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityDataLoaderMagicResonanceBonusTest {

  @Test
  void adeptMagicianAndMysticAdeptGrantMagic() {
    for (String name : new String[] {"Adept", "Magier", "Mystischer Adept"}) {
      QualityData data = QualityDataLoader.findByName(name);
      assertNotNull(data, name);
      assertTrue(data.bonuses().contains(QualityBonusType.GRANTS_MAGIC), name);
      assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE), name);
    }
  }

  @Test
  void technomancerGrantsResonance() {
    QualityData data = QualityDataLoader.findByName("Technomant");
    assertNotNull(data);
    assertTrue(data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE));
    assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_MAGIC));
  }

  @Test
  void onlyMagicianGrantsSpellcasting() {
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);
    assertTrue(magier.bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING));

    for (String name : new String[] {"Adept", "Mystischer Adept", "Technomant"}) {
      QualityData data = QualityDataLoader.findByName(name);
      assertNotNull(data, name);
      assertFalse(data.bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING), name);
    }
  }

  @Test
  void magicianQualityCostsFifteenBp() {
    // GRW S. 118 ("Zauberer — Kosten: 15 GP"), bildkorrigierter Bug-Fix aus dem Magie-Feature (war
    // fälschlich 5 BP, siehe docs/magie_analyse.md).
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);
    assertEquals(15, magier.bp());
  }
}
