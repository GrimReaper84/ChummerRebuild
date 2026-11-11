package de.shalorian.chummer.model.character.vehicle;

import org.jspecify.annotations.NonNull;

/**
 * Mit dem Fahrzeug gelieferte Ausrüstung laut Fahrzeugtext, z.B. Clearsight- und
 * Zielerfassungs-Autosoft (Stufe 3) des GM-Nissan Doberman (SR4-Grundregelwerk S. 398) oder das
 * Sensorpaket, das jedes Fahrzeug besitzt (S. 380: Die Sensorstufe eines Fahrzeugs ist die Stufe
 * seines eingebauten Sensorpakets).
 *
 * @param name   Name des Eintrags im Ausrüstungskatalog ({@code gear.xml})
 * @param rating Stufe, {@code 0} bei Gegenständen ohne Stufe
 * @param extra  Zusatztext (z.B. Autosoft-Typ), sonst leer
 */
public record IncludedVehicleGear(@NonNull String name, int rating, @NonNull String extra) {
}
