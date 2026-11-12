package de.shalorian.chummer.view.shell;

import de.shalorian.chummer.view.ViewPreferences;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jspecify.annotations.NonNull;

/**
 * Wo ein schwebender Knopf an seinem Rand sitzt, gemerkt zwischen den Starts: 0 ist unten, 1 oben im Bereich, in dem er
 * sich bewegen kann, damit die Position zu jeder Fensterhöhe passt (übernommen aus dem Fallout2d20-Projekt).
 */
public final class FloatingButtonPosition {

  private final @NonNull ViewPreferences preferences;
  private final @NonNull String key;
  private final @NonNull DoubleProperty value;

  public FloatingButtonPosition(@NonNull ViewPreferences preferences, @NonNull String key) {
    this.preferences = preferences;
    this.key = key;
    value = new SimpleDoubleProperty(Math.clamp(preferences.getDouble(key, 0), 0.0, 1.0));
  }

  @NonNull DoubleProperty value() {
    return value;
  }

  /** Merkt sich die aktuelle Position; einmal je Verschieben, nicht bei jedem Schritt. */
  void save() {
    preferences.putDouble(key, value.get());
  }

  /**
   * Die Position nach dem Ziehen.
   *
   * @param start  die Position zu Beginn
   * @param deltaY die gezogene Strecke, nach unten positiv
   * @param range  die Pixel zwischen tiefster und höchster Position
   * @return die neue Position zwischen 0 und 1; unverändert, wenn der Knopf keinen Spielraum hat
   */
  static double afterDrag(double start, double deltaY, double range) {
    return range <= 0 ? start : Math.clamp(start - deltaY / range, 0.0, 1.0);
  }
}
