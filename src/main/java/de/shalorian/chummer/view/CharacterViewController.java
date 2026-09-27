package de.shalorian.chummer.view;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.preventShrink;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_AUG;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterBiography;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.converter.MetatypeStringConverter;
import de.shalorian.chummer.view.converter.TraditionStringConverter;
import de.shalorian.chummer.view.dialog.ComplexFormSelectDialogController;
import de.shalorian.chummer.view.dialog.ContactEditDialogController;
import de.shalorian.chummer.view.dialog.CyberwareSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.FinalizeDialogController;
import de.shalorian.chummer.view.dialog.KnowledgeSkillAddDialogController;
import de.shalorian.chummer.view.dialog.LogEntryDialogController;
import de.shalorian.chummer.view.dialog.QualitySelectDialogController;
import de.shalorian.chummer.view.dialog.QualitySelectResult;
import de.shalorian.chummer.view.dialog.SpellSelectDialogController;
import de.shalorian.chummer.view.grid.AttributeRowLabel;
import de.shalorian.chummer.view.grid.GridSupport;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import de.shalorian.chummer.view.grid.SkillSortMode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

public class CharacterViewController {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  /**
   * Platzhalter für den augmentierten Attributwert bzw. das augmentierte Metatyp-Maximum, solange
   * es noch keine Essenz-/Cyberware-Berechnung gibt (Roadmap Stufe 2, Cyberware/Bioware). Struktur
   * (eigene Spalte + Klammerzusatz im Bereichs-Label, analog zum alten WinForms-UI) ist bereits
   * vorbereitet — bei Einführung der echten Berechnung wird hier nur dieser Platzhalter ersetzt.
   */
  private static final String ATTRIBUTE_AUGMENTED_PLACEHOLDER = "—";
  /**
   * Feste Breite der Attribut-"Wert"-Spalte — einzige Rating-Spalte, die zweistellig werden kann
   * (Metatyp-Varianten erlauben Attributmaxima bis 10, siehe {@code max="10"} in den Metatyp-XML-Dateien).
   * Alle anderen Rating-Spalten (Fertigkeiten, Wissensfertigkeiten, Kontakte, Gruppen) sind durch
   * {@code MAX_RATING = 6} einstellig und brauchen daher keine feste Breite — ohne diese Ausnahme
   * würde die Spalte beim Überschreiten von 9 sichtbar nachspringen, da die direkt angrenzenden
   * +/−-Buttons mitverschoben würden.
   */
  private static final int ATTRIBUTE_VALUE_COLUMN_WIDTH = 46;
  private static final String KARMA_SUFFIX = " Karma";
  private static final String NUYEN_SUFFIX = " ¥";
  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  private static final String CELL_COL_0 = "cell 0 ";
  private static final String CELL_COL_1 = "cell 1 ";
  private static final String CELL_COL_2 = "cell 2 ";
  private static final String CELL_COL_3 = "cell 3 ";
  private static final String CELL_COL_4 = "cell 4 ";
  private static final String CELL_COL_5 = "cell 5 ";
  // Attribute-Refresher bleiben stabil; Skill-Refresher werden beim Entkoppeln neu aufgebaut
  private final @NonNull List<Runnable> attrRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> specialAttrRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> skillRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> knowledgeSkillRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> qualityRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> contactRefreshers = new ArrayList<>();
  @FXML
  private Label nameLabel;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private Label remainingBpLabel;
  @FXML
  private VBox attributeGridHost;
  @FXML
  private VBox skillsContainer;
  @FXML
  private Button finalizeBtn;
  @FXML
  private HBox bpBox;
  @FXML
  private HBox nuyenBpBox;
  @FXML
  private Label nuyenBpValueLabel;
  @FXML
  private Button nuyenBpMinusBtn;
  @FXML
  private Button nuyenBpPlusBtn;
  @FXML
  private Label metatypeLabelCareer;
  @FXML
  private TableView<LogEntry> karmaTable;
  @FXML
  private TableView<LogEntry> nuyenTable;
  @FXML
  private Label karmaTotalLabel;
  @FXML
  private Label nuyenTotalLabel;
  @FXML
  private Label karmaStatusLabel;
  @FXML
  private Label nuyenStatusLabel;
  @FXML
  private VBox qualitiesContainer;
  @FXML
  private VBox contactsContainer;
  @FXML
  private VBox biographyContainer;
  @FXML
  private TabPane mainTabPane;
  @FXML
  private Tab magicTab;
  @FXML
  private VBox magicContainer;
  @FXML
  private Tab technomancerTab;
  @FXML
  private VBox technomancerContainer;
  @FXML
  private VBox cyberwareContainer;
  @FXML
  private VBox lifestyleContainer;
  @FXML
  private VBox armorContainer;
  @FXML
  private VBox weaponContainer;
  @FXML
  private Label remainingNuyenLabel;
  @FXML
  private VBox knowledgeSkillsContainer;
  @FXML
  private ToggleGroup skillSortToggleGroup;
  @FXML
  private ToggleButton sortByCategoryBtn;
  @FXML
  private ToggleButton sortByAlphabetBtn;
  private int magicTabIndex;
  private int technomancerTabIndex;
  private @NonNull SkillSortMode skillSortMode = SkillSortMode.CATEGORY;
  private @Nullable Character character;
  /** Wird in {@link #initialize()} erzeugt, sobald die FXML-Container injiziert sind. */
  @SuppressWarnings("NullAway.Init")
  private StreetGearTabBuilder streetGear;

  @FXML
  public void initialize() {
    streetGear = new StreetGearTabBuilder(lifestyleContainer, armorContainer, weaponContainer,
        streetGearHost());
    metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    metatypeBox.setConverter(new MetatypeStringConverter());
    metatypeBox.valueProperty().addListener((obs, old, newVal) -> {
      if (character == null || newVal == null || old == null) {
        return;
      }
      character.setMetatype(newVal);
      buildAttributeGrid();
      updateBpDisplay();
      refreshAllRows();
    });

    buildLogTable(karmaTable);
    buildLogTable(nuyenTable);

    magicTabIndex = mainTabPane.getTabs().indexOf(magicTab);
    mainTabPane.getTabs().remove(magicTab);

    technomancerTabIndex = mainTabPane.getTabs().indexOf(technomancerTab);
    mainTabPane.getTabs().remove(technomancerTab);

    skillSortToggleGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
      if (newVal == null) {
        // Ein ToggleButton in einer ToggleGroup kann sich durch erneutes Klicken selbst abwählen —
        // die Auswahl muss exklusiv bleiben, daher Rücksprung auf den vorherigen Zustand.
        skillSortToggleGroup.selectToggle(old);
        return;
      }
      if (newVal == sortByAlphabetBtn) {
        skillSortMode = SkillSortMode.ALPHABETICAL;
      } else if (newVal == sortByCategoryBtn) {
        skillSortMode = SkillSortMode.CATEGORY;
      }
      buildActiveSkillsSection();
    });
  }

  /**
   * Blendet den Magie-Tab ein bzw. aus, je nachdem ob {@link Character#isMagician()} aktuell wahr
   * ist — funktioniert identisch im Erstellungs- und im Karrieremodus. Anders als das bestehende
   * {@code setVisible()}/{@code setManaged()}-Muster für einzelne Controls wird der Tab tatsächlich
   * aus {@link TabPane#getTabs()} entfernt bzw. an der ursprünglichen FXML-Position wieder
   * eingefügt (analog zum C#-Original, siehe {@code docs/magie_analyse.md}).
   */
  private void syncMagicTabVisibility() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean shouldBeVisible = ch.isMagician();
    boolean isVisible = mainTabPane.getTabs().contains(magicTab);
    if (shouldBeVisible && !isVisible) {
      int index = Math.min(magicTabIndex, mainTabPane.getTabs().size());
      mainTabPane.getTabs().add(index, magicTab);
    } else if (!shouldBeVisible && isVisible) {
      mainTabPane.getTabs().remove(magicTab);
    }
  }

  /**
   * Blendet den Technomancer-Tab ein bzw. aus, je nachdem ob {@link Character#hasResonance()}
   * aktuell wahr ist — 1:1 dasselbe Muster wie {@link #syncMagicTabVisibility()}. Anders als bei Magie
   * (Magier vs. Adept) gibt es bei Technomancer keine feinere Differenzierung nötig, da nur eine
   * einzige Quality ("Technomant") RES freischaltet.
   */
  private void syncTechnomancerTabVisibility() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean shouldBeVisible = ch.hasResonance();
    boolean isVisible = mainTabPane.getTabs().contains(technomancerTab);
    if (shouldBeVisible && !isVisible) {
      int index = Math.min(technomancerTabIndex, mainTabPane.getTabs().size());
      mainTabPane.getTabs().add(index, technomancerTab);
    } else if (!shouldBeVisible && isVisible) {
      mainTabPane.getTabs().remove(technomancerTab);
    }
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    nameLabel.setText(character.getName());
    metatypeBox.setValue(character.getMetatype());
    updateBpDisplay();
    updateNuyenBpDisplay();
    updateLogDisplay();
    updateModeDisplay();
    buildQualitiesTab();
    buildBiographyTab();
    buildMagicTab();
    buildTechnomancerTab();
    buildCyberwareTab();
    buildStreetGearTabs();
  }

  // ── Biografie-Tab ─────────────────────────────────────────

  private void buildBiographyTab() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    biographyContainer.getChildren().clear();
    CharacterBiography bio = ch.getBiography();

    biographyContainer.getChildren().add(biographySectionTitle("Aussehen"));
    biographyContainer.getChildren().add(buildAppearanceGrid(bio));

    biographyContainer.getChildren().add(biographySectionTitle("Hintergrund"));
    biographyContainer.getChildren().add(buildBackgroundGrid(bio));

    biographyContainer.getChildren().add(biographySectionTitle("Spieler-Metadaten"));
    biographyContainer.getChildren().add(buildPlayerGrid(bio, ch));

    if (ch.isCreated()) {
      biographyContainer.getChildren().add(biographySectionTitle("Spielnotizen"));
      biographyContainer.getChildren().add(buildGameNotesGrid(bio));
    }
  }

  private @NonNull Label biographySectionTitle(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(label, new Insets(16, 0, 4, 0));
    return label;
  }

  private @NonNull GridPane newBiographyGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(14);
    grid.setVgap(10);
    ColumnConstraints labelCol = new ColumnConstraints();
    labelCol.setMinWidth(120);
    labelCol.setPrefWidth(140);
    ColumnConstraints fieldCol = new ColumnConstraints();
    fieldCol.setMinWidth(220);
    fieldCol.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(labelCol, fieldCol);
    return grid;
  }

  private @NonNull GridPane buildAppearanceGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    int row = 0;
    row = addTextFieldRow(grid, row, "Alias:", bio::getAlias, bio::setAlias);
    row = addTextFieldRow(grid, row, "Geschlecht:", bio::getSex, bio::setSex);
    row = addTextFieldRow(grid, row, "Alter:", bio::getAge, bio::setAge);
    row = addTextFieldRow(grid, row, "Augen:", bio::getEyes, bio::setEyes);
    row = addTextFieldRow(grid, row, "Haare:", bio::getHair, bio::setHair);
    row = addTextFieldRow(grid, row, "Größe:", bio::getHeight, bio::setHeight);
    row = addTextFieldRow(grid, row, "Gewicht:", bio::getWeight, bio::setWeight);
    addTextFieldRow(grid, row, "Haut:", bio::getSkin, bio::setSkin);
    return grid;
  }

  private @NonNull GridPane buildBackgroundGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    int row = 0;
    row = addTextAreaRow(grid, row, "Beschreibung:", bio::getDescription, bio::setDescription);
    row = addTextAreaRow(grid, row, "Hintergrund:", bio::getBackground, bio::setBackground);
    row = addTextAreaRow(grid, row, "Konzept:", bio::getConcept, bio::setConcept);
    addTextAreaRow(grid, row, "Notizen:", bio::getNotes, bio::setNotes);
    return grid;
  }

  /**
   * "Name:" bindet direkt an {@link Character#getName()}/{@link Character#setName(String)} (nicht an
   * {@link CharacterBiography}) — im alten WinForms-UI stand dieses Feld (bürgerlicher Name, getrennt
   * vom Straßennamen/Alias) in derselben Zeile wie der Spielername. Der Setter aktualisiert zusätzlich
   * {@link #nameLabel}, da dessen Text sonst nur beim initialen {@link #setCharacter} gesetzt wird und
   * nach einer Live-Änderung hier sonst veraltet bliebe.
   */
  private @NonNull GridPane buildPlayerGrid(@NonNull CharacterBiography bio,
      @NonNull Character character) {
    GridPane grid = newBiographyGrid();
    int row = addTextFieldRow(grid, 0, "Name:", character::getName,
        val -> {
          character.setName(val);
          nameLabel.setText(val);
        });
    addTextFieldRow(grid, row, "Spielername:", bio::getPlayerName, bio::setPlayerName);
    return grid;
  }

  private @NonNull GridPane buildGameNotesGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    addTextAreaRow(grid, 0, "Spielnotizen:", bio::getGameNotes, bio::setGameNotes);
    return grid;
  }

  private int addTextFieldRow(@NonNull GridPane grid, int row, @NonNull String labelText,
      @NonNull Supplier<String> getter, @NonNull Consumer<String> setter) {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");
    TextField field = new TextField();
    bindTextControl(field, getter, setter);
    grid.add(label, 0, row);
    grid.add(field, 1, row);
    return row + 1;
  }

  private int addTextAreaRow(@NonNull GridPane grid, int row, @NonNull String labelText,
      @NonNull Supplier<String> getter, @NonNull Consumer<String> setter) {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");
    GridPane.setValignment(label, VPos.TOP);
    TextArea area = new TextArea();
    area.setPrefRowCount(3);
    area.setWrapText(true);
    bindTextControl(area, getter, setter);
    grid.add(label, 0, row);
    grid.add(area, 1, row);
    return row + 1;
  }

  /**
   * Bindet ein einzeiliges Textfeld live ans Modell: der Wert wird beim Aufbau vorbefüllt, jede
   * Änderung schreibt sofort über {@code setter} ins Modell — kein Trimmen, keine Validierung,
   * kein Bestätigungsdialog (anders als die bestehenden Dialog-Formulare, die nur beim Bestätigen
   * einmalig auslesen).
   */
  private void bindTextControl(@NonNull TextField field, @NonNull Supplier<String> getter,
      @NonNull Consumer<String> setter) {
    field.setText(getter.get());
    field.textProperty().addListener((obs, old, val) -> setter.accept(val));
  }

  /** Mehrzeiliges Gegenstück zu {@link #bindTextControl(TextField, Supplier, Consumer)}. */
  private void bindTextControl(@NonNull TextArea area, @NonNull Supplier<String> getter,
      @NonNull Consumer<String> setter) {
    area.setText(getter.get());
    area.textProperty().addListener((obs, old, val) -> setter.accept(val));
  }

  // ── Attribute-Tab ─────────────────────────────────────────

  private void buildAttributeGrid() {
    if (character == null) {
      return;
    }

    attributeGridHost.getChildren().clear();
    attrRefreshers.clear();

    MigPane grid = buildAttributeMigPane();

    boolean career = character.isCreated();
    String[] headers = {"Attribut", "", "", "Wert", "", "(Aug)", "Bereich",
        career ? "Karma" : "BP-Kosten"};
    addHeaderRow(grid, headers);

    int row = 1;
    for (AttributeType type : AttributeType.values()) {
      addAttributeRow(grid, row++, type, character.getAttribute(type), character);
    }

    attributeGridHost.getChildren().add(withRightFiller(grid));

    buildSpecialAttributeSection();
  }

  /**
   * Baut die Sektion für die Sonderattribute Edge/Magic/Resonance unterhalb des Standardattribut-Grids. Edge ist immer sichtbar; Magic bzw. Resonance
   * werden nur gerendert, wenn der Charakter eine entsprechende Quality besitzt (siehe {@link Character#hasMagic()}/
   * {@link Character#hasResonance()}) — analog zum Verbergen im Original, siehe SR4-Regelwerksabgleich.
   */
  private void buildSpecialAttributeSection() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    specialAttrRefreshers.clear();

    Label sectionLabel = new Label("Sonderattribute");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(12, 0, 6, 0));
    attributeGridHost.getChildren().add(sectionLabel);

    MigPane grid = buildAttributeMigPane();

    addSpecialAttributeRow(grid, 0, ch.getEdge(), ch);

    SpecialAttribute magicOrResonance = ch.getMagicOrResonance();
    if (magicOrResonance != null) {
      addSpecialAttributeRow(grid, 1, magicOrResonance, ch);
    }

    attributeGridHost.getChildren().add(withRightFiller(grid));
    attributeGridHost.getChildren().add(buildEssenceInfoLabel(ch));
  }

  /**
   * Essenz-Zeile unter den Sonderattributen. Weist zusätzlich auf eine Magie-/Resonanz-Reduktion durch
   * Essenzverlust hin, damit der Unterschied zwischen angezeigtem (wirksamem) Wert und den nach dem
   * investierten Wert berechneten BP-/Karma-Kosten nachvollziehbar bleibt.
   */
  private @NonNull Label buildEssenceInfoLabel(@NonNull Character ch) {
    String text = "Essenz: " + ESSENCE_FORMAT.toString(ch.getEssence());
    SpecialAttribute magicOrResonance = ch.getMagicOrResonance();
    int penalty = ch.getEssencePenalty();
    if (magicOrResonance != null && penalty > 0) {
      text += "  —  " + magicOrResonance.getType().getDisplayName() + " (Wert und Maximum) um "
          + penalty + " reduziert";
    }
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_ATTR_RANGE);
    VBox.setMargin(label, new Insets(8, 0, 0, 0));
    return label;
  }

  /**
   * Sonderattribut-Zeile. Für Magie/Resonanz zeigen Wert und Maximum die durch Essenzverlust reduzierten
   * Werte ({@link Character#getEffectiveMagicOrResonanceValue()}/{@link
   * Character#getEffectiveMagicOrResonanceMax()}); Grenzen, +/−-Logik und BP-/Karma-Kosten arbeiten
   * dagegen weiterhin auf dem investierten Wert. Da investierter Wert und Maximum um dieselbe Penalty
   * verschoben werden, bleibt die Sperre am Maximum trotzdem konsistent mit der Anzeige.
   */
  private void addSpecialAttributeRow(@NonNull MigPane grid, int row,
      @NonNull SpecialAttribute attr, @NonNull Character character) {
    SpecialAttributeType type = attr.getType();
    boolean reducedByEssence = type != SpecialAttributeType.EDGE;
    int displayMax = reducedByEssence ? character.getEffectiveMagicOrResonanceMax() : attr.getMax();
    addAttributeGridRow(grid, row, specialAttrRefreshers,
        new AttributeRowLabel(type.getDisplayName(), type.getAbbreviation(), attr.getMin(),
            displayMax), null, new RowModel() {
          @Override
          public int value() {
            return attr.getValue();
          }

          @Override
          public @NonNull String valueDisplayText() {
            return String.valueOf(reducedByEssence
                ? character.getEffectiveMagicOrResonanceValue()
                : attr.getValue());
          }

          @Override
          public int min() {
            return attr.getMin();
          }

          @Override
          public int max() {
            return attr.getMax();
          }

          @Override
          public int bpStepCost() {
            return 10;
          }

          @Override
          public @NonNull String bpDisplayText() {
            return attr.getBpCost() + " BP";
          }

          @Override
          public int karmaStepCost() {
            return attr.getKarmaCost();
          }

          @Override
          public void increment() {
            attr.setValue(attr.getValue() + 1);
          }

          @Override
          public void decrement() {
            attr.setValue(attr.getValue() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return type.getDisplayName() + " " + from + " → " + to;
          }
        }, character);
  }

  private void addAttributeRow(@NonNull MigPane grid, int row, @NonNull AttributeType type,
      @NonNull CharacterAttribute attr, @NonNull Character character) {
    addAttributeGridRow(grid, row, attrRefreshers,
        new AttributeRowLabel(type.getDisplayName(), type.getAbbreviation(), attr.getMin(),
            attr.getMax()), () -> augmentedAttributeText(character, type), new RowModel() {
          @Override
          public int value() {
            return attr.getValue();
          }

          @Override
          public int min() {
            return attr.getMin();
          }

          @Override
          public int max() {
            return attr.getMax();
          }

          @Override
          public boolean canIncrement() {
            return character.canRaiseAttributeAtCreation(type);
          }

          @Override
          public int bpStepCost() {
            return 10;
          }

          @Override
          public @NonNull String bpDisplayText() {
            return attr.getBpCost() + " BP";
          }

          @Override
          public int karmaStepCost() {
            return attr.getKarmaCost();
          }

          @Override
          public void increment() {
            attr.setValue(attr.getValue() + 1);
          }

          @Override
          public void decrement() {
            attr.setValue(attr.getValue() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return type.getDisplayName() + " " + from + " → " + to;
          }
        }, character);
  }

  /**
   * Baut eine Attribut-Zeile (Name, Abkürzung, +/−, Wert, Aug-Platzhalter, Bereich, BP-/Karma-Kosten) in ein
   * MigPane ein und verdrahtet sie über {@link #wireIncrementable}. Gemeinsam genutzt von
   * {@link #addAttributeRow} und {@link #addSpecialAttributeRow}, die sich nur in Datenquelle und
   * {@link RowModel#canIncrement()} unterscheiden. Spaltenreihenfolge orientiert sich strukturell am
   * alten WinForms-{@code panAttributes} (Name/Abkürzung, Basiswert, augmentierter Wert, Metatyp-Grenzen
   * inkl. augmentiertem Maximum). Die Aug-Spalte zeigt den Text aus {@code augmentedText} (bei jedem
   * Refresh neu ermittelt, damit z.B. eine KON-Änderung die Panzerungsbehinderung sofort nachzieht),
   * bzw. {@link #ATTRIBUTE_AUGMENTED_PLACEHOLDER}, wenn {@code augmentedText} {@code null} ist; der
   * Klammerzusatz im Bereichs-Label bleibt bis zur Modellierung von Attributboni ein Platzhalter.
   */
  private void addAttributeGridRow(@NonNull MigPane grid, int row,
      @NonNull List<Runnable> refresherList, @NonNull AttributeRowLabel label,
      @Nullable Supplier<String> augmentedText, @NonNull RowModel model,
      @NonNull Character character) {
    Label attrNameLabel = new Label(label.displayName());
    Label abbrLabel = new Label(label.abbreviation());
    Button minus = new Button("−");
    Label valueLabel = new Label();
    Button plus = new Button("+");
    Label augLabel = new Label(ATTRIBUTE_AUGMENTED_PLACEHOLDER);
    Label rangeLabel = new Label(
        label.min() + " / " + label.max() + " (" + ATTRIBUTE_AUGMENTED_PLACEHOLDER + ")");
    Label bpLabel = new Label();

    attrNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    abbrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    valueLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    augLabel.getStyleClass().add(STYLE_ATTR_AUG);
    rangeLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);

    if (augmentedText != null) {
      Runnable refreshAugmented = () -> augLabel.setText(augmentedText.get());
      refresherList.add(refreshAugmented);
      refreshAugmented.run();
    }
    wireIncrementable(refresherList, new RowControls(valueLabel, bpLabel, minus, plus),
        character.isCreated(), model, character);

    addRowCells(grid, row, attrNameLabel, abbrLabel, minus, valueLabel, plus, augLabel, rangeLabel,
        bpLabel);
  }

  /**
   * Inhalt der Aug-Spalte eines Körperlichen/Geistigen Attributs: der wirksame Wert (derzeit nur durch
   * Panzerungsbehinderung auf GES/REA reduziert, siehe {@link
   * Character#getAugmentedAttributeValue(AttributeType)}), sofern er vom investierten Wert abweicht.
   */
  private static @NonNull String augmentedAttributeText(@NonNull Character character,
      @NonNull AttributeType type) {
    int augmented = character.getAugmentedAttributeValue(type);
    return augmented == character.getAttribute(type).getValue()
        ? ATTRIBUTE_AUGMENTED_PLACEHOLDER
        : String.valueOf(augmented);
  }

  /**
   * Gemeinsames Spalten-Layout für Standard- und Sonderattribut-Grid: alle Spalten inhaltsbasiert ohne
   * eigenes Wachstum — ungenutzter Platz wird stattdessen außen über {@link GridSupport#withRightFiller} verteilt.
   * Die Wert-Spalte ist die einzige feste Ausnahme, siehe {@link #ATTRIBUTE_VALUE_COLUMN_WIDTH}.
   */
  private @NonNull MigPane buildAttributeMigPane() {
    return new MigPane(MIGPANE_HIDEMODE_3,
        "[][center][center][" + ATTRIBUTE_VALUE_COLUMN_WIDTH
            + "!,center][center][center][][]");
  }

  // ── Skills-Tab ────────────────────────────────────────────

  private void buildSkillsTab() {
    buildActiveSkillsSection();
    buildKnowledgeSkillSection();
  }

  /**
   * Baut die aktive-Fertigkeiten-Sektion neu auf, je nach {@link #skillSortMode} entweder nach
   * Kategorie ({@link #buildCategorySkillsSection}) oder als zwei parallele alphabetische Listen
   * ({@link #buildAlphabeticalSkillsSection}). Betrifft ausschließlich {@link #skillsContainer} — die
   * Wissensfertigkeiten-Sektion liegt seit der Layout-Trennung in einem eigenen Container und wird
   * unabhängig über {@link #buildKnowledgeSkillSection()} aufgebaut.
   */
  private void buildActiveSkillsSection() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }

    skillsContainer.getChildren().clear();
    skillRefreshers.clear();

    if (skillSortMode == SkillSortMode.ALPHABETICAL) {
      buildAlphabeticalSkillsSection(ch);
    } else {
      buildCategorySkillsSection(ch);
    }
  }

  private void buildCategorySkillsSection(@NonNull Character character) {
    Map<SkillCategory, Map<String, List<Skill>>> grouped = groupSkillsByCategory(character);

    boolean firstSection = true;
    for (Map.Entry<SkillCategory, Map<String, List<Skill>>> catEntry : grouped.entrySet()) {
      if (catEntry.getValue().isEmpty()) {
        continue;
      }

      Label catLabel = new Label(catEntry.getKey().getDisplayName());
      catLabel.getStyleClass().add(STYLE_SECTION_TITLE);
      VBox.setMargin(catLabel, new Insets(firstSection ? 0 : 20, 0, 4, 0));
      skillsContainer.getChildren().add(catLabel);
      firstSection = false;

      MigPane grid = buildSkillGrid();
      skillsContainer.getChildren().add(withRightFiller(grid));

      int row = 0;
      for (Map.Entry<String, List<Skill>> groupEntry : catEntry.getValue().entrySet()) {
        row = addSkillGroupRows(grid, row, groupEntry.getKey(), groupEntry.getValue(), character);
      }
    }
  }

  /**
   * Alphabet-Modus: zwei unabhängig sortierte Listen nebeneinander — links alle noch gekoppelten
   * Fertigkeitsgruppen (nur Name + gemeinsame Rating-Steuerung, keine Mitgliedsnamen, siehe
   * {@code docs/skills_sortierung_plan.md} Abschnitt 13), rechts alle Einzelfertigkeiten (ungruppiert
   * oder entkoppelt), jeweils alphabetisch nach Name sortiert über {@link Character#getCoupledGroupNames()}
   * bzw. {@link Character#getIndividualSkills()}.
   */
  private void buildAlphabeticalSkillsSection(@NonNull Character character) {
    HBox columns = new HBox(24);

    VBox leftColumn = new VBox();
    Label groupsTitle = new Label("Fertigkeitsgruppen");
    groupsTitle.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(groupsTitle, new Insets(0, 0, 4, 0));
    leftColumn.getChildren().add(groupsTitle);
    MigPane groupGrid = buildGroupOnlyGrid();
    leftColumn.getChildren().add(withRightFiller(groupGrid));
    int groupRow = 0;
    for (String groupName : character.getCoupledGroupNames()) {
      groupRow = addGroupHeaderRow(groupGrid, groupRow, groupName, true, character);
    }

    VBox rightColumn = new VBox();
    Label individualTitle = new Label("Einzelfertigkeiten");
    individualTitle.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(individualTitle, new Insets(0, 0, 4, 0));
    rightColumn.getChildren().add(individualTitle);
    MigPane skillGrid = buildSkillGrid();
    rightColumn.getChildren().add(withRightFiller(skillGrid));
    int skillRow = 0;
    for (Skill skill : character.getIndividualSkills()) {
      addSkillRow(skillGrid, skillRow++, skill, false, character);
    }

    columns.getChildren().addAll(leftColumn, rightColumn);
    skillsContainer.getChildren().add(columns);
  }

  /**
   * Schmaleres Grid für die Gruppen-Liste im Alphabet-Modus — {@link #addGroupHeaderRow} befüllt die
   * Spalten 0 (Name), 1 (Auskoppeln-Button, nur bei gekoppelten Gruppen im Karrieremodus) und 2/3/4/5
   * (−/Wert/+/BP), nie Spalte 6/7 (Attribut-Abkürzung/Spezialisierung). Die volle 8-Spalten-Spec aus
   * {@link #buildSkillGrid()} würde daher in einem Nebeneinander-Layout unnötig Platz verschenken. Der
   * Auskoppeln-Button steht bewusst in einer eigenen Spalte statt direkt am Namenstext dran, damit er
   * über alle Zeilen hinweg untereinander ausgerichtet ist statt je nach Namenslänge zu wandern.
   */
  private @NonNull MigPane buildGroupOnlyGrid() {
    // Wert-Spalte ist bei Fertigkeitsgruppen durch Skill.MAX_RATING = 6 einstellig und braucht keine
    // feste Ausnahme (siehe ATTRIBUTE_VALUE_COLUMN_WIDTH-Kommentar). Kein eigenes Spaltenwachstum —
    // ungenutzter Platz wird außen über withRightFiller verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][center][center][center][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    return grid;
  }

  /**
   * Gruppiert die Fertigkeiten des Charakters nach Kategorie und innerhalb der Kategorie nach Gruppenname ({@code ""} für ungruppierte
   * Fertigkeiten).
   */
  private @NonNull Map<SkillCategory, Map<String, List<Skill>>> groupSkillsByCategory(
      @NonNull Character character) {
    Map<SkillCategory, Map<String, List<Skill>>> grouped = new LinkedHashMap<>();
    for (SkillCategory cat : SkillCategory.values()) {
      grouped.put(cat, new LinkedHashMap<>());
    }
    for (Skill skill : character.getSkills()) {
      String key = skill.getGroupName() != null ? skill.getGroupName() : "";
      Objects.requireNonNull(grouped.get(skill.getCategory()))
          .computeIfAbsent(key, k -> new ArrayList<>()).add(skill);
    }
    return grouped;
  }

  /**
   * Fügt die Zeilen einer Gruppe (bzw. der ungruppierten Fertigkeiten, wenn {@code groupName} leer ist) in den Skill-Grid ein.
   *
   * @return die Zeilennummer nach den eingefügten Zeilen
   */
  private int addSkillGroupRows(@NonNull MigPane grid, int row, @NonNull String groupName,
      @NonNull List<Skill> skills, @NonNull Character character) {
    if (groupName.isEmpty()) {
      for (Skill skill : skills) {
        addSkillRow(grid, row++, skill, false, character);
      }
      return row;
    }
    boolean coupled = character.isGroupCoupled(groupName);
    row = addGroupHeaderRow(grid, row, groupName, coupled, character);
    for (Skill skill : skills) {
      addSkillRow(grid, row++, skill, coupled, character);
    }
    return row;
  }

  /**
   * Baut den Skills-Tab neu auf, z.B. nach dem Entkoppeln einer Gruppe.
   */
  private void rebuildSkillsTab() {
    buildSkillsTab();
    attrRefreshers.forEach(Runnable::run);
    updateBpDisplay();
  }

  // ── Wissensfertigkeiten (Teil des Skills-Tabs) ─────────────

  private void buildKnowledgeSkillSection() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    knowledgeSkillsContainer.getChildren().clear();
    knowledgeSkillRefreshers.clear();

    Label sectionLabel = new Label("Wissensfertigkeiten");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(0, 0, 4, 0));

    Label poolLabel = new Label();
    poolLabel.getStyleClass().add(STYLE_BP_LABEL);

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddKnowledgeSkill());

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(sectionLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    if (!ch.isCreated()) {
      header.getChildren().add(poolLabel);
    }
    header.getChildren().add(addBtn);
    knowledgeSkillsContainer.getChildren().add(header);

    if (!ch.isCreated()) {
      Runnable poolRefresh = () -> poolLabel.setText(
          ch.getKnowledgeSkillPointsUsed() + " / " + ch.getKnowledgeSkillPointPool()
              + " Punkte" + (ch.getKnowledgeSkillOverflowBP() > 0
              ? " (" + ch.getKnowledgeSkillOverflowBP() + " BP über Pool)" : ""));
      knowledgeSkillRefreshers.add(poolRefresh);
      poolRefresh.run();
    }

    knowledgeSkillsContainer.getChildren().add(buildNativeLanguageRow(ch));

    MigPane grid = buildKnowledgeSkillGrid();
    knowledgeSkillsContainer.getChildren().add(withRightFiller(grid));

    int row = 0;
    for (KnowledgeSkill skill : ch.getKnowledgeSkills()) {
      if (skill.isNativeLanguage()) {
        continue;
      }
      addKnowledgeSkillRow(grid, row++, skill, ch);
    }
  }

  /**
   * Zeile für die Muttersprache — kostenlos, kein Rating, außerhalb des Wissensfertigkeiten- Punktepools (siehe
   * {@link KnowledgeSkill#isNativeLanguage()}).
   */
  private @NonNull HBox buildNativeLanguageRow(@NonNull Character ch) {
    Label label = new Label();
    label.getStyleClass().add(STYLE_ATTR_NAME);

    Button editBtn = new Button("Ändern");
    editBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    // EDIT statt PEN_TO_SQUARE: die gepinnte ikonli-fontawesome6-pack-Version 12.4.0 führt die
    // FA6-Icons noch unter ihren alten FA5-Namen, liefert für "edit" aber denselben Glyph/Codepoint
    // (U+F044), den FontAwesome 6 unter "pen-to-square" listet.
    useIcon(editBtn, FontAwesomeSolid.EDIT);

    Runnable refresh = () -> {
      KnowledgeSkill nativeLanguage = ch.getNativeLanguage();
      label.setText(
          "Muttersprache: " + (nativeLanguage != null ? nativeLanguage.getName() + " (M)" : "–"));
    };
    knowledgeSkillRefreshers.add(refresh);
    refresh.run();

    editBtn.setOnAction(e -> {
      KnowledgeSkill current = ch.getNativeLanguage();
      TextInputDialog dialog = new TextInputDialog(current != null ? current.getName() : "");
      applyDialogStyle(dialog.getDialogPane());
      dialog.setTitle("Muttersprache");
      dialog.setHeaderText("Muttersprache des Charakters");
      dialog.setContentText("Sprache:");
      dialog.showAndWait()
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .ifPresent(name -> {
            ch.setNativeLanguage(name);
            refreshAllRows();
          });
    });

    HBox row = new HBox(8, label, editBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(row, new Insets(0, 0, 8, 0));
    return row;
  }

  private @NonNull MigPane buildKnowledgeSkillGrid() {
    // Wert-Spalte durch KnowledgeSkill.MAX_RATING = 6 einstellig, keine feste Ausnahme nötig. Kein
    // eigenes Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][][center][center][center][][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    return grid;
  }

  private void addKnowledgeSkillRow(@NonNull MigPane grid, int row,
      @NonNull KnowledgeSkill skill, @NonNull Character character) {
    Label nameLabelDummy = new Label(skill.getName());
    Label categoryLabel = new Label(skill.getCategory().getDisplayName());
    Button minus = new Button("−");
    Label ratingLabel = new Label();
    Button plus = new Button("+");
    Label costLabel = new Label();
    Label specLabel = new Label();
    Button specBtn = new Button("Spez.");
    Button removeBtn = new Button("Entfernen");

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    specLabel.getStyleClass().add(STYLE_ATTR_NAME);
    specBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

    boolean careerMode = character.isCreated();
    Runnable specRefresh = () -> {
      String spec = skill.getSpecialization();
      specLabel.setText(spec != null ? spec : "–");
      boolean eligible = character.canSpecializeKnowledgeSkill(skill);
      boolean affordable = careerMode
          ? character.getKarmaTotal() >= character.getSpecializationKarmaCost()
          : spec != null || character.getRemainingBP() >= character.getKnowledgeSkillSpecializationBP();
      specBtn.setDisable(!eligible || !affordable);
    };
    knowledgeSkillRefreshers.add(specRefresh);
    specRefresh.run();
    specBtn.setOnAction(e -> onEditKnowledgeSkillSpecialization(skill, character));

    wireIncrementable(knowledgeSkillRefreshers, new RowControls(ratingLabel, costLabel, minus, plus),
        character.isCreated(), new RowModel() {
          @Override
          public int value() {
            return skill.getRating();
          }

          @Override
          public int min() {
            return 0;
          }

          @Override
          public int max() {
            return KnowledgeSkill.MAX_RATING;
          }

          @Override
          public boolean canIncrement() {
            return character.canRaiseKnowledgeSkillAtCreation(skill);
          }

          @Override
          public int bpStepCost() {
            return character.getKnowledgeSkillIncrementBP(skill);
          }

          @Override
          public @NonNull String bpDisplayText() {
            if (skill.getRating() >= KnowledgeSkill.MAX_RATING) {
              return "–";
            }
            int stepCost = character.getKnowledgeSkillIncrementBP(skill);
            return stepCost == 0 ? "frei" : stepCost + " BP";
          }

          @Override
          public int karmaStepCost() {
            return character.getKnowledgeSkillKarmaCost(skill);
          }

          @Override
          public void increment() {
            skill.setRating(skill.getRating() + 1);
          }

          @Override
          public void decrement() {
            skill.setRating(skill.getRating() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return skill.getName() + " " + from + " → " + to;
          }
        }, character);

    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(e -> {
      character.removeKnowledgeSkill(skill.getName());
      buildSkillsTab();
      updateBpDisplay();
      refreshAllRows();
    });

    addRowCells(grid, row, nameLabelDummy, categoryLabel, minus, ratingLabel, plus, specLabel,
        specBtn, costLabel, removeBtn);
  }

  private void onAddKnowledgeSkill() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = openDialog("KnowledgeSkillAddDialog.fxml", "Wissensfertigkeit hinzufügen");
      KnowledgeSkillAddDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        ch.addKnowledgeSkill(r.name(), r.category());
        buildSkillsTab();
        updateBpDisplay();
        refreshAllRows();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  // ── Qualities-Tab ──────────────────────────────────────────

  private void buildQualitiesTab() {
    if (character == null) {
      return;
    }
    qualitiesContainer.getChildren().clear();
    qualityRefreshers.clear();

    boolean career = character.isCreated();

    buildQualitySection("Gaben", QualityType.POSITIVE, career);

    Separator sep = new Separator();
    VBox.setMargin(sep, new Insets(16, 0, 16, 0));
    qualitiesContainer.getChildren().add(sep);

    buildQualitySection("Handicaps", QualityType.NEGATIVE, career);
    syncMagicTabVisibility();
    syncTechnomancerTabVisibility();
  }

  private void buildQualitySection(
      @NonNull String title, @NonNull QualityType type, boolean career) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }

    qualitiesContainer.getChildren().add(buildQualitySectionHeader(title, type, career, ch));

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(4);

    // Keine festen Breiten und kein eigenes Spaltenwachstum: alle vier Spalten bleiben bei ihrer
    // Inhaltsbreite (siehe preventShrink in addQualityRow); ungenutzter Platz wird außen über
    // withRightFiller verteilt.
    grid.getColumnConstraints()
        .addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(),
            new ColumnConstraints());

    List<CharacterQuality> section = ch.getQualities().stream()
        .filter(q -> q.data().type() == type)
        .toList();

    int row = 0;
    for (CharacterQuality q : section) {
      addQualityRow(grid, row++, q);
    }

    if (row == 0) {
      Label empty = new Label("Keine " + title + " gewählt");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      grid.add(empty, 0, 0);
    }

    qualitiesContainer.getChildren().add(withRightFiller(grid));
  }

  private @NonNull HBox buildQualitySectionHeader(@NonNull String title,
      @NonNull QualityType type, boolean career, @NonNull Character ch) {
    Label sectionLabel = new Label(title);
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    Label limitLabel = new Label();
    limitLabel.getStyleClass().add(STYLE_BP_LABEL);

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddQuality(type));

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(sectionLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    if (!career) {
      header.getChildren().add(limitLabel);
    }
    header.getChildren().add(addBtn);
    VBox.setMargin(header, new Insets(0, 0, 6, 0));

    if (!career) {
      Runnable limitRefresh = () -> {
        int used = type == QualityType.POSITIVE
            ? ch.getPositiveQualityBP()
            : ch.getNegativeQualityBP();
        limitLabel.setText(used + " / " + Character.MAX_POSITIVE_QUALITY_BP + " BP");
      };
      qualityRefreshers.add(limitRefresh);
      limitRefresh.run();
    }

    return header;
  }

  private void addQualityRow(@NonNull GridPane grid, int row, @NonNull CharacterQuality q) {
    boolean isMetatype = q.source() == QualitySource.METATYPE;

    Label qualityNameLabel = new Label(q.data().name());
    qualityNameLabel.getStyleClass().add(isMetatype ? "quality-metatype" : STYLE_ATTR_NAME);

    Label costLabel = new Label();
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    if (q.gratis()) {
      costLabel.setText("Gratis");
    } else {
      costLabel.setText(Math.abs(q.data().bp()) + " BP");
    }

    Label sourceLabel = new Label(isMetatype ? "(Metatyp)" : "");
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);

    preventShrink(qualityNameLabel);
    preventShrink(costLabel);
    preventShrink(sourceLabel);
    grid.add(qualityNameLabel, 0, row);
    grid.add(costLabel, 1, row);
    grid.add(sourceLabel, 2, row);

    if (!isMetatype) {
      Button removeBtn = new Button("Entfernen");
      removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
      useIcon(removeBtn, FontAwesomeSolid.TRASH);
      removeBtn.setOnAction(e -> onRemoveQuality(q));
      preventShrink(removeBtn);
      grid.add(removeBtn, 3, row);
    }
  }

  /**
   * Entfernt eine gewählte Quality. Handicaps im Karrieremodus loswerden kostet die doppelten GP-Kosten in Karma (siehe
   * {@link Character#getQualityKarmaCost}); alle anderen Fälle (Erstellungsmodus, Gaben, Gratis-Qualities) bleiben unverändert kostenlos.
   */
  private void onRemoveQuality(@NonNull CharacterQuality q) {
    Character c = this.character;
    if (c == null) {
      return;
    }
    String qualityName = q.data().name();
    if (c.isCreated() && q.data().type() == QualityType.NEGATIVE && !q.gratis()) {
      int karmaCost = c.getQualityKarmaCost(q.data());
      if (karmaChargeFailed(c, "%s loswerden".formatted(qualityName), karmaCost)) {
        return;
      }
    }
    c.removeQuality(qualityName);
    buildQualitiesTab();
    buildAttributeGrid();
    buildCyberwareTab();
    updateBpDisplay();
    refreshAllRows();
  }

  /**
   * Prüft Karma-Verfügbarkeit, holt eine Bestätigung ein und verbucht den Karma-Log-Eintrag für einen Karrieremodus-Kauf (z.B. Erwerb einer Gabe,
   * Loswerden eines Handicaps, Erlernen eines Zaubers) — gemeinsame Verdrahtung für alle Karrieremodus-Käufe mit fixem Karma-Preis.
   *
   * @return true, wenn das Karma NICHT verbucht wurde (fehlendes Karma oder abgebrochen) — die Aufrufer sollen in diesem Fall die eigentliche Aktion
   * abbrechen
   */
  private boolean karmaChargeFailed(@NonNull Character c, @NonNull String label,
      int karmaCost) {
    return chargeFailed(SpendCurrency.KARMA, label, karmaCost, c.getKarmaTotal(),
        c::addKarmaEntry);
  }

  /**
   * Gegenstück zu {@link #karmaChargeFailed} für Nuyen-Käufe im Karrieremodus (Cyberware, Rüstung;
   * Muster des C#-Originals: Guthabenprüfung, Log-Eintrag, sonst Abbruch mit Hinweis).
   *
   * @return true, wenn NICHT gebucht wurde — der Aufrufer bricht dann den Kauf ab
   */
  private boolean nuyenChargeFailed(@NonNull Character c, @NonNull String label, int nuyenCost) {
    return chargeFailed(SpendCurrency.NUYEN, label, nuyenCost, c.getNuyenTotal(),
        c::addNuyenEntry);
  }

  /**
   * Gemeinsamer Ablauf aller Karrieremodus-Käufe: bei zu wenig Guthaben Hinweis und Abbruch, sonst
   * Bestätigung einholen und einen negativen Log-Eintrag über {@code logSink} buchen.
   *
   * @return true, wenn NICHT gebucht wurde (zu wenig Guthaben oder abgebrochen)
   */
  private boolean chargeFailed(@NonNull SpendCurrency currency, @NonNull String label, int cost,
      int available, @NonNull Consumer<LogEntry> logSink) {
    if (available < cost) {
      Notifications.create()
          .title("Nicht genug " + currency.getDisplayName())
          .text("Benötigt: " + currency.formatAmount(cost))
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
      return true;
    }
    if (!confirmSpend(currency, label, cost, available, null)) {
      return true;
    }
    logSink.accept(new LogEntry(label, -cost, LocalDate.now(ZoneId.systemDefault())));
    updateLogDisplay();
    return false;
  }

  private void rebuildQualitiesTab() {
    buildQualitiesTab();
    buildAttributeGrid();
    buildCyberwareTab();
    refreshAllRows();
  }

  private void onAddQuality(@NonNull QualityType filter) {
    openQualitySelectDialog(filter);
  }

  private void openQualitySelectDialog(@NonNull QualityType filter) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean again = true;
    while (again) {
      again = openQualitySelectDialogOnce(ch, filter);
    }
  }

  /**
   * Öffnet den Quality-Auswahl-Dialog einmal und wertet das Ergebnis aus.
   *
   * @return ob der Dialog erneut geöffnet werden soll ({@code r.addAnother()})
   */
  private boolean openQualitySelectDialogOnce(@NonNull Character ch,
      @NonNull QualityType filter) {
    try {
      DialogHandle dialog = openDialog("QualitySelectDialog.fxml", "Quality hinzufügen");
      QualitySelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      ctrl.setFilter(filter);
      dialog.stage().showAndWait();

      Optional<QualitySelectResult> result = ctrl.getResult();
      if (result.isEmpty()) {
        return false;
      }
      QualitySelectResult r = result.get();
      tryAddQuality(ch, r);
      rebuildQualitiesTab();
      updateBpDisplay();
      return r.addAnother();
    } catch (IOException e) {
      showDialogLoadError();
      return false;
    }
  }

  private void tryAddQuality(@NonNull Character ch, @NonNull QualitySelectResult r) {
    // Eine Gabe im Karrieremodus erwerben kostet die doppelten GP-Kosten in Karma.
    if (ch.isCreated() && r.data().type() == QualityType.POSITIVE && !r.gratis()) {
      int karmaCost = ch.getQualityKarmaCost(r.data());
      if (karmaChargeFailed(ch, "%s erwerben".formatted(r.data().name()), karmaCost)) {
        return;
      }
    }
    try {
      ch.addQuality(r.data(), r.gratis());
    } catch (IllegalArgumentException ex) {
      Notifications.create()
          .title("Limit erreicht")
          .text(ex.getMessage())
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
    }
  }

  private @NonNull MigPane buildSkillGrid() {
    // Wert-Spalte durch Skill.MAX_RATING = 6 einstellig, keine feste Ausnahme nötig. Kein eigenes
    // Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][center][center][center][center][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    return grid;
  }

  /**
   * Fügt eine Gruppen-Kopfzeile mit +/−-Buttons und Auskoppeln-Button ein.
   */
  private int addGroupHeaderRow(@NonNull MigPane grid, int row, @NonNull String groupName,
      boolean coupled, @NonNull Character character) {
    Label groupLabel = new Label(groupName);
    groupLabel.getStyleClass().add(coupled ? "skill-group-header-coupled" : "skill-group-header");
    preventShrink(groupLabel);
    grid.add(groupLabel, CELL_COL_0 + row);

    // GRW S. 109: Fertigkeitsgruppen dürfen während der Erstellung nicht aufgelöst werden. Eigene
    // Spalte statt Anhängen an groupLabel, damit der Button unabhängig von der Namenslänge über alle
    // Zeilen hinweg untereinander steht.
    if (coupled && character.isCreated()) {
      Button decoupleBtn = buildDecoupleButton(groupName, character);
      preventShrink(decoupleBtn);
      grid.add(decoupleBtn, CELL_COL_1 + row);
    }

    if (coupled) {
      Button minus = new Button("−");
      Label ratingLabel = new Label();
      Button plus = new Button("+");
      Label bpLabel = new Label();

      minus.getStyleClass().add(STYLE_ATTR_BTN);
      ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
      plus.getStyleClass().add(STYLE_ATTR_BTN);
      bpLabel.getStyleClass().add(STYLE_ATTR_BP);

      wireIncrementable(skillRefreshers, new RowControls(ratingLabel, bpLabel, minus, plus),
          character.isCreated(), new RowModel() {
            @Override
            public int value() {
              return character.getGroupRating(groupName);
            }

            @Override
            public int min() {
              return 0;
            }

            @Override
            public int max() {
              return Skill.MAX_RATING;
            }

            @Override
            public boolean canIncrement() {
              return character.canRaiseGroupAtCreation(groupName);
            }

            @Override
            public int bpStepCost() {
              return 10;
            }

            @Override
            public @NonNull String bpDisplayText() {
              int rating = character.getGroupRating(groupName);
              return rating == 0 ? "–" : (rating * 10) + " BP";
            }

            @Override
            public int karmaStepCost() {
              return character.getGroupKarmaCost(groupName);
            }

            @Override
            public void increment() {
              character.raiseGroup(groupName);
            }

            @Override
            public void decrement() {
              character.lowerGroup(groupName);
            }

            @Override
            public @NonNull String actionLabel(int from, int to) {
              return groupName + " " + from + " → " + to;
            }
          }, character);

      preventShrink(minus);
      preventShrink(ratingLabel);
      preventShrink(plus);
      preventShrink(bpLabel);
      grid.add(minus, CELL_COL_2 + row);
      grid.add(ratingLabel, CELL_COL_3 + row);
      grid.add(plus, CELL_COL_4 + row);
      grid.add(bpLabel, CELL_COL_5 + row);
    }

    return row + 1;
  }

  private @NonNull Button buildDecoupleButton(@NonNull String groupName,
      @NonNull Character character) {
    Button decoupleBtn = new Button("Auskoppeln");
    decoupleBtn.getStyleClass().add("btn-decouple");
    // UNLINK statt LINK_SLASH: die gepinnte ikonli-fontawesome6-pack-Version 12.4.0 führt die
    // FA6-Icons noch unter ihren alten FA5-Namen, liefert für "unlink" aber denselben Glyph/Codepoint
    // (U+F127), den FontAwesome 6 unter "link-slash" listet.
    useIcon(decoupleBtn, FontAwesomeSolid.UNLINK);
    decoupleBtn.setOnAction(e -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      applyDialogStyle(confirm.getDialogPane());
      confirm.setTitle("Gruppe auskoppeln");
      confirm.setHeaderText("Skillgruppe \"" + groupName + "\" wirklich auskoppeln?");
      int rating = character.getGroupRating(groupName);
      String costInfo = rating > 0
          ? "\nAchtung: Die " + groupName + "-Skills werden ab jetzt einzeln bepreist "
          + "(je " + (rating * 4) + " BP statt " + (rating * 10) + " BP gesamt)."
          : "";
      confirm.setContentText(
          "Nach dem Auskoppeln können die Skills der Gruppe unabhängig voneinander "
              + "erhöht werden." + costInfo);
      confirm.showAndWait()
          .filter(btn -> btn == ButtonType.OK)
          .ifPresent(btn -> {
            character.decoupleGroup(groupName);
            rebuildSkillsTab();
          });
    });
    return decoupleBtn;
  }

  private void addSkillRow(@NonNull MigPane grid, int row, @NonNull Skill skill, boolean coupled,
      @NonNull Character character) {
    Label skillNameLabel = new Label(skill.getName());
    Label attrLabel = new Label(skill.getLinkedAttribute());
    Button minus = new Button("−");
    Label ratingLabel = new Label();
    Button plus = new Button("+");
    Label bpLabel = new Label();
    Label specLabel = new Label();
    Button specBtn = new Button("Spez.");

    skillNameLabel.getStyleClass().add(coupled ? "attr-name-coupled" : STYLE_ATTR_NAME);
    attrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);
    specLabel.getStyleClass().add(STYLE_ATTR_NAME);
    specBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

    boolean career = character.isCreated();

    Runnable specRefresh = () -> {
      String spec = skill.getSpecialization();
      specLabel.setText(spec != null ? spec : "–");
      boolean eligible = character.canSpecializeSkill(skill);
      boolean affordable = career
          ? character.getKarmaTotal() >= character.getSpecializationKarmaCost()
          : spec != null || character.getRemainingBP() >= character.getActiveSkillSpecializationBpCost();
      specBtn.setDisable(!eligible || !affordable);
    };
    skillRefreshers.add(specRefresh);
    specRefresh.run();
    specBtn.setOnAction(e -> onEditSkillSpecialization(skill, character));

    if (coupled) {
      if (career) {
        minus.setVisible(false);
      }
      Runnable refresh = () -> {
        ratingLabel.setText(String.valueOf(skill.getRating()));
        bpLabel.setText("");
        minus.setDisable(true);
        plus.setDisable(true);
      };
      skillRefreshers.add(refresh);
      refresh.run();
    } else {
      wireIncrementable(skillRefreshers, new RowControls(ratingLabel, bpLabel, minus, plus),
          career, new RowModel() {
            @Override
            public int value() {
              return skill.getRating();
            }

            @Override
            public int min() {
              return 0;
            }

            @Override
            public int max() {
              return Skill.MAX_RATING;
            }

            @Override
            public boolean canIncrement() {
              return character.canRaiseSkill(skill);
            }

            @Override
            public int bpStepCost() {
              return skill.getBpCostPerRating(character.isSkillCostDoubled(skill.getCategory()));
            }

            @Override
            public @NonNull String bpDisplayText() {
              boolean doubled = character.isSkillCostDoubled(skill.getCategory());
              return skill.getRating() == 0 ? "–" : skill.getBpCost(doubled) + " BP";
            }

            @Override
            public int karmaStepCost() {
              return skill.getKarmaCost(character.isSkillCostDoubled(skill.getCategory()));
            }

            @Override
            public void increment() {
              skill.setRating(skill.getRating() + 1);
            }

            @Override
            public void decrement() {
              skill.setRating(skill.getRating() - 1);
            }

            @Override
            public @NonNull String actionLabel(int from, int to) {
              return skill.getName() + " " + from + " → " + to;
            }
          }, character);
    }

    addRowCells(grid, row, skillNameLabel, attrLabel, minus, ratingLabel, plus, specLabel, specBtn,
        bpLabel);
  }

  // ── Kontakte-Tab ──────────────────────────────────────────

  private void buildContactsTab() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    contactsContainer.getChildren().clear();
    contactRefreshers.clear();

    Label sectionLabel = new Label("Kontakte");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(0, 0, 4, 0));

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddContact());

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(sectionLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    header.getChildren().add(addBtn);
    contactsContainer.getChildren().add(header);

    MigPane grid = buildContactGrid();
    contactsContainer.getChildren().add(withRightFiller(grid));

    int row = 1;
    for (Contact contact : ch.getContacts()) {
      addContactRow(grid, row++, contact, ch);
    }
  }

  /**
   * Baut das Kontakte-Grid inkl. Kopfzeile (Zeile 0) mit "Verbindung"/"Loyalität"-Spaltenüberschriften
   * über den jeweiligen Wert-Spalten — ersetzt die im alten WinForms-UI pro Zeile wiederholten
   * "Connection:"/"Loyalty:"-Inline-Labels (dort nötig, da jede Kontakt-Zeile aus einer eigenständigen
   * UserControl-Instanz bestand) durch eine einzelne gemeinsame Kopfzeile, analog zum bestehenden
   * Attribute-/Zauber-/Komplexe-Formen-Grid-Muster.
   */
  private @NonNull MigPane buildContactGrid() {
    // Beide Wert-Spalten (Verbindung/Loyalität) durch Contact.MAX_RATING = 6 einstellig, keine feste
    // Ausnahme nötig. Kein eigenes Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller
    // verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][][center][center][center][][center][center][center][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);

    String[] headers = {"", "", "", "Verbindung", "", "", "", "Loyalität", "", "", ""};
    addHeaderRow(grid, headers);

    return grid;
  }

  private void addContactRow(@NonNull MigPane grid, int row, @NonNull Contact contact,
      @NonNull Character character) {
    Label nameLabelDummy = new Label(contact.getName());
    Button editBtn = new Button("Bearbeiten");
    Button connectionMinus = new Button("−");
    Label connectionValue = new Label();
    Button connectionPlus = new Button("+");
    Label connectionCost = new Label();
    Button loyaltyMinus = new Button("−");
    Label loyaltyValue = new Label();
    Button loyaltyPlus = new Button("+");
    Label loyaltyCost = new Label();
    Button removeBtn = new Button("Entfernen");

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    editBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    connectionMinus.getStyleClass().add(STYLE_ATTR_BTN);
    connectionValue.getStyleClass().add(STYLE_ATTR_VALUE);
    connectionPlus.getStyleClass().add(STYLE_ATTR_BTN);
    connectionCost.getStyleClass().add(STYLE_ATTR_BP);
    loyaltyMinus.getStyleClass().add(STYLE_ATTR_BTN);
    loyaltyValue.getStyleClass().add(STYLE_ATTR_VALUE);
    loyaltyPlus.getStyleClass().add(STYLE_ATTR_BTN);
    loyaltyCost.getStyleClass().add(STYLE_ATTR_BP);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

    Runnable tooltipRefresh = () -> {
      String description = contact.getDescription();
      Tooltip.install(nameLabelDummy, description != null ? new Tooltip(description) : null);
    };
    contactRefreshers.add(tooltipRefresh);
    tooltipRefresh.run();

    editBtn.setOnAction(e -> onEditContact(contact));

    boolean career = character.isCreated();

    RowModel connectionModel = new RowModel() {
      @Override
      public int value() {
        return contact.getConnection();
      }

      @Override
      public int min() {
        return Contact.MIN_RATING;
      }

      @Override
      public int max() {
        return Contact.MAX_RATING;
      }

      @Override
      public int bpStepCost() {
        return 1;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return contact.getConnection() >= Contact.MAX_RATING ? "–" : "1 BP";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        contact.setConnection(contact.getConnection() + 1);
      }

      @Override
      public void decrement() {
        contact.setConnection(contact.getConnection() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return contact.getName() + " Connection " + from + " → " + to;
      }
    };

    RowModel loyaltyModel = new RowModel() {
      @Override
      public int value() {
        return contact.getLoyalty();
      }

      @Override
      public int min() {
        return Contact.MIN_RATING;
      }

      @Override
      public int max() {
        return Contact.MAX_RATING;
      }

      @Override
      public int bpStepCost() {
        return 1;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return contact.getLoyalty() >= Contact.MAX_RATING ? "–" : "1 BP";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        contact.setLoyalty(contact.getLoyalty() + 1);
      }

      @Override
      public void decrement() {
        contact.setLoyalty(contact.getLoyalty() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return contact.getName() + " Loyalty " + from + " → " + to;
      }
    };

    if (career) {
      wireFreeIncrementable(contactRefreshers,
          new RowControls(connectionValue, connectionCost, connectionMinus, connectionPlus),
          connectionModel);
      wireFreeIncrementable(contactRefreshers,
          new RowControls(loyaltyValue, loyaltyCost, loyaltyMinus, loyaltyPlus), loyaltyModel);
    } else {
      wireIncrementable(contactRefreshers,
          new RowControls(connectionValue, connectionCost, connectionMinus, connectionPlus), false,
          connectionModel, character);
      wireIncrementable(contactRefreshers,
          new RowControls(loyaltyValue, loyaltyCost, loyaltyMinus, loyaltyPlus), false, loyaltyModel,
          character);
    }

    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(e -> {
      character.removeContact(contact);
      buildContactsTab();
      updateBpDisplay();
      refreshAllRows();
    });

    addRowCells(grid, row, nameLabelDummy, editBtn, connectionMinus, connectionValue, connectionPlus,
        connectionCost, loyaltyMinus, loyaltyValue, loyaltyPlus, loyaltyCost, removeBtn);
  }

  private void onAddContact() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = openDialog("ContactEditDialog.fxml", "Kontakt hinzufügen");
      ContactEditDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        ch.addContact(r.name(), r.description());
        buildContactsTab();
        updateBpDisplay();
        refreshAllRows();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  private void onEditContact(@NonNull Contact contact) {
    try {
      DialogHandle dialog = openDialog("ContactEditDialog.fxml", "Kontakt bearbeiten");
      ContactEditDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.prefill(contact);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        contact.setName(r.name());
        contact.setDescription(r.description());
        buildContactsTab();
        refreshAllRows();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  // ── Magie-Tab ─────────────────────────────────────────────

  private void buildMagicTab() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    magicContainer.getChildren().clear();

    Label sectionLabel = new Label("Magie");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    ComboBox<Tradition> traditionBox = new ComboBox<>();
    traditionBox.getItems().setAll(Tradition.values());
    traditionBox.setConverter(new TraditionStringConverter());
    traditionBox.setValue(ch.getTradition());
    traditionBox.valueProperty().addListener((obs, old, val) -> {
      if (val != null) {
        ch.setTradition(val);
      }
    });

    Button addBtn = new Button("Zauber hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddSpell());

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().addAll(sectionLabel, traditionBox);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    header.getChildren().add(addBtn);
    magicContainer.getChildren().add(header);

    MigPane grid = buildSpellGrid();
    magicContainer.getChildren().add(withRightFiller(grid));

    int row = 1;
    for (SpellData spell : ch.getSpells()) {
      addSpellRow(grid, row++, spell, ch);
    }
  }

  /**
   * Baut das Zauber-Grid inkl. Kopfzeile (Zeile 0). Alle Datenspalten bleiben bei ihrer Inhaltsbreite
   * (feststehendes Vokabular aus den Zauber-Stammdaten, kein eigenes Wachstum nötig) — ungenutzter
   * Platz wird stattdessen außen über {@link GridSupport#withRightFiller} verteilt. Das Grid wird bei jeder Änderung
   * komplett neu aufgebaut (kein In-Place-Refresh einzelner Zellen), daher gibt es hier keine
   * Wert-Spalten-Ausnahme wie bei Attributen.
   */
  private @NonNull MigPane buildSpellGrid() {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][center][][][center][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));

    String[] headers = {"Zauber", "Kategorie", "Typ", "Reichweite", "Schaden", "Dauer", "Entzug",
        "Quelle", ""};
    addHeaderRow(grid, headers);

    return grid;
  }

  private void addSpellRow(@NonNull MigPane grid, int row, @NonNull SpellData spell,
      @NonNull Character character) {
    Label spellNameLabel = new Label(spell.name());
    Label categoryLabel = new Label(spell.category());
    Label typeLabel = new Label(spell.type());
    Label rangeLabel = new Label(spell.range());
    Label damageLabel = new Label(spell.damage());
    Label durationLabel = new Label(spell.duration());
    Label dvLabel = new Label(spell.dv());
    Label sourceLabel = new Label(spell.source());
    Button removeBtn = new Button("Entfernen");

    spellNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    removeBtn.setOnAction(e -> {
      character.removeSpell(spell.name());
      buildMagicTab();
      updateBpDisplay();
    });

    addRowCells(grid, row, spellNameLabel, categoryLabel, typeLabel, rangeLabel, damageLabel,
        durationLabel, dvLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Zauber-Auswahl-Dialog und fügt den gewählten Zauber hinzu. Im Erstellungsmodus greift
   * das Cap aus {@link Character#canAddSpellAtCreation()}; im Karrieremodus kostet jeder neue Zauber
   * {@link Character#getSpellKarmaCost()} Karma (Bestätigungsdialog analog anderen
   * Karrieremodus-Käufen, siehe {@link #karmaChargeFailed}).
   */
  private void onAddSpell() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    if (!ch.isCreated() && !ch.canAddSpellAtCreation()) {
      Notifications.create()
          .title("Zauberlimit erreicht")
          .text("Kein weiterer Zauber ohne höheres Spruchzauberei-/Ritualzauberei-Rating möglich")
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
      return;
    }
    try {
      DialogHandle dialog = openDialog("SpellSelectDialog.fxml", "Zauber hinzufügen");
      SpellSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(spellData -> {
        if (ch.isCreated()) {
          String label = "Zauber gelernt: " + spellData.name();
          if (karmaChargeFailed(ch, label, ch.getSpellKarmaCost())) {
            return;
          }
        }
        ch.addSpell(spellData);
        buildMagicTab();
        updateBpDisplay();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  // ── Technomancer-Tab ───────────────────────────────────────

  private void buildTechnomancerTab() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    technomancerContainer.getChildren().clear();

    Label sectionLabel = new Label("Technomancer");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    // Fester Anzeigetext statt Auswahl-Steuerelement: das SR4-Grundregelwerk kennt kein "Stream"-
    // Auswahlkonzept (0 Treffer im GRW-Kern-Volltext), die Schwund-Widerstandsformel gilt
    // bedingungslos für jeden Technomancer (siehe docs/plaene/technomancer_plan.md Abschnitt 13).
    Label fadingLabel = new Label("Schwund-Widerstand: Willenskraft + Resonanz");
    fadingLabel.getStyleClass().add(STYLE_ATTR_NAME);

    // Bewusst kein addBtn.setDisable() anhand von canAddComplexForm() an dieser Stelle: der
    // Tab-Inhalt wird nur bei setCharacter()/updateModeDisplay() neu gebaut, nicht bei jeder
    // Quality-Änderung (nur die Tab-Sichtbarkeit wird dort synchronisiert, siehe
    // syncTechnomancerTabVisibility()) — ein hier gesetzter Disabled-Zustand würde nach Erwerb der
    // Quality "Technomant" eingefroren bleiben. Die Prüfung erfolgt stattdessen live beim Klick in
    // onAddComplexForm() (analog dem Zauber-Button in buildMagicTab()).
    Button addBtn = new Button("Komplexe Form hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddComplexForm());

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().addAll(sectionLabel, fadingLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    header.getChildren().add(addBtn);
    technomancerContainer.getChildren().add(header);

    MigPane grid = buildComplexFormGrid();
    technomancerContainer.getChildren().add(withRightFiller(grid));

    int row = 1;
    for (ComplexForm complexForm : ch.getComplexForms()) {
      addComplexFormRow(grid, row++, complexForm, ch);
    }
  }

  /**
   * Baut das Komplexe-Formen-Grid inkl. Kopfzeile (Zeile 0) — Spaltenlayout analog {@link
   * #buildSkillGrid()}, aber ohne Spezialisierungs-Spalten (Komplexe Formen kennen keine
   * Spezialisierung).
   */
  private @NonNull MigPane buildComplexFormGrid() {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][center][][center][center][center][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));

    String[] headers = {"Komplexe Form", "Kategorie", "Fertigkeit", "", "Stufe", "", "Kosten",
        "Quelle", ""};
    addHeaderRow(grid, headers);

    return grid;
  }

  private void addComplexFormRow(@NonNull MigPane grid, int row,
      @NonNull ComplexForm complexForm, @NonNull Character character) {
    Label nameLabel = new Label(complexForm.getName());
    Label categoryLabel = new Label(complexForm.getData().category());
    Label skillLabel = new Label(complexForm.getData().skill());
    Button minus = new Button("−");
    Label ratingLabel = new Label();
    Button plus = new Button("+");
    Label costLabel = new Label();
    Label sourceLabel = new Label(complexForm.getData().source());
    Button removeBtn = new Button("Entfernen");

    nameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    skillLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    boolean career = character.isCreated();

    wireIncrementable(skillRefreshers, new RowControls(ratingLabel, costLabel, minus, plus),
        career, new RowModel() {
          @Override
          public int value() {
            return complexForm.getRating();
          }

          @Override
          public int min() {
            return ComplexForm.MIN_RATING;
          }

          @Override
          public int max() {
            return character.hasResonance()
                ? character.getEffectiveMagicOrResonanceValue()
                : ComplexForm.MIN_RATING;
          }

          @Override
          public boolean canIncrement() {
            return character.canRaiseComplexForm(complexForm);
          }

          @Override
          public int bpStepCost() {
            return complexForm.getBpCostPerRating();
          }

          @Override
          public @NonNull String bpDisplayText() {
            return (complexForm.getRating() * complexForm.getBpCostPerRating()) + " BP";
          }

          @Override
          public int karmaStepCost() {
            return complexForm.getKarmaCost(complexForm.getRating());
          }

          @Override
          public void increment() {
            complexForm.setRating(complexForm.getRating() + 1);
          }

          @Override
          public void decrement() {
            complexForm.setRating(complexForm.getRating() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return complexForm.getName() + " " + from + " → " + to;
          }
        }, character);

    removeBtn.setOnAction(e -> {
      character.removeComplexForm(complexForm.getName());
      buildTechnomancerTab();
      updateBpDisplay();
    });

    addRowCells(grid, row, nameLabel, categoryLabel, skillLabel, minus, ratingLabel, plus,
        costLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Komplexe-Formen-Auswahl-Dialog und lernt die gewählte Form mit Stufe 1 (SR4-
   * Grundregelwerk: "lernt die neue Komplexe Form mit Stufe 1"). Im Erstellungsmodus greift {@link
   * Character#canAddComplexForm()}; im Karrieremodus kostet der Erwerb {@link
   * ComplexForm#getKarmaCost(int)} mit {@code currentRating=0}, analog anderen
   * Karrieremodus-Käufen.
   */
  private void onAddComplexForm() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    if (!ch.canAddComplexForm()) {
      Notifications.create()
          .title("Keine Resonanz")
          .text("Ohne Resonanzwert kann keine Komplexe Form gelernt werden")
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
      return;
    }
    try {
      DialogHandle dialog = openDialog("ComplexFormSelectDialog.fxml", "Komplexe Form hinzufügen");
      ComplexFormSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(data -> {
        if (ch.isCreated()) {
          String label = "Komplexe Form gelernt: " + data.name();
          int karmaCost = ComplexForm.KARMA_NEW;
          if (karmaChargeFailed(ch, label, karmaCost)) {
            return;
          }
        }
        ch.addComplexForm(data);
        buildTechnomancerTab();
        updateBpDisplay();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  // ── Cyberware-Tab ──────────────────────────────────────────

  /**
   * Baut den (immer sichtbaren) Cyberware-&-Bioware-Tab: Kopfzeile mit Essenz-Übersicht (Gesamt-Essenz
   * sowie getrennte Cyberware-/Bioware-Verluste, wie vom GRW zum Notieren empfohlen) und zwei Sektionen
   * mit je eigenem Hinzufügen-Button — analog {@link #buildQualitiesTab()}. Wird bei jeder
   * Implantat-Änderung komplett neu aufgebaut (kein In-Place-Refresh einzelner Zellen).
   */
  private void buildCyberwareTab() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    cyberwareContainer.getChildren().clear();

    Label sectionLabel = new Label("Cyberware & Bioware");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    Label essenceLabel = new Label("Essenz: " + ESSENCE_FORMAT.toString(ch.getEssence()));
    essenceLabel.getStyleClass().add(STYLE_BP_VALUE);
    Label lossLabel = new Label("(Cyberware " + ESSENCE_FORMAT.toString(ch.getCyberwareEssenceLoss())
        + " / Bioware " + ESSENCE_FORMAT.toString(ch.getBiowareEssenceLoss()) + ")");
    lossLabel.getStyleClass().add(STYLE_BP_LABEL);
    Label costLabel = new Label("Kosten: " + ch.getCyberwareNuyenCost() + NUYEN_SUFFIX);
    costLabel.getStyleClass().add(STYLE_BP_LABEL);

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().addAll(sectionLabel, essenceLabel, lossLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(spacer, costLabel);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    cyberwareContainer.getChildren().add(header);

    buildCyberwareSection(ch, EquipmentKind.CYBERWARE);

    Separator sep = new Separator();
    VBox.setMargin(sep, new Insets(16, 0, 16, 0));
    cyberwareContainer.getChildren().add(sep);

    buildCyberwareSection(ch, EquipmentKind.BIOWARE);
  }

  private void buildCyberwareSection(@NonNull Character ch, @NonNull EquipmentKind kind) {
    Label title = new Label(kind.getDisplayName());
    title.getStyleClass().add(STYLE_SECTION_TITLE);

    Button addBtn = new Button(kind.getDisplayName() + " hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddCyberware(kind));

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(title, spacer, addBtn);
    cyberwareContainer.getChildren().add(header);

    List<Cyberware> implants = ch.getCyberware().stream()
        .filter(c -> c.getKind() == kind)
        .toList();
    if (implants.isEmpty()) {
      Label empty = new Label("Keine " + kind.getDisplayName() + " eingebaut");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      cyberwareContainer.getChildren().add(empty);
      return;
    }

    MigPane grid = buildCyberwareGrid();
    int row = 1;
    for (Cyberware implant : implants) {
      addCyberwareRow(grid, row++, implant, ch);
    }
    cyberwareContainer.getChildren().add(withRightFiller(grid));
  }

  /** Implantat-Grid inkl. Kopfzeile (Zeile 0), inhaltsbasierte Spalten analog {@link #buildSpellGrid()}. */
  private @NonNull MigPane buildCyberwareGrid() {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][][center][right][right][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));

    String[] headers = {"Implantat", "Bereich", "Kategorie", "Stufe", "Essenz", "Preis", "Quelle",
        ""};
    addHeaderRow(grid, headers);
    return grid;
  }

  private void addCyberwareRow(@NonNull MigPane grid, int row, @NonNull Cyberware implant,
      @NonNull Character character) {
    Label implantNameLabel = new Label(implant.getName());
    Label categoryLabel = new Label(implant.getData().category());
    Label gradeLabel = new Label(implant.getGrade().getDisplayName());
    Label ratingLabel = new Label(implant.getData().hasRating()
        ? String.valueOf(implant.getRating())
        : "–");
    Label essenceLabel = new Label(ESSENCE_FORMAT.toString(implant.getEssenceCost().doubleValue()));
    Label priceLabel = new Label(implant.getNuyenCost() + NUYEN_SUFFIX);
    Label sourceLabel = new Label(implant.getData().source() + " S. " + implant.getData().page());
    Button removeBtn = new Button("Entfernen");

    implantNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    gradeLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    essenceLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    priceLabel.getStyleClass().add(STYLE_ATTR_BP);
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    removeBtn.setOnAction(e -> {
      character.removeCyberware(implant);
      onCyberwareChanged();
    });

    addRowCells(grid, row, implantNameLabel, categoryLabel, gradeLabel, ratingLabel, essenceLabel,
        priceLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Implantat-Auswahl-Dialog für Cyberware bzw. Bioware und baut die Auswahl ein. Fällt die
   * Essenz während der Charaktererschaffung dadurch auf 0 oder darunter, erscheint nur eine Warnung
   * (keine Sperre, siehe {@link Character#isEssenceBelowCreationMinimum()}). Bei der Erschaffung zählt
   * der Preis gegen das Ressourcengeld ({@link Character#getRemainingNuyen()}), im Karrieremodus wird
   * er nach Bestätigung vom Nuyen-Guthaben abgebucht ({@link #nuyenChargeFailed}).
   */
  private void onAddCyberware(@NonNull EquipmentKind kind) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = openDialog("CyberwareSelectDialog.fxml",
          kind.getDisplayName() + " hinzufügen");
      CyberwareSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setKind(kind);
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(implant -> {
        if (ch.isCreated()
            && nuyenChargeFailed(ch, "Kauf: " + cyberwareLabel(implant), implant.getNuyenCost())) {
          return;
        }
        ch.addCyberware(implant);
        onCyberwareChanged();
        if (ch.isEssenceBelowCreationMinimum()) {
          Notifications.create()
              .title("Essenz zu niedrig")
              .text("Bei der Charaktererschaffung darf die Essenz nicht auf 0 oder darunter fallen")
              .position(Pos.BOTTOM_RIGHT)
              .owner(nameLabel.getScene().getWindow())
              .showWarning();
        }
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  /**
   * Zieht alle essenzabhängigen Anzeigen nach einer Implantat-Änderung nach: den Cyberware-Tab selbst,
   * das Attribut-Grid (Essenz-Zeile, wirksame Magie/Resonanz inkl. Maximum im Bereichs-Label) und die
   * übrigen Zeilen (z.B. Komplexe Formen, deren Maximum am wirksamen Resonanzwert hängt).
   */
  private void onCyberwareChanged() {
    buildCyberwareTab();
    buildAttributeGrid();
    updateNuyenBpDisplay();
    refreshAllRows();
  }

  /** Bezeichnung eines Implantats für Log-Einträge, z.B. "Dermalpanzerung (Stufe 2, Alphaware)". */
  private static @NonNull String cyberwareLabel(@NonNull Cyberware implant) {
    String details = implant.getGrade().getDisplayName();
    if (implant.getData().hasRating()) {
      details = "Stufe " + implant.getRating() + ", " + details;
    }
    return implant.getName() + " (" + details + ")";
  }

  // ── Straßenausrüstung-Tab (Lebensstil, Rüstung) ───────────

  /** Baut den Straßenausrüstung-Tab über {@link StreetGearTabBuilder} neu auf. */
  private void buildStreetGearTabs() {
    if (character == null) {
      return;
    }
    streetGear.build(character);
  }

  /** Verbindet den {@link StreetGearTabBuilder} mit den Anzeigen und Diensten dieses Controllers. */
  private @NonNull StreetGearHost streetGearHost() {
    return new StreetGearHost() {
      @Override
      public @NonNull DialogHandle openDialog(@NonNull String fxmlName, @NonNull String title)
          throws IOException {
        return CharacterViewController.this.openDialog(fxmlName, title);
      }

      @Override
      public void showDialogLoadError() {
        CharacterViewController.this.showDialogLoadError();
      }

      @Override
      public boolean nuyenChargeFailed(@NonNull Character c, @NonNull String label, int cost) {
        return CharacterViewController.this.nuyenChargeFailed(c, label, cost);
      }

      @Override
      public void onNuyenSpentChanged() {
        updateNuyenBpDisplay();
      }

      @Override
      public void onArmorChanged() {
        buildAttributeGrid();
        updateNuyenBpDisplay();
        refreshAllRows();
      }
    };
  }

  // ── Gemeinsam ─────────────────────────────────────────────

  /**
   * Lädt ein Dialog-FXML und richtet die zugehörige modale Stage (Titel, Modality, Stylesheet) ein. Der Aufrufer holt sich den Controller über
   * {@code dialog.loader().getController()}, verdrahtet ihn und ruft {@code dialog.stage().showAndWait()} auf.
   */
  private @NonNull DialogHandle openDialog(@NonNull String fxmlName, @NonNull String title)
      throws IOException {
    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource(fxmlName)));
    Parent root = loader.load();

    Stage dialogStage = new Stage();
    dialogStage.setTitle(title);
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(nameLabel.getScene().getWindow());
    dialogStage.setResizable(false);
    Scene scene = new Scene(root);
    scene.getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
    dialogStage.setScene(scene);

    return new DialogHandle(loader, dialogStage);
  }

  /**
   * Hängt das App-Stylesheet an einen {@link Alert} oder {@link TextInputDialog} — deren
   * {@code DialogPane} bekommt sonst nie {@code style.css} zugewiesen und rendert rein mit dem
   * JavaFX-Standard-Theme "Modena", was neben dem restlichen dunklen UI wie ein Fremdkörper wirkt.
   */
  private void applyDialogStyle(@NonNull DialogPane pane) {
    pane.getStylesheets()
        .add(Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm());
  }

  private void showDialogLoadError() {
    Notifications.create()
        .title("Fehler")
        .text("Dialog konnte nicht geöffnet werden")
        .position(Pos.BOTTOM_RIGHT)
        .owner(nameLabel.getScene().getWindow())
        .showError();
  }

  /**
   * Verdrahtet eine +/−-Zeile (Refresh-Runnable, Klick-Handler, BP- bzw. Karma-Kostenprüfung inkl. Bestätigungsdialog im Karrieremodus) für
   * {@code addAttributeRow}, {@code addGroupHeaderRow} und {@code addSkillRow} einheitlich. Das Ausblenden von {@code minus} im Karrieremodus
   * verlässt sich auf {@code hidemode 3} des umgebenden {@link MigPane} — ein zusätzliches {@code setManaged(false)} ist dafür nicht nötig.
   */
  private void wireIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, boolean career, @NonNull RowModel model,
      @NonNull Character character) {
    if (career) {
      wireCareerIncrementable(refresherList, controls, model, character);
    } else {
      wireCreationIncrementable(refresherList, controls, model, character);
    }
  }

  private void wireCareerIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model, @NonNull Character character) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    minus.setVisible(false);
    Runnable refresh = () -> {
      int val = model.value();
      int karmaCost = model.karmaStepCost();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(val >= model.max() ? "–" : karmaCost + KARMA_SUFFIX);
      plus.setDisable(val >= model.max() || character.getKarmaTotal() < karmaCost
          || !model.canIncrement());
    };
    refresherList.add(refresh);

    plus.setOnAction(e -> {
      int val = model.value();
      int karmaCost = model.karmaStepCost();
      String label = model.actionLabel(val, val + 1);
      if (val < model.max() && character.getKarmaTotal() >= karmaCost && model.canIncrement()
          && confirmKarmaSpend(label, karmaCost, character)) {
        character.addKarmaEntry(
            new LogEntry(label, -karmaCost, LocalDate.now(ZoneId.systemDefault())));
        model.increment();
        refreshAllRows();
        updateLogDisplay();
      }
    });

    refresh.run();
  }

  private void wireCreationIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model, @NonNull Character character) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    Runnable refresh = () -> {
      int val = model.value();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(model.bpDisplayText());
      minus.setDisable(val <= model.min());
      plus.setDisable(val >= model.max() || character.getRemainingBP() < model.bpStepCost()
          || !model.canIncrement());
    };
    refresherList.add(refresh);

    minus.setOnAction(e -> {
      if (model.value() > model.min()) {
        model.decrement();
        refreshAllRows();
        updateBpDisplay();
      }
    });
    plus.setOnAction(e -> {
      if (model.value() < model.max() && character.getRemainingBP() >= model.bpStepCost()
          && model.canIncrement()) {
        model.increment();
        refreshAllRows();
        updateBpDisplay();
      }
    });

    refresh.run();
  }

  /**
   * Verdrahtet eine +/−-Zeile ohne jegliche BP-/Karma-Kosten und ohne Bestätigung — für Werte, die in beiden Richtungen jederzeit frei änderbar
   * bleiben sollen. Aktuell nur für Kontakt-Ratings im Karrieremodus genutzt (siehe {@code docs/kontakte_plan.md}: das SR4-Grundregelwerk kennt
   * keinen Karma-Kauf für Connections). Bewusst getrennt von {@link #wireCareerIncrementable}, das strukturell einen Karma-Preis voraussetzt und das
   * Minus-Control ausblendet — beides passt hier nicht.
   */
  private void wireFreeIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    Runnable refresh = () -> {
      int val = model.value();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText("");
      minus.setDisable(val <= model.min());
      plus.setDisable(val >= model.max());
    };
    refresherList.add(refresh);

    minus.setOnAction(e -> {
      if (model.value() > model.min()) {
        model.decrement();
        refreshAllRows();
      }
    });
    plus.setOnAction(e -> {
      if (model.value() < model.max()) {
        model.increment();
        refreshAllRows();
      }
    });

    refresh.run();
  }

  private boolean confirmKarmaSpend(@NonNull String label, int karmaCost,
      @NonNull Character character) {
    return confirmKarmaSpend(label, karmaCost, character, null);
  }

  /**
   * Wie {@link #confirmKarmaSpend(String, int, Character)}, aber mit einem optionalen Zusatzhinweis (z.B. Gruppenauflösungs-Warnung), der vor dem
   * eigentlichen Kostentext eingeblendet wird — ein einziger kombinierter Dialog statt zweier verschachtelter Bestätigungen.
   */
  private boolean confirmKarmaSpend(@NonNull String label, int karmaCost,
      @NonNull Character character, @Nullable String extraWarning) {
    return confirmSpend(SpendCurrency.KARMA, label, karmaCost, character.getKarmaTotal(),
        extraWarning);
  }

  /** Bestätigungsdialog für einen Karrieremodus-Kauf; {@code extraWarning} wird dem Kostentext vorangestellt. */
  private boolean confirmSpend(@NonNull SpendCurrency currency, @NonNull String label, int cost,
      int available, @Nullable String extraWarning) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    applyDialogStyle(confirm.getDialogPane());
    confirm.setTitle(currency.getDisplayName() + " ausgeben");
    confirm.setHeaderText(label);
    String content = currency.formatAmount(cost) + " ausgeben?\n\n"
        + currency.formatRemaining(available - cost);
    if (extraWarning != null) {
      content = extraWarning + "\n\n" + content;
    }
    confirm.setContentText(content);
    confirm.initOwner(nameLabel.getScene().getWindow());
    return confirm.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
  }

  /**
   * Öffnet den Freitext-Dialog zum Setzen/Ändern der Spezialisierung einer aktiven Fertigkeit (analog {@link #buildNativeLanguageRow}). Im
   * Erstellungsmodus wird direkt gesetzt (kostet nur beim erstmaligen Setzen den Fixpreis aus {@link Character#getActiveSkillSpecializationBpCost()},
   * ein späteres Umbenennen ist kostenlos). Im Karrieremodus kostet jedes Setzen/Ändern erneut Karma (SR4-Grundregelwerk S. 312); ist die Fertigkeit
   * noch Teil einer gekoppelten Fertigkeitsgruppe, wird das Auflösen der Gruppe im selben Bestätigungsdialog mit angekündigt.
   */
  private void onEditSkillSpecialization(@NonNull Skill skill, @NonNull Character character) {
    if (!character.canSpecializeSkill(skill)) {
      return;
    }
    TextInputDialog dialog = new TextInputDialog(
        skill.getSpecialization() != null ? skill.getSpecialization() : "");
    applyDialogStyle(dialog.getDialogPane());
    dialog.setTitle("Spezialisierung");
    dialog.setHeaderText(skill.getName() + " – Spezialisierung");
    dialog.setContentText("Spezialisierung:");
    dialog.showAndWait().map(String::trim)
        .ifPresent(value -> applySkillSpecialization(skill, character, value.isBlank() ? null : value));
  }

  private void applySkillSpecialization(@NonNull Skill skill, @NonNull Character character,
      @Nullable String newSpec) {
    if (character.isCreated()) {
      applySkillSpecializationInCareerMode(skill, character, newSpec);
    } else {
      applySkillSpecializationInCreationMode(skill, character, newSpec);
    }
  }

  /**
   * Karrieremodus-Zweig von {@link #applySkillSpecialization}: Karma-Bestätigung (kombiniert mit einem Gruppenauflösungs-Hinweis, falls die
   * Fertigkeit noch gekoppelt ist), dann Buchung.
   */
  private void applySkillSpecializationInCareerMode(@NonNull Skill skill,
      @NonNull Character character, @Nullable String newSpec) {
    int karmaCost = character.getSpecializationKarmaCost();
    if (character.getKarmaTotal() < karmaCost) {
      return;
    }
    String groupName = skill.getGroupName();
    String warning = groupName != null && character.isGroupCoupled(groupName)
        ? "Dies löst die Fertigkeitsgruppe \"" + groupName + "\" auf. Die Mitglieder "
        + "behalten ihr aktuelles Rating, können danach aber einzeln gesteigert werden."
        : null;
    String label = skill.getName() + ": Spezialisierung \"" + (newSpec == null ? "–" : newSpec) + "\"";
    if (confirmKarmaSpend(label, karmaCost, character, warning)) {
      character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now(ZoneId.systemDefault())));
      character.setSkillSpecialization(skill, newSpec);
      // rebuildSkillsTab() statt refreshAllRows(): eine Gruppenauflösung ändert, wie die
      // betroffenen Zeilen verdrahtet sind (gekoppelt = deaktivierte Rating-Buttons, entkoppelt =
      // einzeln steigerbar) — das lässt sich nicht über die reinen Refresh-Callbacks nachziehen,
      // die Zeilen müssen neu gebaut werden.
      rebuildSkillsTab();
      updateLogDisplay();
    }
  }

  /**
   * Erstellungsmodus-Zweig von {@link #applySkillSpecialization}: erstmaliges Setzen kostet den Fixpreis aus
   * {@link Character#getActiveSkillSpecializationBpCost()}, ein späteres Umbenennen oder Löschen ist kostenlos.
   */
  private void applySkillSpecializationInCreationMode(@NonNull Skill skill,
      @NonNull Character character, @Nullable String newSpec) {
    boolean affordable = newSpec == null || skill.getSpecialization() != null
        || character.getRemainingBP() >= character.getActiveSkillSpecializationBpCost();
    if (!affordable) {
      return;
    }
    character.setSkillSpecialization(skill, newSpec);
    refreshAllRows();
    updateBpDisplay();
  }

  /**
   * Öffnet den Freitext-Dialog zum Setzen/Ändern der Spezialisierung einer Wissensfertigkeit. Im Erstellungsmodus zählt eine neue Spezialisierung als
   * +1 Punkt zum Wissensfertigkeitspunkte- Pool ({@link Character#getKnowledgeSkillSpecializationBP()}); im Karrieremodus kostet jedes Setzen/Ändern
   * erneut Karma (SR4-Grundregelwerk S. 312). Wissensfertigkeiten haben keine Gruppen, daher entfällt der Gruppenauflösungs-Hinweis aus
   * {@link #onEditSkillSpecialization}.
   */
  private void onEditKnowledgeSkillSpecialization(@NonNull KnowledgeSkill skill,
      @NonNull Character character) {
    if (!character.canSpecializeKnowledgeSkill(skill)) {
      return;
    }
    TextInputDialog dialog = new TextInputDialog(
        skill.getSpecialization() != null ? skill.getSpecialization() : "");
    applyDialogStyle(dialog.getDialogPane());
    dialog.setTitle("Spezialisierung");
    dialog.setHeaderText(skill.getName() + " – Spezialisierung");
    dialog.setContentText("Spezialisierung:");
    dialog.showAndWait().map(String::trim)
        .ifPresent(value -> applyKnowledgeSkillSpecialization(skill, character,
            value.isBlank() ? null : value));
  }

  private void applyKnowledgeSkillSpecialization(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    if (character.isCreated()) {
      applyKnowledgeSkillSpecializationInCareerMode(skill, character, newSpec);
    } else {
      applyKnowledgeSkillSpecializationInCreationMode(skill, character, newSpec);
    }
  }

  private void applyKnowledgeSkillSpecializationInCareerMode(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    int karmaCost = character.getSpecializationKarmaCost();
    if (character.getKarmaTotal() < karmaCost) {
      return;
    }
    String label = skill.getName() + ": Spezialisierung \"" + (newSpec == null ? "–" : newSpec) + "\"";
    if (confirmKarmaSpend(label, karmaCost, character)) {
      character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now(ZoneId.systemDefault())));
      character.setKnowledgeSkillSpecialization(skill, newSpec);
      refreshAllRows();
      updateLogDisplay();
    }
  }

  /**
   * Erstellungsmodus-Zweig von {@link #applyKnowledgeSkillSpecialization}: erstmaliges Setzen kostet ggf. BP, falls es den
   * Wissensfertigkeitspunkte-Pool überschreitet (siehe {@link Character#getKnowledgeSkillSpecializationBP()}), ein späteres Umbenennen oder Löschen
   * ist kostenlos.
   */
  private void applyKnowledgeSkillSpecializationInCreationMode(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    boolean affordable = newSpec == null || skill.getSpecialization() != null
        || character.getRemainingBP() >= character.getKnowledgeSkillSpecializationBP();
    if (!affordable) {
      return;
    }
    character.setKnowledgeSkillSpecialization(skill, newSpec);
    refreshAllRows();
    updateBpDisplay();
  }

  private void refreshAllRows() {
    attrRefreshers.forEach(Runnable::run);
    specialAttrRefreshers.forEach(Runnable::run);
    skillRefreshers.forEach(Runnable::run);
    knowledgeSkillRefreshers.forEach(Runnable::run);
    qualityRefreshers.forEach(Runnable::run);
    contactRefreshers.forEach(Runnable::run);
    streetGear.refresh();
  }

  private void updateBpDisplay() {
    if (character == null) {
      return;
    }
    int remaining = character.getRemainingBP();
    remainingBpLabel.setText(String.valueOf(remaining));
    setWarnStyle(remainingBpLabel, remaining < 0);
  }

  private void updateNuyenBpDisplay() {
    if (character == null) {
      return;
    }
    int bp = character.getNuyenBP();
    nuyenBpValueLabel.setText(bp + " BP (" + character.getResourceNuyen() + NUYEN_SUFFIX + ")");
    int remaining = character.getRemainingNuyen();
    remainingNuyenLabel.setText(remaining + NUYEN_SUFFIX);
    setWarnStyle(remainingNuyenLabel, remaining < 0);
    nuyenBpMinusBtn.setDisable(bp <= 0);
    nuyenBpPlusBtn.setDisable(bp >= Character.MAX_NUYEN_BP || character.getRemainingBP() <= 0);
  }

  @FXML
  private void onNuyenBpMinus() {
    if (character == null) {
      return;
    }
    character.setNuyenBP(character.getNuyenBP() - 1);
    updateNuyenBpDisplay();
    updateBpDisplay();
    refreshAllRows();
  }

  @FXML
  private void onNuyenBpPlus() {
    if (character == null) {
      return;
    }
    character.setNuyenBP(character.getNuyenBP() + 1);
    updateNuyenBpDisplay();
    updateBpDisplay();
    refreshAllRows();
  }

  /**
   * Schließt die Charaktererschaffung ab. Hinderungsgründe aus {@link Character#getFinalizeProblems()}
   * (derzeit negatives Restgeld) brechen mit einer Warnung ab; sonst zeigt der Abschlussdialog den
   * Startkapital-Wurf nach Lebensstil (GRW S. 114), gebucht wird über {@link
   * Character#finalizeCreation(int)}.
   */
  @FXML
  private void onFinalize() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    List<String> problems = ch.getFinalizeProblems();
    if (!problems.isEmpty()) {
      Notifications.create()
          .title("Abschluss nicht möglich")
          .text(String.join("\n", problems))
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
      return;
    }
    try {
      DialogHandle dialog = openDialog("FinalizeDialog.fxml", "Charakter abschließen");
      FinalizeDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(diceSum -> {
        ch.finalizeCreation(diceSum);
        updateModeDisplay();
        updateLogDisplay();
      });
    } catch (IOException e) {
      showDialogLoadError();
    }
  }

  private void updateModeDisplay() {
    if (character == null) {
      return;
    }
    boolean career = character.isCreated();

    finalizeBtn.setVisible(!career);
    finalizeBtn.setManaged(!career);
    bpBox.setVisible(!career);
    bpBox.setManaged(!career);
    nuyenBpBox.setVisible(!career);
    nuyenBpBox.setManaged(!career);
    metatypeBox.setVisible(!career);
    metatypeBox.setManaged(!career);
    metatypeLabelCareer.setVisible(career);
    metatypeLabelCareer.setManaged(career);
    if (career) {
      metatypeLabelCareer.setText(character.getMetatype().name());
    }

    buildAttributeGrid();
    buildSkillsTab();
    buildQualitiesTab();
    buildContactsTab();
    buildBiographyTab();
    buildMagicTab();
    buildTechnomancerTab();
    buildCyberwareTab();
    buildStreetGearTabs();
  }

  @SuppressWarnings("unchecked")
  private void buildLogTable(@NonNull TableView<LogEntry> table) {
    TableColumn<LogEntry, String> dateCol = new TableColumn<>("Datum");
    dateCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
    dateCol.setPrefWidth(90);

    TableColumn<LogEntry, String> amountCol = new TableColumn<>("Menge");
    amountCol.setCellValueFactory(c -> {
      int a = c.getValue().amount();
      return new SimpleStringProperty(a > 0 ? "+" + a : String.valueOf(a));
    });
    amountCol.setPrefWidth(70);
    amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    TableColumn<LogEntry, String> descCol = new TableColumn<>("Bezeichnung");
    descCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().description()));

    table.getColumns().addAll(dateCol, amountCol, descCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPlaceholder(new Label("Keine Einträge"));
  }

  // ── Karrieremodus-Transition ──────────────────────────────

  private void updateLogDisplay() {
    if (character == null) {
      return;
    }

    karmaTable.getItems().setAll(character.getKarmaLog());
    nuyenTable.getItems().setAll(character.getNuyenLog());

    int karma = character.getKarmaTotal();
    karmaTotalLabel.setText(String.valueOf(karma));
    karmaStatusLabel.setText(String.valueOf(karma));

    int nuyen = character.getNuyenTotal();
    nuyenTotalLabel.setText(nuyen + " ¥");
    nuyenStatusLabel.setText(nuyen + " ¥");
  }

  @FXML
  private void onAddKarma() {
    if (character == null) {
      return;
    }
    Optional<LogEntry> result = openLogEntryDialog("Karma-Eintrag hinzufügen");
    if (result.isEmpty()) {
      return;
    }
    character.addKarmaEntry(result.get());
    updateLogDisplay();
    refreshAllRows();
  }

  // ── Log-Tab ───────────────────────────────────────────────

  @FXML
  private void onAddNuyen() {
    if (character == null) {
      return;
    }
    Optional<LogEntry> result = openLogEntryDialog("Nuyen-Eintrag hinzufügen");
    if (result.isEmpty()) {
      return;
    }
    character.addNuyenEntry(result.get());
    updateLogDisplay();
  }

  private @NonNull Optional<LogEntry> openLogEntryDialog(@NonNull String title) {
    try {
      DialogHandle dialog = openDialog("LogEntryDialog.fxml", title);
      LogEntryDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      dialog.stage().showAndWait();
      return ctrl.getResult();
    } catch (IOException e) {
      showDialogLoadError();
      return Optional.empty();
    }
  }

}