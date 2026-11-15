package de.shalorian.chummer.model.character.reputation;

/**
 * Was der Spielleiter an der Reputation festlegt (GRW S. 308/309): zusätzliche Punkte Straßenruf und Schlechter Ruf,
 * geopferter Straßenruf und eine Anpassung der Prominenz. Die Werte selbst rechnet der Charakter aus verdientem Karma
 * und Gaben/Handicaps; diese Klasse hält nur die Eingaben. Änderungen sind keine Buchungen und stehen nicht im Log.
 */
public class Reputation {

  /** Straßenruf, der für 1 Punkt weniger Schlechten Ruf geopfert wird (S. 308). */
  public static final int STREET_CRED_PER_NOTORIETY = 2;

  private int streetCredBonus;
  private int notorietyBonus;
  private int burnedStreetCred;
  private int awarenessAdjustment;

  /** Vom Spielleiter vergebener zusätzlicher Straßenruf (nie negativ). */
  public int getStreetCredBonus() {
    return streetCredBonus;
  }

  /**
   * Setzt den zusätzlichen Straßenruf.
   *
   * @throws IllegalArgumentException bei negativem Wert
   */
  public void setStreetCredBonus(int streetCredBonus) {
    this.streetCredBonus = nonNegative(streetCredBonus, "Straßenruf vom Spielleiter");
  }

  /** Vom Spielleiter vergebener zusätzlicher Schlechter Ruf (nie negativ). */
  public int getNotorietyBonus() {
    return notorietyBonus;
  }

  /**
   * Setzt den zusätzlichen Schlechten Ruf.
   *
   * @throws IllegalArgumentException bei negativem Wert
   */
  public void setNotorietyBonus(int notorietyBonus) {
    this.notorietyBonus = nonNegative(notorietyBonus, "Schlechter Ruf vom Spielleiter");
  }

  /** Dauerhaft geopferter Straßenruf, immer ein Vielfaches von {@value #STREET_CRED_PER_NOTORIETY}. */
  public int getBurnedStreetCred() {
    return burnedStreetCred;
  }

  /**
   * Opfert {@value #STREET_CRED_PER_NOTORIETY} Straßenruf. Ob genug Straßenruf und Schlechter Ruf da sind, prüft
   * {@code Character#burnStreetCred()}, das diese Methode aufruft.
   */
  public void burn() {
    burnedStreetCred += STREET_CRED_PER_NOTORIETY;
  }

  /** Anpassung der Prominenz durch den Spielleiter, positiv oder negativ (S. 309). */
  public int getAwarenessAdjustment() {
    return awarenessAdjustment;
  }

  public void setAwarenessAdjustment(int awarenessAdjustment) {
    this.awarenessAdjustment = awarenessAdjustment;
  }

  /** Stellt gespeicherte Werte wieder her, ohne Regelprüfung über die Grenzen hinaus. */
  public void restore(int streetCredBonus, int notorietyBonus, int burnedStreetCred, int awarenessAdjustment) {
    this.streetCredBonus = Math.max(0, streetCredBonus);
    this.notorietyBonus = Math.max(0, notorietyBonus);
    this.burnedStreetCred = Math.max(0, burnedStreetCred);
    this.awarenessAdjustment = awarenessAdjustment;
  }

  private static int nonNegative(int value, String label) {
    if (value < 0) {
      throw new IllegalArgumentException(label + " darf nicht negativ sein: " + value);
    }
    return value;
  }
}
