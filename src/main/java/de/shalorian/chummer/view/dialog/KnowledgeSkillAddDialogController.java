package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.KnowledgeSuggestionLoader;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class KnowledgeSkillAddDialogController {

  @FXML
  private ComboBox<String> nameBox;
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

    nameBox.getEditor().textProperty().addListener((_, _, _) -> clearError());
    // Vorschläge je Kategorie (GRW S. 110, 155, 156; ADR-032), Freitext bleibt möglich.
    categoryBox.valueProperty().addListener((_, _, category) -> fillSuggestions(category));
    fillSuggestions(categoryBox.getValue());
  }

  private void fillSuggestions(@Nullable KnowledgeSkillCategory category) {
    String typed = nameBox.getEditor().getText();
    nameBox.getItems().setAll(category == null ? List.of() : KnowledgeSuggestionLoader.names(category));
    nameBox.getEditor().setText(typed);
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
    String name = nameBox.getEditor().getText() == null ? "" : nameBox.getEditor().getText().trim();
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
