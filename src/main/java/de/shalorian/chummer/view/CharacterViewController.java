package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterAttribute;
import de.shalorian.chummer.model.character.CharacterQuality;
import de.shalorian.chummer.model.character.KnowledgeSkill;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.QualitySource;
import de.shalorian.chummer.model.character.QualityType;
import de.shalorian.chummer.model.character.Skill;
import de.shalorian.chummer.model.character.SkillCategory;
import de.shalorian.chummer.model.character.SpecialAttribute;
import de.shalorian.chummer.model.character.SpecialAttributeType;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
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
import org.tbee.javafx.scene.layout.MigPane;

public class CharacterViewController {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final String STYLE_ATTR_NAME = "attr-name";
  private static final String STYLE_ATTR_BTN = "attr-btn";
  private static final String STYLE_ATTR_VALUE = "attr-value";
  private static final String STYLE_ATTR_RANGE = "attr-range";
  private static final String STYLE_ATTR_BP = "attr-bp";
  private static final String STYLE_ATTR_ABBR = "attr-abbr";
  private static final String STYLE_SECTION_TITLE = "section-title";
  private static final String STYLE_BTN_SECONDARY = "btn-secondary";
  private static final String MIGPANE_HIDEMODE_3 = "hidemode 3";
  private static final String KARMA_SUFFIX = " Karma";
  private static final String CELL_COL_0 = "cell 0 ";
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
  private @Nullable Character character;

  @FXML
  public void initialize() {
    metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    metatypeBox.setConverter(new MetatypeStringConverter());
    metatypeBox.valueProperty().addListener((obs, old, newVal) -> {
      if (character == null || newVal == null || old == null) {
        return;
      }
      character.setMetatype(newVal);
      buildAttributeGrid();
      updateBpDisplay();
    });

    buildLogTable(karmaTable);
    buildLogTable(nuyenTable);
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
  }

  // ── Attribute-Tab ─────────────────────────────────────────

  private void buildAttributeGrid() {
    if (character == null) {
      return;
    }

    attributeGridHost.getChildren().clear();
    attrRefreshers.clear();

    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[170!][46!,center][30!,center][46!,center][30!,center][90!][70!]");

    boolean career = character.isCreated();
    String[] headers = {"Attribut", "", "", "Wert", "", "Bereich",
        career ? "Karma" : "BP-Kosten"};
    for (int i = 0; i < headers.length; i++) {
      if (!headers[i].isEmpty()) {
        Label h = new Label(headers[i]);
        h.getStyleClass().add("attr-header");
        grid.add(h, "cell " + i + " 0");
      }
    }

    int row = 1;
    for (AttributeType type : AttributeType.values()) {
      addAttributeRow(grid, row++, type, character.getAttribute(type), character);
    }

    attributeGridHost.getChildren().add(grid);

    buildSpecialAttributeSection();
  }

  /**
   * Baut die Sektion für die Sonderattribute Edge/Magic/Resonance unterhalb des
   * Standardattribut-Grids. Edge ist immer sichtbar; Magic bzw. Resonance werden nur gerendert,
   * wenn der Charakter eine entsprechende Quality besitzt (siehe {@link Character#hasMagic()}/
   * {@link Character#hasResonance()}) — analog zum Verbergen im Original, siehe
   * SR4-Regelwerksabgleich.
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

    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[170!][46!,center][30!,center][46!,center][30!,center][90!][70!]");

    addSpecialAttributeRow(grid, 0, ch.getEdge(), ch);

    SpecialAttribute magicOrResonance = ch.getMagicOrResonance();
    if (magicOrResonance != null) {
      addSpecialAttributeRow(grid, 1, magicOrResonance, ch);
    }

    attributeGridHost.getChildren().add(grid);
  }

  private void addSpecialAttributeRow(@NonNull MigPane grid, int row,
      @NonNull SpecialAttribute attr, @NonNull Character character) {
    SpecialAttributeType type = attr.getType();
    addAttributeGridRow(grid, row, specialAttrRefreshers,
        new AttributeRowLabel(type.getDisplayName(), type.getAbbreviation(), attr.getMin(),
            attr.getMax()), new RowModel() {
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
            attr.getMax()), new RowModel() {
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
   * Baut eine Attribut-Zeile (Name, Abkürzung, +/−, Wert, Bereich, BP-/Karma-Kosten) in ein
   * MigPane ein und verdrahtet sie über {@link #wireIncrementable}. Gemeinsam genutzt von
   * {@link #addAttributeRow} und {@link #addSpecialAttributeRow}, die sich nur in Datenquelle
   * und {@link RowModel#canIncrement()} unterscheiden.
   */
  private void addAttributeGridRow(@NonNull MigPane grid, int row,
      @NonNull List<Runnable> refresherList, @NonNull AttributeRowLabel label,
      @NonNull RowModel model, @NonNull Character character) {
    Label attrNameLabel = new Label(label.displayName());
    Label abbrLabel = new Label(label.abbreviation());
    Button minus = new Button("−");
    Label valueLabel = new Label();
    Button plus = new Button("+");
    Label rangeLabel = new Label(label.min() + " – " + label.max());
    Label bpLabel = new Label();

    attrNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    abbrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    valueLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    rangeLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);

    wireIncrementable(refresherList, new RowControls(valueLabel, bpLabel, minus, plus),
        character.isCreated(), model, character);

    addRowCells(grid, row, attrNameLabel, abbrLabel, minus, valueLabel, plus, rangeLabel, bpLabel);
  }

  /**
   * Fügt die übergebenen Knoten spaltenweise (Spalte 0, 1, 2, …) in {@code grid} ein, in der
   * angegebenen Zeile. Gemeinsam genutzt von allen Grid-Zeilen-Buildern, die durchgehend ab
   * Spalte 0 befüllen (Attribut-, Sonderattribut-, Wissensfertigkeiten- und Skill-Zeilen).
   */
  private void addRowCells(@NonNull MigPane grid, int row, @NonNull Node... cells) {
    for (int col = 0; col < cells.length; col++) {
      grid.add(cells[col], "cell " + col + " " + row);
    }
  }

  // ── Skills-Tab ────────────────────────────────────────────

  private void buildSkillsTab() {
    if (character == null) {
      return;
    }

    skillsContainer.getChildren().clear();
    skillRefreshers.clear();

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
      skillsContainer.getChildren().add(grid);

      int row = 0;
      for (Map.Entry<String, List<Skill>> groupEntry : catEntry.getValue().entrySet()) {
        row = addSkillGroupRows(grid, row, groupEntry.getKey(), groupEntry.getValue(), character);
      }
    }

    buildKnowledgeSkillSection();
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
    knowledgeSkillRefreshers.clear();

    Label sectionLabel = new Label("Wissensfertigkeiten");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(20, 0, 4, 0));

    Label poolLabel = new Label();
    poolLabel.getStyleClass().add("bp-label");

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
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
    skillsContainer.getChildren().add(header);

    if (!ch.isCreated()) {
      Runnable poolRefresh = () -> poolLabel.setText(
          ch.getKnowledgeSkillPointsUsed() + " / " + ch.getKnowledgeSkillPointPool()
              + " Punkte" + (ch.getKnowledgeSkillOverflowBP() > 0
              ? " (" + ch.getKnowledgeSkillOverflowBP() + " BP über Pool)" : ""));
      knowledgeSkillRefreshers.add(poolRefresh);
      poolRefresh.run();
    }

    skillsContainer.getChildren().add(buildNativeLanguageRow(ch));

    MigPane grid = buildKnowledgeSkillGrid();
    skillsContainer.getChildren().add(grid);

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
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[200!][90!][30!,center][30!,center][30!,center][70!][90!]");
    grid.getStyleClass().add("attr-grid");
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
    Button removeBtn = new Button("Entfernen");

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

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

    removeBtn.setOnAction(e -> {
      character.removeKnowledgeSkill(skill.getName());
      buildSkillsTab();
      updateBpDisplay();
      refreshAllRows();
    });

    addRowCells(grid, row, nameLabelDummy, categoryLabel, minus, ratingLabel, plus, costLabel,
        removeBtn);
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

    int[] widths = {220, 90, 80, 90};
    for (int width : widths) {
      ColumnConstraints cc = new ColumnConstraints(width);
      grid.getColumnConstraints().add(cc);
    }

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

    qualitiesContainer.getChildren().add(grid);
  }

  private @NonNull HBox buildQualitySectionHeader(@NonNull String title,
      @NonNull QualityType type, boolean career, @NonNull Character ch) {
    Label sectionLabel = new Label(title);
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    Label limitLabel = new Label();
    limitLabel.getStyleClass().add("bp-label");

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
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

    grid.add(qualityNameLabel, 0, row);
    grid.add(costLabel, 1, row);
    grid.add(sourceLabel, 2, row);

    if (!isMetatype) {
      Button removeBtn = new Button("Entfernen");
      removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
      removeBtn.setOnAction(e -> onRemoveQuality(q));
      grid.add(removeBtn, 3, row);
    }
  }

  /**
   * Entfernt eine gewählte Quality. Handicaps im Karrieremodus loswerden kostet die doppelten
   * GP-Kosten in Karma (siehe {@link Character#getQualityKarmaCost}); alle anderen Fälle
   * (Erstellungsmodus, Gaben, Gratis-Qualities) bleiben unverändert kostenlos.
   */
  private void onRemoveQuality(@NonNull CharacterQuality q) {
    Character c = this.character;
    if (c == null) {
      return;
    }
    String qualityName = q.data().name();
    if (c.isCreated() && q.data().type() == QualityType.NEGATIVE && !q.gratis()) {
      int karmaCost = c.getQualityKarmaCost(q.data());
      if (qualityKarmaChargeFailed(c, "%s loswerden".formatted(qualityName), karmaCost)) {
        return;
      }
    }
    c.removeQuality(qualityName);
    buildQualitiesTab();
    buildAttributeGrid();
    updateBpDisplay();
    refreshAllRows();
  }

  /**
   * Prüft Karma-Verfügbarkeit, holt eine Bestätigung ein und verbucht den Karma-Log-Eintrag für
   * den Erwerb einer Gabe bzw. das Loswerden eines Handicaps im Karrieremodus (siehe
   * {@link Character#getQualityKarmaCost}).
   *
   * @return true, wenn das Karma NICHT verbucht wurde (fehlendes Karma oder abgebrochen) — die
   *     Aufrufer sollen in diesem Fall die eigentliche Aktion abbrechen
   */
  private boolean qualityKarmaChargeFailed(@NonNull Character c, @NonNull String label,
      int karmaCost) {
    if (c.getKarmaTotal() < karmaCost) {
      Notifications.create()
          .title("Nicht genug Karma")
          .text("Benötigt: " + karmaCost + KARMA_SUFFIX)
          .position(Pos.BOTTOM_RIGHT)
          .owner(nameLabel.getScene().getWindow())
          .showWarning();
      return true;
    }
    if (!confirmKarmaSpend(label, karmaCost, c)) {
      return true;
    }
    c.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now(ZoneId.systemDefault())));
    updateLogDisplay();
    return false;
  }

  private void rebuildQualitiesTab() {
    buildQualitiesTab();
    buildAttributeGrid();
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
      if (qualityKarmaChargeFailed(ch, "%s erwerben".formatted(r.data().name()), karmaCost)) {
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
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[200!][46!,center][30!,center][46!,center][30!,center][70!]");
    grid.getStyleClass().add("attr-grid");
    return grid;
  }

  /**
   * Fügt eine Gruppen-Kopfzeile mit +/−-Buttons und Auskoppeln-Button ein.
   */
  private int addGroupHeaderRow(@NonNull MigPane grid, int row, @NonNull String groupName,
      boolean coupled, @NonNull Character character) {
    HBox nameBox = new HBox(8);
    nameBox.setAlignment(Pos.CENTER_LEFT);

    Label groupLabel = new Label(groupName);
    groupLabel.getStyleClass().add(coupled ? "skill-group-header-coupled" : "skill-group-header");
    nameBox.getChildren().add(groupLabel);

    // GRW S. 109: Fertigkeitsgruppen dürfen während der Erstellung nicht aufgelöst werden.
    if (coupled && character.isCreated()) {
      nameBox.getChildren().add(buildDecoupleButton(groupName, character));
    }

    grid.add(nameBox, CELL_COL_0 + row);

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
    decoupleBtn.setOnAction(e -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
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
    Label attrLabel = new Label(skill.getLinkedAttribute().getAbbreviation());
    Button minus = new Button("−");
    Label ratingLabel = new Label();
    Button plus = new Button("+");
    Label bpLabel = new Label();

    skillNameLabel.getStyleClass().add(coupled ? "attr-name-coupled" : STYLE_ATTR_NAME);
    attrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);

    boolean career = character.isCreated();

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
              return character.canRaiseSkillAtCreation(skill);
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

    addRowCells(grid, row, skillNameLabel, attrLabel, minus, ratingLabel, plus, bpLabel);
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
      plus.setDisable(val >= model.max() || character.getKarmaTotal() < karmaCost);
    };
    refresherList.add(refresh);

    plus.setOnAction(e -> {
      int val = model.value();
      int karmaCost = model.karmaStepCost();
      String label = model.actionLabel(val, val + 1);
      if (val < model.max() && character.getKarmaTotal() >= karmaCost
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

  private boolean confirmKarmaSpend(@NonNull String label, int karmaCost,
      @NonNull Character character) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Karma ausgeben");
    confirm.setHeaderText(label);
    confirm.setContentText(karmaCost + " Karma ausgeben?\n\nVerbleibendes Karma danach: "
        + (character.getKarmaTotal() - karmaCost));
    confirm.initOwner(nameLabel.getScene().getWindow());
    return confirm.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
  }

  private void refreshAllRows() {
    attrRefreshers.forEach(Runnable::run);
    specialAttrRefreshers.forEach(Runnable::run);
    skillRefreshers.forEach(Runnable::run);
    knowledgeSkillRefreshers.forEach(Runnable::run);
    qualityRefreshers.forEach(Runnable::run);
  }

  private void updateBpDisplay() {
    if (character == null) {
      return;
    }
    int remaining = character.getRemainingBP();
    remainingBpLabel.setText(String.valueOf(remaining));
    remainingBpLabel.getStyleClass().remove("bp-value-warn");
    if (remaining < 0) {
      remainingBpLabel.getStyleClass().add("bp-value-warn");
    }
  }

  private void updateNuyenBpDisplay() {
    if (character == null) {
      return;
    }
    int bp = character.getNuyenBP();
    nuyenBpValueLabel.setText(bp + " BP (" + (bp * Character.NUYEN_PER_BP) + " ¥)");
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

  @FXML
  private void onFinalize() {
    if (character == null) {
      return;
    }
    int nuyenBp = character.getNuyenBP();
    int startNuyen = nuyenBp * Character.NUYEN_PER_BP;

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Charakter abschließen");
    confirm.setHeaderText("Charakter in den Karrieremodus überführen?");
    String bpInfo = nuyenBp > 0
        ? nuyenBp + " BP Startkapital werden umgerechnet in " + startNuyen + " ¥.\n\n"
        : "";
    confirm.setContentText(bpInfo + "Dieser Schritt kann nicht rückgängig gemacht werden.");
    confirm.initOwner(nameLabel.getScene().getWindow());

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    if (nuyenBp > 0) {
      character.addNuyenEntry(new LogEntry(
          "Startkapital (Charaktererstellung)", startNuyen, LocalDate.now(ZoneId.systemDefault())));
      character.setNuyenBP(0);
    }
    character.setCreated(true);
    updateModeDisplay();
    updateLogDisplay();
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
  }

  @SuppressWarnings("unchecked")
  private void buildLogTable(@NonNull TableView<LogEntry> table) {
    TableColumn<LogEntry, String> dateCol = new TableColumn<>("Datum");
    dateCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
    dateCol.setPrefWidth(90);

    TableColumn<LogEntry, String> descCol = new TableColumn<>("Bezeichnung");
    descCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().description()));

    TableColumn<LogEntry, String> amountCol = new TableColumn<>("Menge");
    amountCol.setCellValueFactory(c -> {
      int a = c.getValue().amount();
      return new SimpleStringProperty(a > 0 ? "+" + a : String.valueOf(a));
    });
    amountCol.setPrefWidth(70);
    amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    table.getColumns().addAll(dateCol, descCol, amountCol);
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

  /**
   * Zugriff auf einen einzelnen erhöh-/senkbaren Wert (Attribut, Skillgruppen-Rating oder Skill-Rating), den {@link #wireIncrementable} unabhängig
   * vom fachlichen Hintergrund verdrahten kann.
   */
  private interface RowModel {

    int value();

    /**
     * Anzeigetext für den aktuellen Wert; standardmäßig die Zahl selbst.
     */
    default @NonNull String valueDisplayText() {
      return String.valueOf(value());
    }

    int min();

    int max();

    /**
     * Zusätzliche regelspezifische Sperre über die reine BP-Verfügbarkeit hinaus (z.B. Erstellungs-Caps, die nicht allein über {@code max()}
     * abbildbar sind). Standardmäßig immer erlaubt.
     */
    default boolean canIncrement() {
      return true;
    }

    /**
     * Kosten für den nächsten BP-Erhöhungsschritt in der Charaktererstellung.
     */
    int bpStepCost();

    /**
     * Anzeigetext für die BP-Kosten-Spalte in der Charaktererstellung.
     */
    @NonNull String bpDisplayText();

    /**
     * Kosten für den nächsten Karma-Erhöhungsschritt im Karrieremodus.
     */
    int karmaStepCost();

    void increment();

    void decrement();

    @NonNull String actionLabel(int from, int to);
  }

  /**
   * Die vier UI-Elemente einer +/−-Zeile, wie sie {@link #wireIncrementable} verdrahtet.
   */
  private record RowControls(
      @NonNull Label valueLabel, @NonNull Label costLabel, @NonNull Button minus,
      @NonNull Button plus) {

  }

  private record DialogHandle(@NonNull FXMLLoader loader, @NonNull Stage stage) {

  }

  /**
   * Anzeigedaten einer Attribut-Zeile ({@link #addAttributeGridRow}), unabhängig davon ob sie von
   * einem Standard- oder Sonderattribut stammen.
   */
  private record AttributeRowLabel(
      @NonNull String displayName, @NonNull String abbreviation, int min, int max) {

  }
}