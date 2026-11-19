package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.AppInfo;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.condition.CheatSheetItem;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.model.critter.CritterTemplate;
import de.shalorian.chummer.model.npc.NpcTemplate;
import de.shalorian.chummer.model.template.ArchetypeTemplate;
import de.shalorian.chummer.model.template.DocumentTemplate;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.persistence.DocumentKind;
import de.shalorian.chummer.persistence.NpcGroupStore;
import de.shalorian.chummer.utils.NumberStyle;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.utils.PersistenceUtils;
import de.shalorian.chummer.view.dialog.NewCharacterDialogController;
import de.shalorian.chummer.view.dialog.SettingsDialogController;
import de.shalorian.chummer.view.dialog.SettingsResult;
import de.shalorian.chummer.view.document.CharacterDocument;
import de.shalorian.chummer.view.document.DocumentState;
import de.shalorian.chummer.view.document.DocumentTabs;
import de.shalorian.chummer.view.document.NpcGroupDocument;
import de.shalorian.chummer.view.document.OpenDocument;
import de.shalorian.chummer.view.shell.FloatingButtonPosition;
import de.shalorian.chummer.view.tab.TabServices;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Hauptfenster: Menüs, Dialoge (Neu, Einstellungen, Dateiauswahl) und Speichern. Die offenen Dokumente liegen als Tabs in
 * {@link DocumentTabs} (ADR-066); Speichern, Spielleiter-Modus und Fenstertitel beziehen sich auf den aktiven Tab, die
 * Anzeige-Einstellungen auf alle.
 */
public class MainWindowController {

  @FXML
  private StackPane documentArea;
  @FXML
  private Node welcomePane;
  @FXML
  private MenuBar menuBar;
  @FXML
  private MenuItem menuNew;
  @FXML
  private MenuItem menuOpen;
  @FXML
  private MenuItem menuSave;
  @FXML
  private MenuItem menuSaveAs;
  @FXML
  private MenuItem menuClose;
  @FXML
  private MenuItem menuExit;
  @FXML
  private CheckMenuItem gmModeItem;

  /** Einstellung: Status als fester Bereich statt als Slide-Panel (Einstellungsdialog). */
  private static final String STATUS_STATIC_KEY = "character.statusStatic";
  private static final String GROUP_THOUSANDS_KEY = "display.groupThousands";
  private static final String DECIMAL_COMMA_KEY = "display.decimalComma";
  /**
   * Vorsilbe der Schlüssel für die Inhalte des Cheat-Sheets, dahinter der Name der {@link CheatSheetItem}-Konstante.
   * Der Wert stammt aus der Zeit der Proben im Status (ADR-061) und bleibt, damit gewählte Haken erhalten bleiben.
   */
  private static final String CHEAT_SHEET_KEY_PREFIX = "display.statusTest.";
  private final @NonNull ViewPreferences preferences = ViewPreferences.user();
  /** Position des Status-Knopfs, gemeinsam für alle Charaktere und gemerkt zwischen den Starts. */
  private final @NonNull FloatingButtonPosition statusButtonPosition =
      new FloatingButtonPosition(preferences, "character.statusButtonPosition");
  private final @NonNull DocumentTabs documents = new DocumentTabs(this::save, this::onDocumentsChanged);

  private @Nullable Stage stage;

  /**
   * Hängt das Fenster an; das Fenster-X fragt wie „Beenden“ nach ungespeicherten Dokumenten. Bekommt das Fenster den
   * Fokus zurück (nach einem modalen Dialog), wird der Stern des aktiven Dokuments neu geprüft (ADR-066).
   */
  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
    stage.setOnCloseRequest(event -> {
      if (!documents.closeAll()) {
        event.consume();
      }
    });
    stage.focusedProperty().addListener((_, _, focused) -> {
      OpenDocument active = documents.active();
      if (focused && active != null) {
        active.refreshLabel();
      }
    });
  }

  @FXML
  public void initialize() {
    menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    menuSaveAs.setAccelerator(
        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    menuClose.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
    menuExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    documentArea.getChildren().add(documents.node());
    Numbers.setStyle(displaySettings().numberStyle());
    BetaMenu.folder().ifPresent(folder -> menuBar.getMenus().add(
        BetaMenu.create(folder, this::openBetaTemplate, this::openBetaSample)));
    onDocumentsChanged();
  }

  /**
   * Lädt {@code file} je nach Dateiart als Charakter oder Schergengruppe in einen neuen Tab. Eine Beta-Kopie hat keine
   * Datei und gilt als nie gespeichert, damit Speichern nach einer neuen Datei fragt.
   */
  private void openFile(@NonNull File file, boolean betaCopy) throws IOException {
    if (DocumentKind.detect(file) == DocumentKind.NPC_GROUP) {
      NpcGroup group = NpcGroupStore.load(file);
      DocumentState state = betaCopy ? DocumentState.unsaved(true)
          : DocumentState.loaded(file, NpcGroupStore.serialize(group));
      documents.add(NpcGroupDocument.create(group, state));
    } else {
      Character character = CharacterStore.load(file);
      DocumentState state = betaCopy ? DocumentState.unsaved(true)
          : DocumentState.loaded(file, CharacterStore.serialize(character));
      documents.add(characterDocument(character, state));
    }
  }

  private void openBetaTemplate(@NonNull DocumentTemplate template) {
    try {
      openTemplate(template, template.name(), true);
    } catch (IOException e) {
      showError("Fehler beim Erstellen", e.getMessage());
    }
  }

  /** Öffnet einen Testfall ohne Dateipfad, damit Speichern nach einer neuen Datei fragt und ihn nie überschreibt. */
  private void openBetaSample(@NonNull Path file) {
    try {
      openFile(file.toFile(), true);
    } catch (IOException e) {
      showError("Fehler beim Laden", e.getMessage());
    }
  }

  /** Anzeige-Einstellungen aus den Preferences; Standard ist das bisherige Verhalten. */
  private @NonNull SettingsResult displaySettings() {
    NumberStyle numberStyle = new NumberStyle(
        preferences.getBoolean(GROUP_THOUSANDS_KEY, NumberStyle.DEFAULT.groupThousands()),
        preferences.getBoolean(DECIMAL_COMMA_KEY, NumberStyle.DEFAULT.decimalComma()));
    Set<CheatSheetItem> items = EnumSet.noneOf(CheatSheetItem.class);
    for (CheatSheetItem item : CheatSheetItem.values()) {
      if (preferences.getBoolean(CHEAT_SHEET_KEY_PREFIX + item.name(), false)) {
        items.add(item);
      }
    }
    return new SettingsResult(numberStyle, preferences.getBoolean(STATUS_STATIC_KEY, false), items);
  }

  private void storeDisplaySettings(@NonNull SettingsResult settings) {
    preferences.putBoolean(GROUP_THOUSANDS_KEY, settings.numberStyle().groupThousands());
    preferences.putBoolean(DECIMAL_COMMA_KEY, settings.numberStyle().decimalComma());
    preferences.putBoolean(STATUS_STATIC_KEY, settings.statusStatic());
    for (CheatSheetItem item : CheatSheetItem.values()) {
      preferences.putBoolean(CHEAT_SHEET_KEY_PREFIX + item.name(), settings.cheatSheetItems().contains(item));
    }
  }

  /**
   * Öffnet den Einstellungsdialog (ADR-061). Die Regeln gelten für den aktiven Charakter. Nach „Übernehmen“ werden die
   * Anzeige-Werte gemerkt und auf alle Tabs angewendet; danach werden alle Dokumente neu aufgebaut, weil Zahlenformat
   * und Regeln überall wirken.
   */
  @FXML
  private void onSettings() {
    try {
      FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("SettingsDialog.fxml")));
      Parent root = loader.load();
      SettingsDialogController controller = loader.getController();
      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(stage);
      dialogStage.setTitle("Einstellungen");
      dialogStage.setResizable(false);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
      dialogStage.setScene(scene);
      Character character = documents.active() instanceof CharacterDocument doc ? doc.character() : null;
      controller.setup(dialogStage, displaySettings(), character == null ? null : character.getRuleOptions(),
          character == null ? "" : character.getName());
      dialogStage.showAndWait();

      SettingsResult result = controller.getResult();
      if (result == null) {
        return;
      }
      storeDisplaySettings(result);
      Numbers.setStyle(result.numberStyle());
      for (OpenDocument document : documents.all()) {
        if (document instanceof CharacterDocument characterDocument) {
          characterDocument.applyDisplay(result);
        }
        document.rebuild();
      }
      OpenDocument active = documents.active();
      if (active != null) {
        active.refreshLabel();
      }
    } catch (IOException e) {
      showError("Fehler beim Öffnen der Einstellungen", e.getMessage());
    }
  }

  /**
   * Schaltet den Spielleiter-Modus des aktiven Charakters (ADR-060): Einschalten nach Rückfrage, Ausschalten sofort.
   * Wird nicht gespeichert; beim Laden oder Neuanlegen ist er aus.
   */
  @FXML
  private void onGmMode() {
    if (!(documents.active() instanceof CharacterDocument active)) {
      gmModeItem.setSelected(false);
      return;
    }
    boolean on = gmModeItem.isSelected();
    if (on) {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
          "Steigern, Senken, Kaufen und Entfernen kosten nichts und werden nicht gebucht. Regelsperren entfallen "
              + "(Erschaffungsgrenzen, nur steigern im Karrieremodus, Voraussetzungen, Verfügbarkeit); die Obergrenzen "
              + "aus dem Grundregelwerk bleiben. Der Modus wird nicht gespeichert.",
          ButtonType.YES, ButtonType.NO);
      TabServices.applyDialogStyle(confirm);
      confirm.setTitle("Spielleiter-Modus");
      confirm.setHeaderText("Spielleiter-Modus einschalten?");
      if (confirm.showAndWait().filter(button -> button == ButtonType.YES).isEmpty()) {
        gmModeItem.setSelected(false);
        return;
      }
    }
    active.setGmMode(on);
  }

  @FXML
  private void onNew() {
    try {
      FXMLLoader dialogLoader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("NewCharacterDialog.fxml")));
      Parent dialogRoot = dialogLoader.load();
      NewCharacterDialogController dialogCtrl = dialogLoader.getController();

      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(stage);
      dialogStage.setTitle("Neu");
      dialogStage.setResizable(false);
      Scene dialogScene = new Scene(dialogRoot);
      dialogScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
      dialogStage.setScene(dialogScene);

      dialogCtrl.setStage(dialogStage);
      dialogStage.showAndWait();

      if (!dialogCtrl.isConfirmed()) {
        return;
      }

      DocumentTemplate template = dialogCtrl.getSelectedTemplate();
      if (template != null) {
        openTemplate(template, dialogCtrl.getCharacterName(), false);
      } else if (dialogCtrl.isNpcGroup()) {
        documents.add(NpcGroupDocument.create(newGroup(dialogCtrl.getCharacterName()), DocumentState.unsaved(false)));
      } else {
        documents.add(characterDocument(new Character(dialogCtrl.getCharacterName(), dialogCtrl.getSelectedMetatype()),
            DocumentState.unsaved(false)));
      }

    } catch (IOException e) {
      showError("Fehler beim Erstellen", e.getMessage());
    }
  }

  /** Öffnet eine Datei in einem neuen Tab; ist sie schon offen, wird nur ihr Tab aktiviert. */
  @FXML
  private void onOpen() {
    FileChooser chooser = buildChooser("Öffnen");
    File file = chooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    OpenDocument open = documents.findByFile(file);
    if (open != null) {
      documents.select(open);
      return;
    }
    try {
      openFile(file, false);
    } catch (IOException e) {
      showError("Fehler beim Laden", e.getMessage());
    }
  }

  @FXML
  private void onSave() {
    OpenDocument active = documents.active();
    if (active != null) {
      save(active);
    }
  }

  @FXML
  private void onSaveAs() {
    OpenDocument active = documents.active();
    if (active != null) {
      saveAs(active);
    }
  }

  @FXML
  private void onClose() {
    OpenDocument active = documents.active();
    if (active != null) {
      documents.close(active);
    }
  }

  /** Beendet nach Rückfrage je ungespeichertem Dokument; Abbrechen stoppt das Beenden. */
  @FXML
  private void onExit() {
    if (documents.closeAll()) {
      Platform.exit();
    }
  }

  // ── Helpers ────────────────────────────────────────────

  /** Speichert in die Datei des Dokuments bzw. fragt nach einer; {@code false}, wenn abgebrochen oder gescheitert. */
  private boolean save(@NonNull OpenDocument document) {
    File file = document.state().getFile();
    return file == null ? saveAs(document) : saveToFile(document, file);
  }

  /**
   * Fragt nach einer Datei und speichert dorthin. Eine Datei, die in einem anderen Tab offen ist, wird nicht
   * überschrieben, damit nicht zwei Fassungen in dieselbe Datei schreiben.
   */
  private boolean saveAs(@NonNull OpenDocument document) {
    FileChooser chooser = buildChooser("Speichern");
    if (document.state().getFile() == null) {
      chooser.setInitialFileName(document.name().replaceAll("\\s+", "_") + PersistenceUtils.JSON_EXT);
    }
    File file = chooser.showSaveDialog(stage);
    if (file == null) {
      return false;
    }
    if (!file.getName().endsWith(PersistenceUtils.JSON_EXT)) {
      file = new File(file.getParentFile(), file.getName() + PersistenceUtils.JSON_EXT);
    }
    OpenDocument other = documents.findByFile(file);
    if (other != null && !other.equals(document)) {
      showError("Nicht gespeichert", "„%s“ ist in einem anderen Tab geöffnet (%s). Bitte dort speichern oder den Tab "
          .formatted(file.getName(), other.name()) + "erst schließen.");
      return false;
    }
    return saveToFile(document, file);
  }

  private boolean saveToFile(@NonNull OpenDocument document, @NonNull File file) {
    try {
      document.saveTo(file);
      return true;
    } catch (IOException e) {
      showError("Fehler beim Speichern", e.getMessage());
      return false;
    }
  }

  private @NonNull CharacterDocument characterDocument(@NonNull Character character, @NonNull DocumentState state)
      throws IOException {
    return CharacterDocument.create(character, state, displaySettings(), statusButtonPosition);
  }

  /** Leere Gruppe (Stufe 0, 4 Mitglieder, Mensch mit Mindestwerten) oder eine Gruppe aus der GRW-Vorlage. */
  private static @NonNull NpcGroup newGroup(@NonNull String name) {
    return new NpcGroup(name, 0, 4, new NpcStatblock(new Character("Scherge", MetatypeDataLoader.find("Mensch")), ""));
  }

  /** Öffnet ein neues, ungespeichertes Dokument aus einer Vorlage unter dem Namen {@code name} in einem neuen Tab. */
  private void openTemplate(@NonNull DocumentTemplate template, @NonNull String name, boolean betaCopy)
      throws IOException {
    DocumentState state = DocumentState.unsaved(betaCopy);
    switch (template) {
      case NpcTemplate npc -> {
        NpcGroup group = npc.create();
        group.setName(name);
        documents.add(NpcGroupDocument.create(group, state));
      }
      case ArchetypeTemplate archetype -> {
        Character character = archetype.create();
        character.setName(name);
        documents.add(characterDocument(character, state));
      }
      case CritterTemplate critter -> {
        NpcGroup group = critter.create();
        group.setName(name);
        documents.add(NpcGroupDocument.create(group, state));
      }
      default -> throw new IllegalStateException("Unbekannte Vorlage: " + template.kind());
    }
  }

  /** Nach Tabwechsel, Öffnen, Schließen oder geändertem Tab-Text: Willkommensbereich, Menüs und Fenstertitel. */
  private void onDocumentsChanged() {
    boolean empty = documents.isEmpty();
    welcomePane.setVisible(empty);
    documents.node().setVisible(!empty);
    menuSave.setDisable(empty);
    menuSaveAs.setDisable(empty);
    menuClose.setDisable(empty);
    OpenDocument active = documents.active();
    if (active instanceof CharacterDocument characterDocument) {
      gmModeItem.setDisable(false);
      gmModeItem.setSelected(characterDocument.character().isGmMode());
    } else {
      gmModeItem.setDisable(true);
      gmModeItem.setSelected(false);
    }
    updateTitle(active);
  }

  private void updateTitle(@Nullable OpenDocument active) {
    if (stage == null) {
      return;
    }
    if (active == null) {
      stage.setTitle(AppInfo.title());
      return;
    }
    File file = active.state().getFile();
    String suffix = file != null ? "  [" + file.getName() + "]" : active.state().isBetaCopy() ? " (Beta)" : "";
    stage.setTitle(AppInfo.title() + " – " + active.name() + (active.isMarkedDirty() ? " *" : "") + suffix);
  }

  private @NonNull FileChooser buildChooser(@NonNull String title) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(title);
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter(
            "Chummer JSON (*" + PersistenceUtils.JSON_EXT + ")",
            "*" + PersistenceUtils.JSON_EXT));
    OpenDocument active = documents.active();
    File current = active == null ? null : active.state().getFile();
    if (current != null) {
      chooser.setInitialDirectory(current.getParentFile());
    }
    return chooser;
  }

  private void showError(@NonNull String header, @Nullable String detail) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    TabServices.applyDialogStyle(alert);
    alert.setTitle("Fehler");
    alert.setHeaderText(header);
    alert.setContentText(detail);
    alert.showAndWait();
  }
}
