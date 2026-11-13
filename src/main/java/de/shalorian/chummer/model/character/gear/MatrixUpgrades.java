package de.shalorian.chummer.model.character.gear;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Einbauregeln für Hardware-Module und Softwarepakete, gemeinsam für Kommlinks und Fahrzeuge (GRW S. 260/261): Die
 * Modulstufe wird zur neuen Stufe des Attributs und muss darüber liegen; Hardware (Prozessor, Signal) höchstens
 * {@value #MAX_HARDWARE_BONUS} über dem Ausgangswert; je Attribut nur ein Modul bzw. Paket. Softwarepakete brauchen ein
 * Betriebssystem, dessen Wert sie ersetzen (Ausgangswert 0 = keins).
 */
public final class MatrixUpgrades {

  /** Höchster Aufschlag eines Hardware-Moduls auf den Ausgangswert (S. 260). */
  public static final int MAX_HARDWARE_BONUS = 2;

  private MatrixUpgrades() {
  }

  /**
   * Warum {@code upgrade} in Stufe {@code rating} nicht zu einem Gerät mit den Ausgangswerten {@code base} und den
   * Einbauten {@code installed} passt, oder {@code null}. Für Gegenstände ohne {@link GearData#matrixAttribute()}
   * gibt es hier keine Regel ({@code null}); ob sie überhaupt in das Gerät passen, prüft der Aufrufer (Kommlink:
   * {@link Gear#installProblem}, Fahrzeug: nur Module und Pakete).
   */
  public static @Nullable String problem(@NonNull GearData upgrade, int rating, @NonNull MatrixBase base,
      @NonNull List<Gear> installed) {
    MatrixAttribute attribute = upgrade.matrixAttribute();
    if (attribute == null) {
      return null;
    }
    String kind = attribute.isHardware() ? "ein Modul" : "ein Paket";
    if (installed.stream().anyMatch(g -> g.getData().matrixAttribute() == attribute)) {
      return "Für " + attribute.getDisplayName() + " ist bereits " + kind + " eingebaut";
    }
    int original = base.valueOf(attribute);
    if (!attribute.isHardware() && original == 0) {
      return "Ein Softwarepaket braucht ein Betriebssystem";
    }
    if (rating <= original) {
      return "%s %d bringt nichts: %s ist schon %d".formatted(upgrade.name(), rating, attribute.getDisplayName(),
          original);
    }
    if (attribute.isHardware() && rating > original + MAX_HARDWARE_BONUS) {
      return "%s höchstens %d (Ausgangswert %d + %d)".formatted(attribute.getDisplayName(),
          original + MAX_HARDWARE_BONUS, original, MAX_HARDWARE_BONUS);
    }
    return null;
  }

  /** Stufe des eingebauten Moduls bzw. Pakets für {@code attribute}, oder {@code 0} ohne. */
  static int upgradeRating(@NonNull List<Gear> installed, @NonNull MatrixAttribute attribute) {
    return installed.stream()
        .filter(g -> g.getData().matrixAttribute() == attribute)
        .mapToInt(Gear::getRating)
        .max()
        .orElse(0);
  }
}
