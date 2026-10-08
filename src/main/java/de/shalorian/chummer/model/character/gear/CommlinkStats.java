package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Matrixattribute eines Kommlinks samt installiertem Betriebssystem: Prozessor und Signal stammen
 * vom Kommlink-Modell, Firewall und System vom Betriebssystem (Tabellen GRW S. 374). Ohne
 * Betriebssystem sind Firewall und System {@code 0}.
 *
 * <p>SR4-Grundregelwerk S. 260: "Wenn die Prozessorstufe des Gerätes kleiner ist als die Systemstufe,
 * dann wird die Systemstufe auf die Prozessorstufe reduziert." {@link #system()} ist der wirksame
 * Wert, {@link #nominalSystem()} der Wert laut Betriebssystem-Tabelle.
 *
 * @param processor     Prozessor des Kommlinks
 * @param signal        Signal des Kommlinks
 * @param firewall      Firewall des Betriebssystems
 * @param system        wirksames System, höchstens {@code processor}
 * @param nominalSystem System laut Tabelle des Betriebssystems
 */
public record CommlinkStats(int processor, int signal, int firewall, int system, int nominalSystem) {

  /**
   * Werte eines Kommlinks mit dem Betriebssystem {@code os}, oder ohne Betriebssystem, wenn {@code os}
   * {@code null} ist.
   */
  public static @NonNull CommlinkStats of(@NonNull GearData commlink, @Nullable GearData os) {
    int firewall = os == null ? 0 : os.firewall();
    int nominalSystem = os == null ? 0 : os.system();
    return new CommlinkStats(commlink.processor(), commlink.signal(), firewall,
        Math.min(nominalSystem, commlink.processor()), nominalSystem);
  }

  /** Ob das System des Betriebssystems auf den Prozessor begrenzt wird (GRW S. 260). */
  public boolean isSystemCapped() {
    return system < nominalSystem;
  }
}
