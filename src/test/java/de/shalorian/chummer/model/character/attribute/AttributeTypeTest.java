package de.shalorian.chummer.model.character.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Deutsche Kürzel wie im GRW, der Enum-Name bleibt der Schlüssel der Daten. */
class AttributeTypeTest {

  @Test
  void abbreviationsFollowGermanRulebook() {
    assertEquals(List.of("KON", "GES", "REA", "STR", "CHA", "INT", "LOG", "WIL"),
        Arrays.stream(AttributeType.values()).map(AttributeType::getAbbreviation).toList());
  }

  @Test
  void enumNameStaysDataKey() {
    assertEquals(AttributeType.BOD, AttributeType.valueOf("BOD"));
    assertEquals(AttributeType.AGI, AttributeType.valueOf("AGI"));
  }

  @Test
  void abbreviationOfMapsKeysAndKeepsSpecialAttributes() {
    assertEquals("GES", AttributeType.abbreviationOf("AGI"));
    assertEquals("KON", AttributeType.abbreviationOf("BOD"));
    assertEquals("MAG", AttributeType.abbreviationOf("MAG"));
  }
}
