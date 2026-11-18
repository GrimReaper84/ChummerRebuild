package de.shalorian.chummer.view.grid;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import org.jspecify.annotations.NonNull;

/**
 * Reihe anklickbarer Zustandsmonitor-Kästchen; Kästchen n steht für n Schaden. Ein Klick setzt den Schaden auf n, ein
 * Klick auf das letzte gefüllte Kästchen nimmt eines zurück. Genutzt im Status-Panel und auf dem Schergen-Bogen.
 */
public final class MonitorBoxRow {

  private MonitorBoxRow() {
  }

  /**
   * Baut die Reihe.
   *
   * @param filledStyle    Stilklasse gefüllter Kästchen (z.B. {@code cm-mini-physical})
   * @param enabled        ob Klicks möglich sind
   * @param separatorAfter ob hinter Kästchen n ein Trenner „|“ steht (Stufe des Verletzungsmodifikators)
   * @param onSet          erhält den neuen Schaden
   */
  public static @NonNull FlowPane build(int boxes, int damage, @NonNull String filledStyle, boolean enabled,
      @NonNull IntPredicate separatorAfter, @NonNull IntConsumer onSet) {
    FlowPane row = new FlowPane(4, 4);
    for (int box = 1; box <= boxes; box++) {
      int value = box;
      Button button = new Button();
      button.getStyleClass().setAll("cm-mini");
      if (box <= damage) {
        button.getStyleClass().add(filledStyle);
      }
      button.setDisable(!enabled);
      button.setOnAction(_ -> onSet.accept(value == damage ? value - 1 : value));
      row.getChildren().add(button);
      if (box < boxes && separatorAfter.test(box)) {
        Label separator = new Label("|");
        separator.getStyleClass().add("key-caption");
        row.getChildren().add(separator);
      }
    }
    return row;
  }
}
