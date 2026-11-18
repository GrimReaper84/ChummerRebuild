package de.shalorian.chummer.model.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Schergengruppe mit Kampfzustand (GRW S. 324/325). */
class NpcGroupTest {

  private static NpcGroup group(int rating, int members) {
    return new NpcGroup("Testgruppe", rating, members, new NpcStatblock(NpcRulesTest.human(4, 3), ""));
  }

  @Test
  void edgePoolIsRatingPlusLeadership() {
    NpcGroup g = group(2, 4);
    assertEquals(2, g.getEdgePool());
    g.setLeadershipBonus(true);
    assertEquals(2, g.getEdgePool(), "ohne Anführer keine Führungsprobe");
    g.setLeader(new NpcStatblock(NpcRulesTest.human(5, 4), ""));
    g.setLeadershipBonus(true);
    assertEquals(3, g.getEdgePool());
    g.setEdgeSpent(5);
    assertEquals(3, g.getEdgeSpent());
    g.setLeadershipBonus(false);
    assertEquals(2, g.getEdgeSpent());
  }

  @Test
  void memberCountGrowsAndShrinks() {
    NpcGroup g = group(1, 4);
    g.getMembers().get(3).setDamage(5, 10);
    g.setMemberCount(6);
    assertEquals(6, g.getMemberCount());
    assertEquals(5, g.getMembers().get(3).getDamage());
    g.setMemberCount(3);
    assertEquals(3, g.getMemberCount());
    assertThrows(IllegalArgumentException.class, () -> g.setMemberCount(0));
    assertThrows(IllegalArgumentException.class, () -> g.setMemberCount(NpcGroup.MAX_MEMBERS + 1));
  }

  @Test
  void fullMonitorKnocksOutAndTriggersFlight() {
    NpcGroup g = group(1, 4);
    int boxes = g.getGrunt().getMonitorBoxes();
    g.getMembers().get(0).setDamage(boxes + 3, boxes);
    assertEquals(boxes, g.getMembers().get(0).getDamage());
    assertEquals(1, g.getKnockedOut());
    g.getMembers().get(1).setDamage(boxes, boxes);
    assertFalse(g.isFleeing(), "2 von 4 ist nicht mehr als die Hälfte");
    g.getMembers().get(2).setDamage(boxes, boxes);
    assertTrue(g.isFleeing());
    g.resetCombat();
    assertEquals(0, g.getKnockedOut());
  }

  @Test
  void leadershipRaisesRatingClassForFlight() {
    NpcGroup g = group(4, 4);
    g.setLeader(new NpcStatblock(NpcRulesTest.human(5, 4), ""));
    g.setLeadershipBonus(true);
    assertEquals(ProfessionalRating.ELITE, g.getRatingClass());
  }
}
