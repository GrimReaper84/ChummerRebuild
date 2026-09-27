package de.shalorian.chummer.view.grid;

import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_HEADER;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE_WARN;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Gemeinsame Bausteine der inhaltsbasierten Grid-Layouts (MigPane) aller Tabs: Zellen und Kopfzeilen
 * einfügen, Schrumpfen verhindern, Icon-Buttons und Warnstil.
 */
public final class GridSupport {

  private GridSupport() {
  }

  /**
   * Fügt die übergebenen Knoten spaltenweise (Spalte 0, 1, 2, …) in {@code grid} ein, in der angegebenen Zeile. Gemeinsam genutzt von allen
   * Grid-Zeilen-Buildern, die durchgehend ab Spalte 0 befüllen (Attribut-, Sonderattribut-, Wissensfertigkeiten- und Skill-Zeilen).
   */
  public static void addRowCells(@NonNull MigPane grid, int row, @NonNull Node... cells) {
    for (int col = 0; col < cells.length; col++) {
      preventShrink(cells[col]);
      grid.add(cells[col], "cell " + col + " " + row);
    }
  }

  /** Kopfzeile (Zeile 0) eines Grids; leere Einträge lassen die Spalte ohne Überschrift. */
  public static void addHeaderRow(@NonNull MigPane grid, @NonNull String... headers) {
    for (int i = 0; i < headers.length; i++) {
      if (!headers[i].isEmpty()) {
        Label h = new Label(headers[i]);
        h.getStyleClass().add(STYLE_ATTR_HEADER);
        preventShrink(h);
        grid.add(h, "cell " + i + " 0");
      }
    }
  }

  /**
   * Verhindert, dass {@code node} unter seine bevorzugte Breite schrumpft. {@code MigPane}/{@code GridPane}
   * ermitteln die Minimalbreite einer Spalte aus {@code minWidth()} der enthaltenen Controls; ohne diesen
   * Aufruf könnten Labels/Buttons ihren Text per Ellipsis abschneiden, statt die Spalte auf Inhaltsbreite
   * zu halten — genau das soll das inhaltsbasierte Spalten-Layout verhindern.
   */
  public static void preventShrink(@NonNull Node node) {
    if (node instanceof Region region) {
      region.setMinWidth(Region.USE_PREF_SIZE);
    }
  }

  /**
   * Ersetzt den Text von {@code button} durch ein FontAwesome-Icon; der bisherige Text bleibt als
   * Tooltip erhalten, damit die Schaltfläche ohne Beschriftung weiterhin erkennbar bleibt.
   */
  public static void useIcon(@NonNull Button button, @NonNull Ikon icon) {
    String label = button.getText();
    button.setText(null);
    button.setGraphic(new FontIcon(icon));
    button.setTooltip(new Tooltip(label));
  }

  /**
   * Umschließt {@code content} (typischerweise eines der inhaltsbasierten Grids) mit einem wachsenden,
   * leeren Platzhalter ganz rechts. Dadurch landet ungenutzter Platz im Vollbildmodus dort, statt
   * Lücken zwischen den Spalten von {@code content} selbst aufzureißen — {@code content} bleibt bei
   * seiner inhaltsbasierten Breite als geschlossener Block am linken Rand.
   */
  public static @NonNull HBox withRightFiller(@NonNull Region content) {
    Region filler = new Region();
    HBox.setHgrow(filler, Priority.ALWAYS);
    return new HBox(content, filler);
  }

  /** Schaltet den roten Warnstil (z.B. negatives Budget) eines Labels ein oder aus. */
  public static void setWarnStyle(@NonNull Label label, boolean warn) {
    label.getStyleClass().remove(STYLE_BP_VALUE_WARN);
    if (warn) {
      label.getStyleClass().add(STYLE_BP_VALUE_WARN);
    }
  }
}
