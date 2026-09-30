package de.shalorian.chummer.view;

import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.WeaponSelectDialogController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Baut den Unter-Tab "Waffen" der Straßenausrüstung: Kopfzeile mit Kosten, Werkzeugleiste
 * (Hinzufügen, Entfernen der Auswahl) und ein {@link TreeTableView} mit den GRW-Tabellenwerten je
 * Waffe. Der Baum ist bewusst schon jetzt ein Baum (flach, eine Ebene): Zubehör wird in einer späteren
 * Ausbaustufe als Kindknoten der Waffe ergänzt (siehe {@code docs/plaene/waffen_plan.md}, Teil C).
 * Der Tab wird nach Kauf/Entfernen neu aufgebaut. Schaden, Reichweiten und Fertigkeitswert hängen an
 * Stärke und Fertigkeiten; {@link #refresh()} erneuert deshalb nur die Zeilenwerte (Auswahl und
 * Scrollposition bleiben erhalten) und läuft über {@link StreetGearTabBuilder#refresh()} bei jedem
 * Attribut-/Fertigkeits-Refresh.
 */
final class WeaponTableBuilder {

  private final @NonNull VBox container;
  private final @NonNull StreetGearHost host;
  private final @NonNull List<Runnable> rowRefreshers = new ArrayList<>();

  WeaponTableBuilder(@NonNull VBox container, @NonNull StreetGearHost host) {
    this.container = container;
    this.host = host;
  }

  void build(@NonNull Character character) {
    container.getChildren().clear();
    rowRefreshers.clear();
    TreeTableView<WeaponTableRow> table = buildTable(character);
    VBox.setVgrow(table, Priority.ALWAYS);
    container.getChildren().addAll(buildHeader(character), buildToolbar(character, table), table);
  }

  /** Berechnet die charakterabhängigen Zeilenwerte (Schaden, Reichweiten, Fertigkeit) neu. */
  void refresh() {
    rowRefreshers.forEach(Runnable::run);
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
      @NonNull TreeTableView<WeaponTableRow> table) {
    Button addBtn = new Button("Waffe hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(e -> onAddWeapon(character));

    Button removeBtn = new Button("Ausgewählte Waffe entfernen");
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    // Bindung an die Tabellenauswahl (UI-Zustand, kein Modellzustand).
    removeBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    removeBtn.setOnAction(e -> onRemoveSelected(character, table));

    HBox toolbar = new HBox(8);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    toolbar.getChildren().addAll(spacer, removeBtn, addBtn);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  private @NonNull TreeTableView<WeaponTableRow> buildTable(@NonNull Character character) {
    TreeItem<WeaponTableRow> root = new TreeItem<>();
    for (Weapon weapon : character.getWeapons()) {
      TreeItem<WeaponTableRow> item = new TreeItem<>(WeaponTableRow.of(weapon, character));
      rowRefreshers.add(() -> item.setValue(WeaponTableRow.of(weapon, character)));
      root.getChildren().add(item);
    }

    TreeTableView<WeaponTableRow> table = new TreeTableView<>(root);
    table.setShowRoot(false);
    table.setPlaceholder(new Label("Keine Waffen gekauft"));
    table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    table.getColumns().add(column("Waffe", WeaponTableRow::name, WeaponTableRow::note, 190));
    table.getColumns().add(column("Kategorie", WeaponTableRow::category, 150));
    table.getColumns().add(column("Fertigkeit", WeaponTableRow::skill, 150));
    table.getColumns().add(column("RW", WeaponTableRow::reach, 40));
    table.getColumns().add(
        column("Schaden", WeaponTableRow::damage, WeaponTableRow::damageFormula, 90));
    table.getColumns().add(column("PB", WeaponTableRow::armorPiercing, 60));
    table.getColumns().add(column("Modus", WeaponTableRow::mode, 80));
    table.getColumns().add(column("RK", WeaponTableRow::recoil, 50));
    table.getColumns().add(column("Munition", WeaponTableRow::ammo, 110));
    table.getColumns().add(column("Reichweiten (K/M/W/E)", WeaponTableRow::ranges,
        WeaponTableRow::rangeRow, 170));
    table.getColumns().add(column("Verf.", WeaponTableRow::availability, 50));
    table.getColumns().add(column("Preis", WeaponTableRow::cost, 80));
    table.getColumns().add(column("Quelle", WeaponTableRow::source, 80));
    return table;
  }

  private static @NonNull TreeTableColumn<WeaponTableRow, String> column(@NonNull String title,
      @NonNull Function<WeaponTableRow, String> value, double prefWidth) {
    TreeTableColumn<WeaponTableRow, String> column = new TreeTableColumn<>(title);
    column.setCellValueFactory(param -> {
      WeaponTableRow row = param.getValue().getValue();
      return new ReadOnlyStringWrapper(row == null ? "" : value.apply(row));
    });
    column.setPrefWidth(prefWidth);
    return column;
  }

  /** Spalte mit Tooltip je Zelle (leerer Tooltip-Text = kein Tooltip). */
  private static @NonNull TreeTableColumn<WeaponTableRow, String> column(@NonNull String title,
      @NonNull Function<WeaponTableRow, String> value,
      @NonNull Function<WeaponTableRow, String> tooltip, double prefWidth) {
    TreeTableColumn<WeaponTableRow, String> column = column(title, value, prefWidth);
    column.setCellFactory(col -> new WeaponTooltipCell(tooltip));
    return column;
  }

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
        onWeaponsChanged(character);
      });
    } catch (IOException e) {
      host.showDialogLoadError();
    }
  }

  private void onRemoveSelected(@NonNull Character character,
      @NonNull TreeTableView<WeaponTableRow> table) {
    TreeItem<WeaponTableRow> selected = table.getSelectionModel().getSelectedItem();
    if (selected == null || selected.getValue() == null) {
      return;
    }
    character.removeWeapon(selected.getValue().weapon());
    onWeaponsChanged(character);
  }

  private void onWeaponsChanged(@NonNull Character character) {
    build(character);
    host.onNuyenSpentChanged();
  }
}
