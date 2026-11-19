package de.shalorian.chummer.view.document;

import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Ein offenes Dokument im Hauptfenster (ADR-066): Charakter oder Schergengruppe mit eigenem Tab und
 * {@link DocumentState}. Der Tab zeigt den Namen und „*“, solange der Stand vom Speicherstand abweicht. Damit der Stern
 * ohne Rückmeldungen aus allen Bereichen aktuell bleibt, prüft das Dokument nach jedem Loslassen einer Maus- oder
 * Tastaturtaste in seinem Inhalt erneut ({@link Platform#runLater}, also nach dem Handler). Öffnet der Handler einen
 * modalen Dialog, läuft diese Prüfung schon während des Dialogs; deshalb prüft das Hauptfenster das aktive Dokument
 * zusätzlich, wenn es den Fokus zurückbekommt. Die Rückfrage beim Schließen vergleicht immer frisch über
 * {@link #isDirty()}.
 */
public abstract class OpenDocument {

  private final @NonNull Tab tab = new Tab();
  private final @NonNull Tooltip tooltip = new Tooltip();
  private final @NonNull DocumentState state;
  private @NonNull Runnable onLabelChanged = () -> { };
  private boolean markedDirty;

  protected OpenDocument(@NonNull DocumentState state, @NonNull Ikon icon) {
    this.state = state;
    tab.setGraphic(new FontIcon(icon));
    tab.setTooltip(tooltip);
  }

  /** Setzt den Inhalt des Tabs; von den Unterklassen nach dem Aufbau der Ansicht aufzurufen. */
  protected final void attach(@NonNull Node content) {
    tab.setContent(content);
    content.addEventFilter(MouseEvent.MOUSE_RELEASED, _ -> Platform.runLater(this::refreshLabel));
    content.addEventFilter(KeyEvent.KEY_RELEASED, _ -> Platform.runLater(this::refreshLabel));
    refreshLabel();
  }

  /** Anzeigename im Tab und im Fenstertitel. */
  public abstract @NonNull String name();

  /** Aktueller Stand im Speicherformat. */
  protected abstract byte @NonNull [] serialize();

  /** Schreibt das Dokument nach {@code file}. */
  protected abstract void write(@NonNull File file) throws IOException;

  /** Baut die Ansicht neu, z.B. nach geänderten Anzeige-Einstellungen. */
  public abstract void rebuild();

  /** Ob der aktuelle Stand ungespeichert ist; vergleicht frisch. */
  public boolean isDirty() {
    return state.isDirty(serialize());
  }

  /** Stand der letzten Prüfung, für Fenstertitel ohne erneute Serialisierung. */
  public boolean isMarkedDirty() {
    return markedDirty;
  }

  /** Speichert nach {@code file} und merkt den Stand als gespeichert. */
  public void saveTo(@NonNull File file) throws IOException {
    write(file);
    state.markSaved(file, serialize());
    refreshLabel();
  }

  /** Aktualisiert Name, Stern und Tooltip des Tabs und meldet das an das Hauptfenster. */
  public void refreshLabel() {
    markedDirty = isDirty();
    tab.setText(name() + (markedDirty ? " *" : ""));
    File file = state.getFile();
    tooltip.setText(file != null ? file.getPath()
        : state.isBetaCopy() ? "Beta-Kopie, nicht gespeichert" : "nicht gespeichert");
    onLabelChanged.run();
  }

  /** Wird nach jeder Aktualisierung des Tabs aufgerufen (Fenstertitel). */
  void setOnLabelChanged(@NonNull Runnable onLabelChanged) {
    this.onLabelChanged = onLabelChanged;
  }

  public @NonNull Tab tab() {
    return tab;
  }

  public @NonNull DocumentState state() {
    return state;
  }
}
