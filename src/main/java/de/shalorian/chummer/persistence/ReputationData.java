package de.shalorian.chummer.persistence;

/** Gespeicherte Eingaben des Spielleiters zur Reputation (GRW S. 308/309); fehlt in Dateien vor ADR-059. */
class ReputationData {

  public int streetCredBonus;
  public int notorietyBonus;
  public int burnedStreetCred;
  public int awarenessAdjustment;

  public ReputationData() {
  }

  ReputationData(int streetCredBonus, int notorietyBonus, int burnedStreetCred, int awarenessAdjustment) {
    this.streetCredBonus = streetCredBonus;
    this.notorietyBonus = notorietyBonus;
    this.burnedStreetCred = burnedStreetCred;
    this.awarenessAdjustment = awarenessAdjustment;
  }
}
