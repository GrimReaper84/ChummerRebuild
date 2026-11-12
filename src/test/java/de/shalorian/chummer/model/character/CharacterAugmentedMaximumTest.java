package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Augmentiertes Maximum ⌊1,5 × Maximum⌋ (GRW S. 91). */
class CharacterAugmentedMaximumTest {

  private static Character of(String metatype) {
    return new Character("Test", MetatypeDataLoader.find(metatype));
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void augmentedMaximumIsOneAndAHalfTimesRoundedDown() {
    assertEquals(15, of("Troll").getAugmentedMaximum(AttributeType.STR));
    assertEquals(7, of("Zwerg").getAugmentedMaximum(AttributeType.REA));
    Character c = of("Mensch");
    assertEquals(9, c.getAugmentedMaximum(AttributeType.STR));
    c.addQuality(q("Außergewöhnliches Attribut"), false, "STR");
    assertEquals(10, c.getAugmentedMaximum(AttributeType.STR));
  }
}
