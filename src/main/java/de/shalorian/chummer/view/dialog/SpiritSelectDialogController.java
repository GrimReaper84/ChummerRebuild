package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.spirit.Spirits;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Hinzufügen eines Geistes (GRW S. 112, 213, 222) bzw. Sprites (S. 113, 281/282): Art (nur die der eigenen Tradition
 * bzw. alle Sprites), Kraft bzw. Stufe, Dienste und ob er gebunden bzw. registriert ist. Bei der Erschaffung ist die
 * Kraft fest Magie bzw. Resonanz; die Grenzen prüft {@link Spirits#getAddProblem}, der Grund steht im Hinweisfeld.
 */
public class SpiritSelectDialogController extends CatalogSelectDialogController<SpiritData> {

  /** Obergrenze der Spinner im Karrieremodus, wo das Regelwerk keine feste Grenze setzt. */
  private static final int CAREER_MAX = 20;

  @FXML
  private Label forceCaption;
  @FXML
  private Spinner<Integer> forceSpinner;
  @FXML
  private Spinner<Integer> servicesSpinner;
  @FXML
  private CheckBox boundCheck;
  @FXML
  private Label categoryLabel;
  @FXML
  private Label statsLabel;
  @FXML
  private Label problemLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @NonNull SpiritKind kind = SpiritKind.SPIRIT;
  private @Nullable Spirit result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    forceSpinner.valueProperty().addListener((_, _, _) -> onListUpdated());
    servicesSpinner.valueProperty().addListener((_, _, _) -> onListUpdated());
    boundCheck.selectedProperty().addListener((_, _, _) -> onListUpdated());
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  /** Charakter und Art; legt Kraft- und Dienste-Bereich nach Modus fest (Erschaffung: Kraft fest, S. 112/113). */
  public void setTarget(@NonNull Character character, @NonNull SpiritKind kind) {
    this.character = character;
    this.kind = kind;
    Spirits spirits = character.getSpirits();
    forceCaption.setText(kind.getPowerName() + ":");
    boundCheck.setText(kind.getBoundName());
    boundCheck.setSelected(true);
    if (character.isCreated()) {
      forceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, CAREER_MAX,
          spirits.getCreationForce()));
      servicesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, CAREER_MAX, 1));
    } else {
      int force = spirits.getCreationForce();
      forceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(force, force, force));
      forceSpinner.setDisable(true);
      int maxServices = Math.max(1, spirits.getCreationMaxServices(kind));
      servicesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxServices, 1));
    }
    refreshCategoryOptions();
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return List.of();
  }

  @Override
  protected @NonNull List<SpiritData> allEntries() {
    return character == null ? List.of() : character.getSpirits().getTypes(kind);
  }

  @Override
  protected boolean matchesCategory(@NonNull SpiritData data, @NonNull String category) {
    return true;
  }

  @Override
  protected void onListUpdated() {
    SpiritData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || character == null) {
      categoryLabel.setText("");
      statsLabel.setText("");
      problemLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    int force = forceSpinner.getValue() == null ? 1 : forceSpinner.getValue();
    String category = selected.categoryIn(character.getTradition());
    categoryLabel.setText(category == null ? "" : category);
    statsLabel.setText(selected.statsText(force));
    String problem = problem(selected);
    problemLabel.setText(problem == null ? "" : "Nicht wählbar: " + problem + ".");
    okButton.setDisable(problem != null);
  }

  private @Nullable String problem(@NonNull SpiritData data) {
    if (character == null || forceSpinner.getValue() == null || servicesSpinner.getValue() == null) {
      return "Werte fehlen";
    }
    return character.getSpirits().getAddProblem(data, forceSpinner.getValue(), servicesSpinner.getValue(),
        boundCheck.isSelected());
  }

  @FXML
  private void onOk() {
    SpiritData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || problem(selected) != null) {
      return;
    }
    result = new Spirit(selected, forceSpinner.getValue(), servicesSpinner.getValue(), boundCheck.isSelected());
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

  public @NonNull Optional<Spirit> getResult() {
    return Optional.ofNullable(result);
  }
}
