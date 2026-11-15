package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.detailHint;
import static de.shalorian.chummer.view.grid.GridSupport.detailKey;
import static de.shalorian.chummer.view.grid.GridSupport.detailValue;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_SCROLL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_KEY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.weapon.AccessoryData;
import de.shalorian.chummer.model.character.weapon.IncludedAccessory;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.view.dialog.AccessorySelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.GearSelectDialogController;
import de.shalorian.chummer.view.dialog.WeaponSelectDialogController;
import de.shalorian.chummer.view.converter.AmmoStringConverter;
import de.shalorian.chummer.view.grid.GridSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Baut den Unter-Tab "Waffen" der Straßenausrüstung nach dem Vorbild von {@code tabWeapons} aus
 * {@code frmCreate.Designer.cs}: Kopfzeile mit Kosten, Werkzeugleiste (Waffe bzw. Zubehör hinzufügen,
 * Auswahl entfernen), links ein Baum der gekauften Waffen mit ihrem Zubehör als Kindknoten
 * (mitgeliefertes zuerst, wie "Part of base Weapon" im Original), rechts ein Detailbereich mit allen
 * Werten des gewählten Knotens. Abweichend vom Original ist der Baum nach Waffenkategorie gruppiert
 * (statt nach Waffenorten); siehe {@code docs/plaene/archiv/waffen_detailansicht_plan.md}. Seit ADR-056 zeigt das
 * Detail den Würfelpool (mit Smartlink/Lasermarkierer) und die geladene Munition.
 *
 * <p>Nach Kauf/Entfernen wird der Tab neu aufgebaut. Schaden, Reichweiten und Fertigkeitswert hängen an
 * Stärke und Fertigkeiten; {@link #refresh()} baut deshalb nur den Detailbereich der aktuellen Auswahl
 * neu und läuft über {@link StreetGearTabBuilder#refresh()} bei jedem Attribut-/Fertigkeits-Refresh.
 */
final class WeaponTabBuilder {

  private static final String STANDARD_AMMO = "Standardmunition";
  /** Spaltenköpfe des Reichweitenblocks mit den Modifikatoren laut GRW S. 179. */
  private static final String[] RANGE_HEADERS =
      {"Kurz (±0)", "Mittel (−1)", "Weit (−3)", "Extrem (−6)"};

  private final @NonNull VBox container;
  private final @NonNull CharacterTabHost host;
  private @Nullable Runnable detailRefresher;

  WeaponTabBuilder(@NonNull VBox container, @NonNull CharacterTabHost host) {
    this.container = container;
    this.host = host;
  }

  void build(@NonNull Character character) {
    build(character, null, null);
  }

  /** Aktualisiert den Detailbereich der gewählten Waffe (Stärke oder Fertigkeiten geändert). */
  void refresh() {
    if (detailRefresher == null) {
      return;
    }
    detailRefresher.run();
  }

  /**
   * Baut den Tab neu auf und wählt {@code selectedChild} (Zubehörteil oder Pfeilstapel) bzw. sonst
   * {@code selectedWeapon} aus, falls angegeben.
   */
  private void build(@NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable Object selectedChild) {
    container.getChildren().clear();
    TreeView<WeaponTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(220);

    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedEntry(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelected.run());
    detailRefresher = showSelected;

    TreeItem<WeaponTreeEntry> toSelect =
        fillTree(tree, character, selectedWeapon, selectedChild);
    if (toSelect != null) {
      tree.getSelectionModel().select(toSelect); // Listener füllt den Detailbereich
      tree.scrollTo(tree.getRow(toSelect));
    } else {
      showSelected.run();
    }

    ScrollPane detailScroll = new ScrollPane(details);
    detailScroll.setFitToWidth(true);
    detailScroll.getStyleClass().add(STYLE_ATTR_SCROLL);
    SplitPane split = new SplitPane(tree, detailScroll);
    split.setDividerPositions(0.3);
    VBox.setVgrow(split, Priority.ALWAYS);

    container.getChildren().addAll(buildHeader(character), buildToolbar(character, tree), split);
  }

  /**
   * Füllt den Baum: je belegter Kategorie ein aufgeklappter Knoten in GRW-Tabellenreihenfolge
   * ({@link WeaponCategory}), darunter die Waffen in Kaufreihenfolge und unter jeder Waffe ihr
   * mitgeliefertes und gekauftes Zubehör sowie beim Bogen seine Pfeile. Waffen aus angelegter Rüstung (Schilde,
   * Schockstreifen, GRW S. 373) stehen mit dem Zusatz „(aus Rüstung)“ dabei und sind nur lesbar, ebenso der
   * waffenlose Grundangriff (S. 188), der immer vorne in der Gruppe Waffenlos steht.
   *
   * @return der Knoten der gewünschten Auswahl, oder {@code null}
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: dieselbe Instanz wie die Auswahl
  private static @Nullable TreeItem<WeaponTreeEntry> fillTree(@NonNull TreeView<WeaponTreeEntry> tree,
      @NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable Object selectedChild) {
    Map<WeaponCategory, TreeItem<WeaponTreeEntry>> groups = new EnumMap<>(WeaponCategory.class);
    TreeItem<WeaponTreeEntry> selectedItem = null;
    List<Weapon> weapons = new ArrayList<>(List.of(character.getEquipment().getUnarmedAttack()));
    weapons.addAll(character.getEquipment().getWeapons());
    weapons.addAll(character.getEquipment().getArmorWeapons());
    weapons.addAll(character.getEquipment().getImplantWeapons());
    for (Weapon weapon : weapons) {
      TreeItem<WeaponTreeEntry> group = groups.computeIfAbsent(weapon.getData().category(),
          category -> {
            TreeItem<WeaponTreeEntry> item = new TreeItem<>(WeaponTreeEntry.ofCategory(category));
            item.setExpanded(true);
            return item;
          });
      TreeItem<WeaponTreeEntry> item = new TreeItem<>(WeaponTreeEntry.ofWeapon(weapon));
      item.setExpanded(true);
      group.getChildren().add(item);
      if (weapon == selectedWeapon) {
        selectedItem = item;
      }
      for (IncludedAccessory included : weapon.getData().included()) {
        item.getChildren().add(new TreeItem<>(WeaponTreeEntry.ofIncluded(weapon, included)));
      }
      for (WeaponAccessory accessory : weapon.getAccessories()) {
        TreeItem<WeaponTreeEntry> child =
            new TreeItem<>(WeaponTreeEntry.ofAccessory(weapon, accessory));
        item.getChildren().add(child);
        if (accessory == selectedChild) {
          selectedItem = child;
        }
      }
      for (Gear arrows : weapon.getArrows()) {
        TreeItem<WeaponTreeEntry> child = new TreeItem<>(WeaponTreeEntry.ofArrows(weapon, arrows));
        item.getChildren().add(child);
        if (arrows == selectedChild) {
          selectedItem = child;
        }
      }
    }
    TreeItem<WeaponTreeEntry> root = new TreeItem<>();
    root.getChildren().addAll(groups.values());
    tree.setRoot(root);
    return selectedItem;
  }

  private static @Nullable WeaponTreeEntry selectedEntry(@NonNull TreeView<WeaponTreeEntry> tree) {
    TreeItem<WeaponTreeEntry> item = tree.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  /** Die gewählte Waffe bzw. die Waffe des gewählten Zubehörs, oder {@code null}. */
  private static @Nullable Weapon selectedWeapon(@NonNull TreeView<WeaponTreeEntry> tree) {
    WeaponTreeEntry entry = selectedEntry(tree);
    return entry == null ? null : entry.weapon();
  }

  private @NonNull HBox buildHeader(@NonNull Character character) {
    Label costLabel = new Label("Kosten: " + character.getEquipment().getWeaponNuyenCost() + " ¥");
    costLabel.getStyleClass().add(STYLE_BP_LABEL);

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(spacer, costLabel);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    return header;
  }

  private @NonNull HBox buildToolbar(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    Button addBtn = new Button("Waffe hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddWeapon(character));

    Button addAccessoryBtn = new Button("Zubehör zur ausgewählten Waffe hinzufügen");
    addAccessoryBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addAccessoryBtn, FontAwesomeSolid.WRENCH);
    // Bindungen an die Baumauswahl (UI-Zustand); die Kategorie einer Waffe ändert sich nie.
    addAccessoryBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      Weapon weapon = selectedWeapon(tree);
      return weapon == null || weapon.getData().builtIn() || !weapon.getData().category().acceptsAccessories();
    }, tree.getSelectionModel().selectedItemProperty()));
    addAccessoryBtn.setOnAction(_ -> onAddAccessory(character, tree));

    Button addArrowsBtn = new Button("Pfeile zum ausgewählten Bogen hinzufügen");
    addArrowsBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addArrowsBtn, FontAwesomeSolid.BULLSEYE);
    addArrowsBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      Weapon weapon = selectedWeapon(tree);
      return weapon == null || !weapon.takesArrows();
    }, tree.getSelectionModel().selectedItemProperty()));
    addArrowsBtn.setOnAction(_ -> onAddArrows(character, tree));

    Button removeBtn = new Button("Ausgewählte Waffe, Zubehör bzw. Pfeile entfernen");
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    // Kategorieknoten, mitgeliefertes und fest eingebautes Zubehör (Gasventil, GRW S. 367) lassen
    // sich nicht entfernen; den Grund zeigt der Detailbereich.
    removeBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      WeaponTreeEntry entry = selectedEntry(tree);
      if (entry == null) {
        return true;
      }
      WeaponAccessory accessory = entry.accessory();
      if (accessory != null) {
        return accessory.data().isPermanent();
      }
      Weapon weapon = entry.weapon();
      if (entry.isWeapon() && weapon != null && weapon.getData().builtIn()) {
        return true; // kommt mit dem Rüstungsteil bzw. Implantat und geht mit ihm
      }
      return !entry.isWeapon() && entry.arrows() == null;
    }, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(_ -> onRemoveSelected(character, tree));

    // Verkaufen (Karriere): nur gekaufte Waffen samt Zubehör und Pfeilen, nichts Eingebautes
    Button sellBtn = host.services().sellButton(character, "Ausgewählte Waffe verkaufen",
        tree.getSelectionModel().selectedItemProperty(), () -> sellableWeapon(character, tree),
        Weapon::getDisplayName, Weapon::getTotalNuyenCost, weapon -> {
          character.getEquipment().removeWeapon(weapon);
          onWeaponsChanged(character, null, null);
        });

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, sellBtn, removeBtn, addArrowsBtn, addAccessoryBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  // ── Detailbereich ─────────────────────────────────────────

  /**
   * Füllt den Detailbereich für den gewählten Knoten: Waffe, gekauftes oder mitgeliefertes Zubehör,
   * bzw. einen Hinweis ohne Auswahl oder bei einem Kategorieknoten.
   */
  private void showDetails(@NonNull VBox details, @Nullable WeaponTreeEntry entry,
      @NonNull Character character) {
    details.getChildren().clear();
    Weapon weapon = entry == null ? null : entry.weapon();
    if (entry == null || weapon == null) {
      details.getChildren().add(detailHint(character.getEquipment().getWeapons().isEmpty()
          && character.getEquipment().getArmorWeapons().isEmpty()
          && character.getEquipment().getImplantWeapons().isEmpty()
          ? "Keine Waffen gekauft"
          : "Waffe auswählen, um ihre Werte zu sehen"));
    } else if (entry.accessory() != null) {
      showAccessoryDetails(details, weapon, entry.accessory());
    } else if (entry.included() != null) {
      showIncludedDetails(details, weapon, entry.included());
    } else if (entry.arrows() != null) {
      showArrowDetails(details, weapon, entry.arrows(), character);
    } else {
      showWeaponDetails(details, weapon, character);
    }
  }

  private void showWeaponDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull Character character) {
    WeaponDetails d = WeaponDetails.of(weapon, character);
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    details.getChildren().addAll(title, buildValueGrid(d));
    if (weapon.getData().category().usesAmmunition() && !weapon.getData().builtIn()) {
      details.getChildren().add(ammoSelector(weapon, character));
    }
    character.getEquipment().getWeaponHints(weapon).forEach(hint -> details.getChildren().add(detailHint(hint)));
    details.getChildren().add(buildRangeSection(d));
    if (weapon.takesArrows()) {
      details.getChildren().add(buildArrowSection(weapon));
    }
    if (!d.note().isEmpty()) {
      Label note = detailHint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  /**
   * Auswahl der geladenen Munition (GRW S. 369, eine Sorte je Waffe): ein passender Stapel aus der Ausrüstung, leer =
   * Standardmunition; „Standard“ entlädt wieder. Die Wahl baut den Tab neu auf, damit Schaden, PB und Panzerung
   * nachziehen.
   */
  private @NonNull HBox ammoSelector(@NonNull Weapon weapon, @NonNull Character character) {
    ComboBox<Gear> box = new ComboBox<>();
    box.setConverter(new AmmoStringConverter());
    box.setPromptText(STANDARD_AMMO);
    box.getItems().setAll(character.getEquipment().getAmmoCandidates(weapon));
    Gear loaded = character.getEquipment().getLoadedAmmo(weapon);
    box.setValue(loaded);
    box.setOnAction(_ -> {
      character.getEquipment().loadAmmo(weapon, box.getValue());
      onWeaponsChanged(character, weapon, null);
    });
    Button standard = new Button("Standard");
    standard.getStyleClass().add(STYLE_BTN_SECONDARY);
    standard.setDisable(loaded == null);
    standard.setOnAction(_ -> {
      character.getEquipment().loadAmmo(weapon, null);
      onWeaponsChanged(character, weapon, null);
    });
    HBox row = new HBox(8, detailKey("Geladene Munition"), box, standard);
    row.setAlignment(Pos.CENTER_LEFT);
    if (box.getItems().isEmpty()) {
      row.getChildren().add(detailHint("keine passende Munition in der Ausrüstung"));
    }
    return row;
  }

  /** Gekauftes Zubehör: Halterung, RK, Verfügbarkeit, Preis an dieser Waffe, Quelle, Regelhinweis. */
  private static void showAccessoryDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull WeaponAccessory accessory) {
    AccessoryData data = accessory.data();
    Mount mount = accessory.mount();
    Label title = new Label(data.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, detailKey("Waffe"), detailValue(weapon.getDisplayName()),
        detailKey("Halterung"), detailValue(mount == null ? "–" : mount.getDisplayName()),
        detailKey("RK"), detailValue(data.rcText()));
    addRowCells(grid, 1, detailKey("Verf."), detailValue(data.availability().displayText(0)),
        detailKey("Preis"), detailValue(weapon.getAccessoryNuyenCost(data) + " ¥"),
        detailKey("Quelle"), detailValue(data.source() + " S. " + data.page()));
    details.getChildren().addAll(title, grid);
    if (data.isPermanent()) {
      details.getChildren().add(
          detailHint("Fest eingebaut, kann nicht mehr entfernt werden (GRW S. 367)"));
    }
    if (!data.note().isEmpty()) {
      Label note = detailHint(data.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  /**
   * Pfeilstapel am Bogen: Stufe, Schaden mit diesem Bogen (niedrigere Stufe von Pfeil und Bogen, GRW
   * S. 361), Menge mit Stepper, Verfügbarkeit, Preis und Hinweis.
   */
  private void showArrowDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull Gear arrows, @NonNull Character character) {
    GearDetails d = GearDetails.of(arrows);
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, detailKey("Bogen"), detailValue(weapon.getDisplayName()),
        detailKey("Stufe"), detailValue(d.rating()),
        detailKey("Schaden"), detailValue(weapon.getArrowDamageText(arrows)));
    addRowCells(grid, 1, detailKey("Menge"), GearQuantityStepper.create(arrows, character, host,
            () -> onWeaponsChanged(character, weapon, arrows)),
        detailKey("Verf."), detailValue(d.availability()),
        detailKey("Preis"), detailValue(d.totalCost()));
    addRowCells(grid, 2, detailKey("Quelle"), detailValue(d.source()));
    details.getChildren().addAll(title, grid);
    if (!d.note().isEmpty()) {
      Label note = detailHint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  /** Schaden je Pfeilsorte am Bogen (GRW S. 361: niedrigere Stufe von Pfeil und Bogen). */
  private static @NonNull VBox buildArrowSection(@NonNull Weapon weapon) {
    Label title = new Label("Pfeile");
    title.getStyleClass().add(STYLE_DETAIL_KEY);
    if (weapon.getArrows().isEmpty()) {
      return new VBox(4, title, detailHint("Keine Pfeile – über „Pfeile hinzufügen“ kaufen"));
    }
    MigPane grid = new MigPane("", "[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    int row = 0;
    for (Gear arrows : weapon.getArrows()) {
      addRowCells(grid, row++, detailKey(arrows.getDisplayName()),
          detailValue(weapon.getArrowDamageText(arrows)));
    }
    return new VBox(4, title, grid,
        detailHint("Schaden nach der niedrigeren Stufe von Pfeil und Bogen (GRW S. 361)"));
  }

  /** Mitgeliefertes Zubehör hat keine eigenen Werte, nur die Regel aus GRW S. 357. */
  private static void showIncludedDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull IncludedAccessory included) {
    Label title = new Label(included.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    Label text = detailHint("Mit der Waffe " + weapon.getDisplayName()
        + " geliefert: im Preis enthalten, belegt keine Halterung, RK ist im Tabellenwert der "
        + "Waffe bereits enthalten (GRW S. 357).");
    text.setWrapText(true);
    details.getChildren().addAll(title, text);
  }

  /**
   * Werte in Label/Wert-Paaren, gruppiert wie die Detail-Labels in {@code tabWeapons}; auch für
   * Fahrzeugwaffen im {@link VehicleTabBuilder}.
   */
  static @NonNull MigPane buildValueGrid(@NonNull WeaponDetails d) {
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    Label pool = DicePoolLabel.create();
    DicePoolLabel.show(pool, Optional.ofNullable(d.dicePool()), d.skillRating(), d.specialization());
    addRowCells(grid, 0, detailKey("Kategorie"), detailValue(d.category()),
        detailKey("Fertigkeit"), detailValue(d.skill()),
        detailKey("Würfelpool"), pool);
    addRowCells(grid, 1, detailKey("Schaden"), damage(d),
        detailKey("RK"), detailValue(d.recoil()),
        detailKey("PB"), detailValue(d.armorPiercing()));
    addRowCells(grid, 2, detailKey("RW"), detailValue(d.reach()),
        detailKey("Modus"), detailValue(d.mode()),
        detailKey("Munition"), detailValue(d.ammo()));
    addRowCells(grid, 3, detailKey("Verf."), detailValue(d.availability()),
        detailKey("Preis"), detailValue(d.cost()),
        detailKey("Quelle"), detailValue(d.source()));
    if (!d.armorType().isEmpty()) {
      addRowCells(grid, 4, detailKey("Panzerung"), detailValue(d.armorType()));
    }
    return grid;
  }

  /** Berechneter Schaden, bei einer GRW-Formel mit dieser klein dahinter. */
  private static @NonNull Node damage(@NonNull WeaponDetails d) {
    if (d.damageFormula().isEmpty()) {
      return detailValue(d.damage());
    }
    HBox box = new HBox(6, detailValue(d.damage()), detailHint("(GRW: " + d.damageFormula() + ")"));
    box.setAlignment(Pos.BASELINE_LEFT);
    return box;
  }

  /** Reichweitenstufen mit den GRW-Modifikatoren als Spaltenköpfe (S. 179); auch für Fahrzeugwaffen. */
  static @NonNull VBox buildRangeSection(@NonNull WeaponDetails d) {
    Label title = new Label("Reichweiten");
    title.getStyleClass().add(STYLE_DETAIL_KEY);
    if (d.rangeBands().isEmpty()) {
      return new VBox(4, title, detailHint("Nahkampfwaffe, Reichweite siehe RW"));
    }
    MigPane grid = new MigPane("", "[center]24[center]24[center]24[center]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addHeaderRow(grid, RANGE_HEADERS);
    addRowCells(grid, 1,
        d.rangeBands().stream().map(GridSupport::detailValue).toArray(Node[]::new));
    return new VBox(4, title, grid, detailHint(d.rangeNote()));
  }


  // ── Aktionen ──────────────────────────────────────────────

  private void onAddWeapon(@NonNull Character character) {
    try {
      DialogHandle dialog = host.services().openDialog("WeaponSelectDialog.fxml", "Waffe hinzufügen");
      WeaponSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(weapon -> {
        if (character.isCreated() && host.services().nuyenChargeFailed(character,
            "Kauf: " + weapon.getDisplayName(), weapon.getNuyenCost())) {
          return;
        }
        character.getEquipment().addWeapon(weapon);
        onWeaponsChanged(character, weapon, null);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private void onAddAccessory(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    Weapon weapon = selectedWeapon(tree);
    if (weapon == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("AccessorySelectDialog.fxml",
          "Zubehör für " + weapon.getDisplayName());
      AccessorySelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setWeapon(character, weapon);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(accessory -> {
        if (character.isCreated() && host.services().nuyenChargeFailed(character,
            "Kauf: " + accessory.getName() + " für " + weapon.getDisplayName(),
            weapon.getAccessoryNuyenCost(accessory.data()))) {
          return;
        }
        weapon.addAccessory(accessory);
        onWeaponsChanged(character, weapon, accessory);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /**
   * Öffnet den Auswahldialog für Pfeile und gibt sie dem gewählten Bogen; gleiche Pfeile werden
   * zusammengeführt. Im Karrieremodus wird der Preis nach Bestätigung abgebucht.
   */
  private void onAddArrows(@NonNull Character character, @NonNull TreeView<WeaponTreeEntry> tree) {
    Weapon weapon = selectedWeapon(tree);
    if (weapon == null || !weapon.takesArrows()) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml",
          "Pfeile für " + weapon.getDisplayName());
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setArrowsOnly(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(arrows -> {
        if (character.isCreated() && host.services().nuyenChargeFailed(character,
            "Kauf: " + arrows.getDisplayName() + " für " + weapon.getDisplayName(),
            arrows.getNuyenCost())) {
          return;
        }
        onWeaponsChanged(character, weapon, weapon.addArrows(arrows));
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Die gewählte gekaufte Waffe (Waffenknoten), oder {@code null}. */
  private static @Nullable Weapon sellableWeapon(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    WeaponTreeEntry entry = selectedEntry(tree);
    Weapon weapon = entry == null || !entry.isWeapon() ? null : entry.weapon();
    return weapon != null && character.getEquipment().getWeapons().contains(weapon) ? weapon : null;
  }

  /** Entfernt die gewählte Waffe, das gewählte Zubehör bzw. den gewählten Pfeilstapel. */
  private void onRemoveSelected(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    WeaponTreeEntry entry = selectedEntry(tree);
    Weapon weapon = entry == null ? null : entry.weapon();
    if (entry == null || weapon == null) {
      return;
    }
    Gear arrows = entry.arrows();
    if (arrows != null) {
      weapon.removeArrows(arrows);
      onWeaponsChanged(character, weapon, null);
      return;
    }
    WeaponAccessory accessory = entry.accessory();
    if (accessory == null) {
      if (entry.isWeapon()) {
        character.getEquipment().removeWeapon(weapon);
        onWeaponsChanged(character, null, null);
      }
      return;
    }
    weapon.removeAccessory(accessory);
    onWeaponsChanged(character, weapon, null);
  }

  private void onWeaponsChanged(@NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable Object selectedChild) {
    build(character, selectedWeapon, selectedChild);
    host.onNuyenSpentChanged();
  }
}
