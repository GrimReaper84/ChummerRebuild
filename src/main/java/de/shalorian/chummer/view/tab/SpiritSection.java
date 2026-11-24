package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.iconTextButton;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.spirit.Spirits;
import de.shalorian.chummer.model.critter.CritterPowerData;
import de.shalorian.chummer.model.critter.CritterPowerDataLoader;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.SpiritSelectDialogController;
import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Abschnitt Geister (im Bereich Magie) bzw. Sprites (im Bereich Technomancer), GRW S. 112/113, 222, 281/282: Kopf mit
 * Zahl der gebundenen bzw. registrierten Einträge gegen Charisma und BP bei der Erschaffung, je Eintrag Art, Kraft bzw.
 * Stufe, Dienste mit +/−, Zustand und Entfernen, darunter Hinweise auf Einträge, die nicht mehr den Regeln entsprechen.
 * Aufgeklappt zeigt ein Eintrag Werteblock, Bewegung, Fertigkeiten, Kräfte (Wirkung als Tooltip je Kraft), die
 * gewählten optionalen Kräfte bzw. Komplexen Formen und Schwächen; fehlende Optionen lassen sich dort nachtragen. Nach
 * jeder Änderung baut sich der Abschnitt selbst neu und meldet BP über {@link CharacterTabHost}; der Tab baut ihn bei {@code refresh()} neu.
 */
final class SpiritSection {

  private final @NonNull SpiritKind kind;
  private final @NonNull CharacterTabHost host;
  private final @NonNull VBox box = new VBox();
  /** Aufgeklappte Einträge (Identität); bleibt beim Neuaufbau erhalten, wird nicht gespeichert. */
  private final @NonNull Set<Spirit> expanded = Collections.newSetFromMap(new IdentityHashMap<>());

  SpiritSection(@NonNull SpiritKind kind, @NonNull CharacterTabHost host) {
    this.kind = kind;
    this.host = host;
  }

  /** Baut den Abschnitt für {@code ch} (neu) und liefert ihn zum Einhängen. */
  @NonNull Node build(@NonNull Character ch) {
    box.getChildren().clear();
    Spirits spirits = ch.getSpirits();

    Separator separator = new Separator();
    VBox.setMargin(separator, new Insets(20, 0, 12, 0));
    Label title = styled(new Label(kind.getPluralName()), STYLE_SECTION_TITLE);
    String count = "%s %d / Charisma %d".formatted(kind.getBoundName(), spirits.countBound(kind),
        spirits.getBoundLimit());
    Label countLabel = styled(new Label(ch.isCreated() ? count : count + " · " + spirits.getBpCost() + " BP"),
        STYLE_BP_LABEL);
    Button addBtn = iconTextButton("Hinzufügen", FontAwesomeSolid.PLUS, kind.getDisplayName() + " hinzufügen");
    addBtn.setOnAction(_ -> onAdd(ch));
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(12, title, countLabel, spacer, addBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    box.getChildren().addAll(separator, header);
    for (String problem : spirits.getProblems(kind)) {
      Label warning = styled(new Label(problem), STYLE_ATTR_RANGE);
      warning.setWrapText(true);
      setWarnStyle(warning, true);
      VBox.setMargin(warning, new Insets(6, 0, 0, 0));
      box.getChildren().add(warning);
    }

    List<Spirit> entries = spirits.getEntries(kind);
    if (entries.isEmpty()) {
      Label empty = styled(new Label("Keine " + kind.getPluralName()), STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      box.getChildren().add(empty);
      return box;
    }
    expanded.retainAll(entries);
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][][center][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    addHeaderRow(grid, "", kind.getDisplayName(), kind.getPowerName(), "Dienste", "Zustand", "");
    int row = 1;
    for (Spirit spirit : entries) {
      addRow(grid, row++, ch, spirit);
      if (expanded.contains(spirit)) {
        grid.add(details(ch, spirit), "cell 1 " + row++ + " 5 1");
      }
    }
    box.getChildren().add(withRightFiller(grid));
    return box;
  }

  private void addRow(@NonNull MigPane grid, int row, @NonNull Character ch, @NonNull Spirit spirit) {
    Spirits spirits = ch.getSpirits();
    SpiritData data = spirit.getData();
    String category = data.categoryIn(ch.getTradition());
    boolean open = expanded.contains(spirit);
    Button toggle = actionButton(open ? "Zuklappen" : "Aufklappen",
        open ? FontAwesomeSolid.CHEVRON_DOWN : FontAwesomeSolid.CHEVRON_RIGHT);
    toggle.setOnAction(_ -> {
      if (!expanded.remove(spirit)) {
        expanded.add(spirit);
      }
      build(ch);
    });
    Label name = styled(new Label(category == null ? data.name() : data.name() + " · " + category), STYLE_ATTR_NAME);
    name.setTooltip(new Tooltip(data.source() + " S. " + data.page()));
    Label force = styled(new Label(String.valueOf(spirit.getForce())), STYLE_ATTR_VALUE);

    Button minus = actionButton("Dienst weniger", FontAwesomeSolid.MINUS);
    minus.setOnAction(_ -> changeServices(ch, spirit, spirit.getServices() - 1));
    Label services = styled(new Label(String.valueOf(spirit.getServices())), STYLE_ATTR_VALUE);
    Button plus = actionButton("Dienst mehr", FontAwesomeSolid.PLUS);
    plus.setOnAction(_ -> changeServices(ch, spirit, spirit.getServices() + 1));
    HBox serviceBox = new HBox(6, minus, services, plus);
    serviceBox.setAlignment(Pos.CENTER);

    Button state = new Button(spirit.isBound() ? kind.getBoundName() : kind.getUnboundName());
    state.getStyleClass().add(STYLE_BTN_SECONDARY);
    state.setTooltip(new Tooltip(spirit.isBound() ? "Klicken zum Lösen" : "Klicken zum " + kind.getBindVerb()));
    state.setOnAction(_ -> toggleBound(ch, spirit));

    Button remove = actionButton("Entfernen", FontAwesomeSolid.TRASH);
    remove.setOnAction(_ -> {
      spirits.remove(spirit);
      changed(ch);
    });
    addRowCells(grid, row, toggle, name, force, serviceBox, state, remove);
  }

  /**
   * Aufgeklappter Teil: Werteblock, Bewegung, Fertigkeiten und Komplexe Formen mit Kraft bzw. Stufe, Kräfte und gewählte
   * Optionen als einzelne Labels mit der Wirkung als Tooltip (gemeinsamer Kräftekatalog, ADR-064) und Schwächen; fehlen
   * Optionen, eine Auswahl zum Nachtragen. Die Quelle steht im Tooltip am Namen.
   */
  private @NonNull Node details(@NonNull Character ch, @NonNull Spirit spirit) {
    SpiritData data = spirit.getData();
    int force = spirit.getForce();
    VBox details = new VBox(6);
    details.getChildren().add(detailText(data.statsText(force)));
    if (!data.movement().isEmpty()) {
      details.getChildren().add(detailText("Bewegung: " + data.movement()));
    }
    details.getChildren().add(detailText("Fertigkeiten (" + force + "): " + String.join(", ", data.skills())));
    if (!data.complexForms().isEmpty()) {
      details.getChildren().add(detailText("Komplexe Formen (" + force + "): " + String.join(", ", data.complexForms())));
    }
    details.getChildren().add(powerLine("Kräfte", data.powers()));
    String optionsTitle = "%s (%d von %d)".formatted(kind.getOptionsName(), spirit.getOptions().size(),
        data.optionalCount(force));
    details.getChildren().add(spirit.getOptions().isEmpty() ? detailText(optionsTitle + ": keine")
        : powerLine(optionsTitle, spirit.getOptions()));
    if (spirit.getMissingOptionCount() > 0) {
      details.getChildren().add(optionAdder(ch, spirit));
    }
    if (!data.weaknesses().isEmpty()) {
      details.getChildren().add(detailText("Schwächen: " + String.join(", ", data.weaknesses())));
    }
    return details;
  }

  /** {@code title} mit Doppelpunkt und je Kraft ein Label; Kräfte aus dem Katalog tragen ihre Wirkung als Tooltip. */
  private static @NonNull Node powerLine(@NonNull String title, @NonNull List<String> powers) {
    FlowPane line = new FlowPane(6, 4, detailText(title + ":"));
    for (int i = 0; i < powers.size(); i++) {
      String power = powers.get(i);
      Label label = styled(new Label(i < powers.size() - 1 ? power + "," : power), STYLE_ATTR_NAME);
      CritterPowerData powerData = CritterPowerDataLoader.findForEntry(power);
      if (powerData != null) {
        label.setTooltip(new Tooltip(powerData.describe()));
      }
      line.getChildren().add(label);
    }
    return line;
  }

  /** Nachtragen einer fehlenden Option; einmal gewählte bleiben (GRW S. 347, 282). */
  private @NonNull Node optionAdder(@NonNull Character ch, @NonNull Spirit spirit) {
    ComboBox<String> choice = new ComboBox<>();
    choice.getItems().setAll(spirit.getData().optional().stream().filter(o -> !spirit.getOptions().contains(o))
        .toList());
    choice.setPromptText(kind.getOptionsName());
    Button add = new Button("Nachtragen");
    add.getStyleClass().add(STYLE_BTN_SECONDARY);
    add.disableProperty().bind(choice.valueProperty().isNull());
    add.setOnAction(_ -> {
      ch.getSpirits().addMissingOption(spirit, choice.getValue());
      changed(ch);
    });
    HBox row = new HBox(8, choice, add);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private static @NonNull Label detailText(@NonNull String text) {
    Label label = styled(new Label(text), STYLE_ATTR_RANGE);
    label.setWrapText(true);
    label.setMaxWidth(640);
    return label;
  }

  private void changeServices(@NonNull Character ch, @NonNull Spirit spirit, int services) {
    String problem = ch.getSpirits().getServicesProblem(spirit, services);
    if (problem == null && spirit.isBound() && services > spirit.getServices()) {
      // jeder Dienst eines gebundenen Eintrags kostet bei der Erschaffung 1 BP (GRW S. 112/113)
      problem = ch.getBpShortfallProblem(services - spirit.getServices());
    }
    if (problem != null) {
      host.services().warn("Dienste", problem);
      return;
    }
    ch.getSpirits().setServices(spirit, services);
    changed(ch);
  }

  private void toggleBound(@NonNull Character ch, @NonNull Spirit spirit) {
    if (!spirit.isBound()) {
      String problem = ch.getSpirits().getBindProblem(spirit);
      if (problem != null) {
        host.services().warn(kind.getDisplayName(), problem);
        return;
      }
    }
    ch.getSpirits().setBound(spirit, !spirit.isBound());
    changed(ch);
  }

  private void onAdd(@NonNull Character ch) {
    try {
      DialogHandle dialog = host.services().openDialog("SpiritSelectDialog.fxml",
          kind.getDisplayName() + " hinzufügen");
      SpiritSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setTarget(ch, kind);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(spirit -> {
        ch.getSpirits().add(spirit);
        changed(ch);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private void changed(@NonNull Character ch) {
    build(ch);
    host.onBpChanged();
  }

  private static @NonNull Button actionButton(@NonNull String text, @NonNull FontAwesomeSolid icon) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    return button;
  }

  private static <T extends Label> @NonNull T styled(@NonNull T label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
