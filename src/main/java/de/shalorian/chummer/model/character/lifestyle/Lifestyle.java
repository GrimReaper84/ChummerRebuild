package de.shalorian.chummer.model.character.lifestyle;

import org.jspecify.annotations.NonNull;

/**
 * Der gekaufte Lebensstil eines Charakters: Stufe und Anzahl im Voraus bezahlter Monate, im Karrieremodus dazu der
 * Rückstand ausgesetzter Monate und offene Schulden (GRW S. 312, nur mit der Regel-Einstellung genutzt).
 * SR4-Grundregelwerk S. 111: ein Lebensstil je Charakter, auf Wunsch mehrere Monate im Voraus bezahlt.
 * Jeder Charakter hat genau einen Lebensstil (Default
 * {@link LifestyleLevel#STREET}, 1 Monat — kostenlos), er wird deshalb nie {@code null}.
 */
public class Lifestyle {

  /** Mindestens ein Monat muss bezahlt sein (eine Unterkunft braucht jeder, GRW S. 111). */
  public static final int MIN_MONTHS = 1;

  private @NonNull LifestyleLevel level;
  private int months;
  private int missedMonths;
  private int debt;

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
   * Bezahlt einen weiteren Monat (Karrieremodus, GRW S. 310). Gebucht wird beim Aufrufer. Eine Zahlung hebt den
   * Rückstand ausgesetzter Monate auf (S. 312); offene Schulden bleiben.
   */
  public void payMonth() {
    months++;
    missedMonths = 0;
  }

  /**
   * Wechselt im Karrieremodus auf eine andere Stufe; der erste Monat der neuen Stufe ist bezahlt (Monate = 1), ein
   * Rückstand entfällt damit.
   */
  public void changeLevel(@NonNull LifestyleLevel newLevel) {
    level = newLevel;
    months = MIN_MONTHS;
    missedMonths = 0;
  }

  /**
   * Was ein ausgesetzter Monat mit diesem Wurf bewirken würde, ohne etwas zu ändern (GRW S. 312): Der Rückstand wächst
   * um eins. Ist der Wurf eines Würfels höchstens so hoch wie die Zahl der ausgesetzten Monate in Folge, sinkt der
   * Lebensstil um eine Stufe, und der Charakter schuldet die Kosten eines Monats der früheren Stufe. So kann die
   * Oberfläche das Ergebnis buchen, bevor sie es mit {@link #skipMonth(int)} anwendet.
   *
   * @param roll Augenzahl des Würfels (1–6)
   * @throws IllegalArgumentException bei einem Wurf außerhalb 1–6
   * @throws IllegalStateException    auf einer kostenlosen Stufe, die nichts auszusetzen hat und nicht sinken kann
   */
  public @NonNull MissedPaymentResult skipOutcome(int roll) {
    if (roll < 1 || roll > 6) {
      throw new IllegalArgumentException("Wurf muss zwischen 1 und 6 liegen: " + roll);
    }
    if (level.getMonthlyCost() == 0) {
      throw new IllegalStateException("Ein kostenloser Lebensstil kann nicht ausgesetzt werden");
    }
    int missed = missedMonths + 1;
    if (roll > missed) {
      return new MissedPaymentResult(roll, missed, false, 0, level);
    }
    return new MissedPaymentResult(roll, missed, true, level.getMonthlyCost(), level.lower());
  }

  /**
   * Setzt einen Monat aus und wendet {@link #skipOutcome(int)} an. Nach einem Abstieg beginnt der Rückstand neu
   * (Nutzerentscheidung 2026-11-18); die bezahlten Monate bleiben unverändert.
   */
  public @NonNull MissedPaymentResult skipMonth(int roll) {
    MissedPaymentResult result = skipOutcome(roll);
    level = result.level();
    debt += result.debtAdded();
    missedMonths = result.downgraded() ? 0 : result.missedMonths();
    return result;
  }

  /** Ausgesetzte Monate in Folge seit der letzten Zahlung. */
  public int getMissedMonths() {
    return missedMonths;
  }

  /** Offene Schulden aus Abstiegen in Nuyen. */
  public int getDebt() {
    return debt;
  }

  /** Stellt Rückstand und Schulden aus einer Speicherdatei wieder her (negative Werte werden zu 0). */
  public void restoreArrears(int missedMonths, int debt) {
    this.missedMonths = Math.max(0, missedMonths);
    this.debt = Math.max(0, debt);
  }

  /** Tilgt die offenen Schulden; gebucht wird beim Aufrufer. */
  public void payDebt() {
    debt = 0;
  }
}
