package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Ausgangswerte der Matrixattribute eines Geräts vor Aufrüstungen (GRW S. 260): beim Kommlink Prozessor und Signal des
 * Modells sowie Firewall und System des Betriebssystems ({@code 0} ohne Betriebssystem), beim Fahrzeug die Gerätestufe.
 */
public record MatrixBase(int processor, int signal, int firewall, int system) {

  /** Gerät, dessen Attribute alle der Gerätestufe {@code deviceRating} entsprechen (S. 261). */
  public static @NonNull MatrixBase ofDeviceRating(int deviceRating) {
    return new MatrixBase(deviceRating, deviceRating, deviceRating, deviceRating);
  }

  /** Ausgangswert von {@code attribute}. */
  public int valueOf(@NonNull MatrixAttribute attribute) {
    return switch (attribute) {
      case PROCESSOR -> processor;
      case SIGNAL -> signal;
      case FIREWALL -> firewall;
      case SYSTEM -> system;
    };
  }
}
