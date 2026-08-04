package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GRW S. 105: Nur der Mensch bekommt "Besondere Fähigkeiten: +1 Edge" (Edge 2-7), alle anderen
 * Grundmetatypen sowie deren Varianten bleiben bei der Basisspanne 1-6.
 */
class MetatypeDataLoaderEdgeTest {

  @Test
  void everyMetatypeAndVariantHasAnEdgeRange() {
    for (MetatypeData m : MetatypeDataLoader.getAllMetatypes()) {
      assertNotNull(m.edgeRange(), m.name());
    }
  }

  @Test
  void humanAndItsVariantGetTheEdgeBonus() {
    assertEquals(new AttributeRange(2, 7), MetatypeDataLoader.find("Mensch").edgeRange());
    assertEquals(new AttributeRange(2, 7), MetatypeDataLoader.find("Nachtmensch").edgeRange());
  }

  @Test
  void nonHumanMetatypesUseTheBaseEdgeRange() {
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Elf").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Zwerg").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Ork").edgeRange());
    assertEquals(new AttributeRange(1, 6), MetatypeDataLoader.find("Troll").edgeRange());
  }
}
