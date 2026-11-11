package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.Notifications;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Gemeinsame Dienste aller Tabs des Charakterbogens: Dialoge im App-Stil, Hinweise, Ja/Nein-Abfragen und die
 * Karrieremodus-Abbuchung von Karma bzw. Nuyen samt Bestätigung und Log-Eintrag. Erreichbar über
 * {@link CharacterTabHost#services()}.
 */
public final class TabServices {

  private final @NonNull Supplier<Window> owner;
  private final @NonNull Runnable onLogChanged;

  /**
   * @param owner        liefert das Fenster des Charakterbogens; wird erst beim Öffnen eines Dialogs abgefragt,
   *                     weil die Szene bei der Initialisierung noch nicht steht
   * @param onLogChanged wird nach jeder Abbuchung aufgerufen (Log- und Guthabenanzeige nachziehen)
   */
  public TabServices(@NonNull Supplier<Window> owner, @NonNull Runnable onLogChanged) {
    this.owner = owner;
    this.onLogChanged = onLogChanged;
  }

  /**
   * Lädt ein Dialog-FXML und richtet die zugehörige modale Stage (Titel, Modality, Stylesheet) ein. Der Aufrufer
   * holt sich den Controller über {@code dialog.loader().getController()}, verdrahtet ihn und ruft
   * {@code dialog.stage().showAndWait()} auf.
   */
  public @NonNull DialogHandle openDialog(@NonNull String fxmlName, @NonNull String title)
      throws IOException {
    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource(fxmlName)));
    Parent root = loader.load();

    Stage dialogStage = new Stage();
    dialogStage.setTitle(title);
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(owner.get());
    dialogStage.setResizable(false);
    Scene scene = new Scene(root);
    scene.getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
    dialogStage.setScene(scene);

    return new DialogHandle(loader, dialogStage);
  }

  /**
   * Hängt das App-Stylesheet an einen {@link Alert} oder {@link TextInputDialog} — deren {@code DialogPane} bekommt
   * sonst nie {@code style.css} zugewiesen und rendert rein mit dem JavaFX-Standard-Theme "Modena", was neben dem
   * restlichen dunklen UI wie ein Fremdkörper wirkt.
   */
  public void applyDialogStyle(@NonNull DialogPane pane) {
    pane.getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
  }

  /** Setzt Stylesheet und Besitzerfenster eines Alerts bzw. Eingabedialogs. */
  private void prepare(@NonNull Dialog<?> dialog) {
    applyDialogStyle(dialog.getDialogPane());
    dialog.initOwner(owner.get());
  }

  public void showDialogLoadError() {
    Notifications.create()
        .title("Fehler")
        .text("Dialog konnte nicht geöffnet werden")
        .position(Pos.BOTTOM_RIGHT)
        .owner(owner.get())
        .showError();
  }

  /** Weicher Hinweis unten rechts (Projektregeln Abschnitt 8). */
  public void warn(@NonNull String title, @NonNull String text) {
    Notifications.create()
        .title(title)
        .text(text)
        .position(Pos.BOTTOM_RIGHT)
        .owner(owner.get())
        .showWarning();
  }

  /** Ja/Nein-Dialog im App-Stil; {@code true} nur bei "Ja". */
  public boolean confirmYesNo(@NonNull String title, @NonNull String header,
      @NonNull String content) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
    prepare(confirm);
    confirm.setTitle(title);
    confirm.setHeaderText(header);
    return confirm.showAndWait().filter(btn -> btn == ButtonType.YES).isPresent();
  }

  /**
   * Bucht einen Karrieremodus-Kauf vom Karma-Guthaben ab (Guthabenprüfung, Bestätigung, Log-Eintrag).
   *
   * @return true, wenn NICHT gebucht wurde — der Aufrufer bricht dann den Kauf ab
   */
  public boolean karmaChargeFailed(@NonNull Character c, @NonNull String label, int karmaCost) {
    return chargeFailed(SpendCurrency.KARMA, label, karmaCost, c.getKarmaTotal(),
        c::addKarmaEntry);
  }

  /**
   * Gegenstück zu {@link #karmaChargeFailed} für Nuyen-Käufe im Karrieremodus (Cyberware, Rüstung; Muster des
   * C#-Originals: Guthabenprüfung, Log-Eintrag, sonst Abbruch mit Hinweis).
   *
   * @return true, wenn NICHT gebucht wurde — der Aufrufer bricht dann den Kauf ab
   */
  public boolean nuyenChargeFailed(@NonNull Character c, @NonNull String label, int nuyenCost) {
    return chargeFailed(SpendCurrency.NUYEN, label, nuyenCost, c.getNuyenTotal(),
        c::addNuyenEntry);
  }

  /**
   * Gemeinsamer Ablauf aller Karrieremodus-Käufe: bei zu wenig Guthaben Hinweis und Abbruch, sonst Bestätigung
   * einholen und einen negativen Log-Eintrag über {@code logSink} buchen.
   *
   * @return true, wenn NICHT gebucht wurde (zu wenig Guthaben oder abgebrochen)
   */
  private boolean chargeFailed(@NonNull SpendCurrency currency, @NonNull String label, int cost,
      int available, @NonNull Consumer<LogEntry> logSink) {
    if (available < cost) {
      warn("Nicht genug " + currency.getDisplayName(), "Benötigt: " + currency.formatAmount(cost));
      return true;
    }
    if (!confirmSpend(currency, label, cost, available, null)) {
      return true;
    }
    logSink.accept(new LogEntry(label, -cost, LocalDate.now(ZoneId.systemDefault())));
    onLogChanged.run();
    return false;
  }

  public boolean confirmKarmaSpend(@NonNull String label, int karmaCost,
      @NonNull Character character) {
    return confirmKarmaSpend(label, karmaCost, character, null);
  }

  /**
   * Wie {@link #confirmKarmaSpend(String, int, Character)}, aber mit einem optionalen Zusatzhinweis (z.B.
   * Gruppenauflösungs-Warnung), der vor dem eigentlichen Kostentext eingeblendet wird — ein einziger kombinierter
   * Dialog statt zweier verschachtelter Bestätigungen.
   */
  public boolean confirmKarmaSpend(@NonNull String label, int karmaCost,
      @NonNull Character character, @Nullable String extraWarning) {
    return confirmSpend(SpendCurrency.KARMA, label, karmaCost, character.getKarmaTotal(),
        extraWarning);
  }

  /**
   * Bestätigungsdialog für einen Karrieremodus-Kauf; {@code extraWarning} wird dem Kostentext vorangestellt.
   */
  private boolean confirmSpend(@NonNull SpendCurrency currency, @NonNull String label, int cost,
      int available, @Nullable String extraWarning) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    prepare(confirm);
    confirm.setTitle(currency.getDisplayName() + " ausgeben");
    confirm.setHeaderText(label);
    String content = currency.formatAmount(cost) + " ausgeben?\n\n"
        + currency.formatRemaining(available - cost);
    if (extraWarning != null) {
      content = extraWarning + "\n\n" + content;
    }
    confirm.setContentText(content);
    return confirm.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
  }
}
