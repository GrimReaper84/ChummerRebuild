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

  /** Stilklasse ungefüllter Überzahl-Kästchen (gestrichelt). */
  private static final String STYLE_OVERFLOW = "cm-mini-overflow";

  private MonitorBoxRow() {
  }

  /**
   * Baut die Reihe ohne Überzahl.
   *
   * @param filledStyle    Stilklasse gefüllter Kästchen (z.B. {@code cm-mini-physical})
   * @param enabled        ob Klicks möglich sind
   * @param separatorAfter ob hinter Kästchen n ein Trenner „|“ steht (Stufe des Verletzungsmodifikators)
   * @param onSet          erhält den neuen Schaden
   */
  public static @NonNull FlowPane build(int boxes, int damage, @NonNull String filledStyle, boolean enabled,
      @NonNull IntPredicate separatorAfter, @NonNull IntConsumer onSet) {
    return build(boxes, 0, damage, filledStyle, enabled, separatorAfter, onSet);
  }

  /**
   * Baut die Reihe mit {@code overflowBoxes} Überzahl-Kästchen (GRW S. 192/193) hinter einem Trenner „‖“. Die Zählung
   * läuft über beide Abschnitte durch: Das erste Überzahl-Kästchen steht für {@code boxes + 1} Schaden. Ungefüllte
   * Überzahl-Kästchen sind gestrichelt; {@code separatorAfter} gilt nur für die normalen Kästchen.
   */
  public static @NonNull FlowPane build(int boxes, int overflowBoxes, int damage, @NonNull String filledStyle,
      boolean enabled, @NonNull IntPredicate separatorAfter, @NonNull IntConsumer onSet) {
    FlowPane row = new FlowPane(4, 4);
    int total = boxes + overflowBoxes;
    for (int box = 1; box <= total; box++) {
      if (box == boxes + 1) {
        row.getChildren().add(separator("‖"));
      }
      int value = box;
      Button button = new Button();
      button.getStyleClass().setAll("cm-mini");
      if (box <= damage) {
        button.getStyleClass().add(filledStyle);
      } else if (box > boxes) {
        button.getStyleClass().add(STYLE_OVERFLOW);
      }
      button.setDisable(!enabled);
      button.setOnAction(_ -> onSet.accept(value == damage ? value - 1 : value));
      row.getChildren().add(button);
      if (box < boxes && separatorAfter.test(box)) {
        row.getChildren().add(separator("|"));
      }
    }
    return row;
  }

  private static @NonNull Label separator(@NonNull String text) {
    Label separator = new Label(text);
    separator.getStyleClass().add("key-caption");
    return separator;
  }
}
