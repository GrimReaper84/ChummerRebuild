package de.shalorian.chummer.model.npc;

import org.jspecify.annotations.NonNull;

/**
 * Klassen der Professionalitätsstufe einer Schergengruppe (SR4-Grundregelwerk S. 325) mit dem Anteil Ausgeschalteter,
 * ab dem die Gruppe flieht bzw. sich zurückzieht. Die Schwelle ist streng: Erst „mehr als“ den Anteil löst die Flucht
 * aus. Elite kämpft bis zum Ende.
 */
public enum ProfessionalRating {
  UNTRAINED("Ungeübt", 0, 0, 1, 4),
  SEMI_TRAINED("Halb ausgebildet", 1, 2, 1, 2),
  TRAINED("Ausgebildet", 3, 4, 3, 4),
  ELITE("Elite", 5, 6, 0, 0);

  /** Höchste Professionalitätsstufe. */
  public static final int MAX_RATING = 6;

  private final @NonNull String displayName;
  private final int minRating;
  private final int maxRating;
  private final int fleeNumerator;
  private final int fleeDenominator;

  ProfessionalRating(@NonNull String displayName, int minRating, int maxRating, int fleeNumerator,
      int fleeDenominator) {
    this.displayName = displayName;
    this.minRating = minRating;
    this.maxRating = maxRating;
    this.fleeNumerator = fleeNumerator;
    this.fleeDenominator = fleeDenominator;
  }

  /**
   * Klasse zur Stufe.
   *
   * @throws IllegalArgumentException außerhalb 0–6
   */
  public static @NonNull ProfessionalRating of(int rating) {
    for (ProfessionalRating r : values()) {
      if (rating >= r.minRating && rating <= r.maxRating) {
        return r;
      }
    }
    throw new IllegalArgumentException("Professionalitätsstufe muss zwischen 0 und 6 liegen: " + rating);
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob eine Gruppe dieser Klasse überhaupt flieht. */
  public boolean flees() {
    return fleeDenominator > 0;
  }

  /** Ob mehr als der Fluchtanteil der {@code members} ausgeschaltet ist. */
  public boolean isFleeing(int knockedOut, int members) {
    return flees() && knockedOut * fleeDenominator > members * fleeNumerator;
  }

  /** Fluchtschwelle als Text, z.B. „mehr als die Hälfte“; leer bei Elite. */
  public @NonNull String fleeThresholdText() {
    return switch (this) {
      case UNTRAINED -> "mehr als ein Viertel";
      case SEMI_TRAINED -> "mehr als die Hälfte";
      case TRAINED -> "mehr als drei Viertel";
      case ELITE -> "";
    };
  }
}
