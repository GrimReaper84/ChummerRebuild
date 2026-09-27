package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import org.jspecify.annotations.NonNull;

/**
 * Rückrufe des {@link StreetGearTabBuilder} in den {@link CharacterViewController}: Dialog-Infrastruktur,
 * Karrieremodus-Abbuchung und die Anzeigen außerhalb des Straßenausrüstung-Tabs, die von Käufen und
 * angelegter Rüstung abhängen.
 */
interface StreetGearHost {

  /** Lädt ein Dialog-FXML samt modaler Stage (siehe {@code CharacterViewController#openDialog}). */
  @NonNull DialogHandle openDialog(@NonNull String fxmlName, @NonNull String title)
      throws IOException;

  void showDialogLoadError();

  /**
   * Bucht einen Karrieremodus-Kauf vom Nuyen-Guthaben ab.
   *
   * @return true, wenn NICHT gebucht wurde — der Kauf ist dann abzubrechen
   */
  boolean nuyenChargeFailed(@NonNull Character character, @NonNull String label, int nuyenCost);

  /** Ein Kaufpreis hat sich geändert: Restgeld-Anzeige nachziehen. */
  void onNuyenSpentChanged();

  /**
   * Gekaufte oder angelegte Rüstung hat sich geändert: Restgeld und alle Anzeigen, die von der
   * Panzerungsbehinderung abhängen (Aug-Spalte GES/REA), nachziehen.
   */
  void onArmorChanged();
}
