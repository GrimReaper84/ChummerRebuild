package de.shalorian.chummer.view;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import org.jspecify.annotations.Nullable;

/**
 * Namenszelle der Waffentabelle: zeigt einen Tabellenhinweis der Waffe (z.B. integrierte
 * Unterlaufwaffe, Flechette-Variante) als Tooltip.
 */
final class WeaponNameCell extends TreeTableCell<WeaponTableRow, String> {

  @Override
  protected void updateItem(@Nullable String item, boolean empty) {
    super.updateItem(item, empty);
    setText(empty ? null : item);
    WeaponTableRow row = empty ? null : getTableRow().getItem();
    setTooltip(row == null || row.note().isEmpty() ? null : new Tooltip(row.note()));
  }
}
