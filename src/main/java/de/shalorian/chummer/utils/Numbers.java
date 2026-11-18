package de.shalorian.chummer.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.jspecify.annotations.NonNull;

/**
 * Einzige Stelle, an der Zahlen für die Anzeige formatiert werden (Nuyen, Essenz, Kraftpunkte …). Der Stil ist eine
 * Einstellung der Oberfläche ({@link NumberStyle}); er liegt hier statisch, weil auch das Modell Anzeigetexte erzeugt
 * (Preise in Katalogeinträgen, Hinweise) und ihn sonst durch jede Schicht reichen müsste. Gesetzt wird er nur vom
 * Hauptfenster beim Start und nach den Einstellungen, gelesen nur im UI-Thread; Tests setzen ihn danach zurück.
 */
public final class Numbers {

  private static volatile @NonNull NumberStyle style = NumberStyle.DEFAULT;

  private Numbers() {
  }

  public static void setStyle(@NonNull NumberStyle newStyle) {
    style = newStyle;
  }

  /** Ganzzahl, z.B. {@code "1500"} bzw. {@code "1.500"}. */
  public static @NonNull String integer(long value) {
    return format("#,##0", value);
  }

  /** Nuyen-Betrag mit Währungszeichen, z.B. {@code "1500 ¥"}. */
  public static @NonNull String nuyen(long value) {
    return integer(value) + " ¥";
  }

  /** Dezimalzahl ohne überflüssige Nullen, z.B. {@code "1,25"} oder {@code "4"}. */
  public static @NonNull String decimal(@NonNull BigDecimal value) {
    BigDecimal stripped = value.stripTrailingZeros();
    return format("#,##0.##########", stripped.scale() < 0 ? stripped.setScale(0) : stripped);
  }

  /**
   * Essenz mit mindestens zwei Nachkommastellen, eine dritte nur, wenn sie vorkommt (Essenzkosten einzelner
   * Implantate sind ungerundet, z.B. {@code "0,105"}).
   */
  public static @NonNull String essence(double value) {
    return format("#,##0.00#", value);
  }

  /**
   * Liest eine eingegebene Ganzzahl; Tausendertrennzeichen des aktuellen Stils werden übergangen.
   *
   * @throws NumberFormatException wenn der Text keine Ganzzahl ist
   */
  public static int parseInteger(@NonNull String text) {
    return Integer.parseInt(text.trim().replace(String.valueOf(style.groupingSeparator()), ""));
  }

  private static @NonNull String format(@NonNull String pattern, @NonNull Object value) {
    NumberStyle current = style;
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
    symbols.setDecimalSeparator(current.decimalSeparator());
    symbols.setGroupingSeparator(current.groupingSeparator());
    symbols.setMinusSign('-');
    // DecimalFormat ist nicht threadsicher, daher pro Aufruf neu; günstig genug für Anzeigetexte.
    DecimalFormat format = new DecimalFormat(pattern, symbols);
    format.setGroupingUsed(current.groupThousands());
    return format.format(value);
  }
}
