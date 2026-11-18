package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.AppInfo;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.condition.StatusTest;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
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
import de.shalorian.chummer.view.npc.NpcGroupViewController;
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
  /** Offene Schergengruppe (ADR-062); schließt einen offenen Charakter aus und umgekehrt. */
  private @Nullable NpcGroup currentGroup;
  /** Ob das offene Dokument eine Kopie aus dem Beta-Menü ist; der Titel zeigt „(Beta)“ bis zum ersten Speichern. */
  private boolean betaCopy;
  private @Nullable NpcGroupViewController currentGroupView;

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
    BetaMenu.folder().ifPresent(folder -> menuBar.getMenus().add(
        BetaMenu.create(folder, this::openBetaTemplate, this::openBetaSample)));
  }

  /** Lädt eine Datei je nach Dateiart als Charakter oder Schergengruppe und zeigt sie. */
  private void showDocument(@NonNull File file) throws IOException {
    if (DocumentKind.detect(file) == DocumentKind.NPC_GROUP) {
      showGroup(NpcGroupStore.load(file));
    } else {
      showCharacter(CharacterStore.load(file));
    }
  }

  private void openBetaTemplate(@NonNull DocumentTemplate template) {
    try {
      currentFile = null;
      betaCopy = true;
      openTemplate(template, template.name());
    } catch (IOException e) {
      showError("Fehler beim Erstellen", e.getMessage());
    }
  }

  /** Öffnet einen Testfall ohne Dateipfad, damit Speichern nach einer neuen Datei fragt und ihn nie überschreibt. */
  private void openBetaSample(@NonNull Path file) {
    try {
      currentFile = null;
      betaCopy = true;
      showDocument(file.toFile());
    } catch (IOException e) {
      showError("Fehler beim Laden", e.getMessage());
    }
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
      if (currentGroupView != null) {
        currentGroupView.rebuild();
      }
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

      currentFile = null;
      betaCopy = false;
      DocumentTemplate template = dialogCtrl.getSelectedTemplate();
      if (template != null) {
        openTemplate(template, dialogCtrl.getCharacterName());
      } else if (dialogCtrl.isNpcGroup()) {
        showGroup(newGroup(dialogCtrl.getCharacterName()));
      } else {
        showCharacter(new Character(dialogCtrl.getCharacterName(), dialogCtrl.getSelectedMetatype()));
      }

    } catch (IOException e) {
      showError("Fehler beim Erstellen", e.getMessage());
    }
  }

  @FXML
  private void onOpen() {
    FileChooser chooser = buildChooser("Öffnen");
    File file = chooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    try {
      currentFile = file;
      betaCopy = false;
      showDocument(file);
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
    FileChooser chooser = buildChooser("Speichern");
    String documentName = currentGroup != null ? currentGroup.getName()
        : currentCharacter != null ? currentCharacter.getName() : null;
    if (documentName != null && currentFile == null) {
      chooser.setInitialFileName(documentName.replaceAll("\\s+", "_") + PersistenceUtils.JSON_EXT);
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
    currentGroup = null;
    currentGroupView = null;
    borderPane.setCenter(charCtrl.node());
    menuSave.setDisable(false);
    menuSaveAs.setDisable(false);
    gmModeItem.setDisable(false);
    gmModeItem.setSelected(false);
    updateTitle();
  }

  /** Leere Gruppe (Stufe 0, 4 Mitglieder, Mensch mit Mindestwerten) oder eine Gruppe aus der GRW-Vorlage. */
  private static @NonNull NpcGroup newGroup(@NonNull String name) {
    return new NpcGroup(name, 0, 4, new NpcStatblock(new Character("Scherge", MetatypeDataLoader.find("Mensch")), ""));
  }

  /** Öffnet ein neues, ungespeichertes Dokument aus einer Vorlage unter dem Namen {@code name}. */
  private void openTemplate(@NonNull DocumentTemplate template, @NonNull String name) throws IOException {
    switch (template) {
      case NpcTemplate npc -> {
        NpcGroup group = npc.create();
        group.setName(name);
        showGroup(group);
      }
      case ArchetypeTemplate archetype -> {
        Character character = archetype.create();
        character.setName(name);
        showCharacter(character);
      }
      default -> throw new IllegalStateException("Unbekannte Vorlage: " + template.kind());
    }
  }

  private void showGroup(@NonNull NpcGroup group) throws IOException {
    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("NpcGroupView.fxml")));
    loader.load();
    NpcGroupViewController controller = loader.getController();
    TabServices services = new TabServices(() -> borderPane.getScene().getWindow(), () -> { },
        () -> group.getGrunt().getCharacter().getRuleOptions());
    controller.setGroup(group, services, this::updateTitle);
    currentGroup = group;
    currentGroupView = controller;
    currentCharacter = null;
    currentView = null;
    borderPane.setCenter(controller.node());
    menuSave.setDisable(false);
    menuSaveAs.setDisable(false);
    gmModeItem.setDisable(true);
    gmModeItem.setSelected(false);
    updateTitle();
  }

  private void saveToFile(@NonNull File file) {
    try {
      if (currentGroup != null) {
        NpcGroupStore.save(currentGroup, file);
      } else if (currentCharacter != null) {
        CharacterStore.save(currentCharacter, file);
      } else {
        return;
      }
      updateTitle();
    } catch (IOException e) {
      showError("Fehler beim Speichern", e.getMessage());
    }
  }

  private void updateTitle() {
    if (stage == null) return;
    String name = currentGroup != null ? currentGroup.getName()
        : currentCharacter != null ? currentCharacter.getName() : "";
    String file = currentFile != null ? "  [" + currentFile.getName() + "]" : betaCopy ? " (Beta)" : "";
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
