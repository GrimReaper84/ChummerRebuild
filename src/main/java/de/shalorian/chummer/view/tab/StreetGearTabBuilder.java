package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
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
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.view.converter.LifestyleLevelStringConverter;
import de.shalorian.chummer.view.dialog.ArmorSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Baut den Tab "Straßenausrüstung" mit seinen Unter-Tabs Lebensstil, Rüstung, Waffen und
 * Ausrüstung (Aufbau nach dem alten {@code tabStreetGearTabs} aus {@code frmCreate.Designer.cs}; den
 * Waffen-Unter-Tab baut {@link WeaponTabBuilder}, den Ausrüstungs-Unter-Tab {@link GearTabBuilder}).
 * Die Unter-Tabs werden bei jeder Änderung komplett neu aufgebaut; die Rüstungs-Kopfzeile
 * (Konstitution) und die Waffenwerte (Stärke, Fertigkeiten) hängen zusätzlich am Charakter und werden
 * deshalb über {@link #refresh()} beim Refresh des {@code CharacterViewController} nachgezogen. Alles
 * außerhalb dieses Tabs erreicht der Builder ausschließlich über den {@link CharacterTabHost}.
 */
public final class StreetGearTabBuilder {


  private final @NonNull VBox lifestyleContainer;
  private final @NonNull VBox armorContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull WeaponTabBuilder weaponTab;
  private final @NonNull GearTabBuilder gearTab;
  private final @NonNull List<Runnable> armorRefreshers = new ArrayList<>();

  public StreetGearTabBuilder(@NonNull VBox lifestyleContainer, @NonNull VBox armorContainer,
      @NonNull VBox weaponContainer, @NonNull VBox gearContainer, @NonNull CharacterTabHost host) {
    this.lifestyleContainer = lifestyleContainer;
    this.armorContainer = armorContainer;
    this.host = host;
    this.weaponTab = new WeaponTabBuilder(weaponContainer, host);
    this.gearTab = new GearTabBuilder(gearContainer, host);
  }

  /** Baut alle Unter-Tabs für {@code character} neu auf. */
  public void build(@NonNull Character character) {
    buildLifestyleTab(character);
    buildArmorTab(character);
    weaponTab.build(character);
    gearTab.build(character);
  }

  /**
   * Zieht die von Attributen und Fertigkeiten abhängigen Anzeigen nach: Schutz/Behinderung der Rüstung
   * sowie der Detailbereich der gewählten Waffe (Schaden, Reichweiten, Fertigkeitswert).
   */
  public void refresh() {
    armorRefreshers.forEach(Runnable::run);
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

    Label sectionLabel = new Label("Lebensstil");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    lifestyleContainer.getChildren().add(sectionLabel);

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

  // ── Rüstung ───────────────────────────────────────────────

  /**
   * Rüstungs-Unter-Tab: Kopfzeile mit Schutzwert (GRW S. 190/372) und Panzerungsbehinderung (GRW
   * S. 190), Werkzeugleiste (Hinzufügen, "Alle anlegen"/"Alle ablegen" aus {@code
   * frmCreate.Designer.cs}) und ein Grid der gekauften Teile mit "Angelegt"-Häkchen.
   */
  private void buildArmorTab(@NonNull Character character) {
    armorContainer.getChildren().clear();
    armorRefreshers.clear();

    armorContainer.getChildren().addAll(buildArmorSummaryHeader(character),
        buildArmorToolbar(character));

    List<Armor> pieces = character.getEquipment().getArmor();
    if (pieces.isEmpty()) {
      Label empty = new Label("Keine Rüstung gekauft");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      armorContainer.getChildren().add(empty);
      return;
    }
    armorContainer.getChildren().add(withRightFiller(buildArmorGrid(character, pieces)));
  }

  /** Kopfzeile mit Schutz, Behinderung und Kosten; Schutz/Behinderung als Refresher registriert (KON-abhängig). */
  private @NonNull HBox buildArmorSummaryHeader(@NonNull Character character) {
    Label sectionLabel = new Label("Rüstung");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    Label protectionLabel = new Label();
    protectionLabel.getStyleClass().add(STYLE_BP_VALUE);
    Label encumbranceLabel = new Label();
    encumbranceLabel.getStyleClass().add(STYLE_BP_LABEL);
    Label costLabel =
        new Label("Kosten: "
            + SpendCurrency.NUYEN.formatAmount(character.getEquipment().getArmorNuyenCost()));
    costLabel.getStyleClass().add(STYLE_BP_LABEL);

    Runnable refreshSummary = () -> {
      protectionLabel.setText(
          "Schutz: " + character.getArmorBallistic() + "/" + character.getArmorImpact());
      int penalty = character.getEquipment().getArmorEncumbrancePenalty();
      encumbranceLabel.setText(penalty == 0
          ? "Keine Behinderung"
          : "Behinderung: " + penalty + " (GES/REA)");
      setWarnStyle(encumbranceLabel, penalty < 0);
    };
    armorRefreshers.add(refreshSummary);
    refreshSummary.run();

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(sectionLabel, protectionLabel, encumbranceLabel, spacer, costLabel);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    return header;
  }

  private @NonNull HBox buildArmorToolbar(@NonNull Character character) {
    Button addBtn = toolbarButton("Rüstung hinzufügen", FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddArmor(character));
    Button equipAllBtn = toolbarButton("Alle anlegen", FontAwesomeSolid.CHECK_DOUBLE);
    equipAllBtn.setOnAction(_ -> onSetAllArmorEquipped(character, true));
    Button unequipAllBtn = toolbarButton("Alle ablegen", FontAwesomeSolid.SQUARE);
    unequipAllBtn.setOnAction(_ -> onSetAllArmorEquipped(character, false));

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, equipAllBtn, unequipAllBtn, addBtn);
    return toolbar;
  }

  private static @NonNull Button toolbarButton(@NonNull String text, @NonNull FontAwesomeSolid icon) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    return button;
  }

  /** Rüstungs-Grid inkl. Kopfzeile (Zeile 0), inhaltsbasierte Spalten wie beim Cyberware-Grid. */
  private @NonNull MigPane buildArmorGrid(@NonNull Character character,
      @NonNull List<Armor> pieces) {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][center][center][right][][center][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    addHeaderRow(grid, "Rüstung", "Kategorie", "B/S", "Verf.", "Preis", "Quelle", "Angelegt", "");

    int row = 1;
    for (Armor piece : pieces) {
      addArmorRow(grid, row++, piece, character);
    }
    return grid;
  }

  private void addArmorRow(@NonNull MigPane grid, int row, @NonNull Armor piece,
      @NonNull Character character) {
    ArmorData data = piece.getData();
    Label armorNameLabel = new Label(piece.getName());
    Label categoryLabel = new Label(data.category().getDisplayName());
    Label protectionLabel = new Label(data.protectionText());
    Label availabilityLabel = new Label(data.availabilityText());
    Label priceLabel = new Label(SpendCurrency.NUYEN.formatAmount(piece.getNuyenCost()));
    Label sourceLabel = new Label(data.source() + " S. " + data.page());
    CheckBox equippedBox = new CheckBox();
    Button removeBtn = new Button("Entfernen");

    armorNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    protectionLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    availabilityLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    priceLabel.getStyleClass().add(STYLE_ATTR_BP);
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    equippedBox.setSelected(piece.isEquipped());
    equippedBox.setOnAction(_ -> {
      piece.setEquipped(equippedBox.isSelected());
      onArmorChanged(character);
    });
    removeBtn.setOnAction(_ -> {
      character.getEquipment().removeArmor(piece);
      onArmorChanged(character);
    });

    addRowCells(grid, row, armorNameLabel, categoryLabel, protectionLabel, availabilityLabel,
        priceLabel, sourceLabel, equippedBox, removeBtn);
  }

  /**
   * Öffnet den Rüstungs-Auswahldialog und kauft die Auswahl. Bei der Erschaffung zählt der Preis gegen
   * das Ressourcengeld, im Karrieremodus wird er nach Bestätigung vom Nuyen-Guthaben abgebucht.
   */
  private void onAddArmor(@NonNull Character character) {
    try {
      DialogHandle dialog = host.services().openDialog("ArmorSelectDialog.fxml", "Rüstung hinzufügen");
      ArmorSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(piece -> {
        if (character.isCreated()
            && host.services().nuyenChargeFailed(character, "Kauf: " + piece.getName(), piece.getNuyenCost())) {
          return;
        }
        character.getEquipment().addArmor(piece);
        onArmorChanged(character);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private void onSetAllArmorEquipped(@NonNull Character character, boolean equipped) {
    character.getEquipment().getArmor().forEach(piece -> piece.setEquipped(equipped));
    onArmorChanged(character);
  }

  private void onArmorChanged(@NonNull Character character) {
    buildArmorTab(character);
    host.onArmorChanged();
  }
}
