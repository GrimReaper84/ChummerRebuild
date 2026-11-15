package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.persistence.CharacterStore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Rückgängig für Karma- und Nuyen-Buchungen (ADR-058, Nutzerentscheidung 2026-11-15): vor jeder Buchung wird der Stand
 * des Charakters gemerkt; „Rückgängig“ stellt den Stand vor der letzten Buchung wieder her, beliebig oft hintereinander.
 * Nur für die laufende Sitzung (nicht gespeichert). Wiederhergestellt wird alles seit der Buchung, auch Änderungen ohne
 * Buchung (Schaden, Edge, Notizen).
 */
public final class UndoHistory {

  private final Deque<UndoStep> steps = new ArrayDeque<>();

  /** Merkt den Stand von {@code character} vor der Buchung von {@code entry}. */
  void record(@NonNull Character character, @NonNull LogEntry entry, @NonNull SpendCurrency currency) {
    steps.push(new UndoStep(CharacterStore.snapshot(character), entry, currency, character.isCreated()));
  }

  /**
   * Der Eintrag, den „Rückgängig“ zurücknehmen würde, oder {@code null}: nur wenn der zuletzt gemerkte Eintrag noch der
   * letzte seines Logs ist und der Abschluss der Erschaffung nicht dazwischen liegt (der ist nicht rückgängig zu
   * machen).
   */
  public @Nullable LogEntry undoableEntry(@NonNull Character character) {
    UndoStep top = steps.peek();
    if (top == null || top.created() != character.isCreated()) {
      return null;
    }
    List<LogEntry> log = top.currency() == SpendCurrency.KARMA ? character.getKarmaLog() : character.getNuyenLog();
    return !log.isEmpty() && log.getLast().equals(top.entry()) ? top.entry() : null;
  }

  /**
   * Nimmt die letzte Buchung zurück und liefert den Charakter im Stand davor.
   *
   * @throws IllegalStateException wenn {@link #undoableEntry} nichts liefert
   */
  public @NonNull Character undo(@NonNull Character character) {
    if (undoableEntry(character) == null) {
      throw new IllegalStateException("Nichts rückgängig zu machen");
    }
    return CharacterStore.restore(steps.pop().before());
  }

  /** Betrag der zuletzt gemerkten Buchung mit Einheit, z.B. „-20 Karma“, „+105 ¥“ (für die Rückfrage); leer ohne. */
  public @NonNull String lastAmountText() {
    UndoStep top = steps.peek();
    if (top == null) {
      return "";
    }
    int amount = top.entry().amount();
    return (amount > 0 ? "+" : "") + top.currency().formatAmount(amount);
  }

  /** Vergisst den Verlauf, z.B. wenn ein anderer Charakter geladen wird. */
  public void clear() {
    steps.clear();
  }
}
