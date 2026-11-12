package de.shalorian.chummer.model.character.metatype;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GRW S. 105: Nur der Mensch bekommt +1 Edge (Edge 2-7), alle anderen Metatypen bleiben bei der Basisspanne 1-6.
 */
class MetatypeDataLoaderEdgeTest {

  @Test
  void everyMetatypeHasAnEdgeRange() {
    for (MetatypeData m : MetatypeDataLoader.getAllMetatypes()) {
      assertNotNull(m.edgeRange(), m.name());
    }
  }

  @Test
  void humanGetsTheEdgeBonus() {
    assertEquals(new AttributeRange(2, 7), MetatypeDataLoader.find("Mensch").edgeRange());
  }

  @Test
  void nonHumanMetatypesUseTheBaseEdgeRange() {
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Elf").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Zwerg").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Ork").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Troll").edgeRange());
  }
}
