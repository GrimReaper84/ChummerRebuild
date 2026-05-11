package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterQuality;
import de.shalorian.chummer.model.character.QualityData;
import de.shalorian.chummer.model.character.QualityDataLoader;
import de.shalorian.chummer.model.character.QualitySource;
import de.shalorian.chummer.model.character.QualityType;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class QualitySelectDialogController {

  private static final String CATEGORY_ALL = "Alle";
  private static final String CATEGORY_POSITIVE = "Gaben";
  private static final String CATEGORY_NEGATIVE = "Handicaps";

  @FXML
  private ComboBox<String> categoryBox;
  @FXML
  private TextField searchField;
  @FXML
  private ListView<QualityData> qualityListView;
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
  public void initialize() {
    categoryBox.getItems().addAll(CATEGORY_ALL, CATEGORY_POSITIVE, CATEGORY_NEGATIVE);
    categoryBox.setValue(CATEGORY_ALL);

    categoryBox.valueProperty().addListener((obs, old, val) -> updateList());
    searchField.textProperty().addListener((obs, old, val) -> updateList());
    onlySelectableBox.selectedProperty().addListener((obs, old, val) -> updateList());
    gratisBox.selectedProperty().addListener((obs, old, val) -> updateInfoPanel());

    qualityListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, old, val) -> updateInfoPanel());

    qualityListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(@Nullable QualityData item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.name());
      }
    });

    okButton.setDisable(true);
    addAgainButton.setDisable(true);

    updateList();
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

  private void updateList() {
    String category = categoryBox.getValue();
    String search = searchField.getText().trim().toLowerCase(Locale.ROOT);
    boolean onlySelectable = onlySelectableBox.isSelected();

    Set<String> alreadySelected = character == null ? Set.of() : character.getQualities().stream()
        .filter(q -> q.source() == QualitySource.SELECTED)
        .map(q -> q.data().name())
        .collect(Collectors.toSet());

    List<QualityData> filtered = QualityDataLoader.getAllQualities().values().stream()
        .filter(q -> matchesCategory(q, category))
        .filter(q -> search.isEmpty() || q.name().toLowerCase(Locale.ROOT).contains(search))
        .filter(q -> !onlySelectable || isSelectable(q, alreadySelected))
        .sorted(Comparator.comparing(QualityData::name))
        .collect(Collectors.toList());

    QualityData selected = qualityListView.getSelectionModel().getSelectedItem();
    qualityListView.getItems().setAll(filtered);
    if (selected != null && filtered.contains(selected)) {
      qualityListView.getSelectionModel().select(selected);
    }
    updateInfoPanel();
  }

  private boolean matchesCategory(@NonNull QualityData q, @Nullable String category) {
    if (category == null || CATEGORY_ALL.equals(category)) {
      return true;
    }
    if (CATEGORY_POSITIVE.equals(category)) {
      return q.type() == QualityType.POSITIVE;
    }
    return q.type() == QualityType.NEGATIVE;
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

  private void updateInfoPanel() {
    QualityData selected = qualityListView.getSelectionModel().getSelectedItem();
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
    QualityData selected = qualityListView.getSelectionModel().getSelectedItem();
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
    QualityData selected = qualityListView.getSelectionModel().getSelectedItem();
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