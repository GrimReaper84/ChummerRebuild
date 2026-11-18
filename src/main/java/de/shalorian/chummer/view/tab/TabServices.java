package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.view.dialog.CatalogSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.Notifications;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Gemeinsame Dienste aller Tabs des Charakterbogens: Dialoge im App-Stil, Hinweise, Ja/Nein-Abfragen und die
 * Karrieremodus-Abbuchung von Karma bzw. Nuyen samt Bestätigung und Log-Eintrag. Erreichbar über
 * {@link CharacterTabHost#services()}.
 */
public final class TabServices {

  private static final int DIALOG_ICON_SIZE = 28;

  private final @NonNull Supplier<Window> owner;
  private final @NonNull Runnable onLogChanged;
  private final @NonNull Supplier<@Nullable RuleOptions> ruleOptions;
  private final @NonNull UndoHistory undoHistory = new UndoHistory();

  /**
   * @param owner        liefert das Fenster des Charakterbogens; wird erst beim Öffnen eines Dialogs abgefragt,
   *                     weil die Szene bei der Initialisierung noch nicht steht
   * @param onLogChanged wird nach jeder Abbuchung aufgerufen (Log- und Guthabenanzeige nachziehen)
   * @param ruleOptions  Regel-Einstellungen des offenen Charakters ({@code null} ohne Charakter); Auswahldialoge
   *                     blenden damit Einträge aus abgeschalteten Regelwerken aus
   */
  public TabServices(@NonNull Supplier<Window> owner, @NonNull Runnable onLogChanged,
      @NonNull Supplier<@Nullable RuleOptions> ruleOptions) {
    this.owner = owner;
    this.onLogChanged = onLogChanged;
    this.ruleOptions = ruleOptions;
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
    RuleOptions options = ruleOptions.get();
    // zentral, damit kein Auswahldialog den Regelwerk-Filter vergisst (ADR-061)
    if (options != null && loader.getController() instanceof CatalogSelectDialogController<?> catalog) {
      catalog.setRuleOptions(options);
    }

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
   * Passt einen {@link Alert} oder {@link TextInputDialog} an die App an: hängt {@code style.css} an (sonst rendert die
   * {@code DialogPane} im Standard-Theme „Modena“) und ersetzt das eingebaute Java-Symbol durch ein Font-Awesome-Icon in
   * der Akzentfarbe: Stift für Eingaben, Fragezeichen für Rückfragen, Dreieck für Warnungen, Kreis für Fehler und
   * Hinweise.
   */
  public static void applyDialogStyle(@NonNull Dialog<?> dialog) {
    dialog.getDialogPane().getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
    FontIcon icon = new FontIcon(dialogIcon(dialog));
    icon.setIconSize(DIALOG_ICON_SIZE);
    icon.getStyleClass().add("dialog-icon");
    dialog.setGraphic(icon);
  }

  private static @NonNull Ikon dialogIcon(@NonNull Dialog<?> dialog) {
    if (!(dialog instanceof Alert alert)) {
      return FontAwesomeSolid.PEN;
    }
    return switch (alert.getAlertType()) {
      case CONFIRMATION -> FontAwesomeSolid.QUESTION_CIRCLE;
      case WARNING -> FontAwesomeSolid.EXCLAMATION_TRIANGLE;
      case ERROR -> FontAwesomeSolid.EXCLAMATION_CIRCLE;
      case INFORMATION, NONE -> FontAwesomeSolid.INFO_CIRCLE;
    };
  }

  /** Icon der Hinweise unten rechts statt des eingebauten ControlsFX-Symbols. */
  private static @NonNull FontIcon notificationIcon(@NonNull Ikon ikon) {
    FontIcon icon = new FontIcon(ikon);
    icon.setIconSize(DIALOG_ICON_SIZE);
    // Das Hinweis-Popup lädt style.css nicht, daher die Akzentfarbe (-cr-accent) direkt.
    icon.setIconColor(Color.web("#4fd1c5"));
    return icon;
  }

  /** Setzt Stil, Icon und Besitzerfenster eines Alerts bzw. Eingabedialogs. */
  private void prepare(@NonNull Dialog<?> dialog) {
    applyDialogStyle(dialog);
    dialog.initOwner(owner.get());
  }

  public void showDialogLoadError() {
    Notifications.create()
        .title("Fehler")
        .text("Dialog konnte nicht geöffnet werden")
        .position(Pos.BOTTOM_RIGHT)
        .owner(owner.get())
        .graphic(notificationIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE))
        .show();
  }

  /** Weicher Hinweis unten rechts (Projektregeln Abschnitt 8). */
  public void warn(@NonNull String title, @NonNull String text) {
    Notifications.create()
        .title(title)
        .text(text)
        .position(Pos.BOTTOM_RIGHT)
        .owner(owner.get())
        .graphic(notificationIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE))
        .show();
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
   * Knopf „Verkaufen“ für eine Werkzeugleiste (nur Karrieremodus sichtbar, GRW S. 358): aktiv, solange {@code selection}
   * einen verkaufbaren Gegenstand liefert; nach dem Verkauf entfernt {@code removeAndRebuild} ihn und baut neu auf.
   *
   * @param selectionChanged Auswahl, bei deren Änderung der Knopf neu prüft
   * @param selection        der gewählte verkaufbare Gegenstand, sonst {@code null}
   */
  public <T> @NonNull Button sellButton(@NonNull Character c, @NonNull String tooltip,
      @NonNull ObservableValue<?> selectionChanged, @NonNull Supplier<@Nullable T> selection,
      @NonNull Function<T, String> name, @NonNull ToIntFunction<T> price, @NonNull Consumer<T> removeAndRebuild) {
    Button button = new Button(tooltip);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, FontAwesomeSolid.HAND_HOLDING_USD);
    button.setVisible(c.isCreated());
    button.setManaged(c.isCreated());
    button.disableProperty().bind(Bindings.createBooleanBinding(() -> selection.get() == null, selectionChanged));
    button.setOnAction(_ -> {
      T item = selection.get();
      if (item != null && sell(c, name.apply(item), price.applyAsInt(item))) {
        removeAndRebuild.accept(item);
      }
    });
    return button;
  }

  /**
   * Verkauf im Karrieremodus (GRW S. 358): fragt den Erlös in einem Zahlenfeld ab (nur ganze Nuyen ab 0), vorbelegt mit
   * {@value Equipment#SALE_PERCENT} % des Originalpreises, und bucht ihn als Verkauf ins Nuyen-Log. Den Gegenstand
   * entfernt der Aufrufer danach.
   *
   * @return true, wenn verkauft (gebucht) wurde
   */
  public boolean sell(@NonNull Character c, @NonNull String itemName, int originalPrice) {
    Spinner<Integer> proceeds = new Spinner<>(0, Integer.MAX_VALUE, Equipment.saleBasePrice(originalPrice), 10);
    proceeds.setEditable(true);
    TextField editor = proceeds.getEditor();
    editor.setTextFormatter(new TextFormatter<String>(change ->
        change.getControlNewText().matches("\\d{0,9}") ? change : null));
    Label hint = new Label("Grundpreis %d %% des Originalpreises; den Erlös bestimmt die Probe auf Verhandlung + "
        .formatted(Equipment.SALE_PERCENT) + "Charisma (GRW S. 358).");
    hint.setWrapText(true);
    Dialog<Integer> dialog = new Dialog<>();
    prepare(dialog);
    dialog.setTitle("Verkaufen");
    dialog.setHeaderText(itemName + " verkaufen (Originalpreis " + SpendCurrency.NUYEN.formatAmount(originalPrice)
        + ")");
    dialog.getDialogPane().setContent(new VBox(8, new Label("Erlös in ¥:"), proceeds, hint));
    dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    // Der Editor übernimmt Getipptes erst beim Bestätigen; deshalb den Text lesen, nicht den Wert des Spinners.
    dialog.setResultConverter(button -> button != ButtonType.OK ? null
        : editor.getText().isEmpty() ? 0 : Integer.parseInt(editor.getText()));
    Optional<Integer> answer = dialog.showAndWait();
    if (answer.isEmpty()) {
      return false;
    }
    bookNuyen(c, LogCategory.SALE, itemName + " verkauft", answer.get());
    return true;
  }

  /** Verlauf für „Rückgängig“ (nur diese Sitzung). */
  public @NonNull UndoHistory undoHistory() {
    return undoHistory;
  }

  /**
   * Bucht Karma mit heutigem Datum, siehe {@link #book(Character, SpendCurrency, LogEntry)}.
   *
   * @param amount Betrag mit Vorzeichen (Ausgabe negativ)
   */
  public void bookKarma(@NonNull Character c, @NonNull LogCategory category, @NonNull String label, int amount) {
    bookKarma(c, new LogEntry(label, amount, LocalDate.now(ZoneId.systemDefault()), category));
  }

  /** Bucht einen fertigen Karma-Eintrag (z.B. aus dem Dialog für manuelle Einträge). */
  public void bookKarma(@NonNull Character c, @NonNull LogEntry entry) {
    book(c, SpendCurrency.KARMA, entry);
  }

  /** Wie {@link #bookKarma(Character, LogCategory, String, int)} für Nuyen. */
  public void bookNuyen(@NonNull Character c, @NonNull LogCategory category, @NonNull String label, int amount) {
    bookNuyen(c, new LogEntry(label, amount, LocalDate.now(ZoneId.systemDefault()), category));
  }

  /** Bucht einen fertigen Nuyen-Eintrag. */
  public void bookNuyen(@NonNull Character c, @NonNull LogEntry entry) {
    book(c, SpendCurrency.NUYEN, entry);
  }

  /**
   * Einzige Stelle, an der Karma bzw. Nuyen gebucht wird: merkt den Stand vor der Buchung für Rückgängig, hängt den
   * Eintrag an und lässt Log und Guthaben nachziehen. Aufrufer buchen, BEVOR sie den Charakter ändern.
   */
  private void book(@NonNull Character c, @NonNull SpendCurrency currency, @NonNull LogEntry entry) {
    undoHistory.record(c, entry, currency);
    if (currency == SpendCurrency.KARMA) {
      c.addKarmaEntry(entry);
    } else {
      c.addNuyenEntry(entry);
    }
    onLogChanged.run();
  }

  /**
   * Bucht einen Karrieremodus-Kauf vom Karma-Guthaben ab (Guthabenprüfung, Bestätigung, Log-Eintrag).
   *
   * @return true, wenn NICHT gebucht wurde — der Aufrufer bricht dann den Kauf ab
   */
  public boolean karmaChargeFailed(@NonNull Character c, @NonNull LogCategory category, @NonNull String label,
      int karmaCost) {
    return karmaChargeFailed(c, category, label, karmaCost, null);
  }

  /**
   * Wie {@link #karmaChargeFailed(Character, LogCategory, String, int)} mit einem Zusatzhinweis (z.B. „löst die Gruppe
   * auf“), der vor dem Kostentext steht; ein Dialog statt zweier.
   */
  public boolean karmaChargeFailed(@NonNull Character c, @NonNull LogCategory category, @NonNull String label,
      int karmaCost, @Nullable String extraWarning) {
    return chargeFailed(c, SpendCurrency.KARMA, category, label, karmaCost, c.getKarmaTotal(), extraWarning);
  }

  /**
   * Gegenstück zu {@link #karmaChargeFailed} für Nuyen-Käufe im Karrieremodus (Cyberware, Rüstung; Muster des
   * C#-Originals: Guthabenprüfung, Log-Eintrag, sonst Abbruch mit Hinweis).
   *
   * @return true, wenn NICHT gebucht wurde — der Aufrufer bricht dann den Kauf ab
   */
  public boolean nuyenChargeFailed(@NonNull Character c, @NonNull String label, int nuyenCost) {
    return nuyenChargeFailed(c, LogCategory.PURCHASE, label, nuyenCost);
  }

  /** Wie {@link #nuyenChargeFailed(Character, String, int)}, mit eigener Art (z.B. Lebensstil). */
  public boolean nuyenChargeFailed(@NonNull Character c, @NonNull LogCategory category, @NonNull String label,
      int nuyenCost) {
    return chargeFailed(c, SpendCurrency.NUYEN, category, label, nuyenCost, c.getNuyenTotal(), null);
  }

  /**
   * Gemeinsamer Ablauf aller Karrieremodus-Käufe: bei zu wenig Guthaben Hinweis und Abbruch, sonst Bestätigung
   * einholen und einen negativen Log-Eintrag buchen. Im Spielleiter-Modus (ADR-060) kostenlos und ohne Buchung; nur
   * ein Zusatzhinweis wird noch bestätigt.
   *
   * @return true, wenn NICHT gekauft werden soll (zu wenig Guthaben oder abgebrochen)
   */
  private boolean chargeFailed(@NonNull Character c, @NonNull SpendCurrency currency, @NonNull LogCategory category,
      @NonNull String label, int cost, int available, @Nullable String extraWarning) {
    if (c.isGmMode()) {
      return extraWarning != null && !confirmYesNo("Spielleiter-Modus", label, extraWarning);
    }
    if (available < cost) {
      warn("Nicht genug " + currency.getDisplayName(), "Benötigt: " + currency.formatAmount(cost));
      return true;
    }
    if (!confirmSpend(currency, label, cost, available, extraWarning)) {
      return true;
    }
    book(c, currency, new LogEntry(label, -cost, LocalDate.now(ZoneId.systemDefault()), category));
    return false;
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
