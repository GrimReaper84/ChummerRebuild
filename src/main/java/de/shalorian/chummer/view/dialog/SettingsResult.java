package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.condition.CheatSheetItem;
import de.shalorian.chummer.utils.NumberStyle;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Anzeige-Einstellungen aus dem Einstellungsdialog; sie gelten für alle Charaktere (Java-Preferences, ADR-061).
 *
 * @param numberStyle  Zahlenformat
 * @param statusStatic Status als fester Bereich statt als Slide-Panel (ADR-049)
 * @param cheatSheetItems Inhalte des Bereichs „Cheat-Sheet“ (ADR-065)
 */
public record SettingsResult(@NonNull NumberStyle numberStyle, boolean statusStatic,
    @NonNull Set<CheatSheetItem> cheatSheetItems) {

  public SettingsResult {
    cheatSheetItems = Set.copyOf(cheatSheetItems);
  }
}
