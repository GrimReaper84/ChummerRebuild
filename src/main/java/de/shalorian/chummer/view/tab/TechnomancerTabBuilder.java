package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.view.dialog.ComplexFormSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Technomancer-Tab (nur bei Resonanz sichtbar): Schwund-Formel und Komplexe Formen mit Stufen-Cap durch die
 * Resonanz. Ob der Tab angezeigt wird, entscheidet der Charakterbogen.
 */
public final class TechnomancerTabBuilder {

  private final @NonNull VBox technomancerContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> complexFormRefreshers = new ArrayList<>();
  private @Nullable Character character;

  public TechnomancerTabBuilder(@NonNull VBox technomancerContainer, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
    this.technomancerContainer = technomancerContainer;
    this.host = host;
    this.wiring = wiring;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Bewertet die +/−-Zeilen der Komplexen Formen neu (Karma, Resonanz-Cap). */
  public void refresh() {
    complexFormRefreshers.forEach(Runnable::run);
  }

  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    technomancerContainer.getChildren().clear();
    complexFormRefreshers.clear();

    Label sectionLabel = new Label("Technomancer");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    // Fester Anzeigetext statt Auswahl-Steuerelement: das SR4-Grundregelwerk kennt kein "Stream"-
    // Auswahlkonzept (0 Treffer im GRW-Kern-Volltext), die Schwund-Widerstandsformel gilt
    // bedingungslos für jeden Technomancer (siehe docs/plaene/archiv/technomancer_plan.md Abschnitt 13).
    Label fadingLabel = new Label("Schwund-Widerstand: Willenskraft + Resonanz");
    fadingLabel.getStyleClass().add(STYLE_ATTR_NAME);

    // Bewusst kein addBtn.setDisable() anhand von canAddComplexForm() an dieser Stelle: der
    // Tab-Inhalt wird nur bei setCharacter()/updateModeDisplay() neu gebaut, nicht bei jeder
    // Quality-Änderung (nur die Tab-Sichtbarkeit wird dort synchronisiert, siehe
    // syncTechnomancerTabVisibility()) — ein hier gesetzter Disabled-Zustand würde nach Erwerb der
    // Quality "Technomant" eingefroren bleiben. Die Prüfung erfolgt stattdessen live beim Klick in
    // onAddComplexForm() (analog dem Zauber-Button in MagicTabBuilder.build()).
    Button addBtn = new Button("Komplexe Form hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddComplexForm());

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
   * Baut das Komplexe-Formen-Grid inkl. Kopfzeile (Zeile 0) — Spaltenlayout analog
   * {@code SkillTabBuilder.buildSkillGrid()}, aber ohne Spezialisierungs-Spalten (Komplexe Formen kennen keine
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

    wiring.wire(complexFormRefreshers, new RowControls(ratingLabel, costLabel, minus, plus),
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

    removeBtn.setOnAction(_ -> {
      character.removeComplexForm(complexForm.getName());
      build();
      host.onBpChanged();
    });

    addRowCells(grid, row, nameLabel, categoryLabel, skillLabel, minus, ratingLabel, plus,
        costLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Komplexe-Formen-Auswahl-Dialog und lernt die gewählte Form mit Stufe 1 (SR4- Grundregelwerk: "lernt die neue Komplexe Form mit Stufe
   * 1"). Im Erstellungsmodus greift {@link Character#canAddComplexForm()}; im Karrieremodus kostet der Erwerb {@link ComplexForm#getKarmaCost(int)}
   * mit {@code currentRating=0}, analog anderen Karrieremodus-Käufen.
   */
  private void onAddComplexForm() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    if (!ch.canAddComplexForm()) {
      host.services().warn("Keine Resonanz", "Ohne Resonanzwert kann keine Komplexe Form gelernt werden");
      return;
    }
    try {
      DialogHandle dialog = host.services()
          .openDialog("ComplexFormSelectDialog.fxml", "Komplexe Form hinzufügen");
      ComplexFormSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(data -> {
        if (ch.isCreated()) {
          String label = "Komplexe Form gelernt: " + data.name();
          int karmaCost = ComplexForm.KARMA_NEW;
          if (host.services().karmaChargeFailed(ch, label, karmaCost)) {
            return;
          }
        }
        ch.addComplexForm(data);
        build();
        host.onBpChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }
}
