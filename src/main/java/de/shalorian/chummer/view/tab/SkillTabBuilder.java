package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.preventShrink;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.SkillsoftStatus;
import de.shalorian.chummer.model.character.skill.ExoticSkills;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.ExoticSkillAddDialogController;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import de.shalorian.chummer.view.grid.SkillSortMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Fertigkeiten-Tab: aktive Fertigkeiten (nach Kategorie oder alphabetisch, Gruppen mit Auskoppeln) und
 * Wissensfertigkeiten samt Muttersprache, jeweils mit Spezialisierungen. BP- bzw. Karma-Kosten über
 * {@link IncrementWiring}.
 */
public final class SkillTabBuilder {

  private static final String CELL_COL_0 = "cell 0 ";
  private static final String CELL_COL_1 = "cell 1 ";
  private static final String CELL_COL_2 = "cell 2 ";
  private static final String CELL_COL_3 = "cell 3 ";
  private static final String CELL_COL_4 = "cell 4 ";
  /** Spalte der Gruppen-BP: im Fertigkeiten-Grid unter der Spezialisierung (hinter der Pool-Spalte). */
  private static final int SKILL_GRID_GROUP_BP_COLUMN = 6;
  /** Spalte der Gruppen-BP im schmalen Gruppen-Grid des Alphabet-Modus. */
  private static final int GROUP_ONLY_BP_COLUMN = 5;
  /** Einrückung der Exotik-Instanzen unter ihrer Grundzeile, in Pixeln. */
  private static final int EXOTIC_INDENT = 16;

  private final @NonNull ScrollPane skillsScroll;
  private final @NonNull VBox skillsContainer;
  private final @NonNull ToggleGroup skillSortToggleGroup;
  private final @NonNull ToggleButton sortByAlphabetBtn;
  private final @NonNull ToggleButton sortByCategoryBtn;
  private final @NonNull CharacterTabHost host;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> skillRefreshers = new ArrayList<>();
  private @NonNull SkillSortMode skillSortMode = SkillSortMode.CATEGORY;
  private final @NonNull KnowledgeSkillSection knowledgeSection;
  private @Nullable Character character;

  public SkillTabBuilder(@NonNull ScrollPane skillsScroll, @NonNull VBox skillsContainer,
      @NonNull VBox knowledgeSkillsContainer,
      @NonNull ToggleGroup skillSortToggleGroup, @NonNull ToggleButton sortByAlphabetBtn,
      @NonNull ToggleButton sortByCategoryBtn, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
    this.skillsScroll = skillsScroll;
    this.skillsContainer = skillsContainer;
    this.skillSortToggleGroup = skillSortToggleGroup;
    this.sortByAlphabetBtn = sortByAlphabetBtn;
    this.sortByCategoryBtn = sortByCategoryBtn;
    this.host = host;
    this.wiring = wiring;
    this.knowledgeSection = new KnowledgeSkillSection(knowledgeSkillsContainer, host, wiring);
    skillSortToggleGroup.selectedToggleProperty()
        .addListener((_, old, newVal) -> onSortToggled(old, newVal));
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
    knowledgeSection.setCharacter(character);
  }

  /** Bewertet die +/−-Zeilen dieses Tabs neu (Restpunkte, Karma, Caps). */
  public void refresh() {
    skillRefreshers.forEach(Runnable::run);
    knowledgeSection.refresh();
  }

  public void build() {
    buildActiveSkills();
    knowledgeSection.build();
  }

  /**
   * Baut die aktive-Fertigkeiten-Sektion neu auf, je nach {@link #skillSortMode} entweder nach Kategorie ({@link #buildCategorySkillsSection}) oder
   * als zwei parallele alphabetische Listen ({@link #buildAlphabeticalSkillsSection}). Betrifft ausschließlich {@link #skillsContainer} — die
   * Wissensfertigkeiten-Sektion liegt seit der Layout-Trennung in einem eigenen Container und wird unabhängig über
   * {@link KnowledgeSkillSection#build()} aufgebaut.
   */
  private void buildActiveSkills() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }

    skillsContainer.getChildren().clear();
    skillRefreshers.clear();

    // Alphabetisch scrollen die beiden Spalten jede für sich; die äußere Fläche füllt dann nur die Höhe aus.
    boolean alphabetical = skillSortMode == SkillSortMode.ALPHABETICAL;
    skillsScroll.setFitToHeight(alphabetical);
    skillsScroll.setVbarPolicy(alphabetical ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
    skillsScroll.setVvalue(0);
    skillsContainer.getChildren().add(buildSkillsoftSection(ch));
    if (alphabetical) {
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

      int row = 1;
      for (Map.Entry<String, List<Skill>> groupEntry : catEntry.getValue().entrySet()) {
        row = addSkillGroupRows(grid, row, groupEntry.getKey(), groupEntry.getValue(), character);
      }
    }
  }

  /**
   * Alphabet-Modus: zwei unabhängig sortierte Listen nebeneinander — links alle noch gekoppelten Fertigkeitsgruppen (nur Name + gemeinsame
   * Rating-Steuerung, keine Mitgliedsnamen, siehe {@code docs/skills_sortierung_plan.md} Abschnitt 13), rechts alle Einzelfertigkeiten (ungruppiert
   * oder entkoppelt), jeweils alphabetisch nach Name sortiert über {@link Character#getCoupledGroupNames()} bzw.
   * {@link Character#getIndividualSkills()}.
   */
  private void buildAlphabeticalSkillsSection(@NonNull Character character) {
    MigPane groupGrid = buildGroupOnlyGrid();
    int groupRow = 0;
    for (String groupName : character.getCoupledGroupNames()) {
      groupRow = addGroupHeaderRow(groupGrid, groupRow, groupName, true, GROUP_ONLY_BP_COLUMN, character);
    }
    MigPane skillGrid = buildSkillGrid();
    int skillRow = 1;
    for (Skill skill : character.getIndividualSkills()) {
      addSkillRow(skillGrid, skillRow++, skill, false, character);
    }

    VBox leftColumn = scrollingColumn("Fertigkeitsgruppen", groupGrid);
    VBox rightColumn = scrollingColumn("Einzelfertigkeiten", skillGrid);
    HBox.setHgrow(rightColumn, Priority.ALWAYS);
    HBox columns = new HBox(24, leftColumn, rightColumn);
    VBox.setVgrow(columns, Priority.ALWAYS);
    skillsContainer.getChildren().add(columns);
  }

  /** Eine Spalte des Alphabet-Modus: Titel fest oben, darunter die eigene, unabhängig scrollende Liste. */
  private static @NonNull VBox scrollingColumn(@NonNull String title, @NonNull MigPane grid) {
    Label label = new Label(title);
    label.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(label, new Insets(0, 0, 4, 0));
    ScrollPane scroll = new ScrollPane(withRightFiller(grid));
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("attr-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);
    return new VBox(label, scroll);
  }

  /**
   * Schmaleres Grid für die Gruppen-Liste im Alphabet-Modus — {@link #addGroupHeaderRow} befüllt die Spalten 0 (Name), 1 (Auskoppeln-Button, nur bei
   * gekoppelten Gruppen im Karrieremodus) und 2/3/4/5 (−/Wert/+/BP), nie Pool oder Spezialisierung. Die volle 9-Spalten-Spec
   * aus {@link #buildSkillGrid()} würde daher in einem Nebeneinander-Layout unnötig Platz verschenken. Der Auskoppeln-Button steht bewusst in einer
   * eigenen Spalte statt direkt am Namenstext dran, damit er über alle Zeilen hinweg untereinander ausgerichtet ist statt je nach Namenslänge zu
   * wandern.
   */
  private @NonNull MigPane buildGroupOnlyGrid() {
    // Wert-Spalte ist bei Fertigkeitsgruppen durch Skill.MAX_RATING = 6 einstellig und braucht keine
    // feste Ausnahme (siehe Kommentar zu AttributeTabBuilder.ATTRIBUTE_VALUE_COLUMN_WIDTH). Kein eigenes
    // Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller verteilt.
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
          .computeIfAbsent(key, _ -> new ArrayList<>()).add(skill);
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
    row = addGroupHeaderRow(grid, row, groupName, coupled, SKILL_GRID_GROUP_BP_COLUMN, character);
    for (Skill skill : skills) {
      addSkillRow(grid, row++, skill, coupled, character);
    }
    return row;
  }

  /**
   * Baut den Skills-Tab neu auf, z.B. nach dem Entkoppeln einer Gruppe.
   */
  private void rebuildSkillsTab() {
    build();
    host.refreshRows();
    host.onBpChanged();
  }

  private @NonNull MigPane buildSkillGrid() {
    // Wert-Spalte durch Skill.MAX_RATING = 6 einstellig, keine feste Ausnahme nötig. Kein eigenes
    // Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][center][center][center][center][center][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addHeaderRow(grid, "", "", "", "Wert", "", "Pool");
    return grid;
  }

  /**
   * Fügt eine Gruppen-Kopfzeile mit +/−-Buttons und Auskoppeln-Button ein.
   */
  private int addGroupHeaderRow(@NonNull MigPane grid, int row, @NonNull String groupName,
      boolean coupled, int bpColumn, @NonNull Character character) {
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

      wiring.wire(skillRefreshers, new RowControls(ratingLabel, bpLabel, minus, plus),
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
              return character.canRaiseGroup(groupName);
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
          }, character, LogCategory.SKILL);

      preventShrink(minus);
      preventShrink(ratingLabel);
      preventShrink(plus);
      preventShrink(bpLabel);
      grid.add(minus, CELL_COL_2 + row);
      grid.add(ratingLabel, CELL_COL_3 + row);
      grid.add(plus, CELL_COL_4 + row);
      grid.add(bpLabel, "cell " + bpColumn + " " + row);
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
    decoupleBtn.setOnAction(_ -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      TabServices.applyDialogStyle(confirm);
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
          .ifPresent(_ -> {
            character.decoupleGroup(groupName);
            rebuildSkillsTab();
          });
    });
    return decoupleBtn;
  }

  private void addSkillRow(@NonNull MigPane grid, int row, @NonNull Skill skill, boolean coupled,
      @NonNull Character character) {
    if (skill.isExoticTemplate()) {
      addExoticTemplateRow(grid, row, skill, character);
      return;
    }
    Label skillNameLabel = new Label(skill.getName());
    Label attrLabel = new Label(skill.getLinkedAttribute());
    Button minus = new Button("−");
    Label ratingLabel = new Label();
    Button plus = new Button("+");
    Label bpLabel = new Label();
    Label specLabel = new Label();
    Button specBtn = new Button("Spez.");
    Label poolLabel = DicePoolLabel.create();

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
      boolean affordable = character.canAffordSkillSpecialization(spec);
      specBtn.setDisable(!eligible || !affordable);
    };
    skillRefreshers.add(specRefresh);
    specRefresh.run();
    specBtn.setOnAction(_ -> onEditSkillSpecialization(skill, character));

    Runnable poolRefresh = () -> DicePoolLabel.show(poolLabel, character.getSkillDicePool(skill),
        character.getEffectiveSkillRating(skill), skill.getSpecialization());
    skillRefreshers.add(poolRefresh);
    poolRefresh.run();

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
      wiring.wire(skillRefreshers, new RowControls(ratingLabel, bpLabel, minus, plus),
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
              return skill.getMaxRating();
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
          }, character, LogCategory.SKILL);
    }

    if (skill.getExoticTarget() != null) {
      // Exotische Fertigkeiten haben keine Spezialisierung (GRW S. 148); an ihrer Stelle das Entfernen, nur bei der
      // Erschaffung bzw. im Spielleiter-Modus (sonst deaktiviert mit Grund).
      skillNameLabel.setPadding(new Insets(0, 0, 0, EXOTIC_INDENT));
      Button removeBtn = new Button("Entfernen");
      removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
      useIcon(removeBtn, FontAwesomeSolid.TRASH);
      // Der Tab wird bei Quality- und Moduswechsel neu gebaut, daher darf der Grund den Knopf hier sperren.
      String removalProblem = character.getExoticSkillRemovalProblem(skill);
      removeBtn.setDisable(removalProblem != null);
      if (removalProblem != null) {
        removeBtn.setTooltip(new Tooltip(removalProblem));
      }
      removeBtn.setOnAction(_ -> {
        String problem = character.getExoticSkillRemovalProblem(skill);
        if (problem != null) {
          host.services().warn("Exotische Fertigkeit", problem);
          return;
        }
        character.removeExoticSkill(skill);
        build();
        host.onBpChanged();
        host.refreshRows();
      });
      addRowCells(grid, row, skillNameLabel, attrLabel, minus, ratingLabel, plus, poolLabel, new Label(),
          removeBtn, bpLabel);
      return;
    }
    addRowCells(grid, row, skillNameLabel, attrLabel, minus, ratingLabel, plus, poolLabel, specLabel,
        specBtn, bpLabel);
  }

  /**
   * Übersicht der Talentsofts (GRW S. 377, 389): je Soft Fertigkeit, Stufe, Wirkung bzw. Grund und Pool, dazu die
   * Auslastung der Talentleitungen. Der Inhalt wird in jedem Refresh neu gefüllt, weil Kauf und Laden von Softs im Tab
   * Ausrüstung passieren; ohne Softs bleibt der Abschnitt unsichtbar.
   */
  private @NonNull VBox buildSkillsoftSection(@NonNull Character character) {
    VBox section = new VBox(4);
    VBox.setMargin(section, new Insets(0, 0, 12, 0));
    Runnable refresh = () -> {
      section.getChildren().clear();
      List<Gear> softs = character.getSkillsofts();
      section.setVisible(!softs.isEmpty());
      section.setManaged(!softs.isEmpty());
      if (softs.isEmpty()) {
        return;
      }
      Label title = new Label("Talentsofts");
      title.getStyleClass().add(STYLE_SECTION_TITLE);
      Label wires = new Label(character.getSkillwireRating() == 0
          ? "Keine Talentleitungen: Aktionssofts wirken nicht (S. 389)."
          : "Talentleitungen Stufe %d, Aktionssofts geladen %d von %d Stufen (S. 389). Laden im Tab Ausrüstung."
              .formatted(character.getSkillwireRating(), character.getLoadedActivesoftRating(),
                  character.getActivesoftCapacity()));
      wires.getStyleClass().add(STYLE_DETAIL_HINT);
      MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][center][][center]");
      grid.getStyleClass().add(STYLE_ATTR_GRID);
      addHeaderRow(grid, "Talentsoft", "Für", "Stufe", "Wirkung", "Pool");
      int row = 1;
      for (Gear soft : softs) {
        SkillsoftStatus status = character.getSkillsoftStatus(soft);
        String problem = status.problem();
        Label pool = DicePoolLabel.create();
        DicePoolLabel.show(pool, character.getSkillsoftPool(soft), status.rating(), null);
        addRowCells(grid, row++, styled(soft.getName(), STYLE_ATTR_NAME), styled(soft.getExtra(), STYLE_ATTR_NAME),
            styled(String.valueOf(soft.getRating()), STYLE_ATTR_VALUE),
            styled(problem != null ? problem : "wirkt", STYLE_DETAIL_HINT), pool);
      }
      section.getChildren().addAll(title, wires, withRightFiller(grid));
    };
    skillRefreshers.add(refresh);
    refresh.run();
    return section;
  }

  private static @NonNull Label styled(@NonNull String text, @NonNull String styleClass) {
    Label label = new Label(text);
    label.getStyleClass().add(styleClass);
    return label;
  }

  /**
   * Grundzeile einer Exotischen Fertigkeit: kein eigener Wert (gelernt wird je Waffe bzw. Fahrzeug, GRW S. 148, 155),
   * Pool für Waffen ohne gelernte Instanz und ein Knopf zum Anlegen einer Instanz.
   */
  private void addExoticTemplateRow(@NonNull MigPane grid, int row, @NonNull Skill template,
      @NonNull Character character) {
    Label nameLabel = new Label(template.getName());
    nameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    Label attrLabel = new Label(template.getLinkedAttribute());
    attrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    Label poolLabel = DicePoolLabel.create();
    Runnable poolRefresh = () -> DicePoolLabel.show(poolLabel, character.getSkillDicePool(template), 0, null);
    skillRefreshers.add(poolRefresh);
    poolRefresh.run();
    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setTooltip(new Tooltip(ExoticSkills.VEHICLE.equals(template.getName())
        ? "Exotische Fertigkeit für ein Fahrzeug lernen" : "Exotische Fertigkeit für eine Waffe lernen"));
    addBtn.setOnAction(_ -> onAddExoticSkill(template, character));
    addRowCells(grid, row, nameLabel, attrLabel, new Label(), new Label(), new Label(), poolLabel, new Label(),
        addBtn, new Label());
  }

  /**
   * Legt eine Exotik-Instanz an. Im Karrieremodus ist das eine neue Fertigkeit: 4 Karma, Start mit Wert 1 (GRW S. 313,
   * Plan-Annahme A7); die Buchung kommt vor dem Anlegen, damit Rückgängig den Stand ohne Instanz wiederherstellt.
   */
  private void onAddExoticSkill(@NonNull Skill template, @NonNull Character character) {
    try {
      DialogHandle dialog = host.services().openDialog("ExoticSkillAddDialog.fxml", "Exotische Fertigkeit hinzufügen");
      ExoticSkillAddDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setup(character, template.getName());
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(r -> {
        String name = ExoticSkills.instanceName(r.base(), r.target());
        if (character.isCreated()) {
          int cost = template.getKarmaCost(character.isSkillCostDoubled(template.getCategory()));
          if (host.services().karmaChargeFailed(character, LogCategory.SKILL, name + " 0 → 1", cost)) {
            return;
          }
          character.addExoticSkill(r.base(), r.target()).setRating(1);
        } else {
          character.addExoticSkill(r.base(), r.target());
        }
        build();
        host.onBpChanged();
        host.refreshRows();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  @SuppressWarnings("ReferenceEquality") // Identität gewollt: welcher der beiden Buttons gewählt ist
  private void onSortToggled(@Nullable Toggle old, @Nullable Toggle newVal) {
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
    buildActiveSkills();
  }

  /**
   * Öffnet den Freitext-Dialog zum Setzen/Ändern der Spezialisierung einer aktiven Fertigkeit (analog
   * {@code KnowledgeSkillSection.buildNativeLanguageRow}). Im Erstellungsmodus wird direkt gesetzt (kostet nur beim
   * erstmaligen Setzen den Fixpreis aus {@link Character#getActiveSkillSpecializationBpCost()}, ein späteres
   * Umbenennen ist kostenlos). Im Karrieremodus kostet jedes Setzen/Ändern erneut Karma (SR4-Grundregelwerk S. 312); ist die Fertigkeit
   * noch Teil einer gekoppelten Fertigkeitsgruppe, wird das Auflösen der Gruppe im selben Bestätigungsdialog mit angekündigt.
   */
  private void onEditSkillSpecialization(@NonNull Skill skill, @NonNull Character character) {
    if (!character.canSpecializeSkill(skill)) {
      return;
    }
    TextInputDialog dialog = new TextInputDialog(
        skill.getSpecialization() != null ? skill.getSpecialization() : "");
    TabServices.applyDialogStyle(dialog);
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
    String groupName = skill.getGroupName();
    String warning = groupName != null && character.isGroupCoupled(groupName)
        ? "Dies löst die Fertigkeitsgruppe \"" + groupName + "\" auf. Die Mitglieder "
        + "behalten ihr aktuelles Rating, können danach aber einzeln gesteigert werden."
        : null;
    String label = skill.getName() + ": Spezialisierung \"" + (newSpec == null ? "–" : newSpec) + "\"";
    if (!host.services().karmaChargeFailed(character, LogCategory.SKILL, label, karmaCost, warning)) {
      character.setSkillSpecialization(skill, newSpec);
      // rebuildSkillsTab() statt host.refreshRows(): eine Gruppenauflösung ändert, wie die
      // betroffenen Zeilen verdrahtet sind (gekoppelt = deaktivierte Rating-Buttons, entkoppelt =
      // einzeln steigerbar) — das lässt sich nicht über die reinen Refresh-Callbacks nachziehen,
      // die Zeilen müssen neu gebaut werden.
      rebuildSkillsTab();
    }
  }

  /**
   * Erstellungsmodus-Zweig von {@link #applySkillSpecialization}: erstmaliges Setzen kostet den Fixpreis aus
   * {@link Character#getActiveSkillSpecializationBpCost()}, ein späteres Umbenennen oder Löschen ist kostenlos.
   */
  private void applySkillSpecializationInCreationMode(@NonNull Skill skill,
      @NonNull Character character, @Nullable String newSpec) {
    boolean affordable = newSpec == null || character.canAffordSkillSpecialization(skill.getSpecialization());
    if (!affordable) {
      return;
    }
    character.setSkillSpecialization(skill, newSpec);
    host.refreshRows();
    host.onBpChanged();
  }

}
