package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.initiation.Initiation;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.initiation.TechniqueDataLoader;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl einer Metamagie (Initiation, GRW S. 232/233) bzw. eines Echos (Wandlung, S. 284). Es erscheinen nur
 * Techniken der passenden Art; gesperrte ({@link Initiation#getLearnProblem}) bleiben sichtbar und deaktiviert, der
 * Grund steht im Hinweisfeld. Das Lernen kostet kein Karma.
 */
public class TechniqueSelectDialogController extends CatalogSelectDialogController<TechniqueData> {

  @FXML
  private Label effectLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Label problemLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Initiation initiation;
  private @Nullable TechniqueData result;

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.initiation = character.getInitiation();
    refreshCategoryOptions();
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return List.of();
  }

  @Override
  protected @NonNull List<TechniqueData> allEntries() {
    return List.copyOf(TechniqueDataLoader.getAllTechniques().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull TechniqueData data, @NonNull String category) {
    return true;
  }

  @Override
  protected boolean additionalFilter(@NonNull TechniqueData data) {
    return initiation != null && data.kind() == initiation.getKind();
  }

  @Override
  protected boolean isSelectable(@NonNull TechniqueData data) {
    return initiation != null && initiation.getLearnProblem(data) == null;
  }

  @Override
  protected void onListUpdated() {
    TechniqueData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || initiation == null) {
      effectLabel.setText("");
      sourceLabel.setText("");
      problemLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    effectLabel.setText(selected.effectText(Math.max(1, initiation.getGrade()), initiation.count(selected) + 1));
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    String problem = initiation.getLearnProblem(selected);
    problemLabel.setText(problem == null ? "" : "Nicht wählbar: " + problem + ".");
    okButton.setDisable(problem != null);
  }

  @FXML
  private void onOk() {
    TechniqueData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || initiation == null || initiation.getLearnProblem(selected) != null) {
      return;
    }
    result = selected;
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

  public @NonNull Optional<TechniqueData> getResult() {
    return Optional.ofNullable(result);
  }
}
