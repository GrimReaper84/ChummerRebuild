package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterAttribute;
import de.shalorian.chummer.model.character.CharacterQuality;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.QualitySource;
import de.shalorian.chummer.model.character.QualityType;
import de.shalorian.chummer.model.character.Skill;
import de.shalorian.chummer.model.character.SkillCategory;
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
  private static final String KARMA_SUFFIX = " Karma";
  private static final String CELL_COL_0 = "cell 0 ";
  private static final String CELL_COL_1 = "cell 1 ";
  private static final String CELL_COL_2 = "cell 2 ";
  private static final String CELL_COL_3 = "cell 3 ";
  private static final String CELL_COL_4 = "cell 4 ";
  private static final String CELL_COL_5 = "cell 5 ";
  private static final String CELL_COL_6 = "cell 6 ";
  // Attribute-Refresher bleiben stabil; Skill-Refresher werden beim Entkoppeln neu aufgebaut
  private final @NonNull List<Runnable> attrRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> skillRefreshers = new ArrayList<>();
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

    MigPane grid = new MigPane("hidemode 3",
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
  }

  private void addAttributeRow(@NonNull MigPane grid, int row, @NonNull AttributeType type,
      @NonNull CharacterAttribute attr, @NonNull Character character) {
    Label attrNameLabel = new Label(type.getDisplayName());
    Label abbrLabel = new Label(type.getAbbreviation());
    Button minus = new Button("−");
    Label valueLabel = new Label();
    Button plus = new Button("+");
    Label rangeLabel = new Label(attr.getMin() + " – " + attr.getMax());
    Label bpLabel = new Label();

    attrNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    abbrLabel.getStyleClass().add("attr-abbr");
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    valueLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    rangeLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);

    wireIncrementable(attrRefreshers, new RowControls(valueLabel, bpLabel, minus, plus),
        character.isCreated(), new RowModel() {
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

    grid.add(attrNameLabel, CELL_COL_0 + row);
    grid.add(abbrLabel, CELL_COL_1 + row);
    grid.add(minus, CELL_COL_2 + row);
    grid.add(valueLabel, CELL_COL_3 + row);
    grid.add(plus, CELL_COL_4 + row);
    grid.add(rangeLabel, CELL_COL_5 + row);
    grid.add(bpLabel, CELL_COL_6 + row);
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
      catLabel.getStyleClass().add("section-title");
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
  }

  /**
   * Gruppiert die Fertigkeiten des Charakters nach Kategorie und innerhalb der Kategorie nach
   * Gruppenname ({@code ""} für ungruppierte Fertigkeiten).
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
   * Fügt die Zeilen einer Gruppe (bzw. der ungruppierten Fertigkeiten, wenn {@code groupName}
   * leer ist) in den Skill-Grid ein.
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
    sectionLabel.getStyleClass().add("section-title");

    Label limitLabel = new Label();
    limitLabel.getStyleClass().add("bp-label");

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add("btn-secondary");
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
      removeBtn.getStyleClass().add("btn-secondary");
      String qualityName = q.data().name();
      removeBtn.setOnAction(e -> {
        Character c = this.character;
        if (c != null) {
          c.removeQuality(qualityName);
          buildQualitiesTab();
          updateBpDisplay();
          refreshAllRows();
        }
      });
      grid.add(removeBtn, 3, row);
    }
  }

  private void rebuildQualitiesTab() {
    buildQualitiesTab();
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
    MigPane grid = new MigPane("hidemode 3",
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

    if (coupled) {
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
    attrLabel.getStyleClass().add("attr-abbr");
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

    grid.add(skillNameLabel, CELL_COL_0 + row);
    grid.add(attrLabel, CELL_COL_1 + row);
    grid.add(minus, CELL_COL_2 + row);
    grid.add(ratingLabel, CELL_COL_3 + row);
    grid.add(plus, CELL_COL_4 + row);
    grid.add(bpLabel, CELL_COL_5 + row);
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
   * Zugriff auf einen einzelnen erhöh-/senkbaren Wert (Attribut, Skillgruppen-Rating oder
   * Skill-Rating), den {@link #wireIncrementable} unabhängig vom fachlichen Hintergrund verdrahten
   * kann.
   */
  private interface RowModel {

    int value();

    int min();

    int max();

    /** Kosten für den nächsten BP-Erhöhungsschritt in der Charaktererstellung. */
    int bpStepCost();

    /** Anzeigetext für die BP-Kosten-Spalte in der Charaktererstellung. */
    @NonNull String bpDisplayText();

    /** Kosten für den nächsten Karma-Erhöhungsschritt im Karrieremodus. */
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
      @NonNull Button plus) {}

  /**
   * Verdrahtet eine +/−-Zeile (Refresh-Runnable, Klick-Handler, BP- bzw. Karma-Kostenprüfung inkl.
   * Bestätigungsdialog im Karrieremodus) für {@code addAttributeRow}, {@code addGroupHeaderRow} und
   * {@code addSkillRow} einheitlich. Das Ausblenden von {@code minus} im Karrieremodus verlässt sich
   * auf {@code hidemode 3} des umgebenden {@link MigPane} — ein zusätzliches {@code setManaged(false)}
   * ist dafür nicht nötig.
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
      valueLabel.setText(String.valueOf(val));
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
      valueLabel.setText(String.valueOf(val));
      costLabel.setText(model.bpDisplayText());
      minus.setDisable(val <= model.min());
      plus.setDisable(val >= model.max() || character.getRemainingBP() < model.bpStepCost());
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
      if (model.value() < model.max() && character.getRemainingBP() >= model.bpStepCost()) {
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
    skillRefreshers.forEach(Runnable::run);
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

  // ── Karrieremodus-Transition ──────────────────────────────

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

  // ── Log-Tab ───────────────────────────────────────────────

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

  private record DialogHandle(@NonNull FXMLLoader loader, @NonNull Stage stage) {

  }
}