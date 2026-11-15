package de.shalorian.chummer.view.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Rückgängig des letzten Schritts, beliebig oft, nur in der Sitzung (ADR-058). */
class UndoHistoryTest {

  private final UndoHistory history = new UndoHistory();

  /** Bucht wie {@code TabServices.book}: Stand merken, dann Eintrag anhängen, dann ändern. */
  private void raiseStrength(Character c, int to) {
    LogEntry entry = new LogEntry("Stärke → " + to, -to * 5, LocalDate.of(2026, 11, 15), LogCategory.ATTRIBUTE);
    history.record(c, entry, SpendCurrency.KARMA);
    c.addKarmaEntry(entry);
    c.getAttribute(AttributeType.STR).setValue(to);
  }

  @Test
  void undoTwoStepsInARow() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addKarmaEntry(new LogEntry("Karma", 50, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));
    raiseStrength(c, 2);
    raiseStrength(c, 3);

    Character once = history.undo(c);
    assertEquals(2, once.getAttribute(AttributeType.STR).getValue());
    Character twice = history.undo(once);
    assertEquals(1, twice.getAttribute(AttributeType.STR).getValue());
    assertEquals(50, twice.getKarmaTotal());
    assertNull(history.undoableEntry(twice));
  }

  @Test
  void finishingCreationCannotBeUndone() {
    // eine Buchung aus der Erschaffung bleibt nach dem Abschluss stehen
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    raiseStrength(c, 2);
    c.setCreated(true);
    assertNull(history.undoableEntry(c));
  }

  @Test
  void nothingToUndoWhenAnotherEntryCameInBetween() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    raiseStrength(c, 2);
    c.addKarmaEntry(new LogEntry("aus der Datei", 5, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));
    assertNull(history.undoableEntry(c));
    assertThrows(IllegalStateException.class, () -> history.undo(c));
    history.clear();
    assertNull(history.undoableEntry(c));
  }
}
