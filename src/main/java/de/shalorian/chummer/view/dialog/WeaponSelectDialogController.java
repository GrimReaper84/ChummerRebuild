package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import de.shalorian.chummer.utils.Numbers;
import java.util.Arrays;
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
 * Auswahl-Dialog für Waffen (GRW-Tabellen S. 361, 363, 366) mit Kategorie-Filter und Info-Bereich
 * mit den Tabellenwerten. Nur der Bogen hat eine Stufen-Auswahl (Mindeststärke). Waffen und
 * einzelne Bogenstufen, deren Verfügbarkeit bei der Charaktererschaffung über 12 liegt, bleiben
 * sichtbar, aber deaktiviert (GRW S. 111, siehe
 * {@link Equipment#isAvailableForPurchase(Availability, int)}).
 *
 * <p>Mit {@link #setMountTarget(Character)} dient der Dialog der Waffenauswahl für eine
 * Fahrzeug-Waffenhalterung und zeigt nur Waffen, die hineinpassen ({@link
 * WeaponCategory#fitsVehicleMount()}, GRW S. 395).
 */
public class WeaponSelectDialogController extends CatalogSelectDialogController<WeaponData> {

  @FXML
  private ComboBox<Integer> ratingBox;
  @FXML
  private Label damageLabel;
  @FXML
  private Label armorPiercingLabel;
  @FXML
  private Label modeLabel;
  @FXML
  private Label recoilLabel;
  @FXML
  private Label ammoLabel;
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
  private @Nullable Weapon result;
  /** Ob nur Waffen für eine Fahrzeug-Waffenhalterung angeboten werden. */
  private boolean mountOnly;
  /** Waffe, für die {@code ratingBox} zuletzt befüllt wurde. */
  private @Nullable WeaponData ratingSource;

  @FXML
  @Override
  public void initialize() {
    ratingBox.setCellFactory(_ -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable Integer rating, boolean empty) {
        super.updateItem(rating, empty);
        setText(empty || rating == null ? null : String.valueOf(rating));
        setDisable(!empty && rating != null && !isRatingAvailable(rating));
      }
    });
    ratingBox.valueProperty().addListener((_, _, _) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  /** Kauf einer Waffe für eine Fahrzeug-Waffenhalterung; nur passende Kategorien erscheinen. */
  public void setMountTarget(@NonNull Character character) {
    this.character = character;
    this.mountOnly = true;
    refreshCategoryOptions();
    updateList();
  }

  private boolean isAvailable(@NonNull WeaponData data, int rating) {
    return character == null
        || character.getEquipment().isAvailableForPurchase(data.availability(), rating);
  }

  private boolean isRatingAvailable(int rating) {
    WeaponData selected = itemListView.getSelectionModel().getSelectedItem();
    return selected == null || isAvailable(selected, rating);
  }

  /** Eine Waffe ist wählbar, wenn wenigstens ihre niedrigste Stufe verfügbar ist. */
  @Override
  protected boolean isSelectable(@NonNull WeaponData data) {
    return isAvailable(data, data.lowestRating());
  }

  /** Kategorien = GRW-Tabellen der Waffen; für eine Halterung nur die passenden. */
  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(WeaponCategory.values())
        .filter(category -> !mountOnly || category.fitsVehicleMount())
        .map(WeaponCategory::getDisplayName)
        .toList();
  }

  @Override
  protected boolean additionalFilter(@NonNull WeaponData data) {
    return !mountOnly || data.category().fitsVehicleMount();
  }

  @Override
  protected @NonNull List<WeaponData> allEntries() {
    // Waffen aus Rüstung (Schilde, Schockstreifen) kommen mit ihrem Teil und werden nicht gekauft.
    return WeaponDataLoader.getAllWeapons().values().stream().filter(w -> !w.builtIn()).toList();
  }

  @Override
  protected boolean matchesCategory(@NonNull WeaponData data, @NonNull String category) {
    return data.category().getDisplayName().equals(category);
  }

  /** Passt die Stufen-Auswahl an die markierte Waffe an und aktualisiert die Vorschau. */
  @Override
  protected void onListUpdated() {
    WeaponData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !selected.hasRating()) {
      ratingBox.getItems().clear();
      ratingBox.setDisable(true);
    } else if (!selected.equals(ratingSource)) {
      // Stufen bei jedem Waffenwechsel neu setzen, damit die Popup-Zellen ihre
      // verfügbarkeitsabhängige Deaktivierung für die neue Waffe neu berechnen.
      ratingBox.getItems().setAll(
          IntStream.rangeClosed(WeaponData.MIN_RATING, selected.maxRating()).boxed().toList());
      ratingBox.setValue(WeaponData.MIN_RATING);
      ratingBox.setDisable(false);
    }
    ratingSource = selected;
    updatePreview();
  }

  private void updatePreview() {
    WeaponData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      for (Label label : List.of(damageLabel, armorPiercingLabel, modeLabel, recoilLabel, ammoLabel,
          availabilityLabel, costLabel, sourceLabel)) {
        label.setText(Numbers.NO_VALUE);
      }
      okButton.setDisable(true);
      return;
    }
    int rating = selectedRating(selected);
    damageLabel.setText(orDash(selected.damage()));
    armorPiercingLabel.setText(orDash(selected.armorPiercing()));
    modeLabel.setText(orDash(selected.mode()));
    recoilLabel.setText(selected.recoil().displayText());
    ammoLabel.setText(orDash(selected.ammo()));
    String availability = selected.availability().displayText(rating);
    if (!isAvailable(selected, rating)) {
      availability += " (bei Erschaffung max. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    availabilityLabel.setText(availability);
    costLabel.setText(Numbers.nuyen(selected.costFor(rating)));
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    okButton.setDisable(buildSelection() == null);
  }

  private int selectedRating(@NonNull WeaponData data) {
    Integer chosen = ratingBox.getValue();
    return data.hasRating() && chosen != null ? chosen : data.lowestRating();
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? Numbers.NO_VALUE : text;
  }

  /** Die aktuelle Auswahl als Waffe, oder {@code null}, solange nichts Kaufbares gewählt ist. */
  private @Nullable Weapon buildSelection() {
    WeaponData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return null;
    }
    int rating = selectedRating(selected);
    if (!selected.isValidRating(rating) || !isAvailable(selected, rating)) {
      return null;
    }
    return new Weapon(selected, rating);
  }

  @FXML
  private void onOk() {
    Weapon selection = buildSelection();
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

  public @NonNull Optional<Weapon> getResult() {
    return Optional.ofNullable(result);
  }
}
