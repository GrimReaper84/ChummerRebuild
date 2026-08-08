package de.shalorian.chummer.model.character.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TraditionTest {

  @Test
  void hermeticHasDisplayNameAndDrainFormula() {
    assertEquals("Hermetisch", Tradition.HERMETIC.getDisplayName());
    assertEquals("WIL + LOG", Tradition.HERMETIC.getDrainFormula());
  }

  @Test
  void shamanicHasDisplayNameAndDrainFormula() {
    assertEquals("Schamanistisch", Tradition.SHAMANIC.getDisplayName());
    assertEquals("WIL + CHA", Tradition.SHAMANIC.getDrainFormula());
  }

  @Test
  void allValuesHaveNonBlankDisplayNames() {
    for (Tradition t : Tradition.values()) {
      assertFalse(t.getDisplayName().isBlank());
      assertFalse(t.getDrainFormula().isBlank());
    }
  }
}
