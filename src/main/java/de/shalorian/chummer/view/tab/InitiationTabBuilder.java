package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.addSourceTooltip;
import static de.shalorian.chummer.view.grid.GridSupport.iconTextButton;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.initiation.Initiation;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.initiation.TechniqueKind;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.TechniqueSelectDialogController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Bereich Initiation (Magie, GRW S. 232/233) bzw. Wandlung (Resonanz, S. 284): Kopf mit Grad, Maximum und Kosten des
 * nächsten Grades, Knopf zum Steigern gegen Karma (nur im Karrieremodus, S. 112), darunter die gelernten Metamagien
 * bzw. Echos mit ihrer Wirkung beim aktuellen Grad und ein Knopf zum Lernen. Änderungen meldet er über
 * {@link CharacterTabHost#onInitiationChanged()}, weil das Maximum von Magie bzw. Resonanz am Grad hängt.
 */
public final class InitiationTabBuilder {

  private final @NonNull VBox container;
  private final @NonNull CharacterTabHost host;
  private @Nullable Character character;

  public InitiationTabBuilder(@NonNull VBox container, @NonNull CharacterTabHost host) {
    this.container = container;
    this.host = host;
  }

  /** Charakter, den dieser Bereich anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Baut den Bereich komplett neu; ohne Magie oder Resonanz bleibt er leer (die Seitenleiste sperrt ihn). */
  public void build() {
    container.getChildren().clear();
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    Initiation initiation = ch.getInitiation();
    TechniqueKind kind = initiation.getKind();
    if (kind == null) {
      return;
    }
    container.getChildren().add(buildHeader(ch, initiation, kind));
    for (Label hint : hints(ch, initiation, kind)) {
      container.getChildren().add(hint);
    }
    buildTechniques(ch, initiation, kind);
  }

  private @NonNull HBox buildHeader(@NonNull Character ch, @NonNull Initiation initiation,
      @NonNull TechniqueKind kind) {
    Label gradeLabel = styled(new Label(kind.getGradeName() + " " + initiation.getGrade()), STYLE_BP_VALUE);
    // wie im Attributbereich das wirksame Maximum (nach Essenzverlust)
    Label maxLabel = styled(new Label(kind.getAttributeName() + "-Maximum " + ch.getEffectiveMagicOrResonanceMax()),
        STYLE_BP_LABEL);
    int cost = initiation.getNextGradeKarmaCost();
    Label costLabel = styled(new Label("nächster Grad: " + cost + " Karma"), STYLE_BP_LABEL);

    Button raiseBtn = iconTextButton("Grad steigern", FontAwesomeSolid.ARROW_UP, kind.getGradeName() + " steigern");
    raiseBtn.setOnAction(_ -> onRaise(ch, initiation, kind));
    // Der Bereich wird beim Moduswechsel neu gebaut, daher darf der Modus den Knopf sperren.
    String modeProblem = initiation.getModeProblem();
    raiseBtn.setDisable(modeProblem != null);
    if (modeProblem != null) {
      raiseBtn.setTooltip(new Tooltip(modeProblem));
    }

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(12, gradeLabel, maxLabel, costLabel, spacer, raiseBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(header, new Insets(0, 0, 8, 0));
    return header;
  }

  /** Erschaffungshinweis, Zugang zu Metaebenen bzw. Resonanzräumen und Warnung bei Grad über dem Attribut. */
  private static @NonNull List<Label> hints(@NonNull Character ch, @NonNull Initiation initiation,
      @NonNull TechniqueKind kind) {
    List<Label> hints = new ArrayList<>();
    if (!ch.isCreated()) {
      hints.add(styled(new Label(kind.getProcessName() + " ist erst im Karrieremodus möglich (GRW S. 112)."),
          STYLE_ATTR_RANGE));
    }
    if (initiation.getGrade() > 0) {
      hints.add(styled(new Label(kind.getAccessHint()), STYLE_ATTR_RANGE));
    }
    String warning = initiation.getAboveAttributeHint();
    if (warning != null) {
      Label warningLabel = styled(new Label(warning), STYLE_ATTR_RANGE);
      warningLabel.setWrapText(true);
      setWarnStyle(warningLabel, true);
      hints.add(warningLabel);
    }
    return hints;
  }

  private void buildTechniques(@NonNull Character ch, @NonNull Initiation initiation, @NonNull TechniqueKind kind) {
    String plural = kind.getPluralName();
    Label title = styled(new Label(plural), STYLE_SECTION_TITLE);
    Label free = styled(new Label("frei: " + initiation.getFreeChoices()), STYLE_BP_LABEL);
    Button learnBtn = iconTextButton("Lernen", FontAwesomeSolid.PLUS, kind.getTechniqueName() + " lernen");
    learnBtn.setOnAction(_ -> onLearn(ch, initiation, kind));
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(8, title, free, spacer, learnBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(header, new Insets(16, 0, 0, 0));
    container.getChildren().add(header);

    List<TechniqueData> learned = initiation.getTechniques().stream().distinct().toList();
    if (learned.isEmpty()) {
      Label empty = styled(new Label("Noch keine " + plural + " gelernt (eine je Grad)"), STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      container.getChildren().add(empty);
      return;
    }
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    addHeaderRow(grid, kind.getTechniqueName(), "Wirkung");
    int row = 1;
    for (TechniqueData technique : learned) {
      int count = initiation.count(technique);
      Label name = styled(new Label(count > 1 ? technique.name() + " (" + count + "×)" : technique.name()),
          STYLE_ATTR_NAME);
      Label effect = styled(new Label(technique.effectText(initiation.getGrade(), count)), "effect-text");
      effect.setWrapText(true);
      effect.setMaxWidth(520);
      addSourceTooltip(name, technique.source(), technique.page());
      addRowCells(grid, row++, name, effect);
    }
    container.getChildren().add(withRightFiller(grid));
  }

  /** Steigert den Grad: Prüfung im Modell, dann Karmabuchung mit Bestätigung (S. 232, 284). */
  private void onRaise(@NonNull Character ch, @NonNull Initiation initiation, @NonNull TechniqueKind kind) {
    String problem = initiation.getRaiseProblem();
    if (problem != null) {
      host.services().warn(kind.getProcessName() + " nicht möglich", problem);
      return;
    }
    String label = "%s %d".formatted(kind.getGradeName(), initiation.getGrade() + 1);
    if (host.services().karmaChargeFailed(ch, LogCategory.MAGIC_RESONANCE, label,
        initiation.getNextGradeKarmaCost())) {
      return;
    }
    initiation.raiseGrade();
    host.onInitiationChanged();
  }

  /** Öffnet die Auswahl und lernt die Technik (kein Karma). */
  private void onLearn(@NonNull Character ch, @NonNull Initiation initiation, @NonNull TechniqueKind kind) {
    String noChoice = initiation.getNoFreeChoiceProblem();
    if (noChoice != null) {
      host.services().warn(kind.getTechniqueName() + " lernen", noChoice);
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("TechniqueSelectDialog.fxml",
          kind.getTechniqueName() + " lernen");
      TechniqueSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(technique -> {
        initiation.learn(technique);
        host.onInitiationChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private static <T extends Label> @NonNull T styled(@NonNull T label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
