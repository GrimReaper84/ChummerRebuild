package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.skill.Skill;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl einer Gabe oder eines Handicaps. Nicht wählbare Einträge bleiben deaktiviert sichtbar; der Detailbereich
 * zeigt Seite, Kurzwirkung und gegebenenfalls den Grund aus {@link Character#getQualityBlockReason}. Verlangt die
 * Quality eine Auswahl (Fertigkeit, Attribut oder Text), ist OK erst mit gültiger Auswahl aktiv.
 */
public class QualitySelectDialogController extends CatalogSelectDialogController<QualityData> {

  private static final String CATEGORY_POSITIVE = "Gaben";
  private static final String CATEGORY_NEGATIVE = "Handicaps";

  @FXML
  private Label costLabel;
  @FXML
  private Label choiceLabel;
  @FXML
  private ComboBox<String> choiceBox;
  @FXML
  private TextField choiceField;
  @FXML
  private Label detailLabel;
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
  private @Nullable QualityData shownChoiceFor;
  private @Nullable QualitySelectResult result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    onlySelectableBox.selectedProperty().addListener((_, _, _) -> updateList());
    gratisBox.selectedProperty().addListener((_, _, _) -> updateList());
    choiceBox.valueProperty().addListener((_, _, _) -> updateButtons());
    choiceField.textProperty().addListener((_, _, _) -> updateButtons());
    showChoiceControls(QualitySelection.NONE);
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

  /** Alle wählbaren Katalogeinträge; Metatyp-Fähigkeiten kommen nur über den Metatyp und erscheinen nicht. */
  @Override
  protected @NonNull List<QualityData> allEntries() {
    return QualityDataLoader.getAllQualities().values().stream()
        .filter(q -> !q.rules().metatypeOnly())
        .toList();
  }

  @Override
  protected boolean matchesCategory(@NonNull QualityData q, @NonNull String category) {
    return CATEGORY_POSITIVE.equals(category)
        ? q.type() == QualityType.POSITIVE
        : q.type() == QualityType.NEGATIVE;
  }

  @Override
  protected boolean additionalFilter(@NonNull QualityData q) {
    return !onlySelectableBox.isSelected() || isSelectable(q);
  }

  @Override
  protected boolean isSelectable(@NonNull QualityData q) {
    return blockReason(q) == null;
  }

  private @Nullable String blockReason(@NonNull QualityData q) {
    Character c = this.character;
    return c == null ? null : c.getQualityBlockReason(q, gratisBox.isSelected());
  }

  @Override
  protected void onListUpdated() {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      costLabel.setText("–");
      detailLabel.setText("");
      showChoiceControls(QualitySelection.NONE);
      shownChoiceFor = null;
      updateButtons();
      return;
    }
    costLabel.setText(gratisBox.isSelected() ? "Gratis" : Math.abs(selected.bp()) + " BP");
    if (!selected.equals(shownChoiceFor)) {
      shownChoiceFor = selected;
      fillChoiceControls(selected);
    }
    updateButtons();
  }

  private void fillChoiceControls(@NonNull QualityData q) {
    showChoiceControls(q.selection());
    choiceLabel.setText(q.selectLabel().isEmpty() ? "Auswahl:" : q.selectLabel() + ":");
    choiceField.clear();
    Collator collator = Collator.getInstance(Locale.GERMAN);
    Character c = this.character;
    switch (q.selection()) {
      case SKILL -> choiceBox.getItems().setAll(c == null ? List.of()
          : c.getSkills().stream().map(Skill::getName).sorted(collator).toList());
      case ATTRIBUTE -> choiceBox.getItems().setAll(
          Arrays.stream(AttributeType.values()).map(AttributeType::getDisplayName).toList());
      default -> choiceBox.getItems().clear();
    }
    choiceBox.setValue(null);
  }

  private void showChoiceControls(@NonNull QualitySelection selection) {
    boolean list = selection == QualitySelection.SKILL || selection == QualitySelection.ATTRIBUTE;
    boolean text = selection == QualitySelection.TEXT;
    choiceLabel.setVisible(list || text);
    choiceBox.setVisible(list);
    choiceBox.setManaged(list);
    choiceField.setVisible(text);
    choiceField.setManaged(text);
  }

  /** Auswahl im Speicherformat: Fertigkeitsname, {@link AttributeType}-Name oder Freitext. */
  private @NonNull String currentChoice(@NonNull QualityData q) {
    return switch (q.selection()) {
      case NONE -> "";
      case TEXT -> choiceField.getText().trim();
      case SKILL -> Optional.ofNullable(choiceBox.getValue()).orElse("");
      case ATTRIBUTE -> Arrays.stream(AttributeType.values())
          .filter(t -> t.getDisplayName().equals(choiceBox.getValue()))
          .map(AttributeType::name)
          .findFirst()
          .orElse("");
    };
  }

  private void updateButtons() {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    Character c = this.character;
    String problem = null;
    if (selected != null) {
      problem = blockReason(selected);
      if (problem == null && c != null) {
        problem = c.getQualityChoiceProblem(selected, currentChoice(selected));
      }
      String page = selected.page() > 0 ? "GRW S. %d. ".formatted(selected.page()) : "";
      detailLabel.setText(page + selected.note() + (problem == null ? "" : "\n→ " + problem));
    }
    boolean disabled = selected == null || problem != null;
    okButton.setDisable(disabled);
    addAgainButton.setDisable(disabled);
  }

  @FXML
  private void onOk() {
    close(false);
  }

  @FXML
  private void onOkAndAdd() {
    close(true);
  }

  private void close(boolean addAnother) {
    QualityData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || okButton.isDisabled()) {
      return;
    }
    result = new QualitySelectResult(selected, gratisBox.isSelected(), addAnother, currentChoice(selected));
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
