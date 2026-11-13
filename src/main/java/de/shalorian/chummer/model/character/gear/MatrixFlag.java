package de.shalorian.chummer.model.character.gear;

import java.util.Arrays;
import org.jspecify.annotations.NonNull;

/** Merkmal eines Ausrüstungsgegenstands für den Matrixzugang ({@code <matrix>} in {@code gear.xml}). */
public enum MatrixFlag {
  /** Direktes Neuralinterface: Elektroden, Nanitencreme-Troden, Simrig (GRW S. 258, 374). */
  DNI("dni"),
  /** Sim-Modul für kaltes Sim (S. 374); auch im Simrig enthalten. */
  SIM_COLD("sim-cold"),
  /** Für heißes Sim modifiziertes Sim-Modul (S. 374). */
  SIM_HOT("sim-hot"),
  /** Lässt sich in ein Kommlink einbauen (Sim-Modul). */
  COMMLINK_OPTION("commlink-option");

  private final @NonNull String code;

  MatrixFlag(@NonNull String code) {
    this.code = code;
  }

  /**
   * Merkmal zum Text in {@code <matrix>}.
   *
   * @throws IllegalArgumentException bei unbekanntem Code (Datenfehler im Katalog)
   */
  public static @NonNull MatrixFlag fromCode(@NonNull String code) {
    return Arrays.stream(values())
        .filter(f -> f.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unbekanntes Matrix-Merkmal: " + code));
  }
}
