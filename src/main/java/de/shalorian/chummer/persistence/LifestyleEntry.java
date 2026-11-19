package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class LifestyleEntry {

  /** Gratis-Anteil aus dem Spielleiter-Modus (ADR-070); fehlt in älteren Dateien, dann 0. */
  public int gmFreeNuyen;

  /** {@code LifestyleLevel}-Enum-Name, z.B. {@code "MIDDLE"}. */
  public @NonNull String level = "STREET";
  /** Im Voraus bezahlte Monate, mindestens 1. */
  public int months = 1;
  /** Ausgesetzte Monate in Folge (GRW S. 312); fehlt in Dateien vor ADR-061. */
  public int missedMonths;
  /** Offene Schulden aus Abstiegen in Nuyen; fehlt in Dateien vor ADR-061. */
  public int debt;

  public LifestyleEntry() {
  }

  public LifestyleEntry(@NonNull String level, int months, int missedMonths, int debt) {
    this.level = level;
    this.months = months;
    this.missedMonths = missedMonths;
    this.debt = debt;
  }
}
