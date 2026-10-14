package de.shalorian.chummer.view;

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
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleCategory;
import de.shalorian.chummer.model.character.vehicle.VehicleModification;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.GearSelectDialogController;
import de.shalorian.chummer.view.dialog.VehicleSelectDialogController;
import de.shalorian.chummer.view.dialog.WeaponSelectDialogController;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import org.controlsfx.control.Notifications;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Baut den Tab "Fahrzeuge &amp; Drohnen" nach dem Vorbild von {@code tabVehicles} aus {@code
 * frmCreate.Designer.cs}: Kopfzeile mit Kosten, Werkzeugleiste, links ein Baum der Fahrzeuge
 * (gruppiert nach den Kategorien der GRW-Fahrzeugtabelle S. 397) mit Riggeradapter, Halterungen samt
 * Waffen und Ausrüstung samt Einbauten als Kindknoten, rechts ein Detailbereich. Mitgelieferte Teile
 * stehen zuerst und lassen sich nicht entfernen. Abweichend vom Original fehlen Slots, Matrixattribute,
 * Zubehör an Fahrzeugwaffen, Eigenname, Notizen und Orte ({@code docs/plaene/fahrzeuge_plan.md}).
 *
 * <p>Regeln werden im Klick-Handler geprüft und als Hinweis gemeldet; Buttons sind nur an die
 * Baumauswahl gebunden (Projektregeln §8). Nach jeder Änderung wird der Tab neu aufgebaut und die
 * Auswahl erhalten. Im Karrieremodus wird jeder Kauf über das Nuyen-Log abgebucht.
 */
final class VehicleTabBuilder {

  private final @NonNull VBox container;
  private final @NonNull StreetGearHost host;

  VehicleTabBuilder(@NonNull VBox container, @NonNull StreetGearHost host) {
    this.container = container;
    this.host = host;
  }

  void build(@NonNull Character character) {
    build(character, null);
  }

  /** Baut den Tab neu auf und wählt den Knoten von {@code selected} aus, falls angegeben. */
  private void build(@NonNull Character character, @Nullable Object selected) {
    container.getChildren().clear();
    TreeView<VehicleTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(240);

    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedEntry(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> showSelected.run());

    TreeItem<VehicleTreeEntry> toSelect = fillTree(tree, character, selected);
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
    split.setDividerPositions(0.35);
    VBox.setVgrow(split, Priority.ALWAYS);

    container.getChildren().addAll(buildHeader(character), buildToolbar(character, tree), split);
  }

  /**
   * Füllt den Baum: je belegter Kategorie ein aufgeklappter Knoten in GRW-Reihenfolge, darunter die
   * Fahrzeuge in Kaufreihenfolge mit Riggeradapter, Halterungen (Waffe als Kind), mitgelieferter und
   * gekaufter Ausrüstung (Einbauten als Kinder).
   *
   * @return der Knoten von {@code selected} (Fahrzeug, Halterung, Waffe oder Gegenstand), oder
   *     {@code null}
   */
  private static @Nullable TreeItem<VehicleTreeEntry> fillTree(
      @NonNull TreeView<VehicleTreeEntry> tree, @NonNull Character character,
      @Nullable Object selected) {
    Map<VehicleCategory, TreeItem<VehicleTreeEntry>> groups = new EnumMap<>(VehicleCategory.class);
    TreeItem<VehicleTreeEntry> selectedItem = null;
    for (Vehicle vehicle : character.getEquipment().getVehicles()) {
      TreeItem<VehicleTreeEntry> group = groups.computeIfAbsent(vehicle.getData().category(),
          category -> expanded(VehicleTreeEntry.ofCategory(category)));
      TreeItem<VehicleTreeEntry> item = expanded(VehicleTreeEntry.ofVehicle(vehicle));
      group.getChildren().add(item);
      if (vehicle == selected) {
        selectedItem = item;
      }
      if (vehicle.hasRiggerAdapter()) {
        item.getChildren().add(new TreeItem<>(VehicleTreeEntry.ofRiggerAdapter(vehicle)));
      }
      for (WeaponMount mount : vehicle.getMounts()) {
        TreeItem<VehicleTreeEntry> mountItem = expanded(VehicleTreeEntry.ofMount(vehicle, mount));
        item.getChildren().add(mountItem);
        if (mount == selected) {
          selectedItem = mountItem;
        }
        Weapon weapon = mount.getWeapon();
        if (weapon != null) {
          TreeItem<VehicleTreeEntry> weaponItem =
              new TreeItem<>(VehicleTreeEntry.ofWeapon(vehicle, mount, weapon));
          mountItem.getChildren().add(weaponItem);
          if (weapon == selected) {
            selectedItem = weaponItem;
          }
        }
      }
      for (Gear gear : vehicle.getIncludedGear()) {
        selectedItem = addGearItem(item, vehicle, gear, selected, selectedItem);
      }
      for (Gear gear : vehicle.getGear()) {
        selectedItem = addGearItem(item, vehicle, gear, selected, selectedItem);
      }
    }
    TreeItem<VehicleTreeEntry> root = new TreeItem<>();
    root.getChildren().addAll(groups.values());
    tree.setRoot(root);
    return selectedItem;
  }

  /** Hängt einen Gegenstand samt Einbauten an und liefert den Knoten der Auswahl. */
  private static @Nullable TreeItem<VehicleTreeEntry> addGearItem(
      @NonNull TreeItem<VehicleTreeEntry> parent, @NonNull Vehicle vehicle, @NonNull Gear gear,
      @Nullable Object selected, @Nullable TreeItem<VehicleTreeEntry> selectedItem) {
    TreeItem<VehicleTreeEntry> item = expanded(VehicleTreeEntry.ofGear(vehicle, gear));
    parent.getChildren().add(item);
    TreeItem<VehicleTreeEntry> result = gear == selected ? item : selectedItem;
    for (Gear child : gear.getInstalled()) {
      TreeItem<VehicleTreeEntry> childItem = new TreeItem<>(VehicleTreeEntry.ofGear(vehicle, child));
      item.getChildren().add(childItem);
      if (child == selected) {
        result = childItem;
      }
    }
    return result;
  }

  private static @NonNull TreeItem<VehicleTreeEntry> expanded(@NonNull VehicleTreeEntry entry) {
    TreeItem<VehicleTreeEntry> item = new TreeItem<>(entry);
    item.setExpanded(true);
    return item;
  }

  private static @Nullable VehicleTreeEntry selectedEntry(@NonNull TreeView<VehicleTreeEntry> tree) {
    TreeItem<VehicleTreeEntry> item = tree.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  /** Das Fahrzeug des gewählten Knotens, oder {@code null} (nichts bzw. ein Kategorieknoten). */
  private static @Nullable Vehicle selectedVehicle(@NonNull TreeView<VehicleTreeEntry> tree) {
    VehicleTreeEntry entry = selectedEntry(tree);
    return entry == null ? null : entry.vehicle();
  }

  private @NonNull HBox buildHeader(@NonNull Character character) {
    Label title = new Label("Fahrzeuge & Drohnen");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    Label costLabel = new Label("Kosten: " + character.getEquipment().getVehicleNuyenCost() + " ¥");
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
      @NonNull TreeView<VehicleTreeEntry> tree) {
    Button addBtn = button("Fahrzeug hinzufügen", FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddVehicle(character));

    // Bindungen nur an die Baumauswahl (UI-Zustand); Regeln prüfen die Handler.
    Button riggerBtn = vehicleButton("Riggeradapter einbauen", FontAwesomeSolid.GAMEPAD, tree);
    riggerBtn.setOnAction(e -> withVehicle(tree, v -> onAddRiggerAdapter(character, v)));

    Button mountBtn = vehicleButton("Waffenhalterung einbauen", FontAwesomeSolid.CROSSHAIRS, tree);
    mountBtn.setOnAction(e -> withVehicle(tree, v -> onAddMount(character, v)));

    Button weaponBtn = vehicleButton("Waffe in Halterung einsetzen", FontAwesomeSolid.BULLSEYE, tree);
    weaponBtn.setOnAction(e -> {
      VehicleTreeEntry entry = selectedEntry(tree);
      Vehicle vehicle = entry == null ? null : entry.vehicle();
      if (entry != null && vehicle != null) {
        onMountWeapon(character, vehicle, entry.mount());
      }
    });

    Button gearBtn = vehicleButton("Ausrüstung ins Fahrzeug legen", FontAwesomeSolid.BOX_OPEN, tree);
    gearBtn.setOnAction(e -> withVehicle(tree, v -> onAddGear(character, v)));

    Button installBtn = button("In ausgewählten Gegenstand einbauen", FontAwesomeSolid.MICROCHIP);
    installBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      VehicleTreeEntry entry = selectedEntry(tree);
      Gear gear = entry == null ? null : entry.gear();
      return gear == null || gear.getData().accepts() == null;
    }, tree.getSelectionModel().selectedItemProperty()));
    installBtn.setOnAction(e -> {
      VehicleTreeEntry entry = selectedEntry(tree);
      Gear gear = entry == null ? null : entry.gear();
      Vehicle vehicle = entry == null ? null : entry.vehicle();
      if (gear != null && vehicle != null) {
        onInstall(character, vehicle, gear);
      }
    });

    Button removeBtn = button("Auswahl entfernen", FontAwesomeSolid.TRASH);
    removeBtn.disableProperty().bind(Bindings.createBooleanBinding(
        () -> selectedVehicle(tree) == null, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(e -> {
      VehicleTreeEntry entry = selectedEntry(tree);
      if (entry != null) {
        onRemove(character, entry);
      }
    });

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, removeBtn, installBtn, gearBtn, weaponBtn, mountBtn,
        riggerBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  private static @NonNull Button button(@NonNull String text, @NonNull FontAwesomeSolid icon) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    return button;
  }

  /** Button, der nur mit einem gewählten Fahrzeug (oder einem seiner Kindknoten) aktiv ist. */
  private static @NonNull Button vehicleButton(@NonNull String text, @NonNull FontAwesomeSolid icon,
      @NonNull TreeView<VehicleTreeEntry> tree) {
    Button button = button(text, icon);
    button.disableProperty().bind(Bindings.createBooleanBinding(
        () -> selectedVehicle(tree) == null, tree.getSelectionModel().selectedItemProperty()));
    return button;
  }

  private static void withVehicle(@NonNull TreeView<VehicleTreeEntry> tree,
      @NonNull Consumer<Vehicle> action) {
    Vehicle vehicle = selectedVehicle(tree);
    if (vehicle != null) {
      action.accept(vehicle);
    }
  }

  // ── Detailbereich ─────────────────────────────────────────

  private void showDetails(@NonNull VBox details, @Nullable VehicleTreeEntry entry,
      @NonNull Character character) {
    details.getChildren().clear();
    Vehicle vehicle = entry == null ? null : entry.vehicle();
    if (entry == null || vehicle == null) {
      details.getChildren().add(detailHint(character.getEquipment().getVehicles().isEmpty()
          ? "Keine Fahrzeuge oder Drohnen gekauft"
          : "Fahrzeug auswählen, um seine Werte zu sehen"));
      return;
    }
    Weapon weapon = entry.weapon();
    WeaponMount mount = entry.mount();
    Gear gear = entry.gear();
    if (weapon != null) {
      showWeaponDetails(details, weapon);
    } else if (mount != null) {
      showTextDetails(details, VehicleModification.WEAPON_MOUNT.getDisplayName(),
          VehicleDetails.mountText(vehicle, mount)
              + ". Nimmt ein LMG oder eine kleinere Waffe sowie 250 Schuss Munition auf (GRW S. 395).");
    } else if (entry.riggerAdapter()) {
      showTextDetails(details, VehicleModification.RIGGER_ADAPTER.getDisplayName(),
          vehicle.isRiggerAdapterIncluded()
              ? "Alle Drohnen sind mit einem Riggeradapter ausgestattet (GRW S. 398)."
              : "Nachgerüstet (" + VehicleModification.RIGGER_ADAPTER.cost() + " ¥). Damit kann ein"
                  + " Charakter mit Riggerkontrolle das Fahrzeug riggen (GRW S. 395).");
    } else if (gear != null) {
      GearTabBuilder.showGearDetails(details, gear, character, host,
          () -> onChanged(character, gear));
      if (vehicle.isIncluded(gear)) {
        details.getChildren().add(detailHint("Mit dem Fahrzeug geliefert, im Preis enthalten."));
      }
    } else {
      showVehicleDetails(details, VehicleDetails.of(vehicle));
    }
  }

  private static void showVehicleDetails(@NonNull VBox details, @NonNull VehicleDetails d) {
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, detailKey("Kategorie"), detailValue(d.category()),
        detailKey("Handling"), detailValue(d.handling()),
        detailKey("Beschl."), detailValue(d.accel()));
    addRowCells(grid, 1, detailKey("Geschw."), detailValue(d.speed()),
        detailKey("Pilot"), detailValue(d.pilot()),
        detailKey("Rumpf"), detailValue(d.body()));
    addRowCells(grid, 2, detailKey("Panzerung"), detailValue(d.armor()),
        detailKey("Sensor"), detailValue(d.sensor()),
        detailKey("Gerätestufe"), detailValue(d.deviceRating()));
    addRowCells(grid, 3, detailKey("Zustandsmonitor"), detailValue(d.conditionMonitor()),
        detailKey("Halterungen"), detailValue(d.mounts()),
        detailKey("Riggeradapter"), detailValue(d.riggerAdapter()));
    addRowCells(grid, 4, detailKey("Verf."), detailValue(d.availability()),
        detailKey("Preis"), detailValue(d.cost()),
        detailKey("Quelle"), detailValue(d.source()));
    Label included = detailHint("Mitgeliefert: " + d.included());
    included.setWrapText(true);
    details.getChildren().addAll(title, grid, included);
    if (!d.note().isEmpty()) {
      Label note = detailHint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  private static void showWeaponDetails(@NonNull VBox details, @NonNull Weapon weapon) {
    WeaponDetails d = WeaponDetails.ofMounted(weapon);
    Label title = new Label(d.name());
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    details.getChildren().addAll(title, WeaponTabBuilder.buildValueGrid(d),
        WeaponTabBuilder.buildRangeSection(d),
        detailHint("Fahrzeugwaffe: nur Tabellenwerte, Würfelpools für Rigger und Drohnen folgen später"
            + " (GRW S. 203, 287)."));
    if (!d.note().isEmpty()) {
      Label note = detailHint(d.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
  }

  private static void showTextDetails(@NonNull VBox details, @NonNull String title,
      @NonNull String text) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    Label textLabel = detailHint(text);
    textLabel.setWrapText(true);
    details.getChildren().addAll(titleLabel, textLabel);
  }

  // ── Aktionen ──────────────────────────────────────────────

  private void onAddVehicle(@NonNull Character character) {
    try {
      DialogHandle dialog = host.openDialog("VehicleSelectDialog.fxml", "Fahrzeug hinzufügen");
      VehicleSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(vehicle -> {
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + vehicle.getName(), vehicle.getNuyenCost())) {
          return;
        }
        character.getEquipment().addVehicle(vehicle);
        onChanged(character, vehicle);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onAddRiggerAdapter(@NonNull Character character, @NonNull Vehicle vehicle) {
    String problem = vehicle.riggerAdapterProblem();
    if (problem != null) {
      warn("Kein Riggeradapter möglich", problem);
      return;
    }
    if (!chargeModification(character, vehicle, VehicleModification.RIGGER_ADAPTER)) {
      return;
    }
    vehicle.installRiggerAdapter();
    onChanged(character, vehicle);
  }

  private void onAddMount(@NonNull Character character, @NonNull Vehicle vehicle) {
    String problem = vehicle.mountProblem();
    if (problem != null) {
      warn("Keine weitere Waffenhalterung", problem);
      return;
    }
    if (!chargeModification(character, vehicle, VehicleModification.WEAPON_MOUNT)) {
      return;
    }
    onChanged(character, vehicle.addMount());
  }

  /**
   * Prüft die Verfügbarkeit einer Modifikation und bucht sie im Karrieremodus ab.
   *
   * @return true, wenn eingebaut werden darf
   */
  private boolean chargeModification(@NonNull Character character, @NonNull Vehicle vehicle,
      @NonNull VehicleModification mod) {
    if (!character.getEquipment().isAvailableForPurchase(mod.availability(), 0)) {
      warn("Nicht verfügbar", mod.getDisplayName() + " ist bei der Erschaffung nicht verfügbar");
      return false;
    }
    return !(character.isCreated() && host.nuyenChargeFailed(character,
        "Kauf: " + mod.getDisplayName() + " für " + vehicle.getName(), mod.cost()));
  }

  /**
   * Setzt eine Waffe in die gewählte Halterung bzw. – ist das Fahrzeug selbst gewählt – in die erste
   * freie Halterung.
   */
  private void onMountWeapon(@NonNull Character character, @NonNull Vehicle vehicle,
      @Nullable WeaponMount selectedMount) {
    WeaponMount mount = selectedMount != null ? selectedMount : vehicle.firstFreeMount();
    if (mount == null) {
      warn("Keine freie Waffenhalterung", vehicle.getName()
          + " hat keine freie Waffenhalterung; zuerst eine Waffenhalterung einbauen.");
      return;
    }
    if (mount.getWeapon() != null) {
      warn("Halterung belegt", "Die gewählte Waffenhalterung ist bereits belegt.");
      return;
    }
    try {
      DialogHandle dialog = host.openDialog("WeaponSelectDialog.fxml",
          "Waffe für " + vehicle.getName());
      WeaponSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setMountTarget(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(weapon -> {
        String problem = vehicle.weaponProblem(mount, weapon.getData());
        if (problem != null) {
          warn("Waffe passt nicht", problem);
          return;
        }
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + weapon.getDisplayName() + " für " + vehicle.getName(),
            weapon.getTotalNuyenCost())) {
          return;
        }
        vehicle.mountWeapon(mount, weapon);
        onChanged(character, weapon);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onAddGear(@NonNull Character character, @NonNull Vehicle vehicle) {
    try {
      DialogHandle dialog = host.openDialog("GearSelectDialog.fxml",
          "Ausrüstung für " + vehicle.getName());
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(gear -> {
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + gear.getDisplayName() + " für " + vehicle.getName(), gear.getNuyenCost())) {
          return;
        }
        onChanged(character, vehicle.addGear(gear));
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onInstall(@NonNull Character character, @NonNull Vehicle vehicle,
      @NonNull Gear device) {
    // Prüfung im Handler statt Sperre aus Live-Zustand (Projektregeln §8)
    if (!device.canInstallAnyOf(GearDataLoader.getAllGear().values())) {
      String capacity = GearDetails.of(device).capacity();
      warn("Kein Einbau möglich", device.getName() + ": kein weiterer Einbau möglich"
          + (capacity.isEmpty() ? "" : " (Kapazität " + capacity + ")")
          + "; zuerst einen Einbau entfernen.");
      return;
    }
    try {
      DialogHandle dialog = host.openDialog("GearSelectDialog.fxml",
          "Einbau in " + device.getDisplayName());
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setInstallTarget(character, device);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(child -> {
        if (character.isCreated() && host.nuyenChargeFailed(character,
            "Kauf: " + child.getDisplayName() + " für " + vehicle.getName(),
            child.getNuyenCost())) {
          return;
        }
        device.install(child);
        onChanged(character, child);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  /**
   * Entfernt den gewählten Knoten: Fahrzeug (nach Rückfrage), nachgerüsteten Riggeradapter oder
   * Halterung, Waffe, gekauften Gegenstand oder Einbau. Mitgelieferte Teile bleiben mit Hinweis.
   * Im Karrieremodus wird nichts erstattet (wie bei Ausrüstung und Waffen).
   */
  private void onRemove(@NonNull Character character, @NonNull VehicleTreeEntry entry) {
    Vehicle vehicle = entry.vehicle();
    if (vehicle == null) {
      return;
    }
    WeaponMount mount = entry.mount();
    Gear gear = entry.gear();
    if (entry.isVehicle()) {
      if (host.confirm("Fahrzeug entfernen",
          "\"" + vehicle.getName() + "\" samt Ausstattung wirklich entfernen?")) {
        character.getEquipment().removeVehicle(vehicle);
        onChanged(character, null);
      }
    } else if (entry.weaponNode() && mount != null) {
      vehicle.unmountWeapon(mount);
      onChanged(character, mount);
    } else if (mount != null) {
      if (mount.isIncluded()) {
        warnIncluded(VehicleModification.WEAPON_MOUNT.getDisplayName());
        return;
      }
      vehicle.removeMount(mount);
      onChanged(character, vehicle);
    } else if (entry.riggerAdapter()) {
      if (vehicle.isRiggerAdapterIncluded()) {
        warnIncluded(VehicleModification.RIGGER_ADAPTER.getDisplayName());
        return;
      }
      vehicle.removeRiggerAdapter();
      onChanged(character, vehicle);
    } else if (gear != null) {
      if (vehicle.isIncluded(gear)) {
        warnIncluded(gear.getName());
        return;
      }
      vehicle.removeGear(gear);
      onChanged(character, vehicle);
    }
  }

  private void warnIncluded(@NonNull String name) {
    warn("Mitgeliefert", name + " ist im Fahrzeug enthalten und kann nicht entfernt werden.");
  }

  private void warn(@NonNull String title, @NonNull String text) {
    Notifications.create()
        .title(title)
        .text(text)
        .position(Pos.BOTTOM_RIGHT)
        .owner(container.getScene().getWindow())
        .showWarning();
  }

  private void onChanged(@NonNull Character character, @Nullable Object selected) {
    build(character, selected);
    host.onNuyenSpentChanged();
  }
}
