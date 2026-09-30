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
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
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
 * {@code frmCreate.Designer.cs}: Kopfzeile mit Kosten, Werkzeugleiste (Hinzufügen, Entfernen der
 * Auswahl), links ein Baum der gekauften Waffen, rechts ein Detailbereich mit allen Werten der gewählten
 * Waffe und ihren Reichweitenstufen. Abweichend vom Original ist der Baum nach Waffenkategorie gruppiert
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
    build(character, null);
  }

  /** Aktualisiert den Detailbereich der gewählten Waffe (Stärke oder Fertigkeiten geändert). */
  void refresh() {
    if (detailRefresher == null) {
      return;
    }
    detailRefresher.run();
  }

  /** Baut den Tab neu auf und wählt {@code selection} aus, falls angegeben. */
  private void build(@NonNull Character character, @Nullable Weapon selection) {
    container.getChildren().clear();
    TreeView<WeaponTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(220);

    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedWeapon(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> showSelected.run());
    detailRefresher = showSelected;

    TreeItem<WeaponTreeEntry> toSelect = fillTree(tree, character, selection);
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
   * ({@link WeaponCategory}), darunter die Waffen in Kaufreihenfolge.
   *
   * @return der Knoten von {@code selection}, oder {@code null}
   */
  private static @Nullable TreeItem<WeaponTreeEntry> fillTree(@NonNull TreeView<WeaponTreeEntry> tree,
      @NonNull Character character, @Nullable Weapon selection) {
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
      group.getChildren().add(item);
      if (weapon == selection) {
        selectedItem = item;
      }
    }
    TreeItem<WeaponTreeEntry> root = new TreeItem<>();
    root.getChildren().addAll(groups.values());
    tree.setRoot(root);
    return selectedItem;
  }

  private static @Nullable Weapon selectedWeapon(@NonNull TreeView<WeaponTreeEntry> tree) {
    TreeItem<WeaponTreeEntry> item = tree.getSelectionModel().getSelectedItem();
    return item == null || item.getValue() == null ? null : item.getValue().weapon();
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

    Button removeBtn = new Button("Ausgewählte Waffe entfernen");
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    // Bindung an die Baumauswahl (UI-Zustand, kein Modellzustand); Kategorieknoten zählen nicht.
    removeBtn.disableProperty().bind(Bindings.createBooleanBinding(
        () -> selectedWeapon(tree) == null, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(e -> onRemoveSelected(character, tree));

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, removeBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  // ── Detailbereich ─────────────────────────────────────────

  /** Füllt den Detailbereich für {@code weapon}, bzw. mit einem Hinweis ohne Waffenauswahl. */
  private static void showDetails(@NonNull VBox details, @Nullable Weapon weapon,
      @NonNull Character character) {
    details.getChildren().clear();
    if (weapon == null) {
      details.getChildren().add(hint(character.getWeapons().isEmpty()
          ? "Keine Waffen gekauft"
          : "Waffe auswählen, um ihre Werte zu sehen"));
      return;
    }
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
        onWeaponsChanged(character, weapon);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onRemoveSelected(@NonNull Character character,
      @NonNull TreeView<WeaponTreeEntry> tree) {
    Weapon weapon = selectedWeapon(tree);
    if (weapon == null) {
      return;
    }
    character.removeWeapon(weapon);
    onWeaponsChanged(character, null);
  }

  private void onWeaponsChanged(@NonNull Character character, @Nullable Weapon selection) {
    build(character, selection);
    host.onNuyenSpentChanged();
  }
}
