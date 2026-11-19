package de.shalorian.chummer.model.character.spirit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Ein Geist bzw. Sprite eines Charakters: Art aus dem Katalog, Kraft bzw. Stufe, geschuldete Dienste und ob er gebunden
 * bzw. registriert ist (GRW S. 112/113, 222, 281). Ungebundene Einträge sind Merkzettel für herbeigerufene Geister und
 * kompilierte Sprites (Nutzerentscheidung); sie kosten nichts und zählen nicht gegen Charisma. Dazu die optionalen
 * Kräfte bzw. Komplexen Formen, die beim Beschwören bzw. Kompilieren gewählt wurden (GRW S. 347, 282); sie stehen danach
 * fest und lassen sich nur nachtragen, solange weniger gewählt sind als erlaubt (siehe {@link Spirits#addMissingOption}).
 */
public class Spirit {

  /** Gratis-Anteil (BP) aus dem Spielleiter-Modus bei der Erschaffung, ADR-070; zählt nicht gegen die 400 BP. */
  private int gmFreeBp;
  private final @NonNull SpiritData data;
  private final int force;
  private int services;
  private boolean bound;
  private final @NonNull List<String> options;

  /**
   * @throws IllegalArgumentException bei Kraft unter 1 oder negativen Diensten
   */
  public Spirit(@NonNull SpiritData data, int force, int services, boolean bound) {
    this(data, force, services, bound, List.of());
  }

  /**
   * Wie {@link #Spirit(SpiritData, int, int, boolean)} mit gewählten optionalen Kräften bzw. Komplexen Formen; die Prüfung
   * übernimmt {@link Spirits#getOptionsProblem}.
   */
  public Spirit(@NonNull SpiritData data, int force, int services, boolean bound, @NonNull List<String> options) {
    if (force < 1 || services < 0) {
      throw new IllegalArgumentException("%s: Kraft %d / Dienste %d ungültig".formatted(data.name(), force, services));
    }
    this.data = data;
    this.force = force;
    this.services = services;
    this.bound = bound;
    this.options = new ArrayList<>(options);
  }

  /** Gewählte optionale Kräfte bzw. Komplexe Formen in Reihenfolge der Wahl. */
  public @NonNull List<String> getOptions() {
    return Collections.unmodifiableList(options);
  }

  /** Wie viele optionale Kräfte bzw. Komplexe Formen noch fehlen (je 3 volle Punkte eine). */
  public int getMissingOptionCount() {
    return Math.max(0, data.optionalCount(force) - options.size());
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

  /** Trägt eine Option nach; die Prüfung übernimmt {@link Spirits#addMissingOption}. */
  void addOption(@NonNull String option) {
    options.add(option);
  }

  /** Setzt den Zustand; die Prüfung übernimmt {@link Spirits#getBindProblem}. */
  void setBound(boolean bound) {
    this.bound = bound;
  }

  /** Gratis-Anteil (BP) aus dem Spielleiter-Modus bei der Erschaffung (ADR-070). */
  public int getGmFreeBp() {
    return gmFreeBp;
  }

  public void setGmFreeBp(int gmFreeBp) {
    this.gmFreeBp = Math.max(0, gmFreeBp);
  }
}
