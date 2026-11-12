package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.jspecify.annotations.NonNull;

/** Gemeinsame Testhilfen für Implantate mit echten Katalogeinträgen, auch für Tests im Paket {@code model.character}. */
public final class ImplantTestSupport {

  private ImplantTestSupport() {
  }

  public static @NonNull Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  public static @NonNull CyberwareData data(@NonNull String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  public static @NonNull Cyberware implant(@NonNull String name, int rating) {
    return new Cyberware(data(name), CyberwareGrade.STANDARD, rating);
  }

  /** Implantiert {@code name} direkt (ohne Kaufprüfung) und gibt es zurück. */
  public static @NonNull Cyberware add(@NonNull Character c, @NonNull String name, int rating) {
    Cyberware implant = implant(name, rating);
    c.getEquipment().addCyberware(implant);
    return implant;
  }

  /** Implantiert {@code name} ohne Stufe mit der Auswahl {@code choice} (Reflexrekorder), ohne Kaufprüfung. */
  public static @NonNull Cyberware add(@NonNull Character c, @NonNull String name, @NonNull String choice) {
    Cyberware implant = new Cyberware(data(name), CyberwareGrade.STANDARD, 0, choice, null);
    c.getEquipment().addCyberware(implant);
    return implant;
  }
}
