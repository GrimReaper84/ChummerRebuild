package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.skill.DicePool;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Eine Zelle der Tabelle „Häufige Rigger-/Drohnenproben“ (GRW S. 287): ein Würfelpool bzw. Initiativewert, oder kein
 * Wert mit Grund.
 *
 * @param pool Wert mit Aufschlüsselung, {@code null} wenn die Probe so nicht möglich ist
 * @param note Grund, warum es keinen Wert gibt (z.B. „keine Zielerfassung-Autosoft“), sonst leer
 */
public record VehiclePoolCell(@Nullable DicePool pool, @NonNull String note) {

  static @NonNull VehiclePoolCell of(@NonNull DicePool pool) {
    return new VehiclePoolCell(pool, "");
  }

  static @NonNull VehiclePoolCell none(@NonNull String reason) {
    return new VehiclePoolCell(null, reason);
  }
}
