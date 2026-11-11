package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class KnowledgeSkillAddDialogController {

  @FXML
  private TextField nameField;
  @FXML
  private ComboBox<KnowledgeSkillCategory> categoryBox;
  @FXML
  private Label errorLabel;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable KnowledgeSkillAddResult result;

  @FXML
  public void initialize() {
    categoryBox.getItems().addAll(KnowledgeSkillCategory.values());
    categoryBox.setValue(KnowledgeSkillCategory.STREET);
    categoryBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(@Nullable KnowledgeSkillCategory category) {
        return category == null ? "" : category.getDisplayName();
      }

      @Override
      public @Nullable KnowledgeSkillCategory fromString(String s) {
        return null;
      }
    });

    nameField.textProperty().addListener((_, _, _) -> clearError());
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
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
    Character ch = character;
    boolean duplicate = ch != null && ch.getKnowledgeSkills().stream()
        .anyMatch(k -> k.getName().equals(name));
    if (duplicate) {
      errorLabel.setText("Wissensfertigkeit \"" + name + "\" existiert bereits");
      return;
    }
    KnowledgeSkillCategory category = categoryBox.getValue();
    if (category == null) {
      errorLabel.setText("Bitte eine Kategorie wählen");
      return;
    }
    result = new KnowledgeSkillAddResult(name, category);
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

  public @NonNull Optional<KnowledgeSkillAddResult> getResult() {
    return Optional.ofNullable(result);
  }
}
