package de.shalorian.chummer.view;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_SCROLL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_KEY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.weapon.AccessoryData;
import de.shalorian.chummer.model.character.weapon.IncludedAccessory;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.view.dialog.AccessorySelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.WeaponSelectDialogController;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
 * (statt nach Waffenorten), und es gibt keinen Würfelpool (Roadmap Stufe 3); siehe {@code
 * docs/plaene/waffen_detailansicht_plan.md}.
 *
 * <p>Nach Kauf/Entfernen wird der Tab neu aufgebaut. Schaden, Reichweiten und Fertigkeitswert hängen an
 * Stärke und Fertigkeiten; {@link #refresh()} baut deshalb nur den Detailbereich der aktuellen Auswahl
 * neu und läuft über {@link StreetGearTabBuilder#refresh()} bei jedem Attribut-/Fertigkeits-Refresh.
 */
final class WeaponTabBuilder {

  /** Spaltenköpfe des Reichweitenblocks mit den Modifikatoren laut GRW S. 179. */
  private static final String[] RANGE_HEADERS =
      {"Kurz (±0)", "Mittel (−1)", "Weit (−3)", "Extrem (−6)"};

  private final @NonNull VBox container;
  private final @NonNull StreetGearHost host;
  private @Nullable Runnable detailRefresher;

  WeaponTabBuilder(@NonNull VBox container, @NonNull StreetGearHost host) {
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
   * Baut den Tab neu auf und wählt {@code selectedAccessory} bzw. sonst {@code selectedWeapon} aus,
   * falls angegeben.
   */
  private void build(@NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable WeaponAccessory selectedAccessory) {
    container.getChildren().clear();
    TreeView<WeaponTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(220);

    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedEntry(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> showSelected.run());
    detailRefresher = showSelected;

    TreeItem<WeaponTreeEntry> toSelect =
        fillTree(tree, character, selectedWeapon, selectedAccessory);
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
   * mitgeliefertes und gekauftes Zubehör.
   *
   * @return der Knoten der gewünschten Auswahl, oder {@code null}
   */
  private static @Nullable TreeItem<WeaponTreeEntry> fillTree(@NonNull TreeView<WeaponTreeEntry> tree,
      @NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable WeaponAccessory selectedAccessory) {
    Map<WeaponCategory, TreeItem<WeaponTreeEntry>> groups = new EnumMap<>(WeaponCategory.class);
    TreeItem<WeaponTreeEntry> selectedItem = null;
    for (Weapon weapon : character.getWeapons()) {
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
        if (accessory.equals(selectedAccessory)) {
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
    Label title = new Label("Waffen");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    Label costLabel = new Label("Kosten: " + character.getWeaponNuyenCost() + " ¥");
    costLabel.getStyleClass().add(STYLE_BP_LABEL);

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(title, spacer, costLabel);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    return header;
  }

  private @NonNull HBox buildToolbar(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    Button addBtn = new Button("Waffe hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddWeapon(character));

    Button addAccessoryBtn = new Button("Zubehör zur ausgewählten Waffe hinzufügen");
    addAccessoryBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addAccessoryBtn, FontAwesomeSolid.WRENCH);
    // Bindungen an die Baumauswahl (UI-Zustand); die Kategorie einer Waffe ändert sich nie.
    addAccessoryBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      Weapon weapon = selectedWeapon(tree);
      return weapon == null || !weapon.getData().category().acceptsAccessories();
    }, tree.getSelectionModel().selectedItemProperty()));
    addAccessoryBtn.setOnAction(e -> onAddAccessory(character, tree));

    Button removeBtn = new Button("Ausgewählte Waffe bzw. ausgewähltes Zubehör entfernen");
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
      return accessory == null ? !entry.isWeapon() : accessory.data().isPermanent();
    }, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(e -> onRemoveSelected(character, tree));

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, removeBtn, addAccessoryBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  // ── Detailbereich ─────────────────────────────────────────

  /**
   * Füllt den Detailbereich für den gewählten Knoten: Waffe, gekauftes oder mitgeliefertes Zubehör,
   * bzw. einen Hinweis ohne Auswahl oder bei einem Kategorieknoten.
   */
  private static void showDetails(@NonNull VBox details, @Nullable WeaponTreeEntry entry,
      @NonNull Character character) {
    details.getChildren().clear();
    Weapon weapon = entry == null ? null : entry.weapon();
    if (entry == null || weapon == null) {
      details.getChildren().add(hint(character.getWeapons().isEmpty()
          ? "Keine Waffen gekauft"
          : "Waffe auswählen, um ihre Werte zu sehen"));
    } else if (entry.accessory() != null) {
      showAccessoryDetails(details, weapon, entry.accessory());
    } else if (entry.included() != null) {
      showIncludedDetails(details, weapon, entry.included());
    } else {
      showWeaponDetails(details, weapon, character);
    }
  }

  private static void showWeaponDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull Character character) {
    WeaponDetails d = WeaponDetails.of(weapon, character);
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    details.getChildren().addAll(title, buildValueGrid(d), buildRangeSection(d));
    if (!d.note().isEmpty()) {
      Label note = hint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
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
    addRowCells(grid, 0, key("Waffe"), value(weapon.getDisplayName()),
        key("Halterung"), value(mount == null ? "–" : mount.getDisplayName()),
        key("RK"), value(data.rcText()));
    addRowCells(grid, 1, key("Verf."), value(data.availability().displayText(0)),
        key("Preis"), value(weapon.getAccessoryNuyenCost(data) + " ¥"),
        key("Quelle"), value(data.source() + " S. " + data.page()));
    details.getChildren().addAll(title, grid);
    if (data.isPermanent()) {
      details.getChildren().add(hint("Fest eingebaut, kann nicht mehr entfernt werden (GRW S. 367)"));
    }
    if (!data.note().isEmpty()) {
      Label note = hint(data.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  /** Mitgeliefertes Zubehör hat keine eigenen Werte, nur die Regel aus GRW S. 357. */
  private static void showIncludedDetails(@NonNull VBox details, @NonNull Weapon weapon,
      @NonNull IncludedAccessory included) {
    Label title = new Label(included.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    Label text = hint("Mit der Waffe " + weapon.getDisplayName() + " geliefert: im Preis enthalten, "
        + "belegt keine Halterung, RK ist im Tabellenwert der Waffe bereits enthalten (GRW S. 357).");
    text.setWrapText(true);
    details.getChildren().addAll(title, text);
  }

  /** Werte in Label/Wert-Paaren, gruppiert wie die Detail-Labels in {@code tabWeapons}. */
  private static @NonNull MigPane buildValueGrid(@NonNull WeaponDetails d) {
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, key("Kategorie"), value(d.category()), key("Fertigkeit"), value(d.skill()));
    addRowCells(grid, 1, key("Schaden"), damage(d), key("RK"), value(d.recoil()),
        key("PB"), value(d.armorPiercing()));
    addRowCells(grid, 2, key("RW"), value(d.reach()), key("Modus"), value(d.mode()),
        key("Munition"), value(d.ammo()));
    addRowCells(grid, 3, key("Verf."), value(d.availability()), key("Preis"), value(d.cost()),
        key("Quelle"), value(d.source()));
    return grid;
  }

  /** Berechneter Schaden, bei einer GRW-Formel mit dieser klein dahinter. */
  private static @NonNull Node damage(@NonNull WeaponDetails d) {
    if (d.damageFormula().isEmpty()) {
      return value(d.damage());
    }
    HBox box = new HBox(6, value(d.damage()), hint("(GRW: " + d.damageFormula() + ")"));
    box.setAlignment(Pos.BASELINE_LEFT);
    return box;
  }

  /** Reichweitenstufen mit den GRW-Modifikatoren als Spaltenköpfe (S. 179). */
  private static @NonNull VBox buildRangeSection(@NonNull WeaponDetails d) {
    Label title = new Label("Reichweiten");
    title.getStyleClass().add(STYLE_DETAIL_KEY);
    if (d.rangeBands().isEmpty()) {
      return new VBox(4, title, hint("Nahkampfwaffe, Reichweite siehe RW"));
    }
    MigPane grid = new MigPane("", "[center]24[center]24[center]24[center]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addHeaderRow(grid, RANGE_HEADERS);
    addRowCells(grid, 1, d.rangeBands().stream().map(WeaponTabBuilder::value).toArray(Node[]::new));
    return new VBox(4, title, grid, hint(d.rangeNote()));
  }

  private static @NonNull Label key(@NonNull String text) {
    Label label = new Label(text + ":");
    label.getStyleClass().add(STYLE_DETAIL_KEY);
    return label;
  }

  private static @NonNull Label value(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_DETAIL_VALUE);
    return label;
  }

  private static @NonNull Label hint(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_DETAIL_HINT);
    return label;
  }

  // ── Aktionen ──────────────────────────────────────────────

  private void onAddWeapon(@NonNull Character character) {
    try {
      DialogHandle dialog = host.openDialog("WeaponSelectDialog.fxml", "Waffe hinzufügen");
      WeaponSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(weapon -> {
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + weapon.getDisplayName(), weapon.getNuyenCost())) {
          return;
        }
        character.addWeapon(weapon);
        onWeaponsChanged(character, weapon, null);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onAddAccessory(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    Weapon weapon = selectedWeapon(tree);
    if (weapon == null) {
      return;
    }
    try {
      DialogHandle dialog = host.openDialog("AccessorySelectDialog.fxml",
          "Zubehör für " + weapon.getDisplayName());
      AccessorySelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setWeapon(character, weapon);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(accessory -> {
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + accessory.getName() + " für " + weapon.getDisplayName(),
            weapon.getAccessoryNuyenCost(accessory.data()))) {
          return;
        }
        weapon.addAccessory(accessory);
        onWeaponsChanged(character, weapon, accessory);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  /** Entfernt die gewählte Waffe bzw. das gewählte Zubehör. */
  private void onRemoveSelected(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    WeaponTreeEntry entry = selectedEntry(tree);
    Weapon weapon = entry == null ? null : entry.weapon();
    if (entry == null || weapon == null) {
      return;
    }
    WeaponAccessory accessory = entry.accessory();
    if (accessory == null) {
      if (entry.isWeapon()) {
        character.removeWeapon(weapon);
        onWeaponsChanged(character, null, null);
      }
      return;
    }
    weapon.removeAccessory(accessory);
    onWeaponsChanged(character, weapon, null);
  }

  private void onWeaponsChanged(@NonNull Character character, @Nullable Weapon selectedWeapon,
      @Nullable WeaponAccessory selectedAccessory) {
    build(character, selectedWeapon, selectedAccessory);
    host.onNuyenSpentChanged();
  }
}
