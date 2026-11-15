package de.shalorian.chummer.model.character.lifestyle;

import org.jspecify.annotations.NonNull;

/**
 * Der gekaufte Lebensstil eines Charakters: Stufe und Anzahl im Voraus bezahlter Monate.
 * SR4-Grundregelwerk S. 111: ein Lebensstil je Charakter, auf Wunsch mehrere Monate im Voraus bezahlt.
 * Jeder Charakter hat genau einen Lebensstil (Default
 * {@link LifestyleLevel#STREET}, 1 Monat — kostenlos), er wird deshalb nie {@code null}.
 */
public class Lifestyle {

  /** Mindestens ein Monat muss bezahlt sein (eine Unterkunft braucht jeder, GRW S. 111). */
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

  /**
   * Bezahlt einen weiteren Monat (Karrieremodus, GRW S. 310). Gebucht wird beim Aufrufer; versäumte Zahlungen (Wurf,
   * Abstieg, Schulden) sind nicht modelliert (Nutzerentscheidung 2026-11-15, später als Einstellung).
   */
  public void payMonth() {
    months++;
  }

  /** Wechselt im Karrieremodus auf eine andere Stufe; der erste Monat der neuen Stufe ist bezahlt (Monate = 1). */
  public void changeLevel(@NonNull LifestyleLevel newLevel) {
    level = newLevel;
    months = MIN_MONTHS;
  }
}
