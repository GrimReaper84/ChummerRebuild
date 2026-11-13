package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Wie der Charakter in die Matrix geht (GRW S. 258, 265, 276): ohne aktives Kommlink gar nicht, mit Kommlink in AR,
 * mit Sim-Modul und Direktem Neuralinterface in VR mit kaltem bzw. heißem Sim. Technomancer sind in VR immer im
 * heißen Sim (S. 279).
 */
public enum MatrixAccess {
  NONE("kein Matrixzugang"),
  AR("AR"),
  VR_COLD("VR kalt"),
  VR_HOT("VR heiß");

  private final @NonNull String displayName;

  MatrixAccess(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob der Charakter in der virtuellen Realität ist und damit eine eigene Matrixinitiative hat. */
  public boolean isVirtual() {
    return this == VR_COLD || this == VR_HOT;
  }
}
