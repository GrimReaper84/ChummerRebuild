package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Gemerkter Stand für Rückgängig: spätere Änderungen berühren ihn nicht (ADR-058). */
class CharacterStoreSnapshotTest {

  @Test
  void restoreGivesTheOldState() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.STR).setValue(3);
    c.addKarmaEntry(new LogEntry("Startkarma", 10, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));
    CharacterSnapshot before = CharacterStore.snapshot(c);

    c.getAttribute(AttributeType.STR).setValue(4);
    c.addKarmaEntry(new LogEntry("Stärke 3 → 4", -20, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));

    Character restored = CharacterStore.restore(before);
    assertNotSame(c, restored);
    assertEquals(3, restored.getAttribute(AttributeType.STR).getValue());
    assertEquals(1, restored.getKarmaLog().size());
    assertEquals(10, restored.getKarmaTotal());
  }
}
