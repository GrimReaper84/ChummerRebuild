package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.magic.MentorDataLoader;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import de.shalorian.chummer.model.character.skill.Skill;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Gemeinsamer Ablauf der Auswahldialoge, bei denen zum Eintrag noch etwas gewählt wird (Gaben/Handicaps,
 * Adeptenkräfte): Auswahlfeld je {@link QualitySelection} (Fertigkeit, Attribut oder Text), Kosten- und Detailzeile mit
 * Sperrgrund, „Nur wählbare anzeigen“, OK erst bei gültiger Auswahl, „OK &amp; Weiteres“. Die Subklasse liefert nur
 * die katalogspezifischen Teile über die abstrakten Hooks.
 *
 * @param <T> Katalogeintrag
 * @param <R> Ergebnis des Dialogs
 */
public abstract class ChoiceCatalogSelectDialogController<T extends CatalogEntry, R>
    extends CatalogSelectDialogController<T> {

  @FXML
  protected Label costLabel;
  @FXML
  protected Label choiceLabel;
  @FXML
  protected ComboBox<String> choiceBox;
  @FXML
  protected TextField choiceField;
  @FXML
  protected Label detailLabel;
  @FXML
  protected CheckBox onlySelectableBox;
  @FXML
  protected Button okButton;
  @FXML
  protected Button addAgainButton;

  protected @Nullable Character character;
  private @Nullable Stage stage;
  private @Nullable T shownChoiceFor;
  private @Nullable R result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    onlySelectableBox.selectedProperty().addListener((_, _, _) -> updateList());
    choiceBox.valueProperty().addListener((_, _, _) -> updateDetails());
    choiceField.textProperty().addListener((_, _, _) -> updateDetails());
    showChoiceControls(QualitySelection.NONE);
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    updateList();
  }

  public @NonNull Optional<R> getResult() {
    return Optional.ofNullable(result);
  }

  /** Was beim Eintrag gewählt werden muss. */
  protected abstract @NonNull QualitySelection selection(@NonNull T entry);

  /** Bezeichnung des Auswahlfelds, leer für „Auswahl“. */
  protected abstract @NonNull String selectLabel(@NonNull T entry);

  /** Attribute, die zur Auswahl stehen. */
  protected abstract @NonNull List<AttributeType> attributeOptions();

  /** Warum der Eintrag mit dieser Auswahl nicht gewählt werden kann, oder {@code null}; nur mit Charakter gerufen. */
  protected abstract @Nullable String problem(@NonNull Character character, @NonNull T entry, @NonNull String choice);

  /** Text der Kostenzeile; nur mit Charakter gerufen. */
  protected abstract @NonNull String costText(@NonNull Character character, @NonNull T entry,
      @NonNull String choice);

  /** Seite und Kurzwirkung für den Detailbereich, ggf. abhängig von der aktuellen Auswahl. */
  protected abstract @NonNull String details(@NonNull T entry, @NonNull String choice);

  protected abstract @NonNull R createResult(@NonNull T entry, @NonNull String choice, boolean addAnother);

  @Override
  protected boolean additionalFilter(@NonNull T entry) {
    return !onlySelectableBox.isSelected() || isSelectable(entry);
  }

  @Override
  protected void onListUpdated() {
    T selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showChoiceControls(QualitySelection.NONE);
      shownChoiceFor = null;
    } else if (!selected.equals(shownChoiceFor)) {
      shownChoiceFor = selected;
      fillChoiceControls(selected);
    }
    updateDetails();
  }

  private void fillChoiceControls(@NonNull T entry) {
    QualitySelection selection = selection(entry);
    showChoiceControls(selection);
    String label = selectLabel(entry);
    choiceLabel.setText(label.isEmpty() ? "Auswahl:" : label + ":");
    choiceField.clear();
    Collator collator = Collator.getInstance(Locale.GERMAN);
    switch (selection) {
      case SKILL -> choiceBox.getItems().setAll(character == null ? List.of()
          : character.getSkills().stream().filter(sk -> !sk.isExoticTemplate()).map(Skill::getName)
              .sorted(collator).toList());
      case ATTRIBUTE -> choiceBox.getItems().setAll(
          attributeOptions().stream().map(AttributeType::getDisplayName).toList());
      case MENTOR -> choiceBox.getItems().setAll(MentorDataLoader.choiceLabels());
      default -> choiceBox.getItems().clear();
    }
    choiceBox.setValue(null);
  }

  private void showChoiceControls(@NonNull QualitySelection selection) {
    boolean list = selection == QualitySelection.SKILL || selection == QualitySelection.ATTRIBUTE
        || selection == QualitySelection.MENTOR;
    boolean text = selection == QualitySelection.TEXT;
    choiceLabel.setVisible(list || text);
    choiceBox.setVisible(list);
    choiceBox.setManaged(list);
    choiceField.setVisible(text);
    choiceField.setManaged(text);
  }

  /** Auswahl im Speicherformat: Fertigkeitsname, {@link AttributeType}-Name oder Freitext. */
  private @NonNull String currentChoice(@NonNull T entry) {
    return switch (selection(entry)) {
      case NONE -> "";
      case TEXT -> choiceField.getText().trim();
      case SKILL, MENTOR -> Optional.ofNullable(choiceBox.getValue()).orElse("");
      case ATTRIBUTE -> Arrays.stream(AttributeType.values())
          .filter(t -> t.getDisplayName().equals(choiceBox.getValue()))
          .map(AttributeType::name)
          .findFirst()
          .orElse("");
    };
  }

  /** Kosten, Details und ggf. Sperrgrund anzeigen; OK nur ohne Sperrgrund. */
  protected void updateDetails() {
    T selected = itemListView.getSelectionModel().getSelectedItem();
    String problem = null;
    if (selected == null) {
      costLabel.setText("–");
      detailLabel.setText("");
    } else {
      String choice = currentChoice(selected);
      if (character != null) {
        problem = problem(character, selected, choice);
        costLabel.setText(costText(character, selected, choice));
      }
      detailLabel.setText(details(selected, choice) + (problem == null ? "" : "\n→ " + problem));
    }
    boolean disabled = selected == null || problem != null;
    okButton.setDisable(disabled);
    addAgainButton.setDisable(disabled);
  }

  @FXML
  private void onOk() {
    close(false);
  }

  @FXML
  private void onOkAndAdd() {
    close(true);
  }

  private void close(boolean addAnother) {
    T selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null || okButton.isDisabled()) {
      return;
    }
    result = createResult(selected, currentChoice(selected), addAnother);
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
}
