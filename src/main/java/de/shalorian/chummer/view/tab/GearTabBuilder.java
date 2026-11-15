package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.detailHint;
import static de.shalorian.chummer.view.grid.GridSupport.detailKey;
import static de.shalorian.chummer.view.grid.GridSupport.detailValue;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_SCROLL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearArea;
import de.shalorian.chummer.model.character.gear.GearCombatStats;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.magic.Foci;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.GearSelectDialogController;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * Baut den Unter-Tab "Ausrüstung" der Straßenausrüstung nach dem Vorbild von {@code tabGear} aus
 * {@code frmCreate.Designer.cs}: Kopfzeile mit Kosten, Werkzeugleiste (Gegenstand hinzufügen,
 * Auswahl entfernen), links ein Baum der gekauften Gegenstände, rechts ein Detailbereich mit den
 * Werten und einem Mengen-Stepper. Abweichend vom Original gruppiert der Baum nach Bereich der
 * GRW-Ausrüstungsliste ({@link GearArea}); Lagerorte, "Equipped" und Fokusbindung
 * fehlen bewusst ({@code docs/plaene/archiv/gear_plan.md} Abschnitt 7). Einbauten (Betriebssystem im
 * Kommlink, Verstärkungen, Sensor-Funktionen, Magschloss-Zusätze) hängen als Kindknoten unter ihrem
 * Gerät und werden über "Einbauen" gekauft; Geräte mit Kapazität zeigen "belegt von gesamt". Kommlinks zeigen
 * ihre Matrixwerte samt Prozessorlimit, eingebaute Programme den Schalter „läuft“; das aktive Kommlink wählt man im
 * Status-Panel, weil dort alle Kommlinks samt Headware bekannt sind.
 *
 * <p>Nach jeder Änderung wird der Tab neu aufgebaut und die Auswahl erhalten. Gear hängt nicht an
 * Attributen oder Fertigkeiten, daher gibt es keinen Refresh des Detailbereichs.
 */
final class GearTabBuilder {

  private final @NonNull VBox container;
  private final @NonNull CharacterTabHost host;

  GearTabBuilder(@NonNull VBox container, @NonNull CharacterTabHost host) {
    this.container = container;
    this.host = host;
  }

  void build(@NonNull Character character) {
    build(character, null);
  }

  /** Baut den Tab neu auf und wählt {@code selected} aus, falls angegeben. */
  private void build(@NonNull Character character, @Nullable Gear selected) {
    container.getChildren().clear();
    TreeView<GearTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(220);

    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedGear(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelected.run());

    TreeItem<GearTreeEntry> toSelect = fillTree(tree, character, selected);
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
   * Füllt den Baum: je belegtem Bereich ein aufgeklappter Knoten in GRW-Reihenfolge, darunter die
   * Gegenstände in Kaufreihenfolge und unter diesen ihre Einbauten.
   *
   * @return der Knoten von {@code selected}, oder {@code null}
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: dieselbe Instanz wie die Auswahl
  private static @Nullable TreeItem<GearTreeEntry> fillTree(@NonNull TreeView<GearTreeEntry> tree,
      @NonNull Character character, @Nullable Gear selected) {
    Map<GearArea, TreeItem<GearTreeEntry>> groups = new EnumMap<>(GearArea.class);
    TreeItem<GearTreeEntry> selectedItem = null;
    for (Gear gear : character.getEquipment().getGear()) {
      TreeItem<GearTreeEntry> group = groups.computeIfAbsent(gear.getData().category().area(),
          area -> {
            TreeItem<GearTreeEntry> item = new TreeItem<>(GearTreeEntry.ofArea(area));
            item.setExpanded(true);
            return item;
          });
      TreeItem<GearTreeEntry> item = new TreeItem<>(GearTreeEntry.ofGear(gear));
      item.setExpanded(true);
      group.getChildren().add(item);
      if (gear == selected) {
        selectedItem = item;
      }
      for (Gear child : gear.getInstalled()) {
        TreeItem<GearTreeEntry> childItem = new TreeItem<>(GearTreeEntry.ofGear(child));
        item.getChildren().add(childItem);
        if (child == selected) {
          selectedItem = childItem;
        }
      }
    }
    TreeItem<GearTreeEntry> root = new TreeItem<>();
    root.getChildren().addAll(groups.values());
    tree.setRoot(root);
    return selectedItem;
  }

  /** Der gewählte Gegenstand, oder {@code null} (nichts bzw. ein Bereichsknoten gewählt). */
  private static @Nullable Gear selectedGear(@NonNull TreeView<GearTreeEntry> tree) {
    TreeItem<GearTreeEntry> item = tree.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue().gear();
  }

  private @NonNull HBox buildHeader(@NonNull Character character) {
    Label costLabel = new Label("Kosten: " + character.getEquipment().getGearNuyenCost() + " ¥");
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
      @NonNull TreeView<GearTreeEntry> tree) {
    Button addBtn = new Button("Gegenstand hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddGear(character));

    Button installBtn = new Button("In ausgewählten Gegenstand einbauen");
    installBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(installBtn, FontAwesomeSolid.MICROCHIP);
    // Bindung an die Baumauswahl (UI-Zustand); die Kategorie eines Gegenstands ändert sich nie.
    installBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      Gear gear = selectedGear(tree);
      return gear == null || gear.getData().accepts().isEmpty();
    }, tree.getSelectionModel().selectedItemProperty()));
    installBtn.setOnAction(_ -> {
      Gear gear = selectedGear(tree);
      if (gear == null) {
        return;
      }
      // Prüfung im Handler statt Sperre aus Live-Zustand (Projektregeln §8)
      if (!gear.canInstallAnyOf(GearDataLoader.getAllGear().values())) {
        String capacity = GearDetails.of(gear).capacity();
        capacity = capacity.isEmpty() ? "" : " (Kapazität " + capacity + ")";
        host.services().warn("Kein Einbau möglich", gear.getName()
            + ": kein weiterer Einbau möglich" + capacity + "; zuerst einen Einbau entfernen.");
        return;
      }
      onInstall(character, gear);
    });

    Button removeBtn = new Button("Ausgewählten Gegenstand entfernen");
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    // Bindung an die Baumauswahl (UI-Zustand): Bereichsknoten lassen sich nicht entfernen.
    removeBtn.disableProperty().bind(Bindings.createBooleanBinding(
        () -> selectedGear(tree) == null, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(_ -> {
      Gear gear = selectedGear(tree);
      if (gear != null) {
        character.getEquipment().removeGear(gear);
        onGearChanged(character, null);
      }
    });

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, removeBtn, installBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  // ── Detailbereich ─────────────────────────────────────────

  private void showDetails(@NonNull VBox details, @Nullable Gear gear,
      @NonNull Character character) {
    details.getChildren().clear();
    if (gear == null) {
      details.getChildren().add(detailHint(character.getEquipment().getGear().isEmpty()
          ? "Keine Ausrüstung gekauft"
          : "Gegenstand auswählen, um seine Werte zu sehen"));
      return;
    }
    showGearDetails(details, gear, character, host, () -> onGearChanged(character, gear));
  }

  /**
   * Werte eines Gegenstands im Detailbereich, mit Mengen-Stepper bei Gegenständen mit veränderlicher
   * Menge; auch für die Ausrüstung im Fahrzeug ({@link VehicleTabBuilder}).
   *
   * @param onChanged baut den Tab nach einer Mengenänderung neu auf
   */
  static void showGearDetails(@NonNull VBox details, @NonNull Gear gear,
      @NonNull Character character, @NonNull CharacterTabHost host, @NonNull Runnable onChanged) {
    GearDetails d = GearDetails.of(gear);
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);

    MigPane grid = new MigPane("", "[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, detailKey("Tabelle"), detailValue(d.category()),
        detailKey("Stufe"), detailValue(d.rating()));
    addRowCells(grid, 1, detailKey("Gerätestufe"), detailValue(d.deviceRating()),
        detailKey("Verf."), detailValue(d.availability()));
    // Einzelstücke (Geräte mit Einbauten und Einbauten selbst) haben keine veränderliche Menge.
    addRowCells(grid, 2, detailKey("Menge"), gear.getData().isSingleItem()
            ? detailValue(d.quantity())
            : GearQuantityStepper.create(gear, character, host, onChanged),
        detailKey("Preis"), detailValue(d.packPrice()));
    addRowCells(grid, 3, detailKey(gear.getInstalled().isEmpty()
            ? "Gesamtpreis" : "Gesamtpreis (inkl. Einbauten)"), detailValue(d.totalCost()),
        detailKey("Quelle"), detailValue(d.source()));
    int row = 4;
    if (!d.extraLabel().isEmpty()) {
      addRowCells(grid, row++, detailKey(d.extraLabel()), detailValue(d.extra()));
    }
    GearCombatStats combat = d.combat();
    if (combat != null) {
      boolean ammo = d.ammunition();
      addRowCells(grid, row++, detailKey(ammo ? "Schadensmod." : "Schaden"),
          detailValue(combat.damage()), detailKey(ammo ? "PB-Mod." : "PB"),
          detailValue(combat.armorPiercing()));
      addRowCells(grid, row++, detailKey(ammo ? "Panzerung" : "Sprengwirkung"),
          detailValue(ammo ? combat.armorType() : combat.blast()));
    }
    if (!d.capacity().isEmpty()) {
      addRowCells(grid, row++, detailKey("Kapazität"), detailValue(d.capacity()));
    }
    if (d.commlink()) {
      addRowCells(grid, row++, detailKey("Prozessor"), detailValue(d.processor()),
          detailKey("Signal"), detailValue(d.signal()));
      addRowCells(grid, row++, detailKey("Firewall"), detailValue(d.firewall()),
          detailKey("System"), detailValue(d.system()));
      addRowCells(grid, row++, detailKey("Programme"), detailValue(d.processorLimit()));
    }
    if (gear.isFocus() && character.getEquipment().getGear().contains(gear)) {
      addRowCells(grid, row++, detailKey("Bindung"), focusControls(gear, character, host, onChanged));
    }
    Gear device = deviceOf(gear, character);
    MatrixStats deviceStats = device == null ? null : device.getMatrixStats();
    if (gear.isProgram() && deviceStats != null) {
      CheckBox running = new CheckBox("läuft");
      running.setSelected(gear.isRunning());
      running.selectedProperty().addListener((_, _, value) -> {
        gear.setRunning(value);
        onChanged.run();
      });
      addRowCells(grid, row, detailKey("Läuft mit Stufe"),
          detailValue(String.valueOf(deviceStats.programRating(gear.getRating()))), detailKey("Zustand"), running);
    }
    details.getChildren().addAll(title, grid);
    if (!d.note().isEmpty()) {
      Label note = detailHint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  /**
   * Binden bzw. Lösen und Aktivieren eines Fokus (GRW S. 233/234): Binden kostet bei der Erschaffung BP (über die
   * BP-Summe, Lösen gibt sie wieder frei), im Karrieremodus Karma mit Bestätigung (Lösen erstattet nichts). Prüfung im
   * Klick-Handler mit Warnung. {@code onChanged} baut den Tab neu und lässt Magie-Tab und Status nachziehen.
   */
  private static @NonNull HBox focusControls(@NonNull Gear focus, @NonNull Character character,
      @NonNull CharacterTabHost host, @NonNull Runnable onChanged) {
    Foci foci = character.getFoci();
    Label state = detailValue(focus.isBound() ? "gebunden" : "ungebunden");
    Button bindBtn = new Button(focus.isBound() ? "Lösen" : "Binden");
    bindBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    bindBtn.setOnAction(_ -> {
      if (focus.isBound()) {
        if (host.services().confirmYesNo("Fokus lösen", focus.getDisplayName() + " lösen?",
            character.isCreated() ? "Gezahltes Karma wird nicht erstattet." : "Die BP werden wieder frei.")) {
          foci.unbind(focus);
          onChanged.run();
        }
        return;
      }
      String problem = foci.getBindProblem(focus);
      if (problem != null) {
        host.services().warn("Fokus binden", problem);
        return;
      }
      if (character.isCreated() && host.services().karmaChargeFailed(character,
          "Fokus binden: " + focus.getDisplayName(), foci.getBindingKarma(focus))) {
        return;
      }
      foci.bind(focus);
      onChanged.run();
    });
    HBox box = new HBox(8, state, bindBtn);
    box.setAlignment(Pos.CENTER_LEFT);
    if (focus.isBound()) {
      CheckBox active = new CheckBox("aktiv");
      active.setSelected(focus.isFocusActive());
      active.setOnAction(_ -> {
        String problem = active.isSelected() ? foci.getActivateProblem(focus) : null;
        if (problem != null) {
          active.setSelected(false);
          host.services().warn("Fokus aktivieren", problem);
          return;
        }
        foci.setActive(focus, active.isSelected());
        onChanged.run();
      });
      box.getChildren().add(active);
    }
    return box;
  }

  /** Das Kommlink, in dem {@code part} eingebaut ist (auch Kommlink-Headware), oder {@code null}. */
  private static @Nullable Gear deviceOf(@NonNull Gear part, @NonNull Character character) {
    return character.getEquipment().getCommlinks().stream()
        .filter(device -> device.getInstalled().contains(part))
        .findFirst()
        .orElse(null);
  }

  // ── Aktionen ──────────────────────────────────────────────

  /**
   * Öffnet den Auswahldialog und kauft die Auswahl; gleiche Gegenstände werden zusammengeführt
   * ({@code Equipment#addGear}). Im Karrieremodus wird der Preis nach Bestätigung vom Nuyen-Guthaben
   * abgebucht.
   */
  private void onAddGear(@NonNull Character character) {
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml", "Gegenstand hinzufügen");
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(gear -> {
        if (character.isCreated() && host.services().nuyenChargeFailed(character,
            "Kauf: " + gear.getDisplayName(), gear.getNuyenCost())) {
          return;
        }
        onGearChanged(character, character.getEquipment().addGear(gear));
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /**
   * Öffnet den Auswahldialog gefiltert auf die Kategorie, die {@code device} aufnimmt, und baut die
   * Auswahl ein. Im Karrieremodus wird der Preis nach Bestätigung abgebucht.
   */
  private void onInstall(@NonNull Character character, @NonNull Gear device) {
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml",
          "Einbau in " + device.getDisplayName());
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setInstallTarget(character, device);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(child -> {
        if (character.isCreated() && host.services().nuyenChargeFailed(character,
            "Kauf: " + child.getDisplayName() + " für " + device.getDisplayName(),
            child.getNuyenCost())) {
          return;
        }
        device.install(child);
        onGearChanged(character, child);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Baut den Tab neu; Foki und situative Pools im Magie- und Adepten-Tab sowie der Status ziehen nach. */
  private void onGearChanged(@NonNull Character character, @Nullable Gear selected) {
    build(character, selected);
    host.onNuyenSpentChanged();
    host.refreshRows();
  }
}
