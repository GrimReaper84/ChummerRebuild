package de.shalorian.chummer.view.grid;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Zugriff auf einen einzelnen erhöh-/senkbaren Wert (Attribut, Skillgruppen-Rating oder Skill-Rating), den
 * {@link de.shalorian.chummer.view.tab.IncrementWiring#wire} unabhängig vom fachlichen Hintergrund verdrahten kann.
 */
public interface RowModel {

  int value();

  /**
   * Anzeigetext für den aktuellen Wert; standardmäßig die Zahl selbst.
   */
  default @NonNull String valueDisplayText() {
    return String.valueOf(value());
  }

  int min();

  int max();

  /**
   * Zusätzliche regelspezifische Sperre über Höchstwert und BP- bzw. Karma-Verfügbarkeit hinaus (z.B. Erstellungs-Caps,
   * die nicht allein über {@code max()} abbildbar sind): der Grund als Stichpunkt, oder {@code null}, wenn erlaubt.
   * Standardmäßig immer erlaubt.
   */
  default @Nullable String incrementProblem() {
    return null;
  }

  /**
   * Kosten für den nächsten BP-Erhöhungsschritt in der Charaktererstellung.
   */
  int bpStepCost();

  /**
   * Anzeigetext für die BP-Kosten-Spalte in der Charaktererstellung.
   */
  @NonNull String bpDisplayText();

  /**
   * Kosten für den nächsten Karma-Erhöhungsschritt im Karrieremodus.
   */
  int karmaStepCost();

  void increment();

  void decrement();

  @NonNull String actionLabel(int from, int to);
}
