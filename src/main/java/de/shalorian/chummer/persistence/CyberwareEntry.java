package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class CyberwareEntry {

  public @NonNull String name = "";
  /** {@code CyberwareGrade}-Enum-Name, z.B. {@code "ALPHAWARE"}. */
  public @NonNull String grade = "STANDARD";
  /** Gewählte Stufe, {@code 0} bei Implantaten ohne Stufe. */
  public int rating = 0;

  public CyberwareEntry() {
  }

  public CyberwareEntry(@NonNull String name, @NonNull String grade, int rating) {
    this.name = name;
    this.grade = grade;
    this.rating = rating;
  }
}
