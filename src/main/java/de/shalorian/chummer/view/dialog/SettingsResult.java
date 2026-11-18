package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.condition.StatusTest;
import de.shalorian.chummer.utils.NumberStyle;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Anzeige-Einstellungen aus dem Einstellungsdialog; sie gelten für alle Charaktere (Java-Preferences, ADR-061).
 *
 * @param numberStyle  Zahlenformat
 * @param statusStatic Status als fester Bereich statt als Slide-Panel (ADR-049)
 * @param statusTests  Proben, die das Status-Panel zeigt
 */
public record SettingsResult(@NonNull NumberStyle numberStyle, boolean statusStatic,
    @NonNull Set<StatusTest> statusTests) {

  public SettingsResult {
    statusTests = Set.copyOf(statusTests);
  }
}
