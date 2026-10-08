package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Munition pro 10 Schuss je Waffenklasse und Bolzen (GRW S. 361, 369, am Bild gelesen). */
class AmmunitionTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void thirtyGelRoundsForHeavyPistolsCostNinety() {
    // Gelgeschosse 30 ¥ pro 10 Schuss
    Gear gel = new Gear(find("Gelgeschosse"), 0, 30, "Schwere Pistolen", null);

    assertEquals(90, gel.getNuyenCost());
    assertEquals("Gelgeschosse (Schwere Pistolen, 30 Schuss)", gel.getDisplayName());
    GearCombatStats combat = Objects.requireNonNull(gel.getData().combat());
    assertEquals("(−1)G", combat.damage());
    assertEquals("+2", combat.armorPiercing());
    assertEquals("Stoß", combat.armorType());
  }

  @Test
  void apdsIsForbiddenAtCreation() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    GearData apds = find("APDS-Geschosse");

    assertEquals("16V", apds.availability().displayText(0));
    assertFalse(c.getEquipment().isAvailableForPurchase(apds.availability(), 0));
    assertTrue(c.getEquipment().isAvailableForPurchase(find("Standardmunition").availability(), 0));
  }

  @Test
  void weaponClassMustBeAFirearmClassWithAmmunition() {
    GearData standard = find("Standardmunition");

    assertThrows(IllegalArgumentException.class,
        () -> new Gear(standard, 0, 10, "Bögen", null));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(standard, 0, 10, "Wasserpistolen", null));
    assertThrows(IllegalArgumentException.class,
        () -> new Gear(standard, 0, 15, "Sturmgewehre", null));
    assertNotNull(new Gear(standard, 0, 10, "Taser", null));
    // Munition für eine feste Waffe braucht keine Klassenwahl
    assertEquals("Taserpfeil (10 Schuss)",
        new Gear(find("Taserpfeil"), 0, 10, "", null).getDisplayName());
    assertFalse(find("Sturmkanonen-Geschosse").needsExtra());
    assertFalse(find("Injektionspfeile").needsExtra());
    assertTrue(ExtraKind.WEAPON_CLASS.suggestions().contains("Sturmkanonen"));
    assertFalse(ExtraKind.WEAPON_CLASS.suggestions().contains("Granatwerfer"));
  }

  @Test
  void sameAmmunitionForTheSameClassIsMerged() {
    Gear first = new Gear(find("Standardmunition"), 0, 20, "Sturmgewehre", null);

    assertTrue(first.isSameKind(new Gear(find("Standardmunition"), 0, 10, "Sturmgewehre", null)));
    assertFalse(first.isSameKind(new Gear(find("Standardmunition"), 0, 10, "Schrotflinten", null)));
  }

  @Test
  void boltsArePlainGearPerPiece() {
    // Bolzen 5 ¥, Verf. 2; Injektionsbolzen 50 ¥, 8E (Tabelle Armbrüste S. 361)
    Gear bolts = new Gear(find("Bolzen"), 0, 12, "", null);

    assertEquals(60, bolts.getNuyenCost());
    assertEquals("Bolzen ×12", bolts.getDisplayName());
    assertEquals("8E", find("Injektionsbolzen").availability().displayText(0));
    assertFalse(find("Bolzen").hasRating());
  }
}
