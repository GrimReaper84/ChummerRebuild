package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class CyberwareEntry {

  public @NonNull String name = "";
  /** {@code CyberwareGrade}-Enum-Name, z.B. {@code "ALPHAWARE"}. */
  public @NonNull String grade = "STANDARD";
  /** Gewählte Stufe, {@code 0} bei Implantaten ohne Stufe. */
  public int rating = 0;
  /** Gewählte Fertigkeit bzw. Fertigkeitsgruppe (Reflexrekorder), sonst leer. */
  public @NonNull String choice = "";
  /** Gewählter Preis bei Preisspanne (Kosmetische Modifikation), sonst {@code null}. */
  public @Nullable Integer price;
  /** In ein Basissystem eingebautes Zubehör; ihre Grade folgt beim Laden dem Basissystem. */
  public @NonNull List<CyberwareEntry> installed = new ArrayList<>();
  /** In die Kommlink-Headware eingebautes Kommlink samt Betriebssystem, sonst {@code null}. */
  public @Nullable GearEntry commlink;

  public CyberwareEntry() {
  }

  public CyberwareEntry(@NonNull String name, @NonNull String grade, int rating) {
    this.name = name;
    this.grade = grade;
    this.rating = rating;
  }
}
