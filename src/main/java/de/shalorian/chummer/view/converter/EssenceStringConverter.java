package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.utils.Numbers;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

/**
 * Formatiert Essenzwerte über {@link Numbers#essence} (Dezimaltrennzeichen nach Einstellung) mit mindestens zwei
 * Nachkommastellen (z.B. {@code "5,80"}). Eine dritte Nachkommastelle wird nur angezeigt, wenn sie vorkommt —
 * Essenzkosten einzelner Implantate sind ungerundet (z.B. Betaware 0,15 × 0,7 = {@code "0,105"}), erst
 * die Gesamt-Essenz wird auf zwei Stellen gerundet.
 */
public class EssenceStringConverter extends StringConverter<Double> {

  @Override
  public String toString(@Nullable Double essence) {
    if (essence == null) {
      return "";
    }
    return Numbers.essence(essence);
  }

  @Override
  public @Nullable Double fromString(String s) {
    return null;
  }
}
