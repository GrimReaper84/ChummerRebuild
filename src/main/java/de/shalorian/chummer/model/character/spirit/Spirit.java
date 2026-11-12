package de.shalorian.chummer.model.character.spirit;

import org.jspecify.annotations.NonNull;

/**
 * Ein Geist bzw. Sprite eines Charakters: Art aus dem Katalog, Kraft bzw. Stufe, geschuldete Dienste und ob er gebunden
 * bzw. registriert ist (GRW S. 112/113, 222, 281). Ungebundene Einträge sind Merkzettel für herbeigerufene Geister und
 * kompilierte Sprites (Nutzerentscheidung); sie kosten nichts und zählen nicht gegen Charisma.
 */
public class Spirit {

  private final @NonNull SpiritData data;
  private final int force;
  private int services;
  private boolean bound;

  /**
   * @throws IllegalArgumentException bei Kraft unter 1 oder negativen Diensten
   */
  public Spirit(@NonNull SpiritData data, int force, int services, boolean bound) {
    if (force < 1 || services < 0) {
      throw new IllegalArgumentException("%s: Kraft %d / Dienste %d ungültig".formatted(data.name(), force, services));
    }
    this.data = data;
    this.force = force;
    this.services = services;
    this.bound = bound;
  }

  public @NonNull SpiritData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Kraft (Geist) bzw. Stufe (Sprite). */
  public int getForce() {
    return force;
  }

  public int getServices() {
    return services;
  }

  /** Gebunden (Geist) bzw. registriert (Sprite). */
  public boolean isBound() {
    return bound;
  }

  /** Setzt die Dienste; die Prüfung übernimmt {@link Spirits#getServicesProblem}. */
  void setServices(int services) {
    this.services = services;
  }

  /** Setzt den Zustand; die Prüfung übernimmt {@link Spirits#getBindProblem}. */
  void setBound(boolean bound) {
    this.bound = bound;
  }
}
