package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/** Gespeicherte Regel-Einstellungen eines Charakters; fehlt in Dateien vor ADR-061 (dann alles aus). */
class RuleOptionsEntry {

  public boolean limbArmorAverage;
  public boolean lateAwakening;
  public boolean missedLifestylePayments;
  public boolean decoupleAtCreation;
  /** Mitgelieferte Teile von Waffen ausbauen (ADR-071); fehlt in älteren Dateien, dann aus. */
  public boolean removeIncludedParts;
  /** Kürzel der zugeschalteten Zusatzbände; das Grundregelwerk ist immer aktiv und wird nicht gespeichert. */
  public @NonNull List<String> books = new ArrayList<>();

  public RuleOptionsEntry() {
  }

  RuleOptionsEntry(boolean limbArmorAverage, boolean lateAwakening, boolean missedLifestylePayments,
      boolean decoupleAtCreation, @NonNull List<String> books) {
    this.decoupleAtCreation = decoupleAtCreation;
    this.limbArmorAverage = limbArmorAverage;
    this.lateAwakening = lateAwakening;
    this.missedLifestylePayments = missedLifestylePayments;
    this.books = books;
  }
}
