package de.shalorian.chummer.view;

import java.util.function.Function;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Zelle der Waffentabelle mit Tooltip aus der Zeile, z.B. Tabellenhinweis am Namen (integrierte
 * Unterlaufwaffe, Flechette-Variante), GRW-Formel am berechneten Schaden oder Reichweitenzeile. Ein
 * leerer Tooltip-Text bedeutet: kein Tooltip.
 */
final class WeaponTooltipCell extends TreeTableCell<WeaponTableRow, String> {

  private final @NonNull Function<WeaponTableRow, String> tooltipText;
  /** Wird wiederverwendet, weil {@link #updateItem} bei jedem Refresh des Charakters läuft. */
  private final @NonNull Tooltip tooltip = new Tooltip();

  WeaponTooltipCell(@NonNull Function<WeaponTableRow, String> tooltipText) {
    this.tooltipText = tooltipText;
  }

  @Override
  protected void updateItem(@Nullable String item, boolean empty) {
    super.updateItem(item, empty);
    setText(empty ? null : item);
    WeaponTableRow row = empty ? null : getTableRow().getItem();
    String text = row == null ? "" : tooltipText.apply(row);
    tooltip.setText(text);
    setTooltip(text.isEmpty() ? null : tooltip);
  }
}
