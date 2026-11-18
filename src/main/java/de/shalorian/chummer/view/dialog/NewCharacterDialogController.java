package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.npc.NpcTemplate;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import de.shalorian.chummer.view.converter.MetatypeStringConverter;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Dialog „Neu“: ein Charakter (Name, Metatyp) oder eine Schergengruppe (Name, leer oder aus einer GRW-Vorlage,
 * ADR-062). Wählt man eine Vorlage, übernimmt das Namensfeld ihren Namen, solange es nicht selbst geändert wurde.
 */
public class NewCharacterDialogController {

  private static final String EMPTY_TEMPLATE = "Leer";

  @FXML
  private ToggleButton characterBtn;
  @FXML
  private ToggleButton groupBtn;
  @FXML
  private TextField nameField;
  @FXML
  private Label metatypeLabel;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private Label templateLabel;
  @FXML
  private ComboBox<@Nullable NpcTemplate> templateBox;
  @FXML
  private Label templateInfo;
  @FXML
  private Button createButton;

  private @Nullable Stage stage;
  private boolean confirmed = false;

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  @FXML
  public void initialize() {
    metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    metatypeBox.setConverter(new MetatypeStringConverter());
    metatypeBox.setValue(MetatypeDataLoader.find("Mensch"));
    Collator collator = Collator.getInstance(Locale.GERMAN);
    templateBox.getItems().add(null);
    // neue Gruppen haben nur das Grundregelwerk aktiv (ADR-061); Vorlagen anderer Regelwerke erscheinen nicht
    RuleOptions defaults = new RuleOptions();
    templateBox.getItems().addAll(NpcTemplateLoader.getAll().stream().filter(defaults::allows)
        .sorted(Comparator.comparing(NpcTemplate::name, collator)).toList());
    templateBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(@Nullable NpcTemplate template) {
        return template == null ? EMPTY_TEMPLATE : template.name() + " (Stufe " + template.rating() + ")";
      }

      @Override
      public @Nullable NpcTemplate fromString(String text) {
        return null;
      }
    });
    templateBox.setValue(null);
    templateBox.valueProperty().addListener((_, before, template) -> {
      templateInfo.setText(template == null ? "" : template.description() + " (" + template.source() + " S. "
          + template.page() + ")");
      String current = nameField.getText().trim();
      if (template != null && (current.isEmpty() || (before != null && current.equals(before.name())))) {
        nameField.setText(template.name());
      }
    });
    characterBtn.setOnAction(_ -> showKind(false));
    groupBtn.setOnAction(_ -> showKind(true));
    showKind(false);
    createButton.disableProperty().bind(
        Bindings.createBooleanBinding(() -> nameField.getText().trim().isEmpty(), nameField.textProperty()));
  }

  private void showKind(boolean group) {
    characterBtn.setSelected(!group);
    groupBtn.setSelected(group);
    for (Node node : List.of(metatypeLabel, metatypeBox)) {
      node.setVisible(!group);
      node.setManaged(!group);
    }
    for (Node node : List.of(templateLabel, templateBox, templateInfo)) {
      node.setVisible(group);
      node.setManaged(group);
    }
  }

  @FXML
  private void onCreate() {
    confirmed = true;
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

  public boolean isConfirmed() {
    return confirmed;
  }

  /** Ob eine Schergengruppe statt eines Charakters angelegt werden soll. */
  public boolean isNpcGroup() {
    return groupBtn.isSelected();
  }

  public @NonNull String getCharacterName() {
    return nameField.getText().trim();
  }

  public @NonNull MetatypeData getSelectedMetatype() {
    return metatypeBox.getValue();
  }

  /** Gewählte GRW-Vorlage, {@code null} für eine leere Gruppe. */
  public @Nullable NpcTemplate getSelectedTemplate() {
    return templateBox.getValue();
  }
}
