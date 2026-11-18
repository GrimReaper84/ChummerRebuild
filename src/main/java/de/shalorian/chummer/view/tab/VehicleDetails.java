package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleData;
import de.shalorian.chummer.model.character.vehicle.VehicleModification;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.utils.Numbers;
import java.util.OptionalInt;
import org.jspecify.annotations.NonNull;

/**
 * Anzeigewerte des Detailbereichs für ein Fahrzeug im Tab "Fahrzeuge &amp; Drohnen". Enthält nur
 * fertige Texte und keine JavaFX-Typen, damit die Werte ohne UI testbar sind. Leere Werte erscheinen
 * wie im GRW als "–".
 *
 * @param sensor           Sensorstufe laut Tabelle, mit eingebauten Sensoren zusätzlich deren
 *                         Durchschnitt (GRW S. 380), z.B. "3 (Ø Sensoren 4)"
 * @param conditionMonitor Kästchen des Zustandsmonitors (GRW S. 197)
 * @param mounts           Halterungen, z.B. "1 von max. 1 (0 belegt)"
 * @param riggerAdapter    "mitgeliefert", "eingebaut" oder "–"
 * @param included         mitgelieferte Ausstattung, kommagetrennt
 * @param cost             Fahrzeugpreis, mit Ausstattung zusätzlich die Gesamtsumme
 * @param matrix           wirksame Matrixwerte aus Gerätestufe und Aufrüstungen (GRW S. 260/261)
 */
record VehicleDetails(
    @NonNull String name,
    @NonNull String category,
    @NonNull String handling,
    @NonNull String accel,
    @NonNull String speed,
    @NonNull String pilot,
    @NonNull String body,
    @NonNull String armor,
    @NonNull String sensor,
    @NonNull String deviceRating,
    @NonNull String conditionMonitor,
    @NonNull String mounts,
    @NonNull String riggerAdapter,
    @NonNull String included,
    @NonNull String availability,
    @NonNull String cost,
    @NonNull String source,
    @NonNull String note,
    @NonNull String matrix
) {

  private static final String NO_VALUE = "–";

  static @NonNull VehicleDetails of(@NonNull Vehicle vehicle) {
    VehicleData data = vehicle.getData();
    int occupied = vehicle.getMountedWeapons().size();
    return new VehicleDetails(
        data.name(),
        data.category().getDisplayName(),
        data.handlingText(),
        data.accelText(),
        String.valueOf(data.speed()),
        String.valueOf(data.pilot()),
        String.valueOf(data.body()),
        String.valueOf(data.armor()),
        sensorText(vehicle),
        String.valueOf(vehicle.getDeviceRating()),
        vehicle.getConditionMonitor() + " Kästchen",
        vehicle.getMounts().size() + " von max. " + vehicle.getMaxMounts() + " (" + occupied
            + " belegt)",
        riggerAdapterText(vehicle),
        orDash(String.join(", ", vehicle.describeIncludedEquipment())),
        data.availability().displayText(0),
        costText(vehicle),
        data.source() + " S. " + data.page(),
        data.note(),
        matrixText(vehicle.getMatrixStats()));
  }

  private static @NonNull String matrixText(@NonNull MatrixStats stats) {
    return "Prozessor %d · Signal %d · Firewall %d · System %d".formatted(stats.processor(), stats.signal(),
        stats.firewall(), stats.system());
  }

  private static @NonNull String sensorText(@NonNull Vehicle vehicle) {
    String text = String.valueOf(vehicle.getData().sensor());
    OptionalInt average = vehicle.getAverageSensorRating();
    return average.isPresent() ? text + " (Ø Sensoren " + average.getAsInt() + ")" : text;
  }

  private static @NonNull String riggerAdapterText(@NonNull Vehicle vehicle) {
    if (vehicle.isRiggerAdapterIncluded()) {
      return "mitgeliefert";
    }
    return vehicle.isRiggerAdapterPurchased() ? "eingebaut" : NO_VALUE;
  }

  /** z.B. "14000 ¥" bzw. mit Ausstattung "14000 ¥ (gesamt 16500 ¥)". */
  private static @NonNull String costText(@NonNull Vehicle vehicle) {
    int own = vehicle.getData().cost();
    String text = Numbers.nuyen(own);
    return vehicle.getNuyenCost() == own ? text : text + " (gesamt " + Numbers.nuyen(vehicle.getNuyenCost()) + ")";
  }

  /** Text zu einer Halterung: Bauform laut Fahrzeugtext und Herkunft. */
  static @NonNull String mountText(@NonNull Vehicle vehicle, @NonNull WeaponMount mount) {
    if (!mount.isIncluded()) {
      VehicleModification mod = VehicleModification.WEAPON_MOUNT;
      return "Nachgerüstet (" + Numbers.nuyen(mod.cost()) + ", Verf. " + mod.availability().displayText(0)
          + ", GRW S. 396)";
    }
    String note = vehicle.getData().mountNote();
    return "Mit dem Fahrzeug geliefert" + (note.isEmpty() ? "" : " (" + note + ")")
        + ", im Preis enthalten";
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? NO_VALUE : text;
  }
}
