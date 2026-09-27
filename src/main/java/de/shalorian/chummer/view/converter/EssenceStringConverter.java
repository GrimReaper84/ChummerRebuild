package de.shalorian.chummer.view.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

/**
 * Formatiert Essenzwerte mit deutschem Dezimalkomma und mindestens zwei Nachkommastellen (z.B.
 * {@code "5,80"}). Eine dritte Nachkommastelle wird nur angezeigt, wenn sie tatsächlich vorkommt —
 * Essenzkosten einzelner Implantate sind ungerundet (z.B. Betaware 0,15 × 0,7 = {@code "0,105"}), erst
 * die Gesamt-Essenz wird auf zwei Stellen gerundet.
 */
public class EssenceStringConverter extends StringConverter<Double> {

  @Override
  public String toString(@Nullable Double essence) {
    if (essence == null) {
      return "";
    }
    // DecimalFormat ist nicht threadsicher, daher pro Aufruf neu — nur im UI-Thread genutzt, günstig genug.
    return new DecimalFormat("0.00#", DecimalFormatSymbols.getInstance(Locale.GERMANY))
        .format(essence);
  }

  @Override
  public @Nullable Double fromString(String s) {
    return null;
  }
}
