package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.condition.StatusTest;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.character.rulebook.RulebookData;
import de.shalorian.chummer.model.character.rulebook.RulebookDataLoader;
import de.shalorian.chummer.utils.NumberStyle;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Einstellungsdialog (ADR-061): Anzeige für alle Charaktere, Regeln nur für den offenen Charakter. Ohne Charakter ist
 * der Regelbereich deaktiviert, nicht ausgeblendet. „Übernehmen“ schreibt die Regeln direkt in die
 * {@link RuleOptions} des Charakters; die Anzeige-Werte holt der Aufrufer über {@link #getResult()}.
 */
public class SettingsDialogController {

  private static final int TEST_COLUMNS = 2;

  @FXML
  private CheckBox groupThousandsBox;
  @FXML
  private ToggleButton decimalCommaBtn;
  @FXML
  private ToggleButton decimalPointBtn;
  @FXML
  private CheckBox statusStaticBox;
  @FXML
  private GridPane testsGrid;
  @FXML
  private VBox rulesContent;
  @FXML
  private Label rulesTitle;
  @FXML
  private Label noCharacterHint;
  @FXML
  private CheckBox limbArmorAverageBox;
  @FXML
  private CheckBox lateAwakeningBox;
  @FXML
  private CheckBox missedPaymentsBox;
  @FXML
  private VBox booksBox;

  private final Map<StatusTest, CheckBox> testBoxes = new EnumMap<>(StatusTest.class);
  private final Map<String, CheckBox> bookBoxes = new LinkedHashMap<>();
  private @Nullable Stage stage;
  private @Nullable RuleOptions ruleOptions;
  private @Nullable SettingsResult result;

  @FXML
  public void initialize() {
    int index = 0;
    for (StatusTest test : StatusTest.values()) {
      CheckBox box = new CheckBox(test.getDisplayName());
      testBoxes.put(test, box);
      testsGrid.add(box, index % TEST_COLUMNS, index / TEST_COLUMNS);
      index++;
    }
    // ein Trennzeichen ist immer gewählt: ein zweiter Klick auf den gewählten Knopf hebt die Wahl nicht auf
    decimalCommaBtn.setOnAction(_ -> decimalCommaBtn.setSelected(true));
    decimalPointBtn.setOnAction(_ -> decimalPointBtn.setSelected(true));
    limbArmorAverageBox.setTooltip(new Tooltip("Auslegung zu GRW S. 390; Standard ist die Summe wie im alten Chummer"));
    lateAwakeningBox.setTooltip(new Tooltip("Hebt die Sperre aus GRW S. 313 im Karrieremodus auf"));
    missedPaymentsBox.setTooltip(new Tooltip("Monat aussetzen im Karrieremodus nach GRW S. 312"));
    for (RulebookData book : RulebookDataLoader.getAll()) {
      CheckBox box = new CheckBox(book.name() + " (" + book.code() + ")");
      if (book.core()) {
        box.setSelected(true);
        box.setDisable(true);
        box.setTooltip(new Tooltip("Grundlage, immer aktiv"));
      }
      bookBoxes.put(book.code(), box);
      booksBox.getChildren().add(box);
    }
  }

  /**
   * Belegt den Dialog vor.
   *
   * @param display       aktuelle Anzeige-Einstellungen
   * @param ruleOptions   Regeln des offenen Charakters, {@code null} ohne Charakter
   * @param characterName Name für die Überschrift des Regelbereichs
   */
  public void setup(@NonNull Stage stage, @NonNull SettingsResult display, @Nullable RuleOptions ruleOptions,
      @NonNull String characterName) {
    this.stage = stage;
    this.ruleOptions = ruleOptions;
    groupThousandsBox.setSelected(display.numberStyle().groupThousands());
    (display.numberStyle().decimalComma() ? decimalCommaBtn : decimalPointBtn).setSelected(true);
    statusStaticBox.setSelected(display.statusStatic());
    testBoxes.forEach((test, box) -> box.setSelected(display.statusTests().contains(test)));
    boolean hasCharacter = ruleOptions != null;
    noCharacterHint.setVisible(!hasCharacter);
    noCharacterHint.setManaged(!hasCharacter);
    rulesContent.setDisable(!hasCharacter);
    if (ruleOptions == null) {
      return;
    }
    rulesTitle.setText("REGELN (NUR DIESER CHARAKTER: " + characterName.toUpperCase(Locale.GERMAN) + ")");
    limbArmorAverageBox.setSelected(ruleOptions.isLimbArmorAverage());
    lateAwakeningBox.setSelected(ruleOptions.isLateAwakening());
    missedPaymentsBox.setSelected(ruleOptions.isMissedLifestylePayments());
    for (Map.Entry<String, CheckBox> book : bookBoxes.entrySet()) {
      book.getValue().setSelected(ruleOptions.isBookEnabled(book.getKey()));
    }
  }

  /** Die übernommenen Anzeige-Einstellungen, {@code null} nach „Abbrechen“. */
  public @Nullable SettingsResult getResult() {
    return result;
  }

  @FXML
  private void onApply() {
    Set<StatusTest> tests = EnumSet.noneOf(StatusTest.class);
    testBoxes.forEach((test, box) -> {
      if (box.isSelected()) {
        tests.add(test);
      }
    });
    result = new SettingsResult(new NumberStyle(groupThousandsBox.isSelected(), decimalCommaBtn.isSelected()),
        statusStaticBox.isSelected(), tests);
    if (ruleOptions != null) {
      ruleOptions.setLimbArmorAverage(limbArmorAverageBox.isSelected());
      ruleOptions.setLateAwakening(lateAwakeningBox.isSelected());
      ruleOptions.setMissedLifestylePayments(missedPaymentsBox.isSelected());
      for (Map.Entry<String, CheckBox> book : bookBoxes.entrySet()) {
        ruleOptions.setBookEnabled(book.getKey(), book.getValue().isSelected());
      }
    }
    close();
  }

  @FXML
  private void onCancel() {
    close();
  }

  private void close() {
    if (stage != null) {
      stage.close();
    }
  }
}
