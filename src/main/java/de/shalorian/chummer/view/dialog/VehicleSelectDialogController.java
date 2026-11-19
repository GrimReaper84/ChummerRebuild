package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleCategory;
import de.shalorian.chummer.model.character.vehicle.VehicleData;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.utils.Numbers;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für Fahrzeuge und Drohnen (Fahrzeugtabelle GRW S. 397) mit Kategorie-Filter und
 * Info-Bereich mit den Tabellenwerten, der mitgelieferten Ausstattung und dem Hinweis aus dem
 * Fahrzeugtext. Fahrzeuge, deren Verfügbarkeit bei der Charaktererschaffung über 12 liegt, bleiben
 * sichtbar, aber deaktiviert (GRW S. 111, siehe {@link Equipment#isAvailableForPurchase(Availability,
 * int)}).
 */
public class VehicleSelectDialogController extends CatalogSelectDialogController<VehicleData> {

  @FXML
  private Label categoryLabel;
  @FXML
  private Label handlingLabel;
  @FXML
  private Label accelLabel;
  @FXML
  private Label speedLabel;
  @FXML
  private Label pilotLabel;
  @FXML
  private Label bodyLabel;
  @FXML
  private Label armorLabel;
  @FXML
  private Label sensorLabel;
  @FXML
  private Label includedLabel;
  @FXML
  private Label noteLabel;
  @FXML
  private Label availabilityLabel;
  @FXML
  private Label costLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable Vehicle result;

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  private boolean isAvailable(@NonNull VehicleData data) {
    return character == null
        || character.getEquipment().isAvailableForPurchase(data.availability(), 0);
  }

  @Override
  protected boolean isSelectable(@NonNull VehicleData data) {
    return isAvailable(data);
  }

  /** Kategorien = Überschriften der GRW-Fahrzeugtabelle. */
  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(VehicleCategory.values()).map(VehicleCategory::getDisplayName).toList();
  }

  @Override
  protected @NonNull List<VehicleData> allEntries() {
    return List.copyOf(VehicleDataLoader.getAllVehicles().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull VehicleData data, @NonNull String category) {
    return data.category().getDisplayName().equals(category);
  }

  @Override
  protected void onListUpdated() {
    VehicleData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      for (Label label : List.of(categoryLabel, handlingLabel, accelLabel, speedLabel, pilotLabel,
          bodyLabel, armorLabel, sensorLabel, includedLabel, noteLabel, availabilityLabel, costLabel,
          sourceLabel)) {
        label.setText(Numbers.NO_VALUE);
      }
      okButton.setDisable(true);
      return;
    }
    categoryLabel.setText(selected.category().getDisplayName());
    handlingLabel.setText(selected.handlingText());
    accelLabel.setText(selected.accelText());
    speedLabel.setText(String.valueOf(selected.speed()));
    pilotLabel.setText(String.valueOf(selected.pilot()));
    bodyLabel.setText(String.valueOf(selected.body()));
    armorLabel.setText(String.valueOf(selected.armor()));
    sensorLabel.setText(String.valueOf(selected.sensor()));
    includedLabel.setText(String.join(", ", new Vehicle(selected).describeIncludedEquipment()));
    noteLabel.setText(selected.note().isEmpty() ? Numbers.NO_VALUE : selected.note());
    String availability = selected.availability().displayText(0);
    if (!isAvailable(selected)) {
      availability += " (bei Erschaffung max. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    availabilityLabel.setText(availability);
    costLabel.setText(Numbers.nuyen(selected.cost()));
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    okButton.setDisable(!isAvailable(selected));
  }

  @FXML
  private void onOk() {
    VehicleData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !isAvailable(selected)) {
      return;
    }
    result = new Vehicle(selected);
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

  public @NonNull Optional<Vehicle> getResult() {
    return Optional.ofNullable(result);
  }
}
