package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.gear.ExtraKind;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearArea;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für die übrige Ausrüstung (GRW-Tabellen S. 368, 374–385, 395) mit Bereichs-Filter
 * in GRW-Reihenfolge. Je nach Katalogeintrag gibt es eine Stufen-Auswahl, eine Menge in
 * Packungsschritten (z.B. RFID-Marker "pro 20 Stück", GRW S. 374), einen Pflicht-Zusatztext (bei
 * gefälschten Lizenzen mit Vorauswahl, GRW S. 377/131) und bei Preisspannen ein Preisfeld, dessen
 * Untergrenze der GRW-Mindestpreis ist (Nutzerentscheidung 2026-10-04).
 *
 * <p>Gegenstände und einzelne Stufen über Stufe 6 bzw. Verfügbarkeit 12 bleiben bei der
 * Charaktererschaffung sichtbar, aber deaktiviert (GRW S. 111, siehe {@link
 * Equipment#isAvailableForPurchase(Availability, int)}). Die Werte prüft {@link Gear} selbst; der
 * Dialog bietet OK erst an, wenn sich daraus ein gültiger Gegenstand bauen lässt.
 */
public class GearSelectDialogController extends CatalogSelectDialogController<GearData> {

  private static final String NO_VALUE = "–";
  /** Obergrenze des Mengen-Spinners in Packungen. */
  private static final int MAX_PACKS = 999;

  @FXML
  private Label categoryLabel;
  @FXML
  private ComboBox<Integer> ratingBox;
  @FXML
  private Spinner<Integer> quantitySpinner;
  @FXML
  private Label unitLabel;
  @FXML
  private Label extraCaption;
  @FXML
  private TextField extraField;
  @FXML
  private ComboBox<String> extraBox;
  @FXML
  private Label priceCaption;
  @FXML
  private TextField priceField;
  @FXML
  private Label availabilityLabel;
  @FXML
  private Label costLabel;
  @FXML
  private Label sourceLabel;
  @FXML
  private Label noteLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable Gear result;
  /** Eintrag, für den Stufe, Menge, Zusatztext und Preis zuletzt eingerichtet wurden. */
  private @Nullable GearData configuredFor;

  @FXML
  @Override
  public void initialize() {
    ratingBox.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable Integer rating, boolean empty) {
        super.updateItem(rating, empty);
        setText(empty || rating == null ? null : String.valueOf(rating));
        setDisable(!empty && rating != null && !isRatingAvailable(rating));
      }
    });
    priceField.setTextFormatter(new TextFormatter<String>(
        change -> change.getControlNewText().matches("\\d{0,7}") ? change : null));
    ratingBox.valueProperty().addListener((obs, old, val) -> {
      showFixedPackPrice();
      updatePreview();
    });
    quantitySpinner.valueProperty().addListener((obs, old, val) -> updatePreview());
    extraField.textProperty().addListener((obs, old, val) -> updatePreview());
    extraBox.getEditor().textProperty().addListener((obs, old, val) -> updatePreview());
    priceField.textProperty().addListener((obs, old, val) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  private boolean isAvailable(@NonNull GearData data, int rating) {
    return character == null
        || character.getEquipment().isAvailableForPurchase(data.availability(), rating);
  }

  private boolean isRatingAvailable(int rating) {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    return selected == null || isAvailable(selected, rating);
  }

  /** Ein Gegenstand ist wählbar, wenn wenigstens seine niedrigste Stufe verfügbar ist. */
  @Override
  protected boolean isSelectable(@NonNull GearData data) {
    return isAvailable(data, data.lowestRating());
  }

  /** Bereiche in der Reihenfolge der GRW-Ausrüstungsliste, nicht alphabetisch. */
  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(GearArea.values()).map(GearArea::getDisplayName).toList();
  }

  @Override
  protected @NonNull List<GearData> allEntries() {
    return List.copyOf(GearDataLoader.getAllGear().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull GearData data, @NonNull String category) {
    return data.category().area().getDisplayName().equals(category);
  }

  /** Richtet die Eingaben für den markierten Eintrag ein und aktualisiert die Vorschau. */
  @Override
  protected void onListUpdated() {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ratingBox.getItems().clear();
      setInputsDisabled(true);
    } else if (!selected.equals(configuredFor)) {
      configureInputs(selected);
    }
    configuredFor = selected;
    updatePreview();
  }

  private void setInputsDisabled(boolean disabled) {
    ratingBox.setDisable(disabled);
    quantitySpinner.setDisable(disabled);
    extraField.setDisable(disabled);
    extraBox.setDisable(disabled);
    priceField.setDisable(disabled);
  }

  private void configureInputs(@NonNull GearData data) {
    setInputsDisabled(false);

    // Stufen bei jedem Wechsel neu setzen, damit die Popup-Zellen ihre verfügbarkeitsabhängige
    // Deaktivierung für den neuen Eintrag neu berechnen.
    if (data.hasRating()) {
      ratingBox.getItems().setAll(
          IntStream.rangeClosed(data.minRating(), data.maxRating()).boxed().toList());
      ratingBox.setValue(data.minRating());
    } else {
      ratingBox.getItems().clear();
      ratingBox.setDisable(true);
    }

    int pack = data.packSize();
    quantitySpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(pack, pack * MAX_PACKS, pack, pack));
    unitLabel.setText(pack == 1 ? data.unit().getPlural()
        : data.unit().getPlural() + " (in Schritten von " + pack + ")");

    boolean license = data.extraKind() == ExtraKind.LICENSE;
    extraCaption.setText(data.needsExtra() ? data.extraLabel() + ":" : "Zusatz:");
    extraField.setVisible(!license);
    extraField.setText("");
    extraField.setDisable(!data.needsExtra());
    extraField.setPromptText(data.needsExtra() ? "Pflichtangabe" : "kein Zusatz");
    extraBox.setVisible(license);
    extraBox.getItems().setAll(data.extraKind().suggestions());
    extraBox.getEditor().setText("");

    priceCaption.setText(data.variableCost() ? "Preis je Packung:" : "Preis je Packung (fest):");
    priceField.setDisable(!data.variableCost());
    priceField.setText(String.valueOf(data.packCostFor(data.lowestRating())));
  }

  /** Zeigt bei festem Preis den Packungspreis der gewählten Stufe im (gesperrten) Preisfeld. */
  private void showFixedPackPrice() {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected != null && !selected.variableCost()) {
      priceField.setText(String.valueOf(selected.packCostFor(selectedRating(selected))));
    }
  }

  private void updatePreview() {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      for (Label label : List.of(categoryLabel, unitLabel, availabilityLabel, costLabel,
          sourceLabel)) {
        label.setText(NO_VALUE);
      }
      noteLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    int rating = selectedRating(selected);
    categoryLabel.setText(selected.category().getDisplayName() + " (Bereich "
        + selected.category().area().getDisplayName() + ")");
    String availability = selected.availability().displayText(rating);
    if (!isAvailable(selected, rating)) {
      availability += " (bei Erschaffung max. Stufe " + Availability.MAX_RATING_AT_CREATION
          + ", Verf. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    availabilityLabel.setText(availability);
    // Preis nur für einen gültigen Gegenstand; Gear rechnet und prüft selbst.
    Gear selection = buildSelection();
    costLabel.setText(selection == null ? NO_VALUE : selection.getNuyenCost() + " ¥");
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    noteLabel.setText(noteText(selected));
    okButton.setDisable(selection == null);
  }

  /** Tabellenhinweis, bei Preisspannen ergänzt um die Preisregel. */
  private static @NonNull String noteText(@NonNull GearData data) {
    if (!data.variableCost()) {
      return data.note();
    }
    String rule = "Preisspanne laut GRW: Preis frei wählbar, mindestens "
        + data.packCostFor(data.lowestRating()) + " ¥ (höchstens " + Gear.MAX_CHOSEN_PACK_PRICE
        + " ¥).";
    return data.note().isEmpty() ? rule : data.note() + " " + rule;
  }

  private int selectedRating(@NonNull GearData data) {
    Integer chosen = ratingBox.getValue();
    return data.hasRating() && chosen != null ? chosen : data.lowestRating();
  }

  private int quantity(@NonNull GearData data) {
    Integer value = quantitySpinner.getValue();
    return value == null ? data.packSize() : value;
  }

  /** Packungspreis aus dem Preisfeld, oder {@code null}, solange es leer ist. */
  private @Nullable Integer enteredPackPrice() {
    String text = priceField.getText();
    return text == null || text.isEmpty() ? null : Integer.valueOf(text);
  }

  private @NonNull String extraText(@NonNull GearData data) {
    return switch (data.extraKind()) {
      case NONE -> "";
      case TEXT -> extraField.getText();
      case LICENSE -> extraBox.getEditor().getText();
    };
  }

  /** Die aktuelle Auswahl als Gegenstand, oder {@code null}, solange nichts Kaufbares gewählt ist. */
  private @Nullable Gear buildSelection() {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return null;
    }
    int rating = selectedRating(selected);
    if (!isAvailable(selected, rating)) {
      return null;
    }
    try {
      return new Gear(selected, rating, quantity(selected), extraText(selected),
          selected.variableCost() ? enteredPackPrice() : null);
    } catch (IllegalArgumentException e) {
      return null; // Zusatztext fehlt, Preis unter Minimum o.Ä. — Gear prüft die Regeln
    }
  }

  @FXML
  private void onOk() {
    Gear selection = buildSelection();
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

  public @NonNull Optional<Gear> getResult() {
    return Optional.ofNullable(result);
  }
}
