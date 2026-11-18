package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.weapon.AccessoryData;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.converter.MountStringConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für Feuerwaffenzubehör (Tabelle GRW S. 368) an einer bestimmten Waffe. Zubehör, das
 * laut Montageregeln (GRW S. 357, 367/368, siehe {@link Weapon#mountProblem(AccessoryData)}) nicht an
 * die Waffe passt oder dessen Verfügbarkeit bei der Erschaffung über 12 liegt, bleibt sichtbar, aber
 * deaktiviert; der Grund steht im Info-Bereich. Passt ein Teil an mehrere freie Halterungen
 * (Lasermarkierer, externes Smartgunsystem), wählt der Spieler die Halterung.
 */
public class AccessorySelectDialogController extends CatalogSelectDialogController<AccessoryData> {

  private static final String NO_VALUE = "–";
  private static final String WITHOUT_MOUNT = "Ohne Halterung";

  @FXML
  private ComboBox<Mount> mountBox;
  @FXML
  private Label mountsLabel;
  @FXML
  private Label recoilLabel;
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
  private @Nullable Weapon weapon;
  private @Nullable WeaponAccessory result;
  /** Zubehör, für das {@code mountBox} zuletzt befüllt wurde. */
  private @Nullable AccessoryData mountSource;

  @FXML
  @Override
  public void initialize() {
    mountBox.setConverter(new MountStringConverter());
    mountBox.valueProperty().addListener((_, _, _) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setWeapon(@NonNull Character character, @NonNull Weapon weapon) {
    this.character = character;
    this.weapon = weapon;
    updateList();
  }

  /** Warum {@code data} nicht gewählt werden kann, oder {@code null}. */
  private @Nullable String problem(@NonNull AccessoryData data) {
    if (weapon == null || character == null) {
      return null;
    }
    String mountProblem = weapon.mountProblem(data);
    if (mountProblem != null) {
      return mountProblem;
    }
    if (!character.getEquipment().isAvailableForPurchase(data.availability(), 0)) {
      return "Verfügbarkeit " + data.availability().displayText(0) + " liegt über "
          + Availability.MAX_AVAILABILITY_AT_CREATION + " (Charaktererschaffung, GRW S. 111)";
    }
    return null;
  }

  @Override
  protected boolean isSelectable(@NonNull AccessoryData data) {
    return problem(data) == null;
  }

  /** Halterungen laut GRW-Aufzählung, dazu Zubehör ohne Halterung. */
  @Override
  protected @NonNull List<String> categoryOptions() {
    List<String> options = new ArrayList<>(
        Arrays.stream(Mount.values()).map(Mount::getDisplayName).toList());
    options.add(WITHOUT_MOUNT);
    return options;
  }

  @Override
  protected @NonNull List<AccessoryData> allEntries() {
    return List.copyOf(AccessoryDataLoader.getAllAccessories().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull AccessoryData data, @NonNull String category) {
    if (WITHOUT_MOUNT.equals(category)) {
      return !data.needsMount();
    }
    return data.mounts().stream().anyMatch(m -> m.getDisplayName().equals(category));
  }

  /** Passt die Halterungsauswahl an das markierte Zubehör an und aktualisiert die Vorschau. */
  @Override
  protected void onListUpdated() {
    AccessoryData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || weapon == null || !selected.needsMount()) {
      mountBox.getItems().clear();
      mountBox.setDisable(true);
    } else if (!selected.equals(mountSource)) {
      List<Mount> free = weapon.freeMounts(selected);
      mountBox.getItems().setAll(free);
      mountBox.setValue(free.isEmpty() ? null : free.getFirst());
      mountBox.setDisable(free.size() < 2);
    }
    mountSource = selected;
    updatePreview();
  }

  private void updatePreview() {
    AccessoryData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || weapon == null) {
      for (Label label : List.of(mountsLabel, recoilLabel, availabilityLabel, costLabel,
          sourceLabel)) {
        label.setText(NO_VALUE);
      }
      noteLabel.setText("");
      problemLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    mountsLabel.setText(selected.mountsText());
    recoilLabel.setText(selected.rcText());
    availabilityLabel.setText(selected.availability().displayText(0));
    String cost = Numbers.nuyen(weapon.getAccessoryNuyenCost(selected));
    String catalogCost = selected.costText();
    costLabel.setText(catalogCost.equals(cost) ? cost : cost + " (" + catalogCost + ")");
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    noteLabel.setText(selected.note());
    String problem = problem(selected);
    problemLabel.setText(problem == null ? "" : "Nicht möglich: " + problem);
    okButton.setDisable(buildSelection() == null);
  }

  /** Die aktuelle Auswahl als Zubehörteil, oder {@code null}, solange nichts Montierbares gewählt ist. */
  private @Nullable WeaponAccessory buildSelection() {
    AccessoryData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || problem(selected) != null) {
      return null;
    }
    Mount mount = selected.needsMount() ? mountBox.getValue() : null;
    if (selected.needsMount() && mount == null) {
      return null;
    }
    return new WeaponAccessory(selected, mount);
  }

  @FXML
  private void onOk() {
    WeaponAccessory selection = buildSelection();
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

  public @NonNull Optional<WeaponAccessory> getResult() {
    return Optional.ofNullable(result);
  }
}
