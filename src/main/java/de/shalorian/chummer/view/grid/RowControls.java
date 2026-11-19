package de.shalorian.chummer.view.grid;

import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

/**
 * Die vier UI-Elemente einer +/−-Zeile, wie sie {@link de.shalorian.chummer.view.tab.IncrementWiring#wire} verdrahtet.
 * Die Fabrikmethoden sind die einzige Stelle, an der Stepper-Knöpfe und ihre Anzeigen entstehen, damit alle
 * +/−-Zeilen gleich aussehen.
 */
public record RowControls(
    @NonNull Label valueLabel, @NonNull Label costLabel, @NonNull Button minus,
    @NonNull Button plus) {

  /** Neue Zeile: „−“, Wert, „+“ und Kosten, jeweils mit der Stilklasse des Designsystems. */
  public static @NonNull RowControls create() {
    Label value = new Label();
    value.getStyleClass().add(STYLE_ATTR_VALUE);
    Label cost = new Label();
    cost.getStyleClass().add(STYLE_ATTR_BP);
    return new RowControls(value, cost, minusButton(), plusButton());
  }

  /** Knopf „−“ eines Steppers. */
  public static @NonNull Button minusButton() {
    return stepButton("−");
  }

  /** Knopf „+“ eines Steppers. */
  public static @NonNull Button plusButton() {
    return stepButton("+");
  }

  private static @NonNull Button stepButton(@NonNull String text) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_ATTR_BTN);
    return button;
  }
}
