package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.AppInfo;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.condition.StatusTest;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.utils.NumberStyle;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.utils.PersistenceUtils;
import de.shalorian.chummer.view.dialog.NewCharacterDialogController;
import de.shalorian.chummer.view.dialog.SettingsDialogController;
import de.shalorian.chummer.view.dialog.SettingsResult;
import de.shalorian.chummer.view.shell.FloatingButtonPosition;
import de.shalorian.chummer.view.tab.TabServices;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MainWindowController {

  @FXML
  private BorderPane borderPane;
  @FXML
  private MenuItem menuNew;
  @FXML
  private MenuItem menuOpen;
  @FXML
  private MenuItem menuSave;
  @FXML
  private MenuItem menuSaveAs;
  @FXML
  private MenuItem menuExit;
  @FXML
  private CheckMenuItem gmModeItem;

  /** Einstellung: Status als fester Bereich statt als Slide-Panel (Einstellungsdialog). */
  private static final String STATUS_STATIC_KEY = "character.statusStatic";
  private static final String GROUP_THOUSANDS_KEY = "display.groupThousands";
  private static final String DECIMAL_COMMA_KEY = "display.decimalComma";
  /** Vorsilbe der Schlüssel für die Proben im Status, dahinter der Name der {@link StatusTest}-Konstante. */
  private static final String STATUS_TEST_KEY_PREFIX = "display.statusTest.";
  private final @NonNull ViewPreferences preferences = ViewPreferences.user();
  /** Position des Status-Knopfs, gemeinsam für alle Charaktere und gemerkt zwischen den Starts. */
  private final @NonNull FloatingButtonPosition statusButtonPosition =
      new FloatingButtonPosition(preferences, "character.statusButtonPosition");
  private @Nullable CharacterViewController currentView;

  private @Nullable Stage stage;
  private @Nullable Character currentCharacter;
  private @Nullable File currentFile;

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  @FXML
  public void initialize() {
    menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    menuSaveAs.setAccelerator(
        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    menuExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    menuSave.setDisable(true);
    menuSaveAs.setDisable(true);
    Numbers.setStyle(displaySettings().numberStyle());
  }

  /** Anzeige-Einstellungen aus den Preferences; Standard ist das bisherige Verhalten. */
  private @NonNull SettingsResult displaySettings() {
    NumberStyle numberStyle = new NumberStyle(
        preferences.getBoolean(GROUP_THOUSANDS_KEY, NumberStyle.DEFAULT.groupThousands()),
        preferences.getBoolean(DECIMAL_COMMA_KEY, NumberStyle.DEFAULT.decimalComma()));
    Set<StatusTest> tests = EnumSet.noneOf(StatusTest.class);
    for (StatusTest test : StatusTest.values()) {
      if (preferences.getBoolean(STATUS_TEST_KEY_PREFIX + test.name(), false)) {
        tests.add(test);
      }
    }
    return new SettingsResult(numberStyle, preferences.getBoolean(STATUS_STATIC_KEY, false), tests);
  }

  private void storeDisplaySettings(@NonNull SettingsResult settings) {
    preferences.putBoolean(GROUP_THOUSANDS_KEY, settings.numberStyle().groupThousands());
    preferences.putBoolean(DECIMAL_COMMA_KEY, settings.numberStyle().decimalComma());
    preferences.putBoolean(STATUS_STATIC_KEY, settings.statusStatic());
    for (StatusTest test : StatusTest.values()) {
      preferences.putBoolean(STATUS_TEST_KEY_PREFIX + test.name(), settings.statusTests().contains(test));
    }
  }

  /**
   * Öffnet den Einstellungsdialog (ADR-061). Nach „Übernehmen“ werden die Anzeige-Werte gemerkt und angewendet, die
   * Regeln stehen schon im Charakter; danach wird der offene Charakter neu aufgebaut, weil Zahlenformat und Regeln
   * überall wirken.
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
      Character character = currentCharacter;
      controller.setup(dialogStage, displaySettings(), character == null ? null : character.getRuleOptions(),
          character == null ? "" : character.getName());
      dialogStage.showAndWait();

      SettingsResult result = controller.getResult();
      if (result == null) {
        return;
      }
      storeDisplaySettings(result);
      Numbers.setStyle(result.numberStyle());
      if (currentView != null) {
        currentView.setStatusTests(result.statusTests());
        if (currentView.isStatusStatic() != result.statusStatic()) {
          currentView.setStatusStatic(result.statusStatic());
        }
        currentView.rebuild();
      }
    } catch (IOException e) {
      showError("Fehler beim Öffnen der Einstellungen", e.getMessage());
    }
  }

  /**
   * Schaltet den Spielleiter-Modus des offenen Charakters (ADR-060): Einschalten nach Rückfrage, Ausschalten sofort.
   * Wird nicht gespeichert; beim Laden oder Neuanlegen ist er aus.
   */
  @FXML
  private void onGmMode() {
    if (currentView == null) {
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
    currentView.setGmMode(on);
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
      dialogStage.setTitle("Neuer Charakter");
      dialogStage.setResizable(false);
      Scene dialogScene = new Scene(dialogRoot);
      dialogScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
      dialogStage.setScene(dialogScene);

      dialogCtrl.setStage(dialogStage);
      dialogStage.showAndWait();

      if (!dialogCtrl.isConfirmed()) {
        return;
      }

      currentFile = null;
      showCharacter(new Character(
          dialogCtrl.getCharacterName(), dialogCtrl.getSelectedMetatype()));

    } catch (IOException e) {
      showError("Fehler beim Erstellen", e.getMessage());
    }
  }

  @FXML
  private void onOpen() {
    FileChooser chooser = buildChooser("Charakter öffnen");
    File file = chooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    try {
      currentFile = file;
      showCharacter(CharacterStore.load(file));
    } catch (IOException e) {
      showError("Fehler beim Laden", e.getMessage());
    }
  }

  @FXML
  private void onSave() {
    if (currentFile == null) {
      onSaveAs();
    } else {
      saveToFile(currentFile);
    }
  }

  @FXML
  private void onSaveAs() {
    FileChooser chooser = buildChooser("Charakter speichern");
    if (currentCharacter != null && currentFile == null) {
      chooser.setInitialFileName(
          currentCharacter.getName().replaceAll("\\s+", "_") + PersistenceUtils.JSON_EXT);
    }
    File file = chooser.showSaveDialog(stage);
    if (file == null) {
      return;
    }
    if (!file.getName().endsWith(PersistenceUtils.JSON_EXT)) {
      file = new File(file.getParentFile(), file.getName() + PersistenceUtils.JSON_EXT);
    }
    currentFile = file;
    saveToFile(file);
  }

  @FXML
  private void onExit() {
    Platform.exit();
  }

  // ── Helpers ────────────────────────────────────────────

  private void showCharacter(@NonNull Character character) throws IOException {
    FXMLLoader viewLoader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("CharacterView.fxml")));
    viewLoader.load();
    CharacterViewController charCtrl = viewLoader.getController();
    SettingsResult display = displaySettings();
    charCtrl.configureStatus(display.statusStatic(), statusButtonPosition);
    charCtrl.setStatusTests(display.statusTests());
    charCtrl.setCharacter(character);
    charCtrl.setOnCharacterReplaced(restored -> {
      currentCharacter = restored;
      updateTitle();
    });

    currentCharacter = character;
    currentView = charCtrl;
    borderPane.setCenter(charCtrl.node());
    menuSave.setDisable(false);
    menuSaveAs.setDisable(false);
    gmModeItem.setDisable(false);
    gmModeItem.setSelected(false);
    updateTitle();
  }

  private void saveToFile(@NonNull File file) {
    if (currentCharacter == null) return;
    try {
      CharacterStore.save(currentCharacter, file);
      updateTitle();
    } catch (IOException e) {
      showError("Fehler beim Speichern", e.getMessage());
    }
  }

  private void updateTitle() {
    if (stage == null) return;
    String name = currentCharacter != null ? currentCharacter.getName() : "";
    String file = currentFile != null ? "  [" + currentFile.getName() + "]" : "";
    stage.setTitle(AppInfo.title() + " – " + name + file);
  }

  private @NonNull FileChooser buildChooser(@NonNull String title) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(title);
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter(
            "Chummer JSON (*" + PersistenceUtils.JSON_EXT + ")",
            "*" + PersistenceUtils.JSON_EXT));
    if (currentFile != null) {
      chooser.setInitialDirectory(currentFile.getParentFile());
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
