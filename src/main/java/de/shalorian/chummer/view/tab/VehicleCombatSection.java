package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.detailHint;
import static de.shalorian.chummer.view.grid.GridSupport.detailKey;
import static de.shalorian.chummer.view.grid.GridSupport.detailValue;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_KEY;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehiclePoolCell;
import de.shalorian.chummer.model.character.vehicle.VehiclePoolRow;
import de.shalorian.chummer.model.character.vehicle.VehiclePools;
import de.shalorian.chummer.view.grid.ConditionBoxes;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Kampfwerte im Detail eines Fahrzeugs: Fahrzeugprobe des Fahrers (GRW S. 198), die Pools der Tabelle „Häufige
 * Rigger-/Drohnenproben“ (S. 287) je Steuerungsart und der Schaden am Zustandsmonitor (S. 197; eintragen nur im
 * Karrieremodus, wie beim Charakter).
 */
final class VehicleCombatSection {

  private static final int BOXES_PER_ROW = 10;
  private static final String NO_VALUE = "–";

  private VehicleCombatSection() {
  }

  /** Abschnitte Würfelpools und Schaden; {@code onDamageChanged} baut den Detailbereich danach neu. */
  static @NonNull VBox build(@NonNull Character character, @NonNull Vehicle vehicle,
      @NonNull Runnable onDamageChanged) {
    return new VBox(12, pools(character, vehicle), damage(character, vehicle, onDamageChanged));
  }

  private static @NonNull VBox pools(@NonNull Character character, @NonNull Vehicle vehicle) {
    Label title = new Label("Würfelpools (GRW S. 287)");
    title.getStyleClass().add(STYLE_DETAIL_KEY);
    Optional<DicePool> driver = VehiclePools.driverPool(character, vehicle);
    Label driverValue = detailValue(driver.map(p -> String.valueOf(p.dice())).orElse(NO_VALUE));
    driverValue.setTooltip(new Tooltip(driver.map(DicePool::breakdown)
        .orElse(vehicle.getData().skill() + " ohne Fertigkeitswert nicht möglich")));
    HBox driverRow = new HBox(8, detailKey("Fahrzeugprobe als Fahrer (S. 198)"), driverValue);
    driverRow.setAlignment(Pos.CENTER_LEFT);

    MigPane grid = new MigPane("", "[]24[center]24[center]24[center]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addHeaderRow(grid, "Probe", "Hineingesprungen", "Autonom", "Fernsteuerung");
    int row = 1;
    for (VehiclePoolRow r : VehiclePools.of(character, vehicle)) {
      addRowCells(grid, row++, detailKey(r.test()), cell(r.jumpedIn()), cell(r.autonomous()), cell(r.remote()));
    }
    VBox box = new VBox(6, title, driverRow, grid,
        detailHint("Fahrzeugfertigkeit: " + vehicle.getData().skill() + ". Werte mit Aufschlüsselung im Tooltip."));
    if (vehicle.hasRiggerAdapter() && !character.getMatrixAccess().isVirtual()) {
      Label vr = detailHint("Hineinspringen geht nur in VR (S. 286): "
          + Optional.ofNullable(character.getMatrixAccessHint()).orElse("VR fehlt"));
      vr.setWrapText(true);
      box.getChildren().add(vr);
    }
    return box;
  }

  private static @NonNull Label cell(@NonNull VehiclePoolCell cell) {
    DicePool pool = cell.pool();
    Label label = detailValue(pool == null ? NO_VALUE : String.valueOf(pool.dice()));
    label.setTooltip(new Tooltip(pool == null ? cell.note() : pool.breakdown()));
    return label;
  }

  /** Kästchen n setzt den Schaden auf n, ein Klick auf das letzte gefüllte leert es (wie im Zustand-Tab). */
  private static @NonNull VBox damage(@NonNull Character character, @NonNull Vehicle vehicle,
      @NonNull Runnable onDamageChanged) {
    Label title = new Label("Schaden (%d / %d)".formatted(vehicle.getDamage(), vehicle.getConditionMonitor()));
    title.getStyleClass().add(STYLE_DETAIL_KEY);
    GridPane grid = ConditionBoxes.grid(vehicle.getConditionMonitor(), 0, vehicle.getDamage(), BOXES_PER_ROW,
        character.isCreated(), _ -> "", List.of(), List.of(), newDamage -> {
          vehicle.setDamage(newDamage);
          onDamageChanged.run();
        });
    VBox box = new VBox(6, title, grid);
    if (!character.isCreated()) {
      box.getChildren().add(detailHint("Schaden wird im Karrieremodus eingetragen."));
    }
    if (vehicle.isDestroyed()) {
      box.getChildren().add(detailHint("Zustandsmonitor voll: Fahrzeug zerstört bzw. außer Gefecht."));
    }
    return box;
  }
}
