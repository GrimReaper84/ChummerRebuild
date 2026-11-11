package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Contact;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Controller für den Dialog, der sowohl zum Anlegen als auch — vorbefüllt über {@link
 * #prefill(Contact)} — zum Bearbeiten von Name und Kurzbeschreibung eines {@link Contact}
 * verwendet wird. Connection/Loyalty werden hier nicht abgefragt (siehe
 * {@code docs/kontakte_plan.md} Abschnitt 13: neue Kontakte starten mit Connection=Loyalty=1 und
 * werden danach inline über +/− angepasst).
 */
public class ContactEditDialogController {

  @FXML
  private TextField nameField;
  @FXML
  private TextArea descriptionArea;
  @FXML
  private Label errorLabel;

  private @Nullable Stage stage;
  private @Nullable ContactEditResult result;

  @FXML
  public void initialize() {
    nameField.textProperty().addListener((_, _, _) -> clearError());
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  /**
   * Befüllt die Felder mit den aktuellen Werten des übergebenen Kontakts, für den Bearbeiten-Fall.
   */
  public void prefill(@NonNull Contact contact) {
    nameField.setText(contact.getName());
    descriptionArea.setText(contact.getDescription());
  }

  private void clearError() {
    errorLabel.setText("");
  }

  @FXML
  private void onOk() {
    String name = nameField.getText() == null ? "" : nameField.getText().trim();
    if (name.isEmpty()) {
      errorLabel.setText("Name darf nicht leer sein");
      return;
    }
    String description = descriptionArea.getText();
    result = new ContactEditResult(name, description);
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

  public @NonNull Optional<ContactEditResult> getResult() {
    return Optional.ofNullable(result);
  }
}
