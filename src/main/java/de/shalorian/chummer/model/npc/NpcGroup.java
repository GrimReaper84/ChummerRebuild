package de.shalorian.chummer.model.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Eine Schergengruppe (SR4-Grundregelwerk S. 324/325): gleiche Werte für alle Schergen, optional ein Anführer mit
 * eigenen Werten, eine Professionalitätsstufe 0–6 und ein gemeinsamer Edge-Pool. Dazu der Kampfzustand: Schaden je
 * Mitglied und Anführer, verbrauchtes Gruppen-Edge und eine gelungene Führungsprobe.
 */
public class NpcGroup {

  /** Höchstzahl der Mitglieder, damit die Kampfkarte überschaubar bleibt. */
  public static final int MAX_MEMBERS = 20;

  private @NonNull String name;
  private int professionalRating;
  private final @NonNull NpcStatblock grunt;
  private @Nullable NpcStatblock leader;
  private final List<NpcCombatant> members = new ArrayList<>();
  private final @NonNull NpcCombatant leaderState = new NpcCombatant();
  private int edgeSpent;
  /** Gelungene Führungsprobe des Anführers: +1 Stufe für die Dauer des Kampfes (S. 325); nicht gespeichert. */
  private boolean leadershipBonus;

  /**
   * @throws IllegalArgumentException bei einer Stufe außerhalb 0–6 oder einer Mitgliederzahl außerhalb 1–{@value
   *                                  #MAX_MEMBERS}
   */
  public NpcGroup(@NonNull String name, int professionalRating, int memberCount, @NonNull NpcStatblock grunt) {
    this.name = name;
    this.grunt = grunt;
    setProfessionalRating(professionalRating);
    setMemberCount(memberCount);
  }

  public @NonNull String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  public int getProfessionalRating() {
    return professionalRating;
  }

  /**
   * Setzt die Professionalitätsstufe; verbrauchtes Edge über dem neuen Pool wird gekappt.
   *
   * @throws IllegalArgumentException außerhalb 0–6
   */
  public void setProfessionalRating(int rating) {
    ProfessionalRating.of(rating);
    this.professionalRating = rating;
    edgeSpent = Math.min(edgeSpent, getEdgePool());
  }

  /** Wirksame Stufe im Kampf: mit gelungener Führungsprobe eine höher (S. 325). */
  public int getEffectiveRating() {
    return professionalRating + (leadershipBonus && leader != null ? 1 : 0);
  }

  public @NonNull ProfessionalRating getRatingClass() {
    return ProfessionalRating.of(Math.min(getEffectiveRating(), ProfessionalRating.MAX_RATING));
  }

  public @NonNull NpcStatblock getGrunt() {
    return grunt;
  }

  public @Nullable NpcStatblock getLeader() {
    return leader;
  }

  /** Setzt oder entfernt den Anführer; ohne Anführer entfällt auch der Führungsbonus. */
  public void setLeader(@Nullable NpcStatblock leader) {
    this.leader = leader;
    leaderState.setDamage(0, 0);
    if (leader == null) {
      leadershipBonus = false;
    }
  }

  public @NonNull NpcCombatant getLeaderState() {
    return leaderState;
  }

  public int getMemberCount() {
    return members.size();
  }

  /**
   * Ändert die Zahl der Schergen; neue Mitglieder kommen unverletzt dazu, überzählige fallen am Ende weg.
   *
   * @throws IllegalArgumentException außerhalb 1–{@value #MAX_MEMBERS}
   */
  public void setMemberCount(int count) {
    if (count < 1 || count > MAX_MEMBERS) {
      throw new IllegalArgumentException("Mitglieder müssen zwischen 1 und %d liegen: %d".formatted(MAX_MEMBERS, count));
    }
    while (members.size() < count) {
      members.add(new NpcCombatant());
    }
    while (members.size() > count) {
      members.removeLast();
    }
  }

  public @NonNull List<NpcCombatant> getMembers() {
    return Collections.unmodifiableList(members);
  }

  /** Gruppen-Edge: so hoch wie die wirksame Professionalitätsstufe (S. 325). */
  public int getEdgePool() {
    return getEffectiveRating();
  }

  public int getEdgeSpent() {
    return edgeSpent;
  }

  /** Setzt das verbrauchte Gruppen-Edge, begrenzt auf 0 bis zum Pool. */
  public void setEdgeSpent(int spent) {
    edgeSpent = Math.clamp(spent, 0, getEdgePool());
  }

  public boolean isLeadershipBonus() {
    return leadershipBonus;
  }

  /** Gelungene Führungsprobe (nur mit Anführer); verfällt sie, wird überzähliges verbrauchtes Edge gekappt. */
  public void setLeadershipBonus(boolean bonus) {
    leadershipBonus = bonus && leader != null;
    edgeSpent = Math.min(edgeSpent, getEdgePool());
  }

  /** Zahl der ausgeschalteten Schergen (ohne Anführer). */
  public int getKnockedOut() {
    int boxes = grunt.getMonitorBoxes();
    return (int) members.stream().filter(m -> m.isKnockedOut(boxes)).count();
  }

  /** Ob die Gruppe nach ihrer Professionalität flieht bzw. sich zurückzieht (S. 325). */
  public boolean isFleeing() {
    return getRatingClass().isFleeing(getKnockedOut(), getMemberCount());
  }

  /** Setzt den Kampf zurück: kein Schaden, kein verbrauchtes Edge, keine Führungsprobe. */
  public void resetCombat() {
    members.forEach(m -> m.setDamage(0, 0));
    leaderState.setDamage(0, 0);
    edgeSpent = 0;
    leadershipBonus = false;
  }

  /** Hinweis zum Anführer ({@link NpcRules#leaderHint}), sonst {@code null}. */
  public @Nullable String getLeaderHint() {
    if (leader == null) {
      return null;
    }
    return NpcRules.leaderHint(grunt.getCharacter(), leader.getCharacter());
  }
}
