package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.LogEntry;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LogEntryDialogController {

  @FXML
  private DatePicker datePicker;
  @FXML
  private ComboBox<String> typeBox;
  @FXML
  private TextField amountField;
  @FXML
  private TextField descriptionField;
  @FXML
  private Button addButton;

  private @Nullable Stage stage;
  private @Nullable LogEntry result;

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  @FXML
  public void initialize() {
    datePicker.setValue(LocalDate.now(ZoneId.systemDefault()));
    typeBox.getItems().addAll("Einnahme +", "Ausgabe −");
    typeBox.setValue("Einnahme +");

    addButton.disableProperty().bind(
        Bindings.createBooleanBinding(
            () -> {
              String a = amountField.getText().trim();
              return a.isEmpty()
                  || !a.matches("[1-9][0-9]*")
                  || descriptionField.getText().trim().isEmpty()
                  || datePicker.getValue() == null;
            },
            amountField.textProperty(),
            descriptionField.textProperty(),
            datePicker.valueProperty()
        )
    );
  }

  @FXML
  private void onAdd() {
    LocalDate date = datePicker.getValue();
    if (date == null) {
      return;
    }
    int amount = Integer.parseInt(amountField.getText().trim());
    if (typeBox.getValue().startsWith("Ausgabe")) {
      amount = -amount;
    }
    result = new LogEntry(descriptionField.getText().trim(), amount, date);
    if (stage != null) {
      stage.close();
    }
  }

  @FXML
  private void onCancel() {
    if (stage != null) {
      stage.close();
    }
  }

  public @NonNull Optional<LogEntry> getResult() {
    return Optional.ofNullable(result);
  }
}
