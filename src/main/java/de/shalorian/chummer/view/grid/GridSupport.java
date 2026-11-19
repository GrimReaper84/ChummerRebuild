package de.shalorian.chummer.view.grid;

import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_HEADER;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE_WARN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_KEY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_VALUE;

import de.shalorian.chummer.model.character.rulebook.RulebookDataLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Gemeinsame Bausteine der inhaltsbasierten Grid-Layouts (MigPane) aller Tabs: Zellen und Kopfzeilen
 * einfügen, Schrumpfen verhindern, Icon-Buttons, Warnstil und die Labels der Detailbereiche (Waffen,
 * Ausrüstung).
 */
public final class GridSupport {

  /** Zusatz am Namen von Einträgen mit Gratis-Anteil aus dem Spielleiter-Modus (ADR-070). */
  private static final String GM_FREE_SUFFIX = " (gratis)";

  /** Hängt {@code style} an {@code control} an und gibt es zurück. */
  public static <T extends Control> @NonNull T styled(@NonNull T control, @NonNull String style) {
    control.getStyleClass().add(style);
    return control;
  }

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
   * Knopf zum Einbauen: überall dasselbe Icon (Puzzleteil) mit dem Text „Einbauen“, weil ein reines Icon dafür schwer
   * zu erkennen ist (Beta-Befund 2026-11-20); {@code tooltip} nennt das Ziel.
   */
  public static @NonNull Button installButton(@NonNull String tooltip) {
    return iconTextButton("Einbauen", FontAwesomeSolid.PUZZLE_PIECE, tooltip);
  }

  /** Sekundärknopf mit Icon und sichtbarem Text, {@code tooltip} erklärt die Aktion genauer. */
  public static @NonNull Button iconTextButton(@NonNull String text, @NonNull Ikon icon, @NonNull String tooltip) {
    Button button = new Button(text);
    button.getStyleClass().add(StyleClasses.STYLE_BTN_SECONDARY);
    button.setGraphic(new FontIcon(icon));
    button.setTooltip(new Tooltip(tooltip));
    return button;
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

  /** Beschriftung eines Werts im Detailbereich, z.B. "Preis:". */
  public static @NonNull Label detailKey(@NonNull String text) {
    Label label = new Label(text + ":");
    label.getStyleClass().add(STYLE_DETAIL_KEY);
    return label;
  }

  /** Wert im Detailbereich. */
  public static @NonNull Label detailValue(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_DETAIL_VALUE);
    return label;
  }

  /**
   * Hängt die Quelle („Grundregelwerk, S. 205“) an den Tooltip von {@code name} an; Tabellen haben keine eigene
   * Quellenspalte mehr (Nutzerbefund 2026-11-20).
   */
  public static void addSourceTooltip(@NonNull Label name, @NonNull String code, @NonNull String page) {
    appendTooltip(name, RulebookDataLoader.describeSource(code, page));
  }

  private static void appendTooltip(@NonNull Label label, @NonNull String text) {
    Tooltip existing = label.getTooltip();
    label.setTooltip(new Tooltip(existing == null ? text : existing.getText() + "\n" + text));
  }

  /**
   * Kennzeichnet einen Eintrag mit Gratis-Anteil aus dem Spielleiter-Modus (ADR-070): Zusatz „(gratis)“ am Namen und
   * der Betrag im Tooltip (an einen vorhandenen angehängt); ohne Gratis-Anteil bleibt {@code name} unverändert.
   * Mehrfaches Aufrufen (z.B. aus einem Refresh, der den Tooltip neu setzt) hängt nichts doppelt an.
   */
  public static void markGmFree(@NonNull Label name, boolean free, @NonNull String amount) {
    if (!free) {
      return;
    }
    if (!name.getText().endsWith(GM_FREE_SUFFIX)) {
      name.setText(name.getText() + GM_FREE_SUFFIX);
    }
    Tooltip existing = name.getTooltip();
    if (existing == null || !existing.getText().contains(gmFreeText(amount))) {
      appendTooltip(name, gmFreeText(amount));
    }
  }


  /** Hinweis im Detailbereich zum Gratis-Anteil aus dem Spielleiter-Modus (ADR-070). */
  public static @NonNull Label gmFreeHint(@NonNull String amount) {
    return detailHint(gmFreeText(amount));
  }

  private static @NonNull String gmFreeText(@NonNull String amount) {
    return "Gratis (Spielleiter): " + amount;
  }

  /** Hinweistext im Detailbereich (gedämpft). */
  public static @NonNull Label detailHint(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_DETAIL_HINT);
    return label;
  }
}
