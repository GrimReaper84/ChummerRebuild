package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.ExoticSkills;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Dialog „Exotische Fertigkeit hinzufügen“: Die Grundfertigkeit steht fest, das Ziel (Waffe bzw. Fahrzeug) kommt aus
 * den Vorschlägen oder als Freitext (GRW S. 148, 155). Die Prüfung macht {@link Character#getExoticSkillProblem}.
 */
public class ExoticSkillAddDialogController {

  @FXML
  private Label baseLabel;
  @FXML
  private Label targetCaption;
  @FXML
  private ComboBox<String> targetBox;
  @FXML
  private Label hintLabel;
  @FXML
  private Label errorLabel;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @NonNull String base = "";
  private @Nullable ExoticSkillAddResult result;

  @FXML
  public void initialize() {
    targetBox.getEditor().textProperty().addListener((_, _, _) -> errorLabel.setText(""));
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  /** Charakter und Grundfertigkeit; füllt Vorschläge und Hinweis. */
  public void setup(@NonNull Character character, @NonNull String base) {
    this.character = character;
    this.base = base;
    baseLabel.setText(base);
    boolean vehicle = ExoticSkills.VEHICLE.equals(base);
    targetCaption.setText(vehicle ? "Fahrzeug:" : "Waffe:");
    targetBox.getItems().setAll(ExoticSkills.targetSuggestions(base));
    hintLabel.setText(vehicle
        ? "Je exotischem Fahrzeug eine eigene Fertigkeit, z.B. Jetpack oder Unterwasserschlitten (GRW S. 155)."
        : "Je Waffe eine eigene Fertigkeit, ohne Spezialisierung (GRW S. 148). Andere Waffen als Freitext.");
  }

  @FXML
  private void onOk() {
    String target = targetBox.getEditor().getText() == null ? "" : targetBox.getEditor().getText().trim();
    if (character == null) {
      return;
    }
    String problem = character.getExoticSkillProblem(base, target);
    if (problem != null) {
      errorLabel.setText(problem);
      return;
    }
    result = new ExoticSkillAddResult(base, target);
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

  public @NonNull Optional<ExoticSkillAddResult> getResult() {
    return Optional.ofNullable(result);
  }
}
