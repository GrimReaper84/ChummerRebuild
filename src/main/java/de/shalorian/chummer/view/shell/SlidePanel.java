package de.shalorian.chummer.view.shell;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Ein Panel, das von rechts über den Grundinhalt gleitet, geöffnet über einen schwebenden runden Knopf (übernommen aus
 * dem Fallout2d20-Projekt). Es deckt ein Drittel der Breite ab, mindestens die Breite seines Inhalts. Solange es offen
 * ist, liegt eine Abdeckung über dem Grundinhalt: Ein Klick darauf schließt nur das Panel und erreicht nichts darunter.
 * Der Knopf lässt sich am rechten Rand nach oben und unten ziehen; seine Position merkt sich {@link
 * FloatingButtonPosition}.
 */
public final class SlidePanel {

  private static final Duration SLIDE_TIME = Duration.millis(180);
  private static final double HOST_SHARE = 1.0 / 3;
  /** Platz für Bildlaufleiste und Innenabstand neben dem Inhalt. */
  private static final double CONTENT_EXTRA = 40;
  private static final double BUTTON_MARGIN = 16;
  private static final int BUTTON_SIZE = 56;
  private static final int ICON_SIZE = 24;
  private static final double SHADOW_RADIUS = 12;
  private static final double SHADOW_OPACITY = 0.45;
  /** Unterhalb dieser Strecke zählt Drücken und Loslassen noch als Klick. */
  private static final double DRAG_THRESHOLD = 5;

  private final @NonNull ScrollPane panel = new ScrollPane();
  private final @NonNull Button button = new Button();
  private final @NonNull Pane scrim = new Pane();
  private final @NonNull StackPane host;
  private final @NonNull TranslateTransition slide = new TranslateTransition(SLIDE_TIME, panel);
  private boolean available = true;
  private double dragStartSceneY;
  private double dragStartPosition;
  /** Ob das aktuelle Drücken zum Ziehen wurde, das das Panel nicht öffnen darf. */
  private boolean dragged;

  /**
   * Erzeugt das geschlossene Panel über dem Grundinhalt.
   *
   * @param icon        Icon des Knopfs
   * @param tooltip     Tooltip des Knopfs
   * @param position    wo der Knopf am rechten Rand sitzt
   */
  public SlidePanel(@NonNull Node base, @NonNull Ikon icon, @NonNull String tooltip,
      @NonNull FloatingButtonPosition position) {
    FontIcon graphic = new FontIcon(icon);
    graphic.setIconSize(ICON_SIZE);
    button.setGraphic(graphic);
    button.setTooltip(new Tooltip(tooltip));
    button.getStyleClass().setAll("floating-button");
    // Nur das Icon, mittig und ohne Innenabstand; Rundung und Farben kommen aus style.css (.floating-button).
    button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    button.setAlignment(Pos.CENTER);
    button.setPadding(Insets.EMPTY);
    button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setEffect(shadow());
    button.setOnAction(_ -> {
      if (!dragged) {
        open();
      }
    });
    scrim.getStyleClass().add("slide-panel-scrim");
    scrim.setPickOnBounds(true);
    scrim.setVisible(false);
    scrim.setOnMousePressed(event -> {
      close();
      event.consume();
    });
    panel.setFitToWidth(true);
    panel.getStyleClass().add("slide-panel");
    panel.setPadding(new Insets(20));
    panel.setEffect(shadow());
    panel.setVisible(false);
    host = new StackPane(base, button, scrim, panel);
    StackPane.setAlignment(button, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(button, new Insets(BUTTON_MARGIN));
    StackPane.setAlignment(panel, Pos.CENTER_RIGHT);
    host.widthProperty().addListener((_, _, _) -> {
      if (panel.isVisible()) {
        fitWidth();
      }
    });
    // Eine Bindung hört schwach zu; die gemeinsame Position hält so keine geschlossene Ansicht am Leben.
    button.translateYProperty().bind(Bindings.createDoubleBinding(() -> -moveRange() * position.value().get(),
        host.heightProperty(), position.value()));
    makeDraggable(position);
  }

  private void makeDraggable(@NonNull FloatingButtonPosition position) {
    button.setOnMousePressed(event -> {
      dragStartSceneY = event.getSceneY();
      dragStartPosition = position.value().get();
      dragged = false;
    });
    button.setOnMouseDragged(event -> {
      double deltaY = event.getSceneY() - dragStartSceneY;
      if (dragged || Math.abs(deltaY) >= DRAG_THRESHOLD) {
        dragged = true;
        button.setCursor(Cursor.V_RESIZE);
        position.value().set(FloatingButtonPosition.afterDrag(dragStartPosition, deltaY, moveRange()));
      }
    });
    button.setOnMouseReleased(_ -> {
      if (dragged) {
        button.setCursor(null);
        position.save();
        // Der Knopf löst seine Aktion beim Loslassen aus; erst danach darf er das Panel wieder öffnen.
        Platform.runLater(() -> dragged = false);
      }
    });
  }

  /** Die Pixel zwischen tiefster und höchster Knopfposition. */
  private double moveRange() {
    return Math.max(0, host.getHeight() - BUTTON_SIZE - 2 * BUTTON_MARGIN);
  }

  /** Grundinhalt samt Knopf, Abdeckung und Panel. */
  public @NonNull Node node() {
    return host;
  }

  /** Ersetzt den Inhalt; ein offenes Panel bleibt offen und behält seine Bildlaufposition. */
  public void setContent(@NonNull Node content) {
    double scrollPosition = panel.getVvalue();
    panel.setContent(content);
    Platform.runLater(() -> panel.setVvalue(scrollPosition));
    if (panel.isVisible()) {
      fitWidth();
    }
  }

  /** Ob das Panel nutzbar ist; ein nicht nutzbares ist geschlossen und hat keinen Knopf. */
  public void setAvailable(boolean value) {
    available = value;
    if (!value) {
      slide.stop();
      showClosed();
    }
    button.setVisible(value && !panel.isVisible());
  }

  /** Breite des Panels: sein Anteil am Host, mindestens die Inhaltsbreite, nie breiter als der Host. */
  static double width(double hostWidth, double contentWidth) {
    return Math.min(hostWidth, Math.max(hostWidth * HOST_SHARE, contentWidth + CONTENT_EXTRA));
  }

  private void open() {
    slide.stop();
    slide.setOnFinished(null);
    button.setVisible(false);
    scrim.setVisible(true);
    panel.setVisible(true);
    fitWidth();
    panel.setTranslateX(panel.getPrefWidth());
    slide.setToX(0);
    slide.playFromStart();
  }

  /** Gleitet hinaus; Abdeckung und Knopf ändern sich erst am Ende, damit nichts darunter den Rest des Klicks bekommt. */
  private void close() {
    slide.stop();
    slide.setToX(panel.getPrefWidth());
    slide.setOnFinished(_ -> showClosed());
    slide.playFromStart();
  }

  private void showClosed() {
    panel.setVisible(false);
    scrim.setVisible(false);
    button.setVisible(available);
  }

  private void fitWidth() {
    double contentWidth = 0;
    Node content = panel.getContent();
    if (content != null) {
      content.applyCss();
      contentWidth = content.prefWidth(-1);
    }
    double width = width(host.getWidth(), contentWidth);
    panel.setPrefWidth(width);
    panel.setMaxWidth(width);
  }

  private static @NonNull DropShadow shadow() {
    return new DropShadow(SHADOW_RADIUS, Color.gray(0, SHADOW_OPACITY));
  }
}
