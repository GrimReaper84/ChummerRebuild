package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Einbauten mit Kapazität und Verfügbarkeitszuschlägen (GRW S. 378–382, am Bild gelesen). */
class GearCapacityTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  private static Gear gear(String name, int rating) {
    return new Gear(find(name), rating);
  }

  @Test
  void gogglesWithLowLightAndInfraredMatchRulebookValues() {
    // Brille Stufe 2 (Kap. 2, 50 ¥) + Restlichtverstärkung (+100 ¥, +4) + Infrarot (+100 ¥, +6)
    Gear goggles = gear("Brille", 2);
    goggles.install(gear("Restlichtverstärkung", 0));
    goggles.install(gear("Infrarot", 0));

    assertEquals(250, goggles.getNuyenCost());
    assertEquals("10", goggles.getAvailabilityText());
    assertEquals(2, goggles.getUsedCapacity());
    assertEquals(2, goggles.getCapacity());
  }

  @Test
  void thirdEnhancementExceedsCapacity() {
    Gear goggles = gear("Brille", 2);
    goggles.install(gear("Restlichtverstärkung", 0));
    goggles.install(gear("Infrarot", 0));

    String problem = goggles.installProblem(find("Blitzkompensation"), 0);
    assertNotNull(problem);
    assertTrue(problem.startsWith("Kapazität reicht nicht"), problem);
    assertThrows(IllegalArgumentException.class,
        () -> goggles.install(gear("Blitzkompensation", 0)));
  }

  @Test
  void ultrasoundNeedsTwoCapacity() {
    // GRW S. 379: belegt 2 Punkte Kapazität
    assertNotNull(gear("Brille", 1).installProblem(find("Ultraschall"), 0));
    assertNull(gear("Brille", 2).installProblem(find("Ultraschall"), 0));
  }

  @Test
  void audioEnhancementUsesOneCapacityPerRating() {
    // GRW S. 379: "1 Punkt Kapazität (pro Stufe)"
    Gear headphones = gear("Kopfhörer", 3);
    headphones.install(gear("Audioverbesserung", 2));

    assertEquals(2, headphones.getUsedCapacity());
    assertNull(headphones.installProblem(find("Richtungsdetektor"), 0));
    assertNotNull(headphones.installProblem(find("Selektiver Geräuschfilter"), 2));
  }

  @Test
  void surchargeLegalityRaisesDeviceLegality() {
    Gear lenses = gear("Kontaktlinsen", 2);
    lenses.install(gear("Bildverbindung", 0));
    lenses.install(gear("Smartlink", 0));

    // Kontaktlinsen 6 + Bildverbindung – + Smartlink +4E → 10E; 100 + 25 + 500 ¥
    assertEquals("10E", lenses.getAvailabilityText());
    assertEquals(625, lenses.getNuyenCost());
    assertEquals("+4E", lenses.getInstalled().get(1).getAvailabilityText());
  }

  @Test
  void sameEnhancementOnlyOnceButBiometricScannersPerPiece() {
    Gear goggles = gear("Brille", 4);
    goggles.install(gear("Infrarot", 0));
    assertEquals("Infrarot ist bereits eingebaut", goggles.installProblem(find("Infrarot"), 0));

    Gear maglock = gear("Magschloss", 4);
    maglock.install(gear("Biometrischer Scanner (Magschloss)", 0));
    maglock.install(gear("Biometrischer Scanner (Magschloss)", 0));
    maglock.install(gear("Anti-Manipulations-System", 3));
    assertNotNull(maglock.installProblem(find("Anti-Manipulations-System"), 1));
    // Magschloss Stufe 4 (400 ¥) + 2 × 200 ¥ + Stufe 3 × 100 ¥; Verf. – + 4 + 4
    assertEquals(1_100, maglock.getNuyenCost());
    assertEquals("8", maglock.getAvailabilityText());
  }

  @Test
  void enhancementsFitOnlyIntoMatchingDevices() {
    assertNotNull(gear("Kopfhörer", 3).installProblem(find("Infrarot"), 0));
    assertNotNull(gear("Fernglas, optisch", 0).installProblem(find("Infrarot"), 0));
    assertNotNull(gear("Schloss", 3).installProblem(find("Tastatur oder Kartenleser"), 0));
    assertNotNull(gear("Brille", 3).installProblem(find("Bewegungsmelder"), 0));
  }

  @Test
  void sensorPackageHoldsSensorFunctionsUpToCapacity() {
    // Handsensor Kapazität 3: Lasermikrofon [2] + Kamera-Sensor [1]; Sensor-Funktionen ändern die
    // Verfügbarkeit des Pakets nicht (eigene Verfügbarkeit, kein Zuschlag)
    Gear pack = gear("Sensorpaket (Hand/Minidrohne)", 0);
    pack.install(gear("Lasermikrofon", 3));
    pack.install(gear("Kamera, Trideo (Sensor)", 2));

    assertEquals(3, pack.getUsedCapacity());
    assertNotNull(pack.installProblem(find("Bewegungsmelder"), 0));
    assertEquals(150 + 200, pack.getNuyenCost());
    assertEquals("–", pack.getAvailabilityText());
  }

  @Test
  void canInstallAnyOfReportsFullDevices() {
    Gear goggles = gear("Brille", 1);
    assertTrue(goggles.canInstallAnyOf(GearDataLoader.getAllGear().values()));
    goggles.install(gear("Infrarot", 0));
    assertFalse(goggles.canInstallAnyOf(GearDataLoader.getAllGear().values()));

    Gear commlink = gear("Meta Link", 0);
    commlink.install(gear("Vector Xim", 0));
    assertFalse(commlink.canInstallAnyOf(GearDataLoader.getAllGear().values()));
    assertFalse(gear("Survival-Kit", 0).canInstallAnyOf(GearDataLoader.getAllGear().values()));
  }

  @Test
  void devicesWithInstallsAreSingleItems() {
    assertTrue(find("Brille").isSingleItem());
    assertTrue(find("Infrarot").isSingleItem());
    assertFalse(gear("Brille", 2).isSameKind(gear("Brille", 2)));
    assertFalse(find("Survival-Kit").isSingleItem());
  }

  @Test
  void creationLimitCountsSurchargeWithDeviceAvailability() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Gear lenses = gear("Kontaktlinsen", 3); // Verf. 6
    lenses.install(gear("Infrarot", 0)); // → 12

    assertFalse(c.getEquipment().isAvailableForInstall(lenses, find("Blitzkompensation"), 0));
    assertTrue(c.getEquipment().isAvailableForInstall(lenses, find("Bildverbindung"), 0));
    // Sensor-Funktionen: eigene Verfügbarkeit zählt allein, nicht die des Pakets
    Gear pack = gear("Sensorpaket (Hand/Minidrohne)", 0);
    assertTrue(c.getEquipment().isAvailableForInstall(pack, find("Lasermikrofon"), 6)); // 8E
    // Kontaktlinsen 6 + Infrarot 6 = 12 bleibt erlaubt, + Ultraschall 8 → 20 nicht
    assertTrue(c.getEquipment().isAvailableForInstall(gear("Kontaktlinsen", 3),
        find("Infrarot"), 0));
    assertFalse(c.getEquipment().isAvailableForInstall(lenses, find("Ultraschall"), 0));
  }
}
