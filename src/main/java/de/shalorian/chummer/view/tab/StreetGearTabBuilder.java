package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.view.converter.LifestyleLevelStringConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Baut die Ausrüstungsbereiche Lebensstil, Rüstung, Waffen und Ausrüstung (ursprünglich der Tab
 * {@code tabStreetGearTabs} aus {@code frmCreate.Designer.cs}; die Rüstung baut {@link ArmorTabBuilder}, die Waffen
 * {@link WeaponTabBuilder}, die Ausrüstung {@link GearTabBuilder}).
 * Die Unter-Tabs werden bei jeder Änderung komplett neu aufgebaut; die Rüstungs-Kopfzeile
 * (Konstitution) und die Waffenwerte (Stärke, Fertigkeiten) hängen zusätzlich am Charakter und werden
 * deshalb über {@link #refresh()} beim Refresh des {@code CharacterViewController} nachgezogen. Alles
 * außerhalb dieses Tabs erreicht der Builder ausschließlich über den {@link CharacterTabHost}.
 */
public final class StreetGearTabBuilder {


  private final @NonNull VBox lifestyleContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull WeaponTabBuilder weaponTab;
  private final @NonNull GearTabBuilder gearTab;
  private final @NonNull ArmorTabBuilder armorTab;

  public StreetGearTabBuilder(@NonNull VBox lifestyleContainer, @NonNull VBox armorContainer,
      @NonNull VBox weaponContainer, @NonNull VBox gearContainer, @NonNull CharacterTabHost host) {
    this.lifestyleContainer = lifestyleContainer;
    this.armorTab = new ArmorTabBuilder(armorContainer, host);
    this.host = host;
    this.weaponTab = new WeaponTabBuilder(weaponContainer, host);
    this.gearTab = new GearTabBuilder(gearContainer, host);
  }

  /** Baut nur die Waffen neu (Waffen aus Rüstung kommen und gehen mit angelegten Schilden, GRW S. 373). */
  public void rebuildWeapons(@NonNull Character character) {
    weaponTab.build(character);
  }

  /** Baut alle Unter-Tabs für {@code character} neu auf. */
  public void build(@NonNull Character character) {
    buildLifestyleTab(character);
    armorTab.build(character);
    weaponTab.build(character);
    gearTab.build(character);
  }

  /**
   * Zieht die von Attributen und Fertigkeiten abhängigen Anzeigen nach: Schutz/Behinderung der Rüstung
   * sowie der Detailbereich der gewählten Waffe (Schaden, Reichweiten, Fertigkeitswert).
   */
  public void refresh() {
    armorTab.refresh();
    weaponTab.refresh();
  }

  // ── Lebensstil ────────────────────────────────────────────

  /**
   * Lebensstil-Unter-Tab: Stufe und im Voraus bezahlte Monate (GRW S. 111), Kosten und — bei der
   * Erschaffung — der Startkapital-Wurf, den die Stufe beim Abschluss festlegt (GRW S. 114). Im
   * Karrieremodus nur Anzeige; laufende Lebensstilzahlungen werden über das Nuyen-Log gebucht.
   */
  private void buildLifestyleTab(@NonNull Character character) {
    lifestyleContainer.getChildren().clear();


    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    lifestyleContainer.getChildren().add(withRightFiller(grid));

    if (character.isCreated()) {
      fillLifestyleInCareerMode(grid, character.getEquipment().getLifestyle());
    } else {
      fillLifestyleInCreationMode(grid, character);
    }
  }

  private void fillLifestyleInCreationMode(@NonNull MigPane grid, @NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    LifestyleLevel level = lifestyle.getLevel();
    addLifestyleRow(grid, 0, "Stufe:", buildLifestyleLevelBox(character));
    addLifestyleRow(grid, 1, "Monate im Voraus:", buildLifestyleMonthsStepper(character));
    addLifestyleCostRows(grid, 2, lifestyle);
    addLifestyleRow(grid, 4, "Startkapital-Wurf:", new Label(
        level.getDice() + "W6 × " + SpendCurrency.NUYEN.formatAmount(level.getMultiplier())));
    addLifestyleHint("Die Stufe bestimmt beim Abschluss den Startkapital-Wurf; nicht ausgegebene "
        + "Ressourcen verfallen (GRW S. 114).");
  }

  private void fillLifestyleInCareerMode(@NonNull MigPane grid, @NonNull Lifestyle lifestyle) {
    addLifestyleRow(grid, 0, "Stufe:", new Label(lifestyle.getLevel().getDisplayName()));
    addLifestyleRow(grid, 1, "Monate im Voraus:", new Label(String.valueOf(lifestyle.getMonths())));
    addLifestyleCostRows(grid, 2, lifestyle);
    addLifestyleHint("Lebensstilzahlungen im Karrieremodus bitte über das Nuyen-Log buchen.");
  }

  private void addLifestyleCostRows(@NonNull MigPane grid, int row, @NonNull Lifestyle lifestyle) {
    addLifestyleRow(grid, row, "Monatskosten:",
        new Label(SpendCurrency.NUYEN.formatAmount(lifestyle.getLevel().getMonthlyCost())));
    addLifestyleRow(grid, row + 1, "Gesamtkosten:",
        new Label(SpendCurrency.NUYEN.formatAmount(lifestyle.getNuyenCost())));
  }

  private @NonNull ComboBox<LifestyleLevel> buildLifestyleLevelBox(@NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    ComboBox<LifestyleLevel> levelBox = new ComboBox<>();
    levelBox.getItems().setAll(LifestyleLevel.values());
    levelBox.setConverter(new LifestyleLevelStringConverter());
    levelBox.setValue(lifestyle.getLevel());
    levelBox.valueProperty().addListener((_, _, val) -> {
      if (val != null) {
        lifestyle.setLevel(val);
        onLifestyleChanged(character);
      }
    });
    return levelBox;
  }

  private @NonNull HBox buildLifestyleMonthsStepper(@NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    Button minus = new Button("−");
    Label value = new Label(String.valueOf(lifestyle.getMonths()));
    Button plus = new Button("+");
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    value.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    // Der Tab wird nach jeder Änderung neu aufgebaut, daher bleibt dieser Zustand aktuell.
    minus.setDisable(lifestyle.getMonths() <= Lifestyle.MIN_MONTHS);
    minus.setOnAction(_ -> {
      if (lifestyle.getMonths() > Lifestyle.MIN_MONTHS) {
        lifestyle.setMonths(lifestyle.getMonths() - 1);
        onLifestyleChanged(character);
      }
    });
    plus.setOnAction(_ -> {
      lifestyle.setMonths(lifestyle.getMonths() + 1);
      onLifestyleChanged(character);
    });
    HBox box = new HBox(6, minus, value, plus);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private static void addLifestyleRow(@NonNull MigPane grid, int row, @NonNull String caption,
      @NonNull Node control) {
    Label captionLabel = new Label(caption);
    captionLabel.getStyleClass().add(STYLE_ATTR_NAME);
    if (control instanceof Label label) {
      label.getStyleClass().add(STYLE_ATTR_BP);
    }
    addRowCells(grid, row, captionLabel, control);
  }

  private void addLifestyleHint(@NonNull String text) {
    Label hint = new Label(text);
    hint.getStyleClass().add(STYLE_ATTR_RANGE);
    VBox.setMargin(hint, new Insets(12, 0, 0, 0));
    lifestyleContainer.getChildren().add(hint);
  }

  private void onLifestyleChanged(@NonNull Character character) {
    buildLifestyleTab(character);
    host.onNuyenSpentChanged();
  }
}
