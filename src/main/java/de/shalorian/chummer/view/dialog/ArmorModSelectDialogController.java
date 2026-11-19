package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.ArmorModDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorModKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.utils.Numbers;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl eines Einbaus für ein Rüstungsteil (GRW S. 372/373): sein Zubehör und die Panzerungsmodifikationen, die es
 * aufnimmt. Einträge, die {@link Armor#installProblem} sperrt oder deren Verfügbarkeit bei der Erschaffung über 12 liegt
 * (ein Zuschlag zählt mit der des Teils), bleiben sichtbar und deaktiviert; der Grund steht unter dem Info-Bereich.
 */
public class ArmorModSelectDialogController extends CatalogSelectDialogController<ArmorModData> {

  @FXML
  private ComboBox<Integer> ratingBox;
  @FXML
  private Label effectLabel;
  @FXML
  private Label availabilityLabel;
  @FXML
  private Label costLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Label noteLabel;
  @FXML
  private Label problemLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable Armor target;
  private @Nullable ArmorMod result;
  /** Eintrag, für den {@code ratingBox} zuletzt befüllt wurde. */
  private @Nullable ArmorModData ratingSource;

  @FXML
  @Override
  public void initialize() {
    ratingBox.valueProperty().addListener((_, _, _) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  /** Das Teil, in das eingebaut wird, und sein Charakter (Erschaffung oder Karriere). */
  public void setTarget(@NonNull Character character, @NonNull Armor target) {
    this.character = character;
    this.target = target;
    refreshCategoryOptions();
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(ArmorModKind.values())
        .filter(kind -> allEntries().stream().anyMatch(m -> m.kind() == kind && additionalFilter(m)))
        .map(ArmorModKind::getDisplayName)
        .toList();
  }

  @Override
  protected @NonNull List<ArmorModData> allEntries() {
    return List.copyOf(ArmorModDataLoader.getAllMods().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull ArmorModData mod, @NonNull String category) {
    return mod.kind().getDisplayName().equals(category);
  }

  /** Nur Einbauten, die grundsätzlich zu diesem Teil passen: sein Zubehör und die Modifikationen, die es aufnimmt. */
  @Override
  protected boolean additionalFilter(@NonNull ArmorModData mod) {
    return target != null && target.fits(mod);
  }

  @Override
  protected boolean isSelectable(@NonNull ArmorModData mod) {
    return problem(mod, lowestRating(mod)) == null;
  }

  private static int lowestRating(@NonNull ArmorModData mod) {
    return mod.hasRating() ? ArmorModData.MIN_RATING : 0;
  }

  /** Grund, warum {@code mod} in dieser Stufe nicht eingebaut werden kann, oder {@code null}. */
  private @Nullable String problem(@NonNull ArmorModData mod, int rating) {
    if (target == null || character == null) {
      return null;
    }
    String problem = target.installProblem(mod, rating);
    if (problem != null) {
      return problem;
    }
    if (!character.getEquipment().isAvailableForInstall(target, mod, rating)) {
      return "Verfügbarkeit %d liegt bei der Erschaffung über %d".formatted(target.availabilityWith(mod, rating),
          Availability.MAX_AVAILABILITY_AT_CREATION);
    }
    return null;
  }

  @Override
  protected void onListUpdated() {
    ArmorModData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !selected.hasRating()) {
      ratingBox.getItems().clear();
      ratingBox.setDisable(true);
    } else if (!selected.equals(ratingSource)) {
      ratingBox.getItems().setAll(
          IntStream.rangeClosed(ArmorModData.MIN_RATING, selected.maxRating()).boxed().toList());
      ratingBox.setValue(ArmorModData.MIN_RATING);
      ratingBox.setDisable(false);
    }
    ratingSource = selected;
    updatePreview();
  }

  private int currentRating(@NonNull ArmorModData mod) {
    Integer chosen = ratingBox.getValue();
    return mod.hasRating() && chosen != null ? chosen : lowestRating(mod);
  }

  private void updatePreview() {
    ArmorModData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || target == null) {
      effectLabel.setText(Numbers.NO_VALUE);
      availabilityLabel.setText(Numbers.NO_VALUE);
      costLabel.setText(Numbers.NO_VALUE);
      sourceLabel.setText(Numbers.NO_VALUE);
      noteLabel.setText("");
      problemLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    int rating = currentRating(selected);
    effectLabel.setText(selected.effectText(rating));
    availabilityLabel.setText(target.availabilityText(selected, rating));
    costLabel.setText(Numbers.nuyen(selected.costFor(rating)));
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    noteLabel.setText(selected.note());
    String problem = problem(selected, rating);
    problemLabel.setText(problem == null ? "" : "Nicht wählbar: " + problem + ".");
    okButton.setDisable(problem != null);
  }

  @FXML
  private void onOk() {
    ArmorModData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    int rating = currentRating(selected);
    if (problem(selected, rating) != null) {
      return;
    }
    result = new ArmorMod(selected, rating);
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

  public @NonNull Optional<ArmorMod> getResult() {
    return Optional.ofNullable(result);
  }
}
