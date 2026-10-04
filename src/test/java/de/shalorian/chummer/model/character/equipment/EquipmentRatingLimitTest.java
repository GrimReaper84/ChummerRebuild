package de.shalorian.chummer.model.character.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Stufengrenze 6 bei der Erschaffung für alle Kaufkategorien (GRW S. 111). */
class EquipmentRatingLimitTest {

  /** Verfügbarkeit 2, damit nur die Stufengrenze greift. */
  private static final Availability LOW_AVAILABILITY = new Availability("2", Legality.LEGAL);

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  @Test
  void ratingSixIsAllowedAndSevenIsBlockedAtCreation() {
    Equipment equipment = mensch().getEquipment();

    assertTrue(equipment.isAvailableForPurchase(LOW_AVAILABILITY, 6));
    assertFalse(equipment.isAvailableForPurchase(LOW_AVAILABILITY, 7));
  }

  @Test
  void careerModeHasNoRatingLimit() {
    Character c = mensch();
    c.setCreated(true);

    assertTrue(c.getEquipment().isAvailableForPurchase(LOW_AVAILABILITY, 10));
  }

  @Test
  void bowRatingSevenStaysBlocked() {
    WeaponData bow = Objects.requireNonNull(WeaponDataLoader.findByName("Bogen"));
    Equipment equipment = mensch().getEquipment();

    assertTrue(equipment.isAvailableForPurchase(bow.availability(), 6));
    assertFalse(equipment.isAvailableForPurchase(bow.availability(), 7));
  }

  @Test
  void cyberwareUpToRatingSixIsUnchanged() {
    // Dermalpanzerung: Stufe 1–3, Verf. (Stufe x 5)E — weiterhin nur über die Verfügbarkeit begrenzt
    Availability dermal = Objects.requireNonNull(
        CyberwareDataLoader.findByName("Dermalpanzerung")).availability();
    Equipment equipment = mensch().getEquipment();

    assertTrue(equipment.isAvailableForPurchase(dermal, 2));
    assertFalse(equipment.isAvailableForPurchase(dermal, 3));
  }
}
