package de.shalorian.chummer.model.character.gear;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Wirksame Matrixattribute eines Kommlinks oder Fahrzeugs samt Aufrüstungen und Prozessorlimit.
 *
 * <ul>
 *   <li>Ein Modul bzw. Paket ersetzt den Ausgangswert durch seine Stufe (GRW S. 260, Regeln in {@link
 *       MatrixUpgrades}).</li>
 *   <li>Das System ist höchstens so hoch wie der aufgerüstete Prozessor (S. 260); {@link #nominalSystem()} ist der
 *       Wert vor dieser Grenze.</li>
 *   <li>Prozessorlimit = System (S. 261): Je volle Anzahl laufender Programme in Höhe des Limits sinkt der Prozessor
 *       um 1, nicht unter 0. Die Senkung ändert die Systemgrenze nicht (S. 271).</li>
 *   <li>Ein Programm läuft höchstens mit der Systemstufe (S. 271), siehe {@link #programRating(int)}.</li>
 * </ul>
 *
 * @param processor         wirksamer Prozessor nach Prozessorlimit
 * @param signal            Signal
 * @param firewall          Firewall
 * @param system            wirksames System, höchstens {@link #hardwareProcessor()}
 * @param nominalSystem     System laut Betriebssystem bzw. Paket
 * @param hardwareProcessor Prozessor mit Modul, vor dem Prozessorlimit
 * @param runningPrograms   laufende Programme auf dem Gerät
 */
public record MatrixStats(int processor, int signal, int firewall, int system, int nominalSystem,
                          int hardwareProcessor, int runningPrograms) {

  /** Werte aus den Ausgangswerten {@code base} und den Einbauten {@code installed} (Module, Pakete, Programme). */
  public static @NonNull MatrixStats of(@NonNull MatrixBase base, @NonNull List<Gear> installed) {
    int hardwareProcessor = upgraded(base, installed, MatrixAttribute.PROCESSOR);
    int nominalSystem = upgraded(base, installed, MatrixAttribute.SYSTEM);
    int system = Math.min(nominalSystem, hardwareProcessor);
    int running = (int) installed.stream().filter(Gear::isRunning).count();
    int penalty = system == 0 ? 0 : running / system;
    return new MatrixStats(Math.max(0, hardwareProcessor - penalty), upgraded(base, installed, MatrixAttribute.SIGNAL),
        upgraded(base, installed, MatrixAttribute.FIREWALL), system, nominalSystem, hardwareProcessor, running);
  }

  private static int upgraded(@NonNull MatrixBase base, @NonNull List<Gear> installed,
      @NonNull MatrixAttribute attribute) {
    return Math.max(base.valueOf(attribute), MatrixUpgrades.upgradeRating(installed, attribute));
  }

  /** Prozessorlimit: so viele Programme laufen ohne Senkung des Prozessors (S. 261). */
  public int processorLimit() {
    return system;
  }

  /** Senkung des Prozessors durch laufende Programme. */
  public int processorPenalty() {
    return hardwareProcessor - processor;
  }

  /** Ob das System durch den Prozessor begrenzt wird (S. 260). */
  public boolean isSystemCapped() {
    return system < nominalSystem;
  }

  /** Stufe, mit der ein Programm der Stufe {@code rating} auf diesem Gerät läuft (S. 271). */
  public int programRating(int rating) {
    return Math.min(rating, system);
  }
}
