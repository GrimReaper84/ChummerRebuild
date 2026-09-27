package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.view.converter.CyberwareGradeStringConverter;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für Cyberware bzw. Bioware (je nach {@link #setKind(EquipmentKind)}). Ergänzt das
 * generische Katalog-Fundament um eine Grade-Auswahl und — bei stufenbehafteten Implantaten — eine
 * Stufen-Auswahl, jeweils mit Live-Vorschau der resultierenden Essenz- und Nuyen-Kosten. Grades, die
 * aktuell nicht gekauft werden dürfen (Beta-/Deltaware bei der Charaktererschaffung, siehe {@link
 * Character#isCyberwareGradeAvailable(CyberwareGrade)}), bleiben sichtbar, aber deaktiviert.
 */
public class CyberwareSelectDialogController extends CatalogSelectDialogController<CyberwareData> {

  private static final String NO_VALUE = "–";
  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();

  @FXML
  private ComboBox<CyberwareGrade> gradeBox;
  @FXML
  private ComboBox<Integer> ratingBox;
  @FXML
  private Label essenceLabel;
  @FXML
  private Label costLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @NonNull EquipmentKind kind = EquipmentKind.CYBERWARE;
  private @Nullable Cyberware result;

  @FXML
  @Override
  public void initialize() {
    gradeBox.getItems().setAll(CyberwareGrade.values());
    gradeBox.setConverter(new CyberwareGradeStringConverter());
    gradeBox.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable CyberwareGrade grade, boolean empty) {
        super.updateItem(grade, empty);
        setText(empty || grade == null ? null : grade.getDisplayName());
        setDisable(!empty && grade != null && !isGradeAvailable(grade));
      }
    });
    gradeBox.setValue(CyberwareGrade.STANDARD);
    gradeBox.valueProperty().addListener((obs, old, val) -> onListUpdated());
    ratingBox.valueProperty().addListener((obs, old, val) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  /** Legt fest, ob Cyberware- oder Bioware-Einträge angeboten werden (Default: Cyberware). */
  public void setKind(@NonNull EquipmentKind kind) {
    this.kind = kind;
    refreshCategoryOptions();
    updateList();
  }

  private boolean isGradeAvailable(@NonNull CyberwareGrade grade) {
    return character == null || character.isCyberwareGradeAvailable(grade);
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return CyberwareDataLoader.getAllCyberware().values().stream()
        .filter(d -> d.kind() == kind)
        .map(CyberwareData::category)
        .distinct()
        .toList();
  }

  @Override
  protected @NonNull List<CyberwareData> allEntries() {
    return List.copyOf(CyberwareDataLoader.getAllCyberware().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull CyberwareData data, @NonNull String category) {
    return data.category().equals(category);
  }

  @Override
  protected boolean additionalFilter(@NonNull CyberwareData data) {
    return data.kind() == kind;
  }

  /** Passt die Stufen-Auswahl an den gewählten Eintrag an und aktualisiert die Kostenvorschau. */
  @Override
  protected void onListUpdated() {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !selected.hasRating()) {
      ratingBox.getItems().clear();
      ratingBox.setDisable(true);
    } else if (ratingBox.getItems().size() != selected.maxRating()) {
      ratingBox.getItems().setAll(
          IntStream.rangeClosed(CyberwareData.MIN_RATING, selected.maxRating()).boxed().toList());
      ratingBox.setValue(CyberwareData.MIN_RATING);
      ratingBox.setDisable(false);
    }
    updatePreview();
  }

  private void updatePreview() {
    Cyberware preview = buildSelection();
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    if (preview == null) {
      essenceLabel.setText(NO_VALUE);
      costLabel.setText(NO_VALUE);
      sourceLabel.setText(selected == null ? NO_VALUE : sourceText(selected));
      okButton.setDisable(true);
      return;
    }
    essenceLabel.setText(ESSENCE_FORMAT.toString(preview.getEssenceCost().doubleValue()));
    costLabel.setText(preview.getNuyenCost() + " ¥");
    sourceLabel.setText(sourceText(preview.getData()));
    okButton.setDisable(false);
  }

  private static @NonNull String sourceText(@NonNull CyberwareData data) {
    return data.source() + " S. " + data.page();
  }

  /**
   * Die aktuelle Auswahl als Implantat-Instanz, oder {@code null}, solange kein Eintrag, keine
   * zulässige Grade oder (bei stufenbehafteten Einträgen) keine Stufe gewählt ist.
   */
  private @Nullable Cyberware buildSelection() {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    CyberwareGrade grade = gradeBox.getValue();
    if (selected == null || grade == null || !isGradeAvailable(grade)) {
      return null;
    }
    if (!selected.hasRating()) {
      return new Cyberware(selected, grade, 0);
    }
    Integer rating = ratingBox.getValue();
    return rating == null ? null : new Cyberware(selected, grade, rating);
  }

  @FXML
  private void onOk() {
    Cyberware selection = buildSelection();
    if (selection == null) {
      return;
    }
    result = selection;
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

  public @NonNull Optional<Cyberware> getResult() {
    return Optional.ofNullable(result);
  }
}
