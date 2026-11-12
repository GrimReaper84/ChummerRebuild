package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.cyberware.ImplantSelection;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.view.converter.CyberwareGradeStringConverter;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl-Dialog für Cyberware bzw. Bioware (je nach {@link #setKind(EquipmentKind)}). Ergänzt das
 * generische Katalog-Fundament um eine Grade-Auswahl und — bei stufenbehafteten Implantaten — eine
 * Stufen-Auswahl, jeweils mit Live-Vorschau der resultierenden Essenz- und Nuyen-Kosten. Grades, die
 * aktuell nicht gekauft werden dürfen (Beta-/Deltaware bei der Charaktererschaffung, siehe {@link
 * Equipment#isCyberwareGradeAvailable(CyberwareGrade)}), bleiben sichtbar, aber deaktiviert. Dasselbe
 * gilt für Implantate und einzelne Stufen, deren Verfügbarkeit bei der Charaktererschaffung über 12
 * liegt (GRW S. 111, siehe {@link Equipment#isAvailableForPurchase(Availability, int)}), und für Implantate, die
 * {@code Implants#getBlockReason} sperrt (Unverträglichkeiten); der Grund steht im Hinweisfeld.
 *
 * <p>Je nach Eintrag erscheinen zusätzlich ein Auswahlfeld (Reflexrekorder: Fertigkeit bzw. Fertigkeitsgruppe) und
 * ein Preisfeld (Preisspanne, Kosmetische Modifikation). Mit {@link #setInstallTarget(Cyberware)} wird Zubehör für
 * ein Basissystem gewählt: Die Liste zeigt nur einbaubares Zubehör seiner Kategorie, die Grade ist die des
 * Basissystems, statt der Essenz steht der Kapazitätsbedarf da (GRW S. 386, 387).
 */
public class CyberwareSelectDialogController extends CatalogSelectDialogController<CyberwareData> {

  private static final String NO_VALUE = "–";
  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();

  @FXML
  private ComboBox<CyberwareGrade> gradeBox;
  @FXML
  private ComboBox<Integer> ratingBox;
  @FXML
  private Label choiceCaption;
  @FXML
  private ComboBox<String> choiceBox;
  @FXML
  private Label priceCaption;
  @FXML
  private TextField priceField;
  @FXML
  private Label availabilityLabel;
  @FXML
  private Label essenceCaption;
  @FXML
  private Label essenceLabel;
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
  private @NonNull EquipmentKind kind = EquipmentKind.CYBERWARE;
  private @Nullable Cyberware installTarget;
  private @Nullable Cyberware result;
  /** Implantat, für das {@code ratingBox} und {@code choiceBox} zuletzt befüllt wurden. */
  private @Nullable CyberwareData ratingSource;

  @FXML
  @Override
  public void initialize() {
    gradeBox.getItems().setAll(CyberwareGrade.values());
    gradeBox.setConverter(new CyberwareGradeStringConverter());
    gradeBox.setCellFactory(_ -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable CyberwareGrade grade, boolean empty) {
        super.updateItem(grade, empty);
        setText(empty || grade == null ? null : grade.getDisplayName());
        setDisable(!empty && grade != null && !isGradeAvailable(grade));
      }
    });
    gradeBox.setValue(CyberwareGrade.STANDARD);
    gradeBox.valueProperty().addListener((_, _, _) -> onListUpdated());
    ratingBox.setCellFactory(_ -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable Integer rating, boolean empty) {
        super.updateItem(rating, empty);
        setText(empty || rating == null ? null : String.valueOf(rating));
        setDisable(!empty && rating != null && !isRatingAvailable(rating));
      }
    });
    ratingBox.valueProperty().addListener((_, _, _) -> updatePreview());
    choiceBox.valueProperty().addListener((_, _, _) -> updatePreview());
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

  /** Legt fest, ob Cyberware- oder Bioware-Einträge angeboten werden (Default: Cyberware). */
  public void setKind(@NonNull EquipmentKind kind) {
    this.kind = kind;
    refreshCategoryOptions();
    updateList();
  }

  /**
   * Wählt Zubehör für das Basissystem {@code base} statt eines Implantats: nur einbaubares Zubehör seiner Kategorie,
   * Grade fest auf die des Basissystems, Kapazität statt Essenz.
   */
  public void setInstallTarget(@NonNull Cyberware base) {
    this.installTarget = base;
    this.kind = base.getKind();
    gradeBox.setValue(base.getGrade());
    gradeBox.setDisable(true);
    essenceCaption.setText("Kapazität:");
    refreshCategoryOptions();
    updateList();
  }

  private boolean isGradeAvailable(@NonNull CyberwareGrade grade) {
    return installTarget != null || character == null || character.getEquipment().isCyberwareGradeAvailable(grade);
  }

  /** Ob die gewählte Stufe des markierten Implantats wegen ihrer Verfügbarkeit gekauft werden darf. */
  private boolean isRatingAvailable(int rating) {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    return selected == null || isAvailable(selected, rating);
  }

  private boolean isAvailable(@NonNull CyberwareData data, int rating) {
    return character == null
        || character.getEquipment().isAvailableForPurchase(data.availability(), rating);
  }

  /**
   * Ein Implantat ist wählbar, wenn wenigstens seine niedrigste Stufe verfügbar ist und nichts den Kauf sperrt
   * (Unverträglichkeit bzw. beim Einbau fehlende Kapazität).
   */
  @Override
  protected boolean isSelectable(@NonNull CyberwareData data) {
    return isAvailable(data, lowestRating(data)) && problem(data, lowestRating(data), null) == null;
  }

  /**
   * Grund, warum {@code data} in dieser Stufe nicht gekauft bzw. eingebaut werden kann (ohne Verfügbarkeit), oder
   * {@code null}. Ohne {@code choice} (noch nichts gewählt bzw. Listenansicht) wird die Auswahl nicht geprüft.
   */
  private @Nullable String problem(@NonNull CyberwareData data, int rating, @Nullable String choice) {
    if (installTarget != null) {
      return installTarget.installProblem(data, rating);
    }
    if (character == null) {
      return null;
    }
    return choice == null
        ? character.getImplants().getBlockReason(data)
        : character.getImplants().getAddProblem(data, choice);
  }

  /** Auswahl für die Prüfung: leer ohne Auswahlfeld, sonst der gewählte Wert oder {@code null}. */
  private @Nullable String currentChoice(@NonNull CyberwareData data) {
    return data.selection() == ImplantSelection.NONE ? "" : choiceBox.getValue();
  }

  private static int lowestRating(@NonNull CyberwareData data) {
    return data.hasRating() ? CyberwareData.MIN_RATING : 0;
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return CyberwareDataLoader.getAllCyberware().values().stream()
        .filter(this::additionalFilter)
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
    if (installTarget != null) {
      return data.category().equals(installTarget.getData().category()) && data.isInstallable();
    }
    return data.kind() == kind;
  }

  /** Passt Stufen- und Auswahlfeld an den gewählten Eintrag an und aktualisiert die Kostenvorschau. */
  @Override
  protected void onListUpdated() {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || !selected.hasRating()) {
      ratingBox.getItems().clear();
      ratingBox.setDisable(true);
    } else if (!selected.equals(ratingSource)) {
      // Stufen bei jedem Implantatwechsel neu setzen, damit die Popup-Zellen ihre
      // verfügbarkeitsabhängige Deaktivierung für das neue Implantat neu berechnen.
      ratingBox.getItems().setAll(
          IntStream.rangeClosed(CyberwareData.MIN_RATING, selected.maxRating()).boxed().toList());
      ratingBox.setValue(CyberwareData.MIN_RATING);
      ratingBox.setDisable(false);
    }
    if (selected != null && !selected.equals(ratingSource)) {
      choiceBox.getItems().setAll(choiceOptions(selected));
      choiceBox.setValue(null);
      priceField.setPromptText(selected.hasVariablePrice() ? priceRange(selected) : "");
    }
    showRow(choiceCaption, choiceBox, selected != null && selected.selection() != ImplantSelection.NONE);
    showRow(priceCaption, priceField, selected != null && selected.hasVariablePrice());
    ratingSource = selected;
    updatePreview();
  }

  private @NonNull List<String> choiceOptions(@NonNull CyberwareData data) {
    if (character == null) {
      return List.of();
    }
    return switch (data.selection()) {
      case NONE -> List.of();
      case SKILL -> character.getImplants().getReflexRecorderSkills();
      case SKILL_GROUP -> character.getImplants().getReflexRecorderGroups();
    };
  }

  private static void showRow(@NonNull Node caption, @NonNull Node field, boolean visible) {
    caption.setVisible(visible);
    caption.setManaged(visible);
    field.setVisible(visible);
    field.setManaged(visible);
  }

  private static @NonNull String priceRange(@NonNull CyberwareData data) {
    NumberFormat format = NumberFormat.getIntegerInstance(Locale.GERMAN);
    return format.format(data.priceMin()) + "–" + format.format(data.priceMax()) + " ¥";
  }

  /** Eingegebener Preis, oder {@code null}, solange er fehlt oder außerhalb der Spanne liegt. */
  private @Nullable Integer enteredPrice(@NonNull CyberwareData data) {
    try {
      int price = Integer.parseInt(priceField.getText().trim().replace(".", ""));
      return data.isValidPrice(price) ? price : null;
    } catch (NumberFormatException _) {
      return null;
    }
  }

  private void updatePreview() {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    availabilityLabel.setText(selected == null ? NO_VALUE : availabilityText(selected));
    sourceLabel.setText(selected == null ? NO_VALUE : sourceText(selected));
    Cyberware preview = buildSelection();
    noteLabel.setText(selected == null ? "" : noteText(selected));
    if (preview == null) {
      essenceLabel.setText(NO_VALUE);
      costLabel.setText(NO_VALUE);
      okButton.setDisable(true);
      return;
    }
    essenceLabel.setText(installTarget != null
        ? String.valueOf(preview.getData().capacityCostFor(preview.getRating()))
        : ESSENCE_FORMAT.toString(preview.getEssenceCost().doubleValue()));
    costLabel.setText(preview.getNuyenCost() + " ¥");
    okButton.setDisable(false);
  }

  /** Hinweis des Katalogs, ergänzt um den Grund, warum die aktuelle Auswahl nicht gekauft werden kann. */
  private @NonNull String noteText(@NonNull CyberwareData data) {
    List<String> parts = new ArrayList<>();
    String problem = problem(data, currentRating(data), currentChoice(data));
    if (problem != null) {
      parts.add("Nicht wählbar: " + problem + ".");
    }
    if (!data.note().isEmpty()) {
      parts.add(data.note());
    }
    return String.join(" ", parts);
  }

  private int currentRating(@NonNull CyberwareData data) {
    Integer chosen = ratingBox.getValue();
    return data.hasRating() && chosen != null ? chosen : lowestRating(data);
  }

  /** Verfügbarkeit der gewählten (bzw. niedrigsten) Stufe, mit Hinweis, falls sie gesperrt ist. */
  private @NonNull String availabilityText(@NonNull CyberwareData data) {
    int rating = currentRating(data);
    String text = data.availability().displayText(rating);
    if (!isAvailable(data, rating)) {
      text += " (bei Erschaffung max. " + Availability.MAX_AVAILABILITY_AT_CREATION + ")";
    }
    return text;
  }

  private static @NonNull String sourceText(@NonNull CyberwareData data) {
    return data.source() + " S. " + data.page();
  }

  /**
   * Die aktuelle Auswahl als Implantat-Instanz, oder {@code null}, solange kein Eintrag, keine zulässige Grade, (bei
   * stufenbehafteten Einträgen) keine Stufe, keine Auswahl oder kein gültiger Preis gewählt ist, die Stufe wegen ihrer
   * Verfügbarkeit nicht gekauft werden darf oder etwas den Kauf bzw. Einbau sperrt.
   */
  private @Nullable Cyberware buildSelection() {
    CyberwareData selected = itemListView.getSelectionModel().getSelectedItem();
    CyberwareGrade grade = gradeBox.getValue();
    if (selected == null || grade == null || !isGradeAvailable(grade)) {
      return null;
    }
    Integer chosenRating = ratingBox.getValue();
    if (selected.hasRating() && chosenRating == null) {
      return null;
    }
    int rating = currentRating(selected);
    String choice = currentChoice(selected);
    Integer price = selected.hasVariablePrice() ? enteredPrice(selected) : null;
    if (choice == null || (selected.hasVariablePrice() && price == null)
        || !isAvailable(selected, rating) || problem(selected, rating, choice) != null) {
      return null;
    }
    return new Cyberware(selected, grade, rating, choice, price);
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
