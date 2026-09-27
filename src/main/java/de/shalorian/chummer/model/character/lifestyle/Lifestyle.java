package de.shalorian.chummer.model.character.lifestyle;

import org.jspecify.annotations.NonNull;

/**
 * Der gekaufte Lebensstil eines Charakters: Stufe und Anzahl im Voraus bezahlter Monate.
 * SR4-Grundregelwerk S. 111: "Charaktere können immer nur einen Lebensstil kaufen, wobei sie ihn
 * mehrere Monate im Voraus erwerben können." Jeder Charakter hat genau einen Lebensstil (Default
 * {@link LifestyleLevel#STREET}, 1 Monat — kostenlos), er wird deshalb nie {@code null}.
 */
public class Lifestyle {

  /** Mindestens ein Monat muss bezahlt sein ("Jeder muss irgendwo wohnen", GRW S. 111). */
  public static final int MIN_MONTHS = 1;

  private @NonNull LifestyleLevel level;
  private int months;

  public Lifestyle() {
    this(LifestyleLevel.STREET, MIN_MONTHS);
  }

  /**
   * Lebensstil mit Stufe und Anzahl im Voraus bezahlter Monate.
   *
   * @throws IllegalArgumentException bei weniger als {@value #MIN_MONTHS} Monat
   */
  public Lifestyle(@NonNull LifestyleLevel level, int months) {
    this.level = level;
    setMonths(months);
  }

  public @NonNull LifestyleLevel getLevel() {
    return level;
  }

  public void setLevel(@NonNull LifestyleLevel level) {
    this.level = level;
  }

  public int getMonths() {
    return months;
  }

  /**
   * Setzt die Anzahl im Voraus bezahlter Monate.
   *
   * @throws IllegalArgumentException bei weniger als {@value #MIN_MONTHS} Monat
   */
  public void setMonths(int months) {
    if (months < MIN_MONTHS) {
      throw new IllegalArgumentException(
          "Lebensstil muss für mindestens %d Monat bezahlt werden".formatted(MIN_MONTHS));
    }
    this.months = months;
  }

  /** Gesamtkosten in Nuyen: Monatskosten × Monate. */
  public int getNuyenCost() {
    return level.getMonthlyCost() * months;
  }
}
