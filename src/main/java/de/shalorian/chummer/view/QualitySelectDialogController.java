package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.QualityData;
import de.shalorian.chummer.model.character.QualityDataLoader;
import de.shalorian.chummer.model.character.QualitySource;
import de.shalorian.chummer.model.character.QualityType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class QualitySelectDialogController extends CatalogSelectDialogController<QualityData> {

  private static final String CATEGORY_POSITIVE = "Gaben";
  private static final String CATEGORY_NEGATIVE = "Handicaps";

  @FXML
  private Label costLabel;
  @FXML
  private CheckBox onlySelectableBox;
  @FXML
  private CheckBox gratisBox;
  @FXML
  private Button okButton;
  @FXML
  private Button addAgainButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable QualitySelectResult result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    onlySelectableBox.selectedProperty().addListener((obs, old, val) -> updateList());
    gratisBox.selectedProperty().addListener((obs, old, val) -> onListUpdated());
    okButton.setDisable(true);
    addAgainButton.setDisable(true);
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  /**
   * Setzt den vorausgewählten Kategoriefilter beim Öffnen des Dialogs aus einer Sektion heraus.
   */
  public void setFilter(@NonNull QualityType filter) {
    categoryBox.setValue(filter == QualityType.POSITIVE ? CATEGORY_POSITIVE : CATEGORY_NEGATIVE);
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return List.of(CATEGORY_POSITIVE, CATEGORY_NEGATIVE);
  }

  @Override
  protected @NonNull List<QualityData> allEntries() {
    return List.copyOf(QualityDataLoader.getAllQualities().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull QualityData q, @NonNull String category) {
    return CATEGORY_POSITIVE.equals(category)
        ? q.type() == QualityType.POSITIVE
        : q.type() == QualityType.NEGATIVE;
  }

  @Override
  protected boolean additionalFilter(@NonNull QualityData q) {
    if (!onlySelectableBox.isSelected()) {
      return true;
    }
    return isSelectable(q, alreadySelectedNames());
  }

  private @NonNull Set<String> alreadySelectedNames() {
    Character c = this.character;
    if (c == null) {
      return Set.of();
    }
    return c.getQualities().stream()
        .filter(q -> q.source() == QualitySource.SELECTED)
        .map(q -> q.data().name())
        .collect(Collectors.toSet());
  }

  private boolean isSelectable(@NonNull QualityData q, @NonNull Set<String> alreadySelected) {
    if (alreadySelected.contains(q.name())) {
      return false;
    }
    if (character == null || character.isCreated()) {
      return true;
    }
    if (q.type() == QualityType.POSITIVE && q.contributeToLimit()) {
      return character.getPositiveQualityBP() + q.bp() <= Character.MAX_POSITIVE_QUALITY_BP;
    }
    if (q.type() == QualityType.NEGATIVE && q.contributeToLimit()) {
      return character.getNegativeQualityBP() + Math.abs(q.bp())
          <= Character.MAX_NEGATIVE_QUALITY_BP;
    }
    return true;
  }

  @Override
  protected void onListUpdated() {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      costLabel.setText("–");
      okButton.setDisable(true);
      addAgainButton.setDisable(true);
      return;
    }
    if (gratisBox.isSelected()) {
      costLabel.setText("Gratis");
    } else {
      int bp = Math.abs(selected.bp());
      costLabel.setText(bp + " BP");
    }
    okButton.setDisable(false);
    addAgainButton.setDisable(false);
  }

  @FXML
  private void onOk() {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    result = new QualitySelectResult(selected, gratisBox.isSelected(), false);
    if (stage != null) {
      stage.close();
    }
  }

  @FXML
  private void onOkAndAdd() {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    result = new QualitySelectResult(selected, gratisBox.isSelected(), true);
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

  public @NonNull Optional<QualitySelectResult> getResult() {
    return Optional.ofNullable(result);
  }
}
