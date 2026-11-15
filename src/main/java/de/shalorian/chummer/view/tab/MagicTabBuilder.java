package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.view.converter.TraditionStringConverter;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.SpellSelectDialogController;
import de.shalorian.chummer.view.grid.GridSupport;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
 * Magie-Tab (nur für Magier sichtbar): Tradition samt Entzugsformel und die gelernten Zauber. Ob der Tab
 * angezeigt wird, entscheidet der Charakterbogen.
 */
public final class MagicTabBuilder {

  private final @NonNull VBox magicContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull SpiritSection spiritSection;
  private final @NonNull FociSection fociSection = new FociSection();
  private final @NonNull SituationalPoolSection poolSection = new SituationalPoolSection();
  private @Nullable Character character;

  public MagicTabBuilder(@NonNull VBox magicContainer, @NonNull CharacterTabHost host) {
    this.magicContainer = magicContainer;
    this.host = host;
    this.spiritSection = new SpiritSection(SpiritKind.SPIRIT, host);
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /**
   * Baut die Abschnitte Geister, Foki und situative Pools neu, damit Charisma, Magie, Fertigkeiten und Hinweise nach
   * Änderungen stimmen.
   */
  public void refresh() {
    if (character != null) {
      spiritSection.build(character);
      fociSection.build(character);
      poolSection.build(character);
    }
  }

  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    magicContainer.getChildren().clear();


    ComboBox<Tradition> traditionBox = new ComboBox<>();
    traditionBox.getItems().setAll(Tradition.values());
    traditionBox.setConverter(new TraditionStringConverter());
    traditionBox.setValue(ch.getTradition());
    traditionBox.valueProperty().addListener((_, _, val) -> {
      if (val != null) {
        ch.setTradition(val);
      }
    });

    Button addBtn = new Button("Zauber hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddSpell());

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(traditionBox);
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
    magicContainer.getChildren().addAll(spiritSection.build(ch), fociSection.build(ch), poolSection.build(ch));
  }

  /**
   * Baut das Zauber-Grid inkl. Kopfzeile (Zeile 0). Alle Datenspalten bleiben bei ihrer Inhaltsbreite (feststehendes Vokabular aus den
   * Zauber-Stammdaten, kein eigenes Wachstum nötig) — ungenutzter Platz wird stattdessen außen über {@link GridSupport#withRightFiller} verteilt. Das
   * Grid wird bei jeder Änderung komplett neu aufgebaut (kein In-Place-Refresh einzelner Zellen), daher gibt es hier keine Wert-Spalten-Ausnahme wie
   * bei Attributen.
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

    removeBtn.setOnAction(_ -> {
      character.removeSpell(spell.name());
      build();
      host.onBpChanged();
    });

    addRowCells(grid, row, spellNameLabel, categoryLabel, typeLabel, rangeLabel, damageLabel,
        durationLabel, dvLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Zauber-Auswahl-Dialog und fügt den gewählten Zauber hinzu. Im Erstellungsmodus greift das Cap aus
   * {@link Character#canAddSpellAtCreation()}; im Karrieremodus kostet jeder neue Zauber {@link Character#getSpellKarmaCost()} Karma
   * (Bestätigungsdialog analog anderen Karrieremodus-Käufen, siehe {@link TabServices#karmaChargeFailed}).
   */
  private void onAddSpell() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    if (!ch.isCreated() && !ch.canAddSpellAtCreation()) {
      host.services().warn("Zauberlimit erreicht",
          "Kein weiterer Zauber ohne höheres Spruchzauberei-/Ritualzauberei-Rating möglich");
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("SpellSelectDialog.fxml", "Zauber hinzufügen");
      SpellSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(spellData -> {
        if (ch.isCreated()) {
          String label = "Zauber gelernt: " + spellData.name();
          if (host.services().karmaChargeFailed(ch, LogCategory.MAGIC_RESONANCE, label,
              ch.getSpellKarmaCost())) {
            return;
          }
        }
        ch.addSpell(spellData);
        build();
        host.onBpChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }
}
