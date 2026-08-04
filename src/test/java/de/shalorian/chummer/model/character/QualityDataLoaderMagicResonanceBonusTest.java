package de.shalorian.chummer.model.character;

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
}
