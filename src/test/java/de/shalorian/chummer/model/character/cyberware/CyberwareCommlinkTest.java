package de.shalorian.chummer.model.character.cyberware;

import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.data;
import static de.shalorian.chummer.model.character.cyberware.ImplantTestSupport.implant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import org.junit.jupiter.api.Test;

/** Kommlink-Headware mit eingebautem Kommlink-Modell (GRW S. 386: 2.000 ¥ plus Kommlink). */
class CyberwareCommlinkTest {

  private static GearData gear(String name) {
    GearData data = GearDataLoader.findByName(name);
    assertNotNull(data, name);
    return data;
  }

  @Test
  void commlinkPriceIsAddedWithoutGradeMultiplier() {
    Cyberware headware = new Cyberware(data("Kommlink"), CyberwareGrade.ALPHAWARE, 0);
    Gear commlink = new Gear(gear("Meta Link"), 0);
    commlink.install(new Gear(gear("Vector Xim"), 0));
    headware.setCommlink(commlink);

    assertSame(commlink, headware.getCommlink());
    assertEquals(2000 * 2 + commlink.getNuyenCost(), headware.getNuyenCost());
  }

  @Test
  void onlyCommlinkModelsFitAndOnlyOne() {
    Cyberware headware = implant("Kommlink", 0);
    assertNotNull(headware.commlinkProblem(gear("Vector Xim")));
    assertNull(headware.commlinkProblem(gear("Meta Link")));

    headware.setCommlink(new Gear(gear("Meta Link"), 0));
    assertNotNull(headware.commlinkProblem(gear("CMT Clip")));
  }

  @Test
  void otherHeadwareTakesNoCommlink() {
    Cyberware datajack = implant("Datenbuchse", 0);
    assertNotNull(datajack.commlinkProblem(gear("Meta Link")));
    assertThrows(IllegalArgumentException.class, () -> datajack.setCommlink(new Gear(gear("Meta Link"), 0)));
  }

  @Test
  void removeCommlinkDropsItsPrice() {
    Cyberware headware = implant("Kommlink", 0);
    headware.setCommlink(new Gear(gear("Meta Link"), 0));
    headware.removeCommlink();
    assertNull(headware.getCommlink());
    assertEquals(2000, headware.getNuyenCost());
  }
}
