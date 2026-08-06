package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.utils.PersistenceUtils;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    Parent charView = viewLoader.load();
    CharacterViewController charCtrl = viewLoader.getController();
    charCtrl.setCharacter(character);

    currentCharacter = character;
    borderPane.setCenter(charView);
    menuSave.setDisable(false);
    menuSaveAs.setDisable(false);
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
    stage.setTitle("Chummer – " + name + file);
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
    alert.getDialogPane().getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
    alert.setTitle("Fehler");
    alert.setHeaderText(header);
    alert.setContentText(detail);
    alert.showAndWait();
  }
}
