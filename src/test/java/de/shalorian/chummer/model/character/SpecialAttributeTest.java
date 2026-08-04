package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk: Edge ist vom Rassenmaximum-Bonus ausgenommen ("10 GP pro Attributsstufe
 * [ausgenommen Maximierung]"), Magic/Resonance erhalten denselben Bonus wie Standardattribute
 * (6. Punkt kostet 25 GP). Karma-Kosten im Karrieremodus folgen für alle drei Sonderattribute
 * derselben Formel wie Standardattribute (neue Stufe × 5).
 */
class SpecialAttributeTest {

  @Test
  void edgeBpCostHasNoRacialMaxBonus() {
    // Mensch-Edge-Spanne 2-7 (GRW S. 105: "Besondere Fähigkeiten: +1 Edge")
    SpecialAttribute edge = new SpecialAttribute(SpecialAttributeType.EDGE, 2, 7, false);
    edge.setValue(7);
    assertEquals(50, edge.getBpCost()); // (7-2) * 10, kein +15-Bonus
  }

  @Test
  void magicBpCostIncludesRacialMaxBonusAtSixthPoint() {
    SpecialAttribute magic = new SpecialAttribute(SpecialAttributeType.MAGIC, 1, 6, true);
    magic.setValue(6);
    assertEquals(65, magic.getBpCost()); // (6-1)*10 + 15
  }

  @Test
  void magicFirstPointIsFree() {
    // GRW: "erhalten automatisch einen Magiewert von 1" — Startwert 1 kostet 0 BP.
    SpecialAttribute magic = new SpecialAttribute(SpecialAttributeType.MAGIC, 1, 6, true);
    assertEquals(0, magic.getBpCost());
  }

  @Test
  void karmaCostIsNewValueTimesFiveForAllSpecialAttributes() {
    // GRW, Kapitel Attribut-Steigerung: "Die Kosten... betragen die neue Stufe x 5" — explizit
    // inklusive Edge/Magie/Resonanz, keine Ausnahme für Edge (anders als bei der BP-Formel).
    SpecialAttribute edge = new SpecialAttribute(SpecialAttributeType.EDGE, 2, 7, false);
    edge.setValue(4);
    assertEquals(25, edge.getKarmaCost()); // (4+1) * 5

    SpecialAttribute resonance = new SpecialAttribute(SpecialAttributeType.RESONANCE, 1, 6, true);
    assertEquals(10, resonance.getKarmaCost()); // (1+1) * 5
  }

  @Test
  void setValueOutsideRangeThrows() {
    SpecialAttribute edge = new SpecialAttribute(SpecialAttributeType.EDGE, 2, 7, false);
    assertThrows(IllegalArgumentException.class, () -> edge.setValue(1));
    assertThrows(IllegalArgumentException.class, () -> edge.setValue(8));
  }
}
