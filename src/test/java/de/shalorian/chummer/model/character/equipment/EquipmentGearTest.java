package de.shalorian.chummer.model.character.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Kauf, Zusammenführen und Budget der übrigen Ausrüstung (GRW S. 110/111). */
class EquipmentGearTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  @Test
  void gearCountsAgainstResourceNuyen() {
    // 10 BP = 50.000 ¥; Panzerjacke 900 ¥ + Automatischer Dietrich Stufe 3 (600 ¥) → 48.500 ¥
    Character c = mensch();
    c.setNuyenBP(10);
    c.getEquipment().addArmor(
        new Armor(Objects.requireNonNull(ArmorDataLoader.findByName("Panzerjacke"))));
    c.getEquipment().addGear(new Gear(find("Automatischer Dietrich"), 3));

    assertEquals(600, c.getEquipment().getGearNuyenCost());
    assertEquals(48_500, c.getRemainingNuyen());
  }

  @Test
  void sameKindIsMergedIntoExistingItem() {
    Equipment equipment = mensch().getEquipment();
    GearData rfid = find("Standard-RFID-Marker");
    Gear first = equipment.addGear(new Gear(rfid, 0));

    Gear merged = equipment.addGear(new Gear(rfid, 0, 40, "", null));

    assertSame(first, merged);
    assertEquals(1, equipment.getGear().size());
    assertEquals(60, first.getQuantity());
    assertEquals(3, equipment.getGearNuyenCost());
  }

  @Test
  void differentExtrasStaySeparate() {
    Equipment equipment = mensch().getEquipment();
    GearData license = find("Gefälschte Lizenz");

    equipment.addGear(new Gear(license, 3, 1, "Waffenlizenz", null));
    equipment.addGear(new Gear(license, 3, 1, "Führerschein", null));

    assertEquals(2, equipment.getGear().size());
    assertEquals(600, equipment.getGearNuyenCost());
  }

  @Test
  void changingQuantityAndRemovingUpdatesCost() {
    Equipment equipment = mensch().getEquipment();
    Gear rope = equipment.addGear(new Gear(find("Standardseil"), 0));

    rope.setQuantity(300);
    assertEquals(150, equipment.getGearNuyenCost());

    equipment.removeGear(rope);
    assertEquals(0, equipment.getGearNuyenCost());
  }
}
