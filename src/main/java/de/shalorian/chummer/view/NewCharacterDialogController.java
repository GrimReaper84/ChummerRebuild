package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;

public class NewCharacterDialogController {

  @FXML
  private TextField nameField;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private Button createButton;

  private @Nullable Stage stage;
  private boolean confirmed = false;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  public void initialize() {
    metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    metatypeBox.setConverter(new MetatypeStringConverter());
    metatypeBox.setValue(MetatypeDataLoader.find("Mensch"));
    createButton.disableProperty().bind(
        Bindings.createBooleanBinding(
            () -> nameField.getText().trim().isEmpty(),
            nameField.textProperty()
        )
    );
  }

  @FXML
  private void onCreate() {
    confirmed = true;
    stage.close();
  }

  @FXML
  private void onCancel() {
    stage.close();
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getCharacterName() {
    return nameField.getText().trim();
  }

  public MetatypeData getSelectedMetatype() {
    return metatypeBox.getValue();
  }
}