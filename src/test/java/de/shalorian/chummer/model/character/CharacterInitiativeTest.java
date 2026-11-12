package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Initiative und astrale Initiative (GRW S. 91, 116, 172, 227). */
class CharacterInitiativeTest {

  private static Character of(String metatype) {
    return new Character("Test", MetatypeDataLoader.find(metatype));
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void initiativeIsReactionPlusIntuition() {
    Character c = of("Mensch");
    c.getAttribute(AttributeType.REA).setValue(3);
    c.getAttribute(AttributeType.INT).setValue(4);
    assertEquals(7, c.getInitiative());
    assertEquals(1, c.getInitiativePasses());
    assertEquals(18, c.getInitiativeMaximum()); // (6 + 6) × 1,5
  }

  @Test
  void astralInitiativeOnlyForMagicians() {
    Character c = of("Mensch");
    c.getAttribute(AttributeType.INT).setValue(4);
    assertFalse(c.hasAstralAccess());
    c.addQuality(q("Zauberer"), false);
    assertTrue(c.hasAstralAccess());
    assertEquals(8, c.getAstralInitiative());

    Character mysticAdept = of("Mensch");
    mysticAdept.addQuality(q("Magieradept"), false);
    assertFalse(mysticAdept.hasAstralAccess()); // GRW S. 116
  }
}
