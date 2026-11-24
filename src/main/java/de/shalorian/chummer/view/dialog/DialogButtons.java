package de.shalorian.chummer.view.dialog;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import org.jspecify.annotations.NonNull;

/**
 * Deutsche Knöpfe für Alerts und Dialoge. Die Standardknöpfe von JavaFX ({@code ButtonType.YES} usw.) holen ihren Text
 * aus der Spracheinstellung von Java und zeigen auf englischen Systemen „Yes/No/Cancel“ (Beta-Befund 2026-11-23).
 */
public final class DialogButtons {

  public static final @NonNull ButtonType YES = new ButtonType("Ja", ButtonData.YES);
  public static final @NonNull ButtonType NO = new ButtonType("Nein", ButtonData.NO);
  public static final @NonNull ButtonType OK = new ButtonType("OK", ButtonData.OK_DONE);
  public static final @NonNull ButtonType CANCEL = new ButtonType("Abbrechen", ButtonData.CANCEL_CLOSE);
  public static final @NonNull ButtonType SAVE = new ButtonType("Speichern", ButtonData.YES);
  public static final @NonNull ButtonType DISCARD = new ButtonType("Verwerfen", ButtonData.NO);

  private DialogButtons() {
  }
}
