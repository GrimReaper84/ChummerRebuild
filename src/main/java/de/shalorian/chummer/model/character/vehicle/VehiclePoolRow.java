package de.shalorian.chummer.model.character.vehicle;

import org.jspecify.annotations.NonNull;

/**
 * Zeile der Tabelle „Häufige Rigger-/Drohnenproben“ (GRW S. 287).
 *
 * @param test       Handlung, z.B. „Angriff“
 * @param jumpedIn   Rigger ist in das Fahrzeug hineingesprungen
 * @param autonomous Drohne handelt selbst
 * @param remote     Rigger steuert fern
 */
public record VehiclePoolRow(@NonNull String test, @NonNull VehiclePoolCell jumpedIn,
                             @NonNull VehiclePoolCell autonomous, @NonNull VehiclePoolCell remote) {
}
