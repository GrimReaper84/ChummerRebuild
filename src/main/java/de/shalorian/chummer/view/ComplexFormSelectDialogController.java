package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.ComplexForm;
import de.shalorian.chummer.model.character.ComplexFormData;
import de.shalorian.chummer.model.character.ComplexFormDataLoader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ComplexFormSelectDialogController extends CatalogSelectDialogController<ComplexFormData> {

  private static final List<String> CATEGORIES = List.of("Standard", "Hacking");

  @FXML
  private Label costLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable ComplexFormData result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    okButton.setDisable(true);
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    costLabel.setText(character.isCreated()
        ? ComplexForm.KARMA_NEW + " Karma"
        : ComplexForm.BP_PER_RATING + " BP");
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return CATEGORIES;
  }

  @Override
  protected @NonNull List<ComplexFormData> allEntries() {
    return List.copyOf(ComplexFormDataLoader.getAllComplexForms().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull ComplexFormData complexFormData,
      @NonNull String category) {
    return complexFormData.category().equals(category);
  }

  @Override
  protected boolean additionalFilter(@NonNull ComplexFormData complexFormData) {
    return !alreadyKnownNames().contains(complexFormData.name());
  }

  private @NonNull Set<String> alreadyKnownNames() {
    Character c = this.character;
    if (c == null) {
      return Set.of();
    }
    return c.getComplexForms().stream().map(ComplexForm::getName).collect(Collectors.toSet());
  }

  @Override
  protected void onListUpdated() {
    okButton.setDisable(itemListView.getSelectionModel().getSelectedItem() == null);
  }

  @FXML
  private void onOk() {
    ComplexFormData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
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

  public @NonNull Optional<ComplexFormData> getResult() {
    return Optional.ofNullable(result);
  }
}
