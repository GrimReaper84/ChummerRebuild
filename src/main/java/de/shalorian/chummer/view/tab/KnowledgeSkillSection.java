package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.KnowledgeSuggestionLoader;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.KnowledgeSkillAddDialogController;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Wissensfertigkeiten-Sektion des Fertigkeiten-Tabs: freier Punktepool (INT+LOG)×3, Muttersprache, Kategorien,
 * Spezialisierungen. Liegt in einem eigenen Container und wird unabhängig von den aktiven Fertigkeiten aufgebaut.
 */
final class KnowledgeSkillSection {


  private final @NonNull VBox knowledgeSkillsContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> knowledgeSkillRefreshers = new ArrayList<>();
  private @Nullable Character character;

  KnowledgeSkillSection(@NonNull VBox knowledgeSkillsContainer, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
    this.knowledgeSkillsContainer = knowledgeSkillsContainer;
    this.host = host;
    this.wiring = wiring;
  }

  void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Bewertet die +/−-Zeilen der Wissensfertigkeiten neu (Pool, Karma, Caps). */
  void refresh() {
    knowledgeSkillRefreshers.forEach(Runnable::run);
  }

  void build() {
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
    addBtn.setOnAction(_ -> onAddKnowledgeSkill());

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

    int row = 1;
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

    editBtn.setOnAction(_ -> {
      KnowledgeSkill current = ch.getNativeLanguage();
      suggestionDialog("Muttersprache", "Muttersprache des Charakters", "Sprache:",
          current != null ? current.getName() : "", KnowledgeSuggestionLoader.names(KnowledgeSkillCategory.LANGUAGE))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .ifPresent(name -> {
            ch.setNativeLanguage(name);
            host.refreshRows();
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
        "[][][center][center][center][center][][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addHeaderRow(grid, "", "", "", "Wert", "", "Pool");
    return grid;
  }

  private void addKnowledgeSkillRow(@NonNull MigPane grid, int row,
      @NonNull KnowledgeSkill skill, @NonNull Character character) {
    Label nameLabelDummy = new Label(skill.getName());
    Label categoryLabel = new Label(skill.getCategory().getDisplayName());
    RowControls controls = RowControls.create();
    Button minus = controls.minus();
    Label ratingLabel = controls.valueLabel();
    Button plus = controls.plus();
    Label costLabel = controls.costLabel();
    Label specLabel = new Label();
    Button specBtn = new Button("Spez.");
    Button removeBtn = new Button("Entfernen");
    Label poolLabel = DicePoolLabel.create();

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    specLabel.getStyleClass().add(STYLE_ATTR_NAME);
    specBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

    Runnable specRefresh = () -> {
      String spec = skill.getSpecialization();
      specLabel.setText(spec != null ? spec : "–");
      boolean eligible = character.canSpecializeKnowledgeSkill(skill);
      boolean affordable = character.canAffordKnowledgeSkillSpecialization(spec);
      specBtn.setDisable(!eligible || !affordable);
    };
    knowledgeSkillRefreshers.add(specRefresh);
    specRefresh.run();
    specBtn.setOnAction(_ -> onEditKnowledgeSkillSpecialization(skill, character));

    Runnable poolRefresh = () -> DicePoolLabel.show(poolLabel, character.getKnowledgeSkillDicePool(skill),
        character.getEffectiveKnowledgeSkillRating(skill), skill.getSpecialization());
    knowledgeSkillRefreshers.add(poolRefresh);
    poolRefresh.run();

    wiring.wire(knowledgeSkillRefreshers, controls,
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
        }, character, LogCategory.SKILL);

    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      character.removeKnowledgeSkill(skill.getName());
      build();
      host.onBpChanged();
      host.refreshRows();
    });

    addRowCells(grid, row, nameLabelDummy, categoryLabel, minus, ratingLabel, plus, poolLabel, specLabel,
        specBtn, costLabel, removeBtn);
  }

  private void onAddKnowledgeSkill() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services()
          .openDialog("KnowledgeSkillAddDialog.fxml", "Wissensfertigkeit hinzufügen");
      KnowledgeSkillAddDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        ch.addKnowledgeSkill(r.name(), r.category());
        build();
        host.onBpChanged();
        host.refreshRows();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /**
   * Öffnet den Freitext-Dialog zum Setzen/Ändern der Spezialisierung einer Wissensfertigkeit. Im Erstellungsmodus zählt eine neue Spezialisierung als
   * +1 Punkt zum Wissensfertigkeitspunkte- Pool ({@link Character#getKnowledgeSkillSpecializationBP()}); im Karrieremodus kostet jedes Setzen/Ändern
   * erneut Karma (SR4-Grundregelwerk S. 312). Wissensfertigkeiten haben keine Gruppen, daher entfällt der Gruppenauflösungs-Hinweis aus
   * {@code SkillTabBuilder.onEditSkillSpecialization}.
   */
  private void onEditKnowledgeSkillSpecialization(@NonNull KnowledgeSkill skill,
      @NonNull Character character) {
    if (!character.canSpecializeKnowledgeSkill(skill)) {
      return;
    }
    suggestionDialog("Spezialisierung", skill.getName() + " – Spezialisierung", "Spezialisierung:",
        skill.getSpecialization() != null ? skill.getSpecialization() : "",
        KnowledgeSuggestionLoader.specializations(skill.getName(), skill.getCategory()))
        .map(String::trim)
        .ifPresent(value -> applyKnowledgeSkillSpecialization(skill, character,
            value.isBlank() ? null : value));
  }

  /**
   * Eingabedialog mit editierbarer Vorschlagsliste (GRW S. 155/156, ADR-059); Freitext bleibt möglich. Leer bei
   * Abbruch.
   */
  private static @NonNull Optional<String> suggestionDialog(@NonNull String title, @NonNull String header,
      @NonNull String caption, @NonNull String current, @NonNull List<String> suggestions) {
    ComboBox<String> box = new ComboBox<>();
    box.setEditable(true);
    box.getItems().setAll(suggestions);
    box.getEditor().setText(current);
    box.setPrefWidth(260);
    Dialog<String> dialog = new Dialog<>();
    dialog.getDialogPane().setContent(new HBox(10, new Label(caption), box));
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dialog.setResultConverter(button -> button == ButtonType.OK ? box.getEditor().getText() : null);
    TabServices.applyDialogStyle(dialog);
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    return dialog.showAndWait();
  }

  private void applyKnowledgeSkillSpecializationInCareerMode(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    int karmaCost = character.getSpecializationKarmaCost();
    String label = skill.getName() + ": Spezialisierung \"" + (newSpec == null ? "–" : newSpec) + "\"";
    if (!host.services().karmaChargeFailed(character, LogCategory.SKILL, label, karmaCost)) {
      character.setKnowledgeSkillSpecialization(skill, newSpec);
      host.refreshRows();
    }
  }

  /**
   * Erstellungsmodus-Zweig von {@link #applyKnowledgeSkillSpecialization}: erstmaliges Setzen kostet ggf. BP, falls es den
   * Wissensfertigkeitspunkte-Pool überschreitet (siehe {@link Character#getKnowledgeSkillSpecializationBP()}), ein späteres Umbenennen oder Löschen
   * ist kostenlos.
   */
  private void applyKnowledgeSkillSpecializationInCreationMode(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    boolean affordable = newSpec == null
        || character.canAffordKnowledgeSkillSpecialization(skill.getSpecialization());
    if (!affordable) {
      return;
    }
    character.setKnowledgeSkillSpecialization(skill, newSpec);
    host.refreshRows();
    host.onBpChanged();
  }

  private void applyKnowledgeSkillSpecialization(@NonNull KnowledgeSkill skill,
      @NonNull Character character, @Nullable String newSpec) {
    if (character.isCreated()) {
      applyKnowledgeSkillSpecializationInCareerMode(skill, character, newSpec);
    } else {
      applyKnowledgeSkillSpecializationInCreationMode(skill, character, newSpec);
    }
  }
}
