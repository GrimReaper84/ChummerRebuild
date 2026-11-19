package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.template.DocumentTemplate;
import de.shalorian.chummer.model.template.Templates;
import de.shalorian.chummer.view.converter.MetatypeStringConverter;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Dialog „Neu“: ein leerer Charakter (Name, Metatyp), eine leere Schergengruppe (Name) oder ein Dokument aus einer
 * Vorlage der Regelwerke (Archetyp, Schergengruppe; ADR-063). Die Vorlagen-Auswahl gruppiert nach Art, zeigt die Quelle
 * in Klammern und als Tooltip Regelwerk und Seite. Wählt man eine Vorlage, übernimmt das Namensfeld ihren Namen,
 * solange es nicht selbst geändert wurde.
 */
public class NewCharacterDialogController {

  @FXML
  private ToggleButton characterBtn;
  @FXML
  private ToggleButton groupBtn;
  @FXML
  private ToggleButton templateBtn;
  @FXML
  private TextField nameField;
  @FXML
  private Label metatypeLabel;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private Label templateLabel;
  @FXML
  private ComboBox<DocumentTemplate> templateBox;
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
    // neue Dokumente haben nur das Grundregelwerk aktiv (ADR-061); Vorlagen anderer Regelwerke erscheinen nicht
    templateBox.getItems().setAll(Templates.available(new RuleOptions()));
    templateBox.setCellFactory(_ -> new TemplateCell());
    templateBox.setButtonCell(new TemplateCell());
    templateBox.valueProperty().addListener((_, before, template) -> {
      templateInfo.setText(template == null ? "" : template.description());
      fitWindow();
      String current = nameField.getText().trim();
      if (template != null && (current.isEmpty() || (before != null && current.equals(before.name())))) {
        nameField.setText(template.name());
      }
    });
    characterBtn.setOnAction(_ -> showKind(characterBtn));
    groupBtn.setOnAction(_ -> showKind(groupBtn));
    templateBtn.setOnAction(_ -> showKind(templateBtn));
    // Beschreibung nur mit gewählter Vorlage: leer brächte sie den Zeilenabstand des Rasters mit
    templateInfo.visibleProperty().bind(templateBtn.selectedProperty().and(templateInfo.textProperty().isNotEmpty()));
    templateInfo.managedProperty().bind(templateInfo.visibleProperty());
    showKind(characterBtn);
    createButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> nameField.getText().trim().isEmpty() || (templateBtn.isSelected() && templateBox.getValue() == null),
        nameField.textProperty(), templateBox.valueProperty(), templateBtn.selectedProperty()));
  }

  private void showKind(@NonNull ToggleButton chosen) {
    for (ToggleButton button : List.of(characterBtn, groupBtn, templateBtn)) {
      button.setSelected(button.equals(chosen));
    }
    show(List.of(metatypeLabel, metatypeBox), chosen.equals(characterBtn));
    show(List.of(templateLabel, templateBox), chosen.equals(templateBtn));
    fitWindow();
  }

  /** Passt das Fenster an den Inhalt an, sonst schiebt eine längere Beschreibung die Buttons aus dem Dialog. */
  private void fitWindow() {
    if (stage != null) {
      Platform.runLater(stage::sizeToScene);
    }
  }

  private static void show(@NonNull List<Node> nodes, boolean visible) {
    for (Node node : nodes) {
      node.setVisible(visible);
      node.setManaged(visible);
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

  /** Ob eine leere Schergengruppe angelegt werden soll. */
  public boolean isNpcGroup() {
    return groupBtn.isSelected();
  }

  public @NonNull String getCharacterName() {
    return nameField.getText().trim();
  }

  public @NonNull MetatypeData getSelectedMetatype() {
    return metatypeBox.getValue();
  }

  /** Gewählte Vorlage, wenn „Vorlagen“ aktiv ist, sonst {@code null}. */
  public @Nullable DocumentTemplate getSelectedTemplate() {
    return templateBtn.isSelected() ? templateBox.getValue() : null;
  }
}
