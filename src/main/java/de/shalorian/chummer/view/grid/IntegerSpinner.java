package de.shalorian.chummer.view.grid;

import javafx.scene.control.Spinner;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NonNull;

/**
 * Zahlen-Spinner mit fester Breite: Neben den Pfeilknöpfen bleibt Platz für zwei Ziffern, und in engen Zeilen schrumpft
 * er nicht unter diese Breite (sonst verschwinden die Zahlen). Einzige Stelle, an der Spinner der Bögen entstehen.
 */
public final class IntegerSpinner {

  /** Breite für zweistellige Werte samt Pfeilknöpfen. */
  private static final double WIDTH = 80;

  private IntegerSpinner() {
  }

  public static @NonNull Spinner<Integer> create(int min, int max, int value) {
    Spinner<Integer> spinner = new Spinner<>(min, max, value);
    spinner.setPrefWidth(WIDTH);
    spinner.setMinWidth(Region.USE_PREF_SIZE);
    return spinner;
  }
}
