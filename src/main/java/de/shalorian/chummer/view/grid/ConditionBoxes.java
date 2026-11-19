package de.shalorian.chummer.view.grid;

import java.util.function.IntConsumer;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

/**
 * Kästchenraster des Zustandsmonitors eines Fahrzeugs: Kästchen n steht für n Schaden; ein Klick auf Kästchen n setzt
 * den Schaden auf n, ein Klick auf das letzte gefüllte leert es wieder. Der Zustandsmonitor des Charakters nutzt die
 * kleinen Kästchen von {@link MonitorBoxRow} im Status-Panel (ADR-065).
 */
public final class ConditionBoxes {

  private static final String STYLE_BOX = "cm-box";
  private static final String STYLE_BOX_FILLED = "cm-box-filled";

  private ConditionBoxes() {
  }

  /**
   * Baut das Raster.
   *
   * @param perRow    Kästchen je Zeile
   * @param enabled   ob Klicks Schaden eintragen (nur im Karrieremodus)
   * @param setDamage erhält den neuen Schaden
   */
  public static @NonNull GridPane grid(int count, int damage, int perRow, boolean enabled,
      @NonNull IntConsumer setDamage) {
    GridPane grid = new GridPane();
    grid.setHgap(4);
    grid.setVgap(4);
    for (int i = 0; i < count; i++) {
      int box = i + 1;
      Button button = new Button();
      button.getStyleClass().add(STYLE_BOX);
      if (box <= damage) {
        button.getStyleClass().add(STYLE_BOX_FILLED);
      }
      button.setDisable(!enabled);
      button.setOnAction(_ -> setDamage.accept(box == damage ? box - 1 : box));
      grid.add(button, i % perRow, i / perRow);
    }
    return grid;
  }
}
