package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.jspecify.annotations.NonNull;

/** Gemeinsame Testhilfen für Implantate mit echten Katalogeinträgen. */
final class ImplantTestSupport {

  private ImplantTestSupport() {
  }

  static @NonNull Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  static @NonNull CyberwareData data(@NonNull String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  static @NonNull Cyberware implant(@NonNull String name, int rating) {
    return new Cyberware(data(name), CyberwareGrade.STANDARD, rating);
  }

  /** Implantiert {@code name} direkt (ohne Kaufprüfung) und gibt es zurück. */
  static @NonNull Cyberware add(@NonNull Character c, @NonNull String name, int rating) {
    Cyberware implant = implant(name, rating);
    c.getEquipment().addCyberware(implant);
    return implant;
  }
}
