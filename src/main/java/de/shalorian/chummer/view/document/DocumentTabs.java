package de.shalorian.chummer.view.document;

import de.shalorian.chummer.view.dialog.DialogButtons;
import de.shalorian.chummer.view.tab.TabServices;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Tableiste der offenen Dokumente im Hauptfenster (ADR-066). Schließen über das ✕ eines Tabs, {@link #close} oder
 * {@link #closeAll} fragt bei ungespeicherten Dokumenten „Speichern / Verwerfen / Abbrechen“; vor der Frage wird der
 * Tab aktiviert. Beim Beenden kommt die Frage je Dokument nacheinander; Abbrechen oder ein abgebrochenes Speichern
 * lässt das Dokument offen und stoppt den Vorgang, bereits geschlossene bleiben geschlossen. Die Tabs sind die einzige
 * Liste der offenen Dokumente; jeder Tab trägt sein Dokument als {@link Tab#getUserData()}.
 */
public final class DocumentTabs {

  private static final ButtonType SAVE = DialogButtons.SAVE;
  private static final ButtonType DISCARD = DialogButtons.DISCARD;
  private static final ButtonType CANCEL = DialogButtons.CANCEL;

  private final @NonNull TabPane pane = new TabPane();
  private final @NonNull Predicate<OpenDocument> save;
  private final @NonNull Runnable onChanged;

  /**
   * @param save      speichert ein Dokument (ggf. mit Dateiauswahl); {@code false}, wenn abgebrochen oder gescheitert
   * @param onChanged nach einem Wechsel des aktiven Tabs, dem Öffnen oder Schließen eines Tabs und nach jeder
   *                  Aktualisierung eines Tab-Textes (Fenstertitel, Menüs)
   */
  public DocumentTabs(@NonNull Predicate<OpenDocument> save, @NonNull Runnable onChanged) {
    this.save = save;
    this.onChanged = onChanged;
    pane.getStyleClass().add("document-tabs");
    pane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    pane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
    // Tabwechsel, Öffnen und Schließen ändern die Auswahl; refreshLabel meldet selbst an onChanged
    pane.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
      OpenDocument active = active();
      if (active != null) {
        active.refreshLabel();
      } else {
        onChanged.run();
      }
    });
  }

  public @NonNull Node node() {
    return pane;
  }

  /** Fügt {@code document} als neuen Tab hinzu und aktiviert ihn. */
  public void add(@NonNull OpenDocument document) {
    Tab tab = document.tab();
    tab.setOnCloseRequest(event -> {
      if (!confirmClose(document)) {
        event.consume();
      }
    });
    tab.setUserData(document);
    document.setOnLabelChanged(onChanged);
    pane.getTabs().add(tab);
    pane.getSelectionModel().select(tab);
  }

  /** Das Dokument des aktiven Tabs, {@code null} ohne Tab. */
  public @Nullable OpenDocument active() {
    Tab selected = pane.getSelectionModel().getSelectedItem();
    return selected == null ? null : document(selected);
  }

  /** Alle offenen Dokumente in Tab-Reihenfolge. */
  public @NonNull List<OpenDocument> all() {
    return pane.getTabs().stream().map(DocumentTabs::document).toList();
  }

  public boolean isEmpty() {
    return pane.getTabs().isEmpty();
  }

  /** Das Dokument, das {@code file} geöffnet hat oder dorthin gespeichert wurde; {@code null}, wenn keins. */
  public @Nullable OpenDocument findByFile(@NonNull File file) {
    Path wanted = normalized(file);
    for (OpenDocument document : all()) {
      File own = document.state().getFile();
      if (own != null && normalized(own).equals(wanted)) {
        return document;
      }
    }
    return null;
  }

  public void select(@NonNull OpenDocument document) {
    pane.getSelectionModel().select(document.tab());
  }

  /** Schließt {@code document} nach Rückfrage; {@code false}, wenn es offen bleibt. */
  public boolean close(@NonNull OpenDocument document) {
    if (!confirmClose(document)) {
      return false;
    }
    pane.getTabs().remove(document.tab());
    return true;
  }

  /** Schließt alle Dokumente nacheinander; {@code false}, sobald eines offen bleibt. */
  public boolean closeAll() {
    for (OpenDocument document : all()) {
      if (!close(document)) {
        return false;
      }
    }
    return true;
  }

  private boolean confirmClose(@NonNull OpenDocument document) {
    if (!document.isDirty()) {
      return true;
    }
    select(document);
    ButtonType answer = ask(document);
    return answer == SAVE ? save.test(document) : answer == DISCARD;
  }

  /** „Speichern / Verwerfen / Abbrechen“; Esc oder Schließen des Dialogs gilt als Abbrechen. */
  private @NonNull ButtonType ask(@NonNull OpenDocument document) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Änderungen gehen sonst verloren.", SAVE, DISCARD, CANCEL);
    TabServices.applyDialogStyle(alert);
    applyLinuxButtonOrder(alert);
    if (pane.getScene() != null) {
      alert.initOwner(pane.getScene().getWindow());
    }
    alert.setTitle("Ungespeicherte Änderungen");
    alert.setHeaderText("„%s“ speichern?".formatted(document.name()));
    return alert.showAndWait().orElse(CANCEL);
  }

  /**
   * JavaFX ordnet unter Linux „Verwerfen, Speichern, Abbrechen“. GTK-Desktops (GNOME, Cinnamon, MATE, Xfce) erwarten
   * Verwerfen links und Speichern ganz rechts wie macOS, KDE die Windows-Reihenfolge „Speichern, Verwerfen,
   * Abbrechen“. Unter Windows und macOS stimmt die eingebaute Reihenfolge schon.
   */
  private static void applyLinuxButtonOrder(@NonNull Alert alert) {
    if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux")) {
      return;
    }
    String desktop = Objects.requireNonNullElse(System.getenv("XDG_CURRENT_DESKTOP"), "");
    String order = desktop.toUpperCase(Locale.ROOT).contains("KDE")
        ? ButtonBar.BUTTON_ORDER_WINDOWS : ButtonBar.BUTTON_ORDER_MAC_OS;
    if (alert.getDialogPane().lookup(".button-bar") instanceof ButtonBar bar) {
      bar.setButtonOrder(order);
    }
  }

  private static @NonNull OpenDocument document(@NonNull Tab tab) {
    return (OpenDocument) tab.getUserData();
  }

  private static @NonNull Path normalized(@NonNull File file) {
    return file.toPath().toAbsolutePath().normalize();
  }
}
