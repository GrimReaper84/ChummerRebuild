package de.shalorian.chummer.view.grid;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

/**
 * Kästchenraster eines Zustandsmonitors (Charakter und Fahrzeug): Kästchen n steht für n Schaden; ein Klick auf
 * Kästchen n setzt den Schaden auf n, ein Klick auf das letzte gefüllte leert es wieder.
 */
public final class ConditionBoxes {

  public static final String STYLE_BOX = "cm-box";
  public static final String STYLE_BOX_FILLED = "cm-box-filled";

  private ConditionBoxes() {
  }

  /**
   * Baut das Raster.
   *
   * @param offset       Kästchen davor (z.B. Überzahl hinter der körperlichen Spalte), damit Kästchen n für n steht
   * @param enabled      ob Klicks Schaden eintragen (nur im Karrieremodus)
   * @param label        Beschriftung je Kästchennummer, z.B. der Verletzungsmodifikator
   * @param boxStyles    zusätzliche Styleklassen aller Kästchen
   * @param filledStyles zusätzliche Styleklassen gefüllter Kästchen
   * @param setDamage    erhält den neuen Schaden
   */
  public static @NonNull GridPane grid(int count, int offset, int damage, int perRow, boolean enabled,
      @NonNull IntFunction<String> label, @NonNull List<String> boxStyles, @NonNull List<String> filledStyles,
      @NonNull IntConsumer setDamage) {
    GridPane grid = new GridPane();
    grid.setHgap(4);
    grid.setVgap(4);
    for (int i = 0; i < count; i++) {
      int box = offset + i + 1;
      Button button = new Button(label.apply(box));
      button.getStyleClass().add(STYLE_BOX);
      button.getStyleClass().addAll(boxStyles);
      if (box <= damage) {
        button.getStyleClass().add(STYLE_BOX_FILLED);
        button.getStyleClass().addAll(filledStyles);
      }
      button.setDisable(!enabled);
      button.setOnAction(_ -> setDamage.accept(box == damage ? box - 1 : box));
      grid.add(button, i % perRow, i / perRow);
    }
    return grid;
  }
}
