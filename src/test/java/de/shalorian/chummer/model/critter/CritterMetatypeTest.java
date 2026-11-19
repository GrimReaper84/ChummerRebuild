package de.shalorian.chummer.model.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.Movement;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** GRW S. 337: Attribute ±3, keines unter 0. */
class CritterMetatypeTest {

  private static MetatypeData of(String name) {
    return CritterMetatype.of(Objects.requireNonNull(CritterDataLoader.findByName(name)));
  }

  @Test
  void rangeIsValuePlusMinusThreeNotBelowZero() {
    assertEquals(new AttributeRange(0, 5), of("Hund").getRangeFor(AttributeType.BOD));
    assertEquals(new AttributeRange(5, 11), of("Pferd").getRangeFor(AttributeType.BOD));
    assertEquals(new AttributeRange(37, 43), of("Westlicher Drache").getRangeFor(AttributeType.STR));
    assertEquals(new AttributeRange(0, 4), of("Hai").getRangeFor(AttributeType.CHA));
  }

  @Test
  void movementFromTheCatalog() {
    assertEquals(new Movement(25, 40), of("Donnervogel").movement());
    assertEquals(new Movement(10, 25), of("Ghul").movement());
  }
}
