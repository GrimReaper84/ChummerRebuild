package de.shalorian.chummer.model.character.lifestyle;

import java.util.random.RandomGenerator;
import org.jspecify.annotations.NonNull;

/**
 * Die sechs Lebensstil-Stufen des SR4-Grundregelwerks mit Monatskosten (Tabelle <i>Kosten von
 * Lebensstilen</i>, S. 111) sowie Würfelzahl und Geldmultiplikator für das Startkapital (Tabelle
 * <i>Startkapital</i>, S. 114). Beide Tabellen gegen das PDF-Bild verifiziert. Ausbaustufen und
 * weitere Lebensstile aus Zusatzbänden sind bewusst nicht enthalten.
 */
public enum LifestyleLevel {
  STREET("Straße", 0, 1, 10),
  SQUATTER("Squatter", 500, 2, 20),
  LOW("Unterschicht", 2_000, 3, 50),
  MIDDLE("Mittelschicht", 5_000, 4, 100),
  HIGH("Oberschicht", 10_000, 4, 500),
  LUXURY("Luxus", 100_000, 4, 1_000);

  private static final int NUYEN_PER_BONUS_POINT = 100;
  private static final int BONUS_CAP_PER_DIE = 3;
  private static final int DIE_SIDES = 6;

  private final @NonNull String displayName;
  private final int monthlyCost;
  private final int dice;
  private final int multiplier;

  LifestyleLevel(@NonNull String displayName, int monthlyCost, int dice, int multiplier) {
    this.displayName = displayName;
    this.monthlyCost = monthlyCost;
    this.dice = dice;
    this.multiplier = multiplier;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Kosten pro Monat in Nuyen (GRW S. 111). */
  public int getMonthlyCost() {
    return monthlyCost;
  }

  /** Anzahl W6 für den Startkapital-Wurf (GRW S. 114). */
  public int getDice() {
    return dice;
  }

  /** Geldmultiplikator für das Startkapital (GRW S. 114). */
  public int getMultiplier() {
    return multiplier;
  }

  /** Kleinste mögliche Würfelsumme (alle Würfel zeigen 1). */
  public int getMinDiceSum() {
    return dice;
  }

  /** Größte mögliche Würfelsumme (alle Würfel zeigen 6). */
  public int getMaxDiceSum() {
    return dice * DIE_SIDES;
  }

  /**
   * Würfelt den Startkapital-Wurf: Summe aus {@link #getDice()} W6. Der Zufallsgenerator wird
   * übergeben, damit der Wurf mit festem Seed testbar bleibt.
   */
  public int rollDiceSum(@NonNull RandomGenerator random) {
    int sum = 0;
    for (int i = 0; i < dice; i++) {
      sum += random.nextInt(1, DIE_SIDES + 1);
    }
    return sum;
  }

  /**
   * Bonus auf das Würfelergebnis aus übrig gebliebenem Ressourcengeld: +1 je volle 100 ¥, höchstens
   * Würfelzahl × 3. Negatives Restgeld ergibt keinen Bonus (0).
   */
  public int restBonus(int remainingNuyen) {
    if (remainingNuyen <= 0) {
      return 0;
    }
    return Math.min(remainingNuyen / NUYEN_PER_BONUS_POINT, dice * BONUS_CAP_PER_DIE);
  }

  /**
   * Startkapital in Nuyen nach SR4-Grundregelwerk S. 114: Restgeld aus den Ressourcen verfällt, gibt
   * aber je volle 100 ¥ +1 auf den Wurf (höchstens 3 je Würfel); die Summe wird mit dem Multiplikator
   * des Lebensstils malgenommen. Also {@code
   * (Würfelsumme + restBonus(Rest)) × Multiplikator}.
   *
   * @param diceSum        Summe der gewürfelten W6, muss in [{@link #getMinDiceSum()}, {@link
   *                       #getMaxDiceSum()}] liegen
   * @param remainingNuyen nicht ausgegebenes Ressourcengeld (darf negativ sein → kein Bonus)
   * @throws IllegalArgumentException bei einer mit dieser Würfelzahl unmöglichen Würfelsumme
   */
  public int startingCapital(int diceSum, int remainingNuyen) {
    if (diceSum < getMinDiceSum() || diceSum > getMaxDiceSum()) {
      throw new IllegalArgumentException("Würfelsumme %d ist mit %dW6 nicht möglich"
          .formatted(diceSum, dice));
    }
    return (diceSum + restBonus(remainingNuyen)) * multiplier;
  }
}
