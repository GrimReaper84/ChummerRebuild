package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorCategory;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.utils.Numbers;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für Rüstung (GRW-Tabellen Kleidung und Panzerung, S. 373) mit Kategorie-Filter und Info-Panel
 * (Schutz, Verfügbarkeit, Preis, Quelle). Bei Kleidung gibt der Spieler den Kleidungspreis innerhalb der Spanne ein;
 * OK ist erst mit gültigem Preis aktiv. Rüstung mit Verfügbarkeit über 12 bleibt bei der
 * Charaktererschaffung sichtbar, aber deaktiviert (GRW S. 111, siehe {@link
 * Equipment#isAvailableForPurchase(Availability, int)}).
 */
public class ArmorSelectDialogController extends CatalogSelectDialogController<ArmorData> {

  private static final String NO_VALUE = "–";

  @FXML
  private Label protectionLabel;
  @FXML
  private Label availabilityLabel;
  @FXML
  private Label costLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Label priceCaption;
  @FXML
  private TextField priceField;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable Armor result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    priceField.textProperty().addListener((_, _, _) -> onListUpdated());
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(ArmorCategory.values()).map(ArmorCategory::getDisplayName).toList();
  }

  @Override
  protected @NonNull List<ArmorData> allEntries() {
    return List.copyOf(ArmorDataLoader.getAllArmor().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull ArmorData data, @NonNull String category) {
    return data.category().getDisplayName().equals(category);
  }

  @Override
  protected boolean isSelectable(@NonNull ArmorData data) {
    return character == null || character.getEquipment().isAvailableForPurchase(data);
  }

  @Override
  protected void onListUpdated() {
    ArmorData selected = itemListView.getSelectionModel().getSelectedItem();
    showPriceField(selected != null && selected.hasVariablePrice());
    if (selected == null) {
      protectionLabel.setText(NO_VALUE);
      availabilityLabel.setText(NO_VALUE);
      costLabel.setText(NO_VALUE);
      sourceLabel.setText(NO_VALUE);
      okButton.setDisable(true);
      return;
    }
    boolean selectable = isSelectable(selected);
    String availability = selected.availabilityText();
    if (!selectable) {
      availability += " (bei Erschaffung max. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    protectionLabel.setText(selected.protectionText());
    availabilityLabel.setText(availability);
    costLabel.setText(selected.priceText());
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    okButton.setDisable(!selectable || (selected.hasVariablePrice() && enteredPrice(selected) == null));
  }

  private void showPriceField(boolean visible) {
    priceCaption.setVisible(visible);
    priceCaption.setManaged(visible);
    priceField.setVisible(visible);
    priceField.setManaged(visible);
  }

  /** Eingegebener Kleidungspreis, oder {@code null}, wenn er fehlt oder außerhalb der Spanne liegt. */
  private @Nullable Integer enteredPrice(@NonNull ArmorData data) {
    try {
      int price = Numbers.parseInteger(priceField.getText());
      return data.isValidPrice(price) ? price : null;
    } catch (NumberFormatException _) {
      return null;
    }
  }

  @FXML
  private void onOk() {
    ArmorData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !isSelectable(selected)) {
      return;
    }
    Integer price = null;
    if (selected.hasVariablePrice()) {
      price = enteredPrice(selected);
      if (price == null) {
        return;
      }
    }
    result = new Armor(selected, true, price);
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

  /** Das gekaufte Rüstungsteil samt gewähltem Kleidungspreis, leer bei Abbruch. */
  public @NonNull Optional<Armor> getResult() {
    return Optional.ofNullable(result);
  }
}
