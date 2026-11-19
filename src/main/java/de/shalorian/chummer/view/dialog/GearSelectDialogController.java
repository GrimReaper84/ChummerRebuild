package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearArea;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.magic.FocusType;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.utils.Numbers;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
 * Auswahl-Dialog für die übrige Ausrüstung (GRW-Tabellen S. 361–395) mit Bereichs-Filter. Je nach
 * Katalogeintrag gibt es eine Stufen-Auswahl, eine Menge in Packungsschritten (z.B. RFID-Marker
 * "pro 20 Stück", GRW S. 374), einen Pflicht-Zusatztext (bei gefälschten Lizenzen mit Vorauswahl,
 * GRW S. 377/131) und bei Preisspannen ein Preisfeld, dessen Untergrenze der GRW-Mindestpreis ist
 * (Nutzerentscheidung 2026-10-04).
 *
 * <p>Gegenstände und einzelne Stufen über Stufe 6 bzw. Verfügbarkeit 12 bleiben bei der
 * Charaktererschaffung sichtbar, aber deaktiviert (GRW S. 111, siehe {@link
 * Equipment#isAvailableForPurchase(Availability, int)}). Die Werte prüft {@link Gear} selbst; der
 * Dialog bietet OK erst an, wenn sich daraus ein gültiger Gegenstand bauen lässt.
 *
 * <p>Mit {@link #setInstallTarget(Character, Gear)} dient der Dialog dem Einbau in ein Gerät
 * (Betriebssystem im Kommlink, Verstärkung in einer Brille, Sensor im Sensorpaket, Zusatz im
 * Magschloss) und zeigt nur Einträge der Kategorie, die das Gerät aufnimmt. Was nicht mehr passt
 * (Kapazität, schon eingebaut), bleibt sichtbar, aber deaktiviert; der Grund steht im Hinweis.
 * Verfügbarkeitszuschläge zählen bei der Erschaffung mit der Verfügbarkeit des Geräts ({@link
 * Equipment#isAvailableForInstall}). Ohne Zielgerät sind reine Einbauten deaktiviert.
 */
public class GearSelectDialogController extends CatalogSelectDialogController<GearData> {
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
  /** Gerät, in das die Auswahl eingebaut wird, oder {@code null} beim normalen Kauf. */
  private @Nullable Gear installTarget;
  /** Ob nur Pfeile für einen Bogen gekauft werden (Waffen-Tab). */
  private boolean arrowsOnly;
  /** Fahrzeug, das ein Hardware-Modul oder Softwarepaket bekommt (GRW S. 260/261), sonst {@code null}. */
  private @Nullable Vehicle upgradeTarget;
  /** Kommlink-Headware, in die das gewählte Kommlink-Modell eingebaut wird (GRW S. 386), sonst {@code null}. */
  private @Nullable Cyberware commlinkTarget;
  /** Eintrag, für den Stufe, Menge, Zusatztext und Preis zuletzt eingerichtet wurden. */
  private @Nullable GearData configuredFor;

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
    priceField.setTextFormatter(new TextFormatter<String>(
        change -> change.getControlNewText().matches("\\d{0,7}") ? change : null));
    ratingBox.valueProperty().addListener((_, _, _) -> {
      showFixedPackPrice();
      updatePreview();
    });
    quantitySpinner.valueProperty().addListener((_, _, _) -> updatePreview());
    extraField.textProperty().addListener((_, _, _) -> updatePreview());
    extraBox.getEditor().textProperty().addListener((_, _, _) -> updatePreview());
    extraBox.valueProperty().addListener((_, _, _) -> updatePreview());
    priceField.textProperty().addListener((_, _, _) -> updatePreview());
    super.initialize();
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  /** Kauf eines Einbaus für {@code target}; nur Einträge der aufgenommenen Kategorie erscheinen. */
  public void setInstallTarget(@NonNull Character character, @NonNull Gear target) {
    this.character = character;
    this.installTarget = target;
    refreshCategoryOptions();
    updateList();
  }

  /**
   * Kauf eines Kommlink-Modells für die Kommlink-Headware {@code headware}; nur die Geräte, die sie aufnimmt, erscheinen
   * (GRW S. 386).
   */
  public void setCommlinkTarget(@NonNull Character character, @NonNull Cyberware headware) {
    this.character = character;
    this.commlinkTarget = headware;
    refreshCategoryOptions();
    updateList();
  }

  /** Kauf eines Hardware-Moduls oder Softwarepakets für {@code vehicle}; nur diese erscheinen (GRW S. 260/261). */
  public void setVehicleUpgradeTarget(@NonNull Character character, @NonNull Vehicle vehicle) {
    this.character = character;
    this.upgradeTarget = vehicle;
    refreshCategoryOptions();
    updateList();
  }

  /** Kauf von Pfeilen für einen Bogen; nur Pfeile erscheinen (GRW S. 361). */
  public void setArrowsOnly(@NonNull Character character) {
    this.character = character;
    this.arrowsOnly = true;
    refreshCategoryOptions();
    updateList();
  }

  /**
   * Warum {@code data} in Stufe {@code rating} hier nicht gekauft werden kann (außer
   * Verfügbarkeit/Stufe), oder {@code null}: Einbauten nur über ihr Gerät, und das Zielgerät muss den
   * Einbau aufnehmen können (Kapazität, schon eingebaut …).
   */
  private @Nullable String purchaseProblem(@NonNull GearData data, int rating) {
    if (installTarget != null) {
      return installTarget.installProblem(data, rating);
    }
    if (commlinkTarget != null) {
      return commlinkTarget.commlinkProblem(data);
    }
    if (upgradeTarget != null) {
      return upgradeTarget.matrixUpgradeProblem(data, rating);
    }
    if (data.category() == GearCategory.ARROWS && !arrowsOnly) {
      return "Pfeile werden im Waffen-Tab an einem Bogen gekauft („Pfeile hinzufügen“).";
    }
    return data.category().isInstalledOnly()
        ? "Wird nur als Einbau gekauft: passendes Gerät im Ausrüstungsbaum wählen und „Einbauen“."
        : null;
  }

  private boolean isAvailable(@NonNull GearData data, int rating) {
    if (character == null) {
      return true;
    }
    Equipment equipment = character.getEquipment();
    return installTarget == null
        ? equipment.isAvailableForPurchase(data.availability(), rating)
        : equipment.isAvailableForInstall(installTarget, data, rating);
  }

  /** Ob {@code data} in Stufe {@code rating} hier gekauft werden kann (Verfügbarkeit und Einbau). */
  private boolean canBuy(@NonNull GearData data, int rating) {
    return isAvailable(data, rating) && purchaseProblem(data, rating) == null;
  }

  private boolean isRatingAvailable(int rating) {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    return selected == null || canBuy(selected, rating);
  }

  /**
   * Ein Gegenstand ist wählbar, wenn wenigstens seine niedrigste Stufe verfügbar ist und er hier
   * gekauft werden kann ({@link #purchaseProblem(GearData, int)}).
   */
  @Override
  protected boolean isSelectable(@NonNull GearData data) {
    return canBuy(data, data.lowestRating());
  }

  /** Beim Einbau nur Einträge der Kategorie, die das Zielgerät aufnimmt; für den Bogen nur Pfeile. */
  @Override
  protected boolean additionalFilter(@NonNull GearData data) {
    if (arrowsOnly) {
      return data.category() == GearCategory.ARROWS;
    }
    if (commlinkTarget != null) {
      return data.category() == commlinkTarget.getData().acceptedGear();
    }
    if (upgradeTarget != null) {
      return data.matrixAttribute() != null;
    }
    return installTarget == null || installTarget.getData().accepts().contains(data.category());
  }

  /**
   * Bereiche der GRW-Ausrüstungsliste; beim Einbau bzw. für Pfeile nur die Bereiche der passenden
   * Einträge.
   */
  @Override
  protected @NonNull List<String> categoryOptions() {
    return Arrays.stream(GearArea.values())
        .filter(area -> allEntries().stream()
            .anyMatch(e -> e.category().area() == area && additionalFilter(e)))
        .map(GearArea::getDisplayName).toList();
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
    int maxQuantity = data.isSingleItem() ? 1 : pack * MAX_PACKS;
    quantitySpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(pack, maxQuantity, pack, pack));
    quantitySpinner.setDisable(data.isSingleItem());
    unitLabel.setText(pack == 1 ? data.unit().getPlural()
        : data.unit().getPlural() + " (in Schritten von " + pack + ")");

    // Vorschläge (Lizenz) bzw. feste Auswahl (Waffenklasse, Granatenform) als ComboBox
    boolean withSuggestions = !data.extraKind().suggestions().isEmpty();
    extraCaption.setText(data.needsExtra() ? data.extraLabel() + ":" : "Zusatz:");
    extraField.setVisible(!withSuggestions);
    extraField.setText("");
    extraField.setDisable(!data.needsExtra());
    extraField.setPromptText(data.needsExtra() ? "Pflichtangabe" : "kein Zusatz");
    extraBox.setVisible(withSuggestions);
    extraBox.setEditable(!data.extraKind().isChoice());
    extraBox.getItems().setAll(data.extraKind().suggestions());
    extraBox.setValue(null);
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
        label.setText(Numbers.NO_VALUE);
      }
      noteLabel.setText("");
      okButton.setDisable(true);
      return;
    }
    int rating = selectedRating(selected);
    categoryLabel.setText(selected.category().getDisplayName() + " (Bereich "
        + selected.category().area().getDisplayName() + ")");
    String availability = selected.availabilityText(rating);
    if (selected.availabilitySurcharge() && installTarget != null) {
      availability += " (Gerät dann "
          + installTarget.availabilityWith(selected, rating).displayText(0) + ")";
    }
    if (!isAvailable(selected, rating)) {
      availability += " (bei Erschaffung max. Stufe " + Availability.MAX_RATING_AT_CREATION
          + ", Verf. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    availabilityLabel.setText(availability);
    // Preis nur für einen gültigen Gegenstand; Gear rechnet und prüft selbst.
    Gear selection = buildSelection();
    costLabel.setText(selection == null ? Numbers.NO_VALUE : Numbers.nuyen(selection.getNuyenCost()));
    sourceLabel.setText(selected.source() + " S. " + selected.page());
    String problem = purchaseProblem(selected, rating);
    noteLabel.setText(problem != null ? problem : noteText(selected));
    okButton.setDisable(selection == null);
  }

  /** Tabellenhinweis, bei Preisspannen ergänzt um die Preisregel, bei Foki um die Bindungskosten. */
  private static @NonNull String noteText(@NonNull GearData data) {
    String rule;
    FocusType focusType = data.focusType();
    if (data.variableCost()) {
      rule = "Preisspanne laut GRW: Preis frei wählbar, mindestens "
          + Numbers.nuyen(data.packCostFor(data.lowestRating())) + " (höchstens "
          + Numbers.nuyen(Gear.MAX_CHOSEN_PACK_PRICE) + ").";
    } else if (focusType != null) {
      // GRW S. 112 und Tabelle S. 234
      rule = "Binden: bei der Erschaffung 1 BP je Kraft, danach Kraft × " + focusType.getKarmaFactor() + " Karma.";
    } else {
      return data.note();
    }
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
      default -> Objects.requireNonNullElse(extraBox.getValue(), ""); // Auswahlarten (ExtraKind#isChoice)
    };
  }

  /** Die aktuelle Auswahl als Gegenstand, oder {@code null}, solange nichts Kaufbares gewählt ist. */
  private @Nullable Gear buildSelection() {
    GearData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return null;
    }
    int rating = selectedRating(selected);
    if (!canBuy(selected, rating)) {
      return null;
    }
    try {
      return new Gear(selected, rating, quantity(selected), extraText(selected),
          selected.variableCost() ? enteredPackPrice() : null);
    } catch (IllegalArgumentException _) {
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
