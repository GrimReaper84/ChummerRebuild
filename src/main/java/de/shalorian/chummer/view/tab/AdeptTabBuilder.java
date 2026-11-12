package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowers;
import de.shalorian.chummer.view.dialog.AdeptPowerSelectDialogController;
import de.shalorian.chummer.view.dialog.AdeptPowerSelectResult;
import de.shalorian.chummer.view.dialog.DialogHandle;
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
 * Tab „Adeptenkräfte“ (nur für Adepten und Magieradepten sichtbar, GRW S. 229–232): Kraftpunkte mit Rest, beim
 * Magieradepten die Aufteilung der Magie, und die gekauften Kräfte mit Stufe und Kosten. Kräfte kosten nur
 * Kraftpunkte, daher gibt es keine BP- oder Karma-Abbuchung. Aufbau nach {@code tabAdept} im alten {@code
 * frmCreate.Designer.cs}; die Aufteilung lag dort im Zauber-Tab und steht hier bei den Kräften (Nutzerentscheidung).
 */
public final class AdeptTabBuilder {

  private final @NonNull VBox adeptContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> refreshers = new ArrayList<>();
  private @Nullable Character character;

  public AdeptTabBuilder(@NonNull VBox adeptContainer, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
    this.adeptContainer = adeptContainer;
    this.host = host;
    this.wiring = wiring;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Kraftpunkte, Stufen und Kosten neu bewerten (z.B. nach geänderter Magie oder geändertem Attribut). */
  public void refresh() {
    refreshers.forEach(Runnable::run);
  }

  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    adeptContainer.getChildren().clear();
    refreshers.clear();

    Label pointsLabel = new Label();
    pointsLabel.getStyleClass().add(STYLE_BP_VALUE);
    refreshers.add(() -> {
      pointsLabel.setText("Kraftpunkte: %s (%s übrig)".formatted(
          AdeptPowers.formatPoints(ch.getAdeptPowers().getPowerPoints()),
          AdeptPowers.formatPoints(ch.getAdeptPowers().getRemainingPowerPoints())));
      String problem = ch.getAdeptPowers().getProblem();
      setWarnStyle(pointsLabel, problem != null);
      pointsLabel.setTooltip(problem == null ? null : new Tooltip(problem));
    });

    // Prüfung beim Klick statt setDisable() hier: der Tab wird nicht bei jeder Magieänderung neu gebaut.
    Button addBtn = new Button("Kraft hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddPower());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(12, pointsLabel, spacer, addBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    adeptContainer.getChildren().add(header);

    if (ch.isMysticAdept()) {
      adeptContainer.getChildren().add(buildMagicSplitRow(ch));
    }

    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][center][center][center][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    addHeaderRow(grid, "Kraft", "", "Stufe", "", "Kraftpunkte", "Seite", "");
    adeptContainer.getChildren().add(withRightFiller(grid));

    int row = 1;
    for (AdeptPower power : ch.getAdeptPowers().getPowers()) {
      addPowerRow(grid, row++, power, ch);
    }
    refresh();
  }

  /**
   * Magieradept: Magiepunkte für Kräfte (+/−, ohne Kosten) und die übrige Magie für Zauberei (GRW S. 229). Senken geht
   * nur bei der Erschaffung und nicht unter die ausgegebenen Kraftpunkte ({@link AdeptPowers#getMinAdeptMagic()}).
   */
  private @NonNull HBox buildMagicSplitRow(@NonNull Character ch) {
    Label title = new Label("Magie für Kräfte:");
    title.getStyleClass().add(STYLE_ATTR_NAME);
    Button minus = new Button("−");
    Button plus = new Button("+");
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    Label value = new Label();
    value.getStyleClass().add(STYLE_ATTR_VALUE);
    Label unused = new Label();
    Label spellcasting = new Label();
    spellcasting.getStyleClass().add(STYLE_BP_LABEL);
    refreshers.add(() -> spellcasting.setText("Zauberei: " + ch.getSpellcastingMagic()));

    wiring.wireFree(refreshers, new RowControls(value, unused, minus, plus), new RowModel() {
      @Override
      public int value() {
        return ch.getAdeptPowers().getAdeptMagic();
      }

      @Override
      public int min() {
        return ch.getAdeptPowers().getMinAdeptMagic();
      }

      @Override
      public int max() {
        return ch.getEffectiveMagicOrResonanceValue();
      }

      @Override
      public int bpStepCost() {
        return 0;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return "";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        ch.getAdeptPowers().setAdeptMagic(ch.getAdeptPowers().getAdeptMagic() + 1);
      }

      @Override
      public void decrement() {
        ch.getAdeptPowers().setAdeptMagic(ch.getAdeptPowers().getAdeptMagic() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return "Magie für Kräfte " + from + " → " + to;
      }
    });

    HBox row = new HBox(8, title, minus, value, plus, spellcasting);
    row.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(row, new Insets(12, 0, 0, 0));
    return row;
  }

  private void addPowerRow(@NonNull MigPane grid, int row, @NonNull AdeptPower power, @NonNull Character ch) {
    Label nameLabel = new Label(power.displayName());
    nameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    nameLabel.setTooltip(new Tooltip(power.getData().note()));
    Button minus = new Button("−");
    Button plus = new Button("+");
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    boolean leveled = power.getData().leveled();
    minus.setVisible(leveled);
    plus.setVisible(leveled);
    Label levelLabel = new Label();
    levelLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    Label costLabel = new Label();
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    Label pageLabel = new Label("S. " + power.getData().page());
    pageLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    Button removeBtn = new Button("Entfernen");
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    refreshers.add(() -> {
      levelLabel.setText(leveled ? String.valueOf(power.getLevel()) : "–");
      costLabel.setText(AdeptPowers.formatPoints(ch.getAdeptPowers().getCost(power)));
      minus.setDisable(!ch.getAdeptPowers().canLower(power));
      plus.setDisable(!ch.getAdeptPowers().canRaise(power));
    });
    minus.setOnAction(_ -> {
      if (ch.getAdeptPowers().canLower(power)) {
        ch.getAdeptPowers().lower(power);
        host.onAdeptPowersChanged();
      }
    });
    plus.setOnAction(_ -> {
      if (ch.getAdeptPowers().canRaise(power)) {
        ch.getAdeptPowers().raise(power);
        host.onAdeptPowersChanged();
      }
    });
    removeBtn.setOnAction(_ -> onRemovePower(ch, power));

    addRowCells(grid, row, nameLabel, minus, levelLabel, plus, costLabel, pageLabel, removeBtn);
  }

  private void onRemovePower(@NonNull Character ch, @NonNull AdeptPower power) {
    List<String> consequences = ch.getAdeptPowerRemovalConsequences(power);
    if (!consequences.isEmpty() && !host.services().confirmYesNo("Kraft entfernen",
        power.displayName() + " entfernen?", String.join("\n", consequences))) {
      return;
    }
    ch.removeAdeptPower(power);
    host.onAdeptPowersChanged();
  }

  /** Öffnet die Kraftauswahl; mit „OK &amp; Weiteres“ öffnet sie sich nach dem Kauf erneut. */
  private void onAddPower() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean again = true;
    while (again) {
      Optional<AdeptPowerSelectResult> result = openSelectDialog(ch);
      if (result.isEmpty()) {
        return;
      }
      ch.getAdeptPowers().add(result.get().data(), result.get().choice());
      host.onAdeptPowersChanged();
      again = result.get().addAnother();
    }
  }

  private @NonNull Optional<AdeptPowerSelectResult> openSelectDialog(@NonNull Character ch) {
    try {
      DialogHandle dialog = host.services().openDialog("AdeptPowerSelectDialog.fxml", "Kraft hinzufügen");
      AdeptPowerSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();
      return ctrl.getResult();
    } catch (IOException _) {
      host.services().showDialogLoadError();
      return Optional.empty();
    }
  }
}
