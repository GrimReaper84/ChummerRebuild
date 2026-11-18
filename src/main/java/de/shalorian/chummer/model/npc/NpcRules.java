package de.shalorian.chummer.model.npc;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.Arrays;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Regeln für Schergen und ihre Anführer (SR4-Grundregelwerk S. 324/325). */
public final class NpcRules {

  /** Mindestabstand der Attribut- und Fertigkeitssummen eines Anführers zu einem Schergen (Richtwert, S. 325). */
  public static final int LEADER_MARGIN = 4;
  private static final int BASE_MONITOR = 8;
  private static final int BOXES_PER_WOUND_LEVEL = 3;

  private NpcRules() {
  }

  /**
   * Kästchen des einen gemeinsamen Zustandsmonitors (S. 324): 8 plus die aufgerundete Hälfte des höheren Werts aus
   * Konstitution und Willenskraft; wie beim normalen Monitor zählen die wirksamen (verstärkten) Werte.
   */
  public static int monitorBoxes(@NonNull Character statblock) {
    int higher = Math.max(statblock.getAugmentedAttributeValue(AttributeType.BOD),
        statblock.getAugmentedAttributeValue(AttributeType.WIL));
    return BASE_MONITOR + (higher + 1) / 2;
  }

  /** Verletzungsmodifikator auf dem gemeinsamen Monitor: −1 je volle 3 Kästchen (S. 193), als negative Zahl. */
  public static int woundModifier(int damage) {
    return -(damage / BOXES_PER_WOUND_LEVEL);
  }

  /**
   * Hinweis, wenn ein Anführer nicht mindestens {@value #LEADER_MARGIN} Punkte mehr Attribute bzw. Fertigkeiten als ein
   * Scherge hat (S. 325, Richtwert); sonst {@code null}. Gezählt werden die natürlichen Grundattribute und die Stufen
   * aller aktiven Fertigkeiten.
   */
  public static @Nullable String leaderHint(@NonNull Character grunt, @NonNull Character leader) {
    boolean attributes = attributeSum(leader) < attributeSum(grunt) + LEADER_MARGIN;
    boolean skills = skillSum(leader) < skillSum(grunt) + LEADER_MARGIN;
    if (!attributes && !skills) {
      return null;
    }
    String what = attributes && skills ? "Attribute und Fertigkeiten" : attributes ? "Attribute" : "Fertigkeiten";
    return "Ein Anführer sollte in der Summe mindestens %d Punkte mehr %s haben als ein Scherge (S. 325)"
        .formatted(LEADER_MARGIN, what);
  }

  static int attributeSum(@NonNull Character c) {
    return Arrays.stream(AttributeType.values()).mapToInt(t -> c.getAttribute(t).getValue()).sum();
  }

  static int skillSum(@NonNull Character c) {
    return c.getSkills().stream().mapToInt(Skill::getRating).sum();
  }
}
