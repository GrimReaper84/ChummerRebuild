package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/** Hilfen für Tests rund um Adeptenkräfte. */
final class AdeptTestSupport {

  private AdeptTestSupport() {
  }

  /** Mensch mit der Gabe {@code gift} (Adept, Magieradept, Zauberer) und Magie {@code magic}. */
  static @NonNull Character withGift(@NonNull String gift, int magic) {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addQuality(quality(gift), false);
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(magic);
    return c;
  }

  static @NonNull QualityData quality(@NonNull String name) {
    QualityData data = QualityDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  static @NonNull AdeptPowerData power(@NonNull String name) {
    AdeptPowerData data = AdeptPowerDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  /** Kauft die Kraft und steigert sie auf {@code level}. */
  static @NonNull AdeptPower buy(@NonNull Character c, @NonNull String name, @NonNull String choice, int level) {
    AdeptPower power = c.getAdeptPowers().add(power(name), choice);
    while (power.getLevel() < level) {
      c.getAdeptPowers().raise(power);
    }
    return power;
  }

  static @NonNull BigDecimal points(@NonNull String value) {
    return new BigDecimal(value);
  }

  static @NonNull Cyberware cyberware(@NonNull String essence) {
    return new Cyberware(new CyberwareData("Test-" + essence, "Test", EquipmentKind.CYBERWARE,
        essence, "100", 0, Availability.NONE, "SR4", "0"), CyberwareGrade.STANDARD, 0);
  }
}
