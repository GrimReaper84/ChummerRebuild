package de.shalorian.chummer.view.tab;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Bausteine im Kartenstil (Designsystem, ADR-049), gemeinsam für Status-Übersicht und Cheat-Sheet, damit beide gleich
 * aussehen: Karte, Kartentitel, Kennzahl mit Beschriftung, Pool-Zeile und gleich breite Spalten.
 */
final class StatusCards {

  /** Abstand zwischen den Karten. */
  static final double CARD_GAP = 12;
  /** Bevorzugte Breite der Karten; das Slide-Panel misst die Übersicht und ist mindestens so breit. */
  static final double CARD_WIDTH = 400;

  private StatusCards() {
  }

  /** Inhalt als Karte ({@code .card}). */
  static @NonNull VBox card(@NonNull Node content) {
    VBox card = new VBox(content);
    card.getStyleClass().add("card");
    return card;
  }

  /** Kartentitel über dem Inhalt. */
  static @NonNull VBox titled(@NonNull String title, @NonNull Node content) {
    return new VBox(8, styled(new Label(title), "card-title"), content);
  }

  /** Große Zahl mit Beschriftung darunter; {@code accent} hebt die Zahl hervor. */
  static @NonNull VBox figure(@NonNull String value, @NonNull String caption, boolean accent) {
    Label number = styled(new Label(value), "key-figure");
    if (accent) {
      number.getStyleClass().add("key-figure-accent");
    }
    Label text = styled(new Label(caption), "key-caption");
    text.setWrapText(true);
    return new VBox(0, number, text);
  }

  /** Zeile „Anlass … Würfel“, Aufschlüsselung im Tooltip. */
  static @NonNull HBox poolRow(@NonNull String caption, @NonNull String diceText, @Nullable String breakdown) {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox row = new HBox(8, styled(new Label(caption), "key-caption"), spacer, styled(new Label(diceText), "key-figure"));
    row.setAlignment(Pos.CENTER_LEFT);
    if (breakdown != null) {
      Tooltip.install(row, new Tooltip(breakdown));
    }
    return row;
  }

  /** Raster mit {@code count} gleich breiten Spalten. */
  static @NonNull GridPane columns(int count) {
    GridPane grid = new GridPane();
    grid.setHgap(8);
    for (int i = 0; i < count; i++) {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(100.0 / count);
      grid.getColumnConstraints().add(column);
    }
    return grid;
  }

  static <T extends Label> @NonNull T styled(@NonNull T label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
