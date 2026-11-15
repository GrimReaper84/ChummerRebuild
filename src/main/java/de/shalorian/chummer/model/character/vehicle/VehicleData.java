package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Unveränderlicher Katalogeintrag eines Fahrzeugs bzw. einer Drohne aus {@code vehicles.xml}.
 * Die Werte stammen aus der Fahrzeugtabelle des SR4-Grundregelwerks (S. 397, am PDF-Bild
 * übertragen), nicht aus dem C#-Original, das bei mehreren Einträgen abweicht (z.B. GMC Everglades
 * 25.500 ¥ statt 25.000 ¥, Sensor der Luftfahrzeuge). Die mitgelieferte Ausstattung (Halterungen,
 * Autosofts, Medkit) folgt den Fahrzeugtexten S. 395–398.
 *
 * @param skill           Fahrzeugfertigkeit, mit der das Fahrzeug gesteuert wird (S. 154/155), z.B. „Bodenfahrzeuge“
 * @param handling        Handling (kann negativ sein)
 * @param accelWalking    Beschleunigung, erster Wert ("Gehen")
 * @param accelRunning    Beschleunigung, zweiter Wert ("Laufen")
 * @param speed           Höchstgeschwindigkeit
 * @param cost            Preis in Nuyen
 * @param includedMounts  Anzahl der mitgelieferten Waffenhalterungen
 * @param mountNote       Bauform der mitgelieferten Halterungen laut Text (z.B. "Geschützturm"),
 *                        sonst leer
 * @param includedGear    mitgelieferte Ausrüstung, als erstes das Sensorpaket
 * @param note            Hinweis aus dem Fahrzeugtext, sonst leer
 */
public record VehicleData(
    @NonNull String name,
    @NonNull VehicleCategory category,
    @NonNull String skill,
    int handling,
    int accelWalking,
    int accelRunning,
    int speed,
    int pilot,
    int body,
    int armor,
    int sensor,
    @NonNull Availability availability,
    int cost,
    int includedMounts,
    @NonNull String mountNote,
    @NonNull List<IncludedVehicleGear> includedGear,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  public VehicleData {
    includedGear = List.copyOf(includedGear);
  }

  /** Beschleunigung wie in der GRW-Tabelle, z.B. {@code "10/20"}. */
  public @NonNull String accelText() {
    return accelWalking + "/" + accelRunning;
  }

  /** Handling wie in der GRW-Tabelle mit Vorzeichen, z.B. {@code "+1"}, {@code "0"}, {@code "-2"}. */
  public @NonNull String handlingText() {
    return handling > 0 ? "+" + handling : String.valueOf(handling);
  }
}
