package de.shalorian.chummer.model.character.situation;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Rechnet aus situativen Boni fertige Würfelpools je Anlass (Plan Magie abrunden, R5–R8).
 *
 * <ul>
 *   <li>Jeder Bonus nennt einen Anlass; daraus werden konkrete Pools, z.B. Zauberkategorie Kampf → „Spruchzauberei ·
 *       Kampfzauber“ und „Ritualzauberei · Kampfzauber“, Geisterart → Herbeirufen, Binden und Verbannen.</li>
 *   <li>Boni für eine Grundlage allgemein (z.B. Kraftfokus auf Spruchzauberei) gelten auch in jedem eingeschränkten
 *       Pool derselben Grundlage.</li>
 *   <li>Je Probe zählt nur der stärkste Fokus (GRW S. 233); Patron- und Adeptenboni zählen alle.</li>
 *   <li>Grundpools: Fertigkeiten wie im Bogen, Verteidigung Reaktion und Schadenswiderstand Konstitution + Panzerung
 *       (S. 181, ohne Verletzungsmodifikator, S. 192), Toxine Konstitution (S. 297), natürliche Heilung Konstitution × 2
 *       bzw. Konstitution + Willenskraft (S. 294), Springen Akrobatik + Geschicklichkeit (S. 163), Zauberwiderstand
 *       Konstitution bzw. Willenskraft; soziale Proben nur als Bonus.</li>
 *   <li>Ohne Grundpool (z.B. Magiefertigkeit ohne Wert) entfällt der Anlass.</li>
 * </ul>
 */
public final class SituationalPools {

  private static final String SPELLCASTING = "Spruchzauberei";
  private static final String RITUAL = "Ritualzauberei";
  private static final String COUNTERSPELLING = "Antimagie";
  private static final String SUMMONING = "Herbeirufen";
  private static final String BINDING = "Binden";
  private static final String BANISHING = "Verbannen";
  private static final List<String> SPIRIT_SKILLS = List.of(SUMMONING, BINDING, BANISHING);
  private static final String DAMAGE = "Schadenswiderstand";
  private static final String SPELL_RESISTANCE = "Zauberwiderstand";
  private static final String TOXINS = "Widerstand gegen Toxine und Krankheiten";
  private static final String DEFENSE = "Verteidigung";
  private static final String HEALING = "Natürliche Heilung";
  private static final String SOCIAL = "Soziale Proben";
  private static final String MAGIC_ATTRIBUTE = "MAG";

  private SituationalPools() {
  }

  /** Pools für alle Anlässe, für die {@code bonuses} mindestens einen Bonus enthalten, in Reihenfolge der Boni. */
  public static @NonNull List<SituationalPool> of(@NonNull Character character,
      @NonNull List<SituationalBonus> bonuses) {
    Map<SituationalBonus, List<SituationKey>> keysByBonus = new LinkedHashMap<>();
    Set<SituationKey> keys = new LinkedHashSet<>();
    for (SituationalBonus bonus : bonuses) {
      List<SituationKey> bonusKeys = keys(character, bonus);
      keysByBonus.put(bonus, bonusKeys);
      keys.addAll(bonusKeys);
    }
    List<SituationalPool> pools = new ArrayList<>();
    for (SituationKey key : keys) {
      List<SituationalBonus> applying = keysByBonus.entrySet().stream()
          .filter(e -> e.getValue().contains(key) || e.getValue().contains(key.general()))
          .map(Map.Entry::getKey)
          .toList();
      SituationalPool pool = pool(character, key, applying);
      if (pool != null) {
        pools.add(pool);
      }
    }
    return pools;
  }

  /** Konkrete Anlässe eines Bonus. */
  private static @NonNull List<SituationKey> keys(@NonNull Character character, @NonNull SituationalBonus bonus) {
    String detail = bonus.detail();
    return switch (bonus.kind()) {
      case SPELL_CATEGORY -> List.of(new SituationKey(SPELLCASTING, spellLabel(detail)),
          new SituationKey(RITUAL, spellLabel(detail)));
      case COUNTERSPELL_CATEGORY -> List.of(new SituationKey(COUNTERSPELLING, spellLabel(detail)));
      case SPIRIT_TYPE -> SPIRIT_SKILLS.stream().map(s -> new SituationKey(s, detail)).toList();
      case SPIRIT_SUMMON -> List.of(new SituationKey(SUMMONING, detail));
      case SPIRIT_BIND -> List.of(new SituationKey(BINDING, detail));
      case SPIRIT_BANISH -> List.of(new SituationKey(BANISHING, detail));
      case SKILL -> List.of(new SituationKey(detail, ""));
      case MAGIC -> character.getSkills().stream()
          .filter(s -> MAGIC_ATTRIBUTE.equals(s.getLinkedAttribute()) && !COUNTERSPELLING.equals(s.getName()))
          .map(s -> new SituationKey(s.getName(), ""))
          .toList();
      case DAMAGE_RESISTANCE -> List.of(new SituationKey(DAMAGE, "ballistisch"), new SituationKey(DAMAGE, "Stoß"));
      case SPELL_RESISTANCE -> List.of(new SituationKey(SPELL_RESISTANCE, "Konstitution"),
          new SituationKey(SPELL_RESISTANCE, "Willenskraft"));
      case TOXIN_RESISTANCE -> List.of(new SituationKey(TOXINS, ""));
      case DEFENSE -> List.of(new SituationKey(DEFENSE, ""));
      case HEALING -> List.of(new SituationKey(HEALING, "körperlich"), new SituationKey(HEALING, "geistig"));
      case JUMP -> List.of(new SituationKey("Akrobatik", "Springen"));
      case SOCIAL -> List.of(new SituationKey(SOCIAL, ""));
    };
  }

  /** „Kampf“ → „Kampfzauber“, „Heilung“ → „Heilzauber“. */
  private static @NonNull String spellLabel(@NonNull String category) {
    return (category.equals("Heilung") ? "Heil" : category) + "zauber";
  }

  /** Pool für {@code key} mit den geltenden Boni, oder {@code null} ohne Grundpool. */
  private static @Nullable SituationalPool pool(@NonNull Character character, @NonNull SituationKey key,
      @NonNull List<SituationalBonus> applying) {
    List<SituationalBonus> counted = new ArrayList<>(applying.stream().filter(b -> !b.focus()).toList());
    // nur der stärkste Fokus zählt je Probe (S. 233)
    applying.stream().filter(SituationalBonus::focus).max(Comparator.comparingInt(SituationalBonus::dice))
        .ifPresent(counted::add);
    int dice = 0;
    StringBuilder text = new StringBuilder();
    for (SituationalBonus bonus : counted) {
      dice += bonus.dice();
      text.append(bonus.dice() < 0 ? " − " : " + ").append(bonus.source()).append(' ').append(Math.abs(bonus.dice()));
    }
    if (key.base().equals(SOCIAL)) {
      return new SituationalPool(key.label(), dice, "Bonus" + text, true);
    }
    Optional<DicePool> base = basePool(character, key);
    if (base.isEmpty()) {
      return null;
    }
    DicePool pool = base.get();
    return new SituationalPool(key.label(), Math.max(0, pool.dice() + dice), pool.breakdown() + text, false);
  }

  /** Grundpool ohne situative Boni; Verletzungsmodifikator wie bei den übrigen Pools (außer Schadenswiderstand). */
  private static @NonNull Optional<DicePool> basePool(@NonNull Character c, @NonNull SituationKey key) {
    int con = c.getAugmentedAttributeValue(AttributeType.BOD);
    int wil = c.getAugmentedAttributeValue(AttributeType.WIL);
    return switch (key.base()) {
      case DAMAGE -> {
        boolean ballistic = key.detail().equals("ballistisch");
        int armor = ballistic ? c.getArmorBallistic() : c.getArmorImpact();
        yield Optional.of(new DicePool(con + armor, "Konstitution %d + %s %d".formatted(con,
            ballistic ? "Ballistik" : "Stoß", armor)));
      }
      case SPELL_RESISTANCE -> key.detail().equals("Konstitution")
          ? Optional.of(wounded(c, con, "Konstitution " + con))
          : Optional.of(wounded(c, wil, "Willenskraft " + wil));
      case TOXINS -> Optional.of(wounded(c, con, "Konstitution " + con));
      case DEFENSE -> {
        int reaction = c.getAugmentedAttributeValue(AttributeType.REA);
        yield Optional.of(wounded(c, reaction, "Reaktion " + reaction));
      }
      case HEALING -> key.detail().equals("körperlich")
          ? Optional.of(wounded(c, con * 2, "Konstitution %d × 2".formatted(con)))
          : Optional.of(wounded(c, con + wil, "Konstitution %d + Willenskraft %d".formatted(con, wil)));
      default -> {
        Skill skill = c.getSkill(key.base());
        yield skill == null ? Optional.empty() : c.getSkillDicePool(skill);
      }
    };
  }

  private static @NonNull DicePool wounded(@NonNull Character c, int dice, @NonNull String breakdown) {
    int wound = c.getWoundModifier();
    return new DicePool(Math.max(0, dice + wound), breakdown + (wound == 0 ? "" : " − " + -wound + " Verletzung"));
  }
}
