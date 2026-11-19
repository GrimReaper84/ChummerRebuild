package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/** Ein Fahrzeug bzw. eine Drohne samt Ausstattung in der Speicherdatei. */
class VehicleEntry {

  /** Gratis-Anteil aus dem Spielleiter-Modus (ADR-070); fehlt in älteren Dateien, dann 0. */
  public int gmFreeNuyen;

  public @NonNull String name = "";
  /** Ob ein Riggeradapter nachgerüstet wurde (bei Drohnen immer mitgeliefert, hier {@code false}). */
  public boolean riggerAdapter = false;
  /** Alle Halterungen in Fahrzeugreihenfolge, mitgelieferte zuerst. */
  public @NonNull List<MountEntry> mounts = new ArrayList<>();
  /**
   * Mitgelieferte Ausrüstung in Katalogreihenfolge. Sie wird beim Laden aus dem Katalog erzeugt; von
   * diesen Einträgen werden nur die Einbauten (z.B. Sensor-Funktionen im Sensorpaket) übernommen.
   */
  public @NonNull List<GearEntry> includedGear = new ArrayList<>();
  /** Gekaufte Ausrüstung im Fahrzeug. */
  public @NonNull List<GearEntry> gear = new ArrayList<>();
  /** Hardware-Module und Softwarepakete; fehlt in Dateien vor ADR-054. */
  public @NonNull List<GearEntry> matrixUpgrades = new ArrayList<>();
  /** Schaden in Kästchen (nur im Karrieremodus eingetragen); fehlt in Dateien vor ADR-056. */
  public int damage = 0;

  public VehicleEntry() {
    // Für Jackson-Deserialisierung benötigt.
  }

  public VehicleEntry(@NonNull String name) {
    this.name = name;
  }
}
