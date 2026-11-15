package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;

/** Edgepunkte ausgeben, zurückgewinnen, auffrischen und Edge verheizen (GRW S. 98/99). */
class CharacterEdgeTest {

  /** Mensch mit Edge 3 im Karrieremodus. */
  private static Character career() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.getEdge().setValue(3);
    c.setCreated(true);
    return c;
  }

  @Test
  void spendRegainAndRefresh() {
    Character c = career();
    assertEquals(3, c.getEdgePoints());
    c.spendEdge();
    c.spendEdge();
    assertEquals(1, c.getEdgePoints());
    c.regainEdge();
    assertEquals(2, c.getEdgePoints());
    c.spendEdge();
    c.spendEdge();
    assertEquals(0, c.getEdgePoints());
    assertThrows(IllegalStateException.class, c::spendEdge);
    c.refreshEdge();
    assertEquals(3, c.getEdgePoints());
    assertThrows(IllegalStateException.class, c::regainEdge); // nie mehr als das Attribut
    assertEquals(3, c.getEdgePoints());
  }

  @Test
  void burningGoesBelowTheMetatypeMinimumDownToZero() {
    // Mensch: Edge-Minimum 2; Verheizen senkt das Attribut trotzdem bis 0
    Character c = career();
    c.spendEdge();
    c.burnEdge();
    assertEquals(2, c.getEdge().getValue());
    assertEquals(1, c.getEdgePoints());
    c.burnEdge();
    c.burnEdge();
    assertEquals(0, c.getEdge().getValue());
    assertEquals(0, c.getEdgePoints());
    assertEquals(3, c.getEdge().getBurned());
    assertThrows(IllegalStateException.class, c::burnEdge);
  }

  @Test
  void burnedEdgeCanBeRaisedAgain() {
    Character c = career();
    c.burnEdge();
    c.burnEdge();
    assertEquals(0, c.getEdge().getLowerBound());
    assertEquals(10, c.getEdge().getKarmaCost()); // Wert 1 → 2 kostet neue Stufe × 5 Karma
    c.getEdge().setValue(2); // normale Steigerung mit Karma
    assertEquals(2, c.getEdge().getValue());
  }

  @Test
  void nothingDuringCreation() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    assertThrows(IllegalStateException.class, c::spendEdge);
    assertThrows(IllegalStateException.class, c::burnEdge);
  }
}
