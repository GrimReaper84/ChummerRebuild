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
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import de.shalorian.chummer.view.grid.SkillSortMode;
import java.time.LocalDate;
import java.time.ZoneId;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
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

  public SkillTabBuilder(@NonNull VBox skillsContainer, @NonNull VBox knowledgeSkillsContainer,
      @NonNull ToggleGroup skillSortToggleGroup, @NonNull ToggleButton sortByAlphabetBtn,
      @NonNull ToggleButton sortByCategoryBtn, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
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
      groupRow = addGroupHeaderRow(groupGrid, groupRow, groupName, true, GROUP_ONLY_BP_COLUMN, character);
    }

    VBox rightColumn = new VBox();
    Label individualTitle = new Label("Einzelfertigkeiten");
    individualTitle.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(individualTitle, new Insets(0, 0, 4, 0));
    rightColumn.getChildren().add(individualTitle);
    MigPane skillGrid = buildSkillGrid();
    rightColumn.getChildren().add(withRightFiller(skillGrid));
    int skillRow = 1;
    for (Skill skill : character.getIndividualSkills()) {
      addSkillRow(skillGrid, skillRow++, skill, false, character);
    }

    columns.getChildren().addAll(leftColumn, rightColumn);
    skillsContainer.getChildren().add(columns);
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
          }, character);

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
      host.services().applyDialogStyle(confirm.getDialogPane());
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
      boolean affordable = career
          ? character.getKarmaTotal() >= character.getSpecializationKarmaCost()
          : spec != null || character.getRemainingBP() >= character.getActiveSkillSpecializationBpCost();
      specBtn.setDisable(!eligible || !affordable);
    };
    skillRefreshers.add(specRefresh);
    specRefresh.run();
    specBtn.setOnAction(_ -> onEditSkillSpecialization(skill, character));

    Runnable poolRefresh = () -> DicePoolLabel.show(poolLabel, character.getSkillDicePool(skill),
        skill.getRating(), skill.getSpecialization());
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
          }, character);
    }

    addRowCells(grid, row, skillNameLabel, attrLabel, minus, ratingLabel, plus, poolLabel, specLabel,
        specBtn, bpLabel);
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
    host.services().applyDialogStyle(dialog.getDialogPane());
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
    if (host.services().confirmKarmaSpend(label, karmaCost, character, warning)) {
      character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now(ZoneId.systemDefault())));
      character.setSkillSpecialization(skill, newSpec);
      // rebuildSkillsTab() statt host.refreshRows(): eine Gruppenauflösung ändert, wie die
      // betroffenen Zeilen verdrahtet sind (gekoppelt = deaktivierte Rating-Buttons, entkoppelt =
      // einzeln steigerbar) — das lässt sich nicht über die reinen Refresh-Callbacks nachziehen,
      // die Zeilen müssen neu gebaut werden.
      rebuildSkillsTab();
      host.onLogChanged();
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
    host.refreshRows();
    host.onBpChanged();
  }

}
