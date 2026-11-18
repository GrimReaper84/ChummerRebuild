package de.shalorian.chummer.utils;

/**
 * Wie Zahlen angezeigt werden: Tausender gruppiert oder nicht, Dezimalkomma oder Dezimalpunkt. Das Trennzeichen der
 * Tausender ist immer das jeweils andere Zeichen ({@code 1.500,5} bzw. {@code 1,500.5}), damit eine Zahl nie
 * zweideutig wird.
 *
 * @param groupThousands Tausender gruppieren ({@code 1.500} bzw. {@code 1,500} statt {@code 1500})
 * @param decimalComma   Komma als Dezimaltrennzeichen ({@code 5,80}), sonst Punkt ({@code 5.80})
 */
public record NumberStyle(boolean groupThousands, boolean decimalComma) {

  /** Das bisherige Verhalten: keine Tausendergruppen, Dezimalkomma. */
  public static final NumberStyle DEFAULT = new NumberStyle(false, true);

  /** Dezimaltrennzeichen. */
  public char decimalSeparator() {
    return decimalComma ? ',' : '.';
  }

  /** Trennzeichen der Tausendergruppen (nur genutzt, wenn {@link #groupThousands()}). */
  public char groupingSeparator() {
    return decimalComma ? '.' : ',';
  }
}
