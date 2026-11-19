package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_VALUE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.view.grid.RowControls;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

/**
 * Mengen-Stepper (−/Menge/+) in Packungsschritten für Ausrüstung im Ausrüstungs-Tab und Pfeile am
 * Bogen im Waffen-Tab. Erhöhen kostet im Karrieremodus Nuyen über das Log, Verringern gilt als
 * Verbrauch und erstattet nichts (Nutzerentscheidung 2026-10-04). Mindestens eine Packung bleibt;
 * ganz entfernen geht über die Werkzeugleiste des Tabs.
 */
final class GearQuantityStepper {

  private GearQuantityStepper() {
  }

  /**
   * @param onChanged baut den Tab nach einer Mengenänderung neu auf; der Zustand der Buttons bleibt
   *                  dadurch aktuell
   */
  static @NonNull HBox create(@NonNull Gear gear, @NonNull Character character,
      @NonNull CharacterTabHost host, @NonNull Runnable onChanged) {
    GearData data = gear.getData();
    Button minus = RowControls.minusButton();
    Label quantity = new Label(data.unit().format(gear.getQuantity()));
    Button plus = RowControls.plusButton();
    quantity.getStyleClass().add(STYLE_DETAIL_VALUE);
    minus.setDisable(!gear.canRemovePack());
    minus.setOnAction(_ -> {
      if (gear.canRemovePack()) {
        gear.removePack();
        onChanged.run();
      }
    });
    plus.setOnAction(_ -> {
      if (character.isCreated() && host.services().nuyenChargeFailed(character,
          "Kauf: " + data.name() + " +" + data.unit().format(data.packSize()),
          gear.getPackPrice())) {
        return;
      }
      gear.addPack();
      onChanged.run();
    });
    HBox box = new HBox(6, minus, quantity, plus);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }
}
