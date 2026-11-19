package de.shalorian.chummer.model.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CritterPowerDataLoaderTest {

  @Test
  void loadsAllCritterAndSpritePowers() {
    assertEquals(48, CritterPowerDataLoader.getAll().size());
    assertEquals(9, CritterPowerDataLoader.getAll().values().stream().filter(CritterPowerData::sprite).count());
    CritterPowerData weapon = CritterPowerDataLoader.findByName("Natürliche Waffe");
    assertNotNull(weapon);
    assertEquals("P", weapon.type());
    assertEquals("Komplex", weapon.action());
    assertEquals("B", weapon.range());
    assertEquals("Sofort", weapon.duration());
    assertEquals(CritterPowerEffect.NATURAL_WEAPON, weapon.effect());
    assertEquals(340, weapon.page());
    assertEquals(CritterPowerEffect.HARDENED_ARMOR,
        CritterPowerDataLoader.findForEntry("Verstärkter Panzer (4/4)").effect());
  }

  @Test
  void loadsWeaknesses() {
    assertEquals(5, CritterWeaknessDataLoader.getAll().size());
    assertNotNull(CritterWeaknessDataLoader.findByName("Essenzverlust"));
  }

  /** Jede Kraft der Geister und Sprites aus spirits.xml steht im Kräftekatalog (bei Sprites ggf. als Komplexe Form). */
  @Test
  void everySpiritPowerHasACatalogEntry() {
    List<String> missing = new ArrayList<>();
    for (SpiritData spirit : SpiritDataLoader.getAll().values()) {
      List<String> names = new ArrayList<>(spirit.powers());
      names.addAll(spirit.optional());
      for (String name : names) {
        boolean complexForm = spirit.kind() == SpiritKind.SPRITE && ComplexFormDataLoader.findByName(name) != null;
        if (CritterPowerDataLoader.findForEntry(name) == null && !complexForm && !name.startsWith("beliebige")) {
          missing.add(spirit.name() + ": " + name);
        }
      }
    }
    assertTrue(missing.isEmpty(), String.join(", ", missing));
  }
}
