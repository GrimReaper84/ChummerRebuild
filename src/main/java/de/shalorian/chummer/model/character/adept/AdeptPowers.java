package de.shalorian.chummer.model.character.adept;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Adeptenkräfte eines Charakters (GRW S. 115/116, 229–232): gekaufte Kräfte, beim Magieradepten der Kräfte-Anteil der
 * Magie, Kraftpunkte, Kosten, Höchststufen und Kaufprüfung. Komponente des Aggregats {@link Character}, erreichbar über
 * {@link Character#getAdeptPowers()}, ausgelagert wie {@code Equipment}, damit {@code Character} nicht mit jedem
 * Fachbereich wächst.
 *
 * <p>Die Rückreferenz {@code owner} liefert Gaben, Modus, wirksame Magie, Attribute und Fertigkeiten. Was mehrere
 * Bereiche verbindet, bleibt in {@code Character}: die Wirkungen der Kräfte auf Attribute, Pools, Initiative,
 * Zustandsmonitor und Schutz, die Zauberei-Magie des Magieradepten sowie das Entfernen einer Kraft samt gesperrter
 * Fertigkeiten ({@link Character#removeAdeptPower}).
 */
public class AdeptPowers {

  /** Verbesserte Fertigkeit kostet bei Kampffertigkeiten 0,5 statt 0,25 Kraftpunkte je Stufe (GRW S. 232). */
  private static final BigDecimal IMPROVED_COMBAT_SKILL_COST = new BigDecimal("0.5");
  /** Fertigkeitskategorien, für die Verbesserte Fertigkeit Kosten nennt (GRW S. 232). */
  private static final Set<SkillCategory> IMPROVABLE_SKILL_CATEGORIES = EnumSet.of(SkillCategory.COMBAT,
      SkillCategory.PHYSICAL, SkillCategory.SOCIAL, SkillCategory.TECHNICAL, SkillCategory.VEHICLE);

  private final @NonNull Character owner;
  private final List<AdeptPower> powers = new ArrayList<>();
  /** Magieradept: Magiepunkte, die in Kräfte gehen, siehe {@link #getAdeptMagic()}. */
  private int adeptMagic = 0;

  /** Keine Kräfte für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public AdeptPowers(@NonNull Character owner) {
    this.owner = owner;
  }

  public @NonNull List<AdeptPower> getPowers() {
    return Collections.unmodifiableList(powers);
  }

  /**
   * Gleicht die Kräfte mit den Gaben ab: ohne Adept bzw. Magieradept fallen alle Kräfte weg, ohne Magieradept die
   * Aufteilung. Ruft {@code Character} nach jeder Änderung der Qualities.
   */
  public void syncWithGifts() {
    if (!owner.hasAdeptPowers()) {
      powers.clear();
    }
    if (!owner.isMysticAdept()) {
      adeptMagic = 0;
    }
  }

  // --- Magieradept-Aufteilung ---

  /**
   * Magiepunkte, die ein Magieradept in Kräfte steckt (GRW S. 229), höchstens die wirksame Magie. Sinkt die Magie durch
   * Essenzverlust, verliert zuerst die Zauberei ihre Punkte; der Kräfte-Anteil schrumpft erst, wenn er die wirksame
   * Magie übersteigt (Annahme, das GRW regelt den Fall nicht). Für alle anderen Charaktere 0.
   */
  public int getAdeptMagic() {
    return owner.isMysticAdept() ? Math.min(adeptMagic, owner.getEffectiveMagicOrResonanceValue()) : 0;
  }

  /**
   * Kleinster erlaubter Kräfte-Anteil: bei der Erschaffung die schon ausgegebenen Kraftpunkte (aufgerundet), im
   * Karrieremodus der aktuelle Anteil. Das GRW (S. 116, 229) beschreibt die Aufteilung als Entscheidung je Magiepunkt
   * und kennt kein Umverteilen; im Spiel kommen nur neue Magiepunkte hinzu, die wahlweise in Kräfte gehen.
   */
  public int getMinAdeptMagic() {
    // Im Spielleiter-Modus darf der GM auch im Karrieremodus senken, bis zu den schon ausgegebenen Kraftpunkten.
    return owner.isCreated() && !owner.isGmMode()
        ? getAdeptMagic() : getSpentPowerPoints().setScale(0, RoundingMode.CEILING).intValue();
  }

  /**
   * Legt fest, wie viele Magiepunkte ein Magieradept in Kräfte steckt; kostet weder BP noch Karma.
   *
   * @throws IllegalArgumentException wenn der Charakter kein Magieradept ist oder der Wert außerhalb {@link
   *                                  #getMinAdeptMagic()} bis wirksame Magie liegt
   */
  public void setAdeptMagic(int value) {
    if (!owner.isMysticAdept() || value < getMinAdeptMagic() || value > owner.getEffectiveMagicOrResonanceValue()) {
      throw new IllegalArgumentException("Magie für Kräfte %d nicht möglich".formatted(value));
    }
    adeptMagic = value;
  }

  /** Setzt den Kräfte-Anteil ohne Prüfung — nur für den Persistenz-Pfad. */
  public void loadAdeptMagic(int value) {
    adeptMagic = Math.max(0, value);
  }

  // --- Kraftpunkte und Kosten ---

  /**
   * Kraftpunkte (GRW S. 115, 229): beim Adepten so viele wie die wirksame Magie, beim Magieradepten so viele wie sein
   * Kräfte-Anteil ({@link #getAdeptMagic()}). Wirksame Magie heißt nach Essenzverlust, wie beim Magie-Pool.
   */
  public @NonNull BigDecimal getPowerPoints() {
    if (!owner.hasAdeptPowers()) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(owner.isMysticAdept() ? getAdeptMagic() : owner.getEffectiveMagicOrResonanceValue());
  }

  /** Summe der Kosten aller gekauften Kräfte; Bruchteile bleiben erhalten (GRW S. 229). */
  public @NonNull BigDecimal getSpentPowerPoints() {
    return powers.stream().map(this::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Nicht ausgegebene Kraftpunkte; negativ, wenn die Magie nach dem Kauf gesunken ist. */
  public @NonNull BigDecimal getRemainingPowerPoints() {
    return getPowerPoints().subtract(getSpentPowerPoints());
  }

  /** Kosten einer gekauften Kraft in Kraftpunkten, siehe {@link #getCost(AdeptPowerData, String, int)}. */
  public @NonNull BigDecimal getCost(@NonNull AdeptPower power) {
    return getCost(power.getData(), power.getChoice(), power.getLevel());
  }

  /**
   * Kosten einer Kraft auf {@code level} (GRW S. 229–232): Tabellenwert (Gesteigerte Reflexe), Preis je Stufe oder fester
   * Preis. Verbesserte Fertigkeit kostet bei Kampffertigkeiten 0,5 je Stufe. Bei Gesteigertes körperliches Attribut
   * kostet jede Stufe, die das Attribut über sein natürliches Maximum hebt, das Doppelte; die Kosten folgen damit dem
   * aktuellen Grundwert des Attributs.
   */
  public @NonNull BigDecimal getCost(@NonNull AdeptPowerData data, @NonNull String choice, int level) {
    if (!data.costTable().isEmpty()) {
      return data.costTable().get(Math.clamp(level, 1, data.costTable().size()) - 1);
    }
    if (!data.leveled()) {
      return data.cost();
    }
    BigDecimal levels = BigDecimal.valueOf(level);
    return switch (data.effect()) {
      case IMPROVED_SKILL -> {
        Skill skill = owner.getSkill(choice);
        boolean combat = skill != null && skill.getCategory() == SkillCategory.COMBAT;
        yield (combat ? IMPROVED_COMBAT_SKILL_COST : data.cost()).multiply(levels);
      }
      case IMPROVED_ATTRIBUTE -> {
        AttributeType type = physicalAttribute(choice);
        if (type == null) {
          yield data.cost().multiply(levels);
        }
        CharacterAttribute attribute = owner.getAttribute(type);
        int overMax = Math.clamp(attribute.getValue() + level - attribute.getMax(), 0, level);
        yield data.cost().multiply(BigDecimal.valueOf(level + overMax));
      }
      default -> data.cost().multiply(levels);
    };
  }

  private static @Nullable AttributeType physicalAttribute(@NonNull String choice) {
    return Arrays.stream(AttributeType.values())
        .filter(t -> t.isPhysical() && t.name().equals(choice))
        .findFirst()
        .orElse(null);
  }

  /** Kraftpunkte in deutscher Schreibweise ohne überflüssige Nullen, z.B. "1,25" oder "4". */
  public static @NonNull String formatPoints(@NonNull BigDecimal points) {
    BigDecimal stripped = points.stripTrailingZeros();
    return (stripped.scale() < 0 ? stripped.setScale(0) : stripped).toPlainString().replace('.', ',');
  }

  // --- Stufen und Prüfungen ---

  /**
   * Höchste erlaubte Stufe einer Kraft (GRW S. 229–232): höchstens die wirksame Magie — beim Magieradepten die volle,
   * nicht nur der Kräfte-Anteil (S. 229) —, höchstens eine feste Grenze aus dem Katalog, und je Kraft: Verbesserte
   * Fertigkeit bis zum Anderthalbfachen des Grundwerts (also halber Wert, abgerundet), Verbesserte Wahrnehmung bis
   * Intuition, Gesteigertes körperliches Attribut und Gesteigerte Reflexe bis zum augmentierten Maximum (bei der
   * Reaktion beide zusammen, damit keine Stufe ohne Wirkung gekauft wird). Kräfte ohne Stufen: 1.
   */
  public int getMaxLevel(@NonNull AdeptPowerData data, @NonNull String choice) {
    if (!data.leveled()) {
      return 1;
    }
    int max = owner.getEffectiveMagicOrResonanceValue();
    if (data.maxLevel() > 0) {
      max = Math.min(max, data.maxLevel());
    }
    return switch (data.effect()) {
      case IMPROVED_SKILL -> {
        Skill skill = owner.getSkill(choice);
        yield skill == null ? 0 : Math.min(max, skill.getRating() / 2);
      }
      case IMPROVED_PERCEPTION -> Math.min(max, owner.getAugmentedAttributeValue(AttributeType.INT));
      case IMPROVED_ATTRIBUTE -> {
        AttributeType type = physicalAttribute(choice);
        if (type == null) {
          yield 0;
        }
        int reflexes = type == AttributeType.REA ? level(AdeptPowerEffect.IMPROVED_REFLEXES) : 0;
        yield Math.min(max, owner.getAugmentedMaximum(type) - owner.getAttribute(type).getValue() - reflexes);
      }
      // Reflexe und Gesteigerte Reaktion teilen sich den Raum bis zum augmentierten Maximum der Reaktion.
      case IMPROVED_REFLEXES -> Math.min(max, owner.getAugmentedMaximum(AttributeType.REA)
          - owner.getAttribute(AttributeType.REA).getValue()
          - level(AdeptPowerEffect.IMPROVED_ATTRIBUTE, AttributeType.REA.name()));
      default -> max;
    };
  }

  /**
   * Warum {@code data} gerade nicht gekauft werden kann (unabhängig von der Auswahl), oder {@code null}: keine
   * Adeptengabe, schon gekauft (außer mehrfach kaufbare), Schmerzresistenz neben Hoher Schmerztoleranz (S. 116) oder zu
   * wenige Kraftpunkte für die erste Stufe zum Grundpreis.
   */
  public @Nullable String getBlockReason(@NonNull AdeptPowerData data) {
    if (!owner.hasAdeptPowers()) {
      return "Nur für Adepten und Magieradepten";
    }
    if (!data.multiple() && powers.stream().anyMatch(p -> p.getData().name().equals(data.name()))) {
      return "Bereits gekauft";
    }
    if (data.effect() == AdeptPowerEffect.PAIN_RESISTANCE
        && owner.hasQualityBonus(QualityBonusType.HIGH_PAIN_TOLERANCE)) {
      return "Nicht zusammen mit Hohe Schmerztoleranz";
    }
    if (data.effect() == AdeptPowerEffect.IMPROVED_REFLEXES && owner.getImplants().hasInitiativeBoost()) {
      return "Nicht zusammen mit technischer Initiativesteigerung";
    }
    BigDecimal firstLevel = data.costTable().isEmpty() ? data.cost() : data.costTable().getFirst();
    return missingPowerPoints(firstLevel);
  }

  /**
   * Warum {@code data} mit dieser Auswahl nicht gekauft werden kann, oder {@code null}: erst {@link #getBlockReason},
   * dann Auswahl (Text, körperliches Attribut, Fertigkeit mit Kostenangabe im GRW), Höchststufe unter 1 und Kosten der
   * ersten Stufe. Einzige Prüfung für Modell, Dialog und Tab.
   */
  public @Nullable String getAddProblem(@NonNull AdeptPowerData data, @NonNull String choice) {
    String reason = getBlockReason(data);
    if (reason != null) {
      return reason;
    }
    if (powers.stream().anyMatch(p -> p.getData().name().equals(data.name()) && p.getChoice().equals(choice))) {
      return "Mit dieser Auswahl bereits gekauft";
    }
    String choiceProblem = switch (data.selection()) {
      case NONE, MENTOR -> null; // MENTOR lehnt schon der Loader ab
      case TEXT -> choice.isBlank() ? data.selectLabel() + " angeben" : null;
      case ATTRIBUTE -> physicalAttribute(choice) == null ? "Körperliches Attribut wählen" : null;
      case SKILL -> {
        Skill skill = owner.getSkill(choice);
        if (skill == null) {
          yield "Fertigkeit wählen";
        }
        yield IMPROVABLE_SKILL_CATEGORIES.contains(skill.getCategory())
            ? null : "Nur Kampf-, körperliche, soziale, technische und Fahrzeugfertigkeiten";
      }
    };
    if (choiceProblem != null) {
      return choiceProblem;
    }
    if (getMaxLevel(data, choice) < 1) {
      return data.effect() == AdeptPowerEffect.IMPROVED_SKILL
          ? "Fertigkeitswert mindestens 2 nötig" : "Höchststufe erreicht";
    }
    return missingPowerPoints(getCost(data, choice, 1));
  }

  private @Nullable String missingPowerPoints(@NonNull BigDecimal cost) {
    BigDecimal remaining = getRemainingPowerPoints();
    return cost.compareTo(remaining) > 0
        ? "Nicht genug Kraftpunkte (%s nötig, %s übrig)".formatted(formatPoints(cost), formatPoints(remaining))
        : null;
  }

  /**
   * Hinweis, wenn mehr Kraftpunkte verbraucht sind als vorhanden oder Kräfte über ihrer Höchststufe stehen (z.B. nach
   * Essenzverlust, gesenkter Magie oder gesenktem Fertigkeitswert), sonst {@code null}. Die Kräfte bleiben bestehen;
   * weiche Warnung wie beim Restgeld.
   */
  public @Nullable String getProblem() {
    List<String> problems = new ArrayList<>();
    BigDecimal remaining = getRemainingPowerPoints();
    if (remaining.signum() < 0) {
      problems.add("Kraftpunkte um %s überzogen".formatted(formatPoints(remaining.negate())));
    }
    for (AdeptPower power : powers) {
      int max = getMaxLevel(power.getData(), power.getChoice());
      if (power.getLevel() > max) {
        problems.add("%s über der Höchststufe %d".formatted(power.displayName(), max));
      }
    }
    return problems.isEmpty() ? null : String.join("; ", problems);
  }

  // --- Kaufen, Stufen, Entfernen ---

  /**
   * Kauft eine Kraft auf Stufe 1. Kräfte kosten nur Kraftpunkte, keine BP und kein Karma (GRW S. 115, 229).
   *
   * @throws IllegalArgumentException mit dem Grund aus {@link #getAddProblem}
   */
  public @NonNull AdeptPower add(@NonNull AdeptPowerData data, @NonNull String choice) {
    String reason = getAddProblem(data, choice);
    if (reason != null) {
      throw new IllegalArgumentException(reason);
    }
    AdeptPower power = new AdeptPower(data, choice);
    powers.add(power);
    return power;
  }

  /** Lädt eine Kraft ohne Regelprüfung — nur für den Persistenz-Pfad. */
  public void load(@NonNull AdeptPowerData data, @NonNull String choice, int level) {
    AdeptPower power = new AdeptPower(data, choice);
    power.setLevel(data.leveled() ? Math.max(1, level) : 1);
    powers.add(power);
  }

  /**
   * Nimmt die Kraft aus der Liste. Nur für {@link Character#removeAdeptPower}, das vorher die dadurch gesperrten
   * Fertigkeiten ermittelt.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau diese Kraft
  public void remove(@NonNull AdeptPower power) {
    powers.removeIf(p -> p == power);
  }

  /** Ob die Kraft eine Stufe höher darf: Stufenkraft, unter der Höchststufe und genug Kraftpunkte für den Aufpreis. */
  public boolean canRaise(@NonNull AdeptPower power) {
    AdeptPowerData data = power.getData();
    if (!data.leveled() || power.getLevel() >= getMaxLevel(data, power.getChoice())) {
      return false;
    }
    BigDecimal extra = getCost(data, power.getChoice(), power.getLevel() + 1).subtract(getCost(power));
    return extra.compareTo(getRemainingPowerPoints()) <= 0;
  }

  public void raise(@NonNull AdeptPower power) {
    if (!canRaise(power)) {
      throw new IllegalArgumentException("%s kann nicht gesteigert werden".formatted(power.displayName()));
    }
    power.setLevel(power.getLevel() + 1);
  }

  public boolean canLower(@NonNull AdeptPower power) {
    return power.getLevel() > 1;
  }

  public void lower(@NonNull AdeptPower power) {
    if (!canLower(power)) {
      throw new IllegalArgumentException("%s kann nicht gesenkt werden".formatted(power.displayName()));
    }
    power.setLevel(power.getLevel() - 1);
  }

  // --- Wirkungen (ausgewertet in Character) ---

  /** Summe der Stufen aller Kräfte mit dieser Wirkung, 0 ohne. */
  public int level(@NonNull AdeptPowerEffect effect) {
    return powers.stream().filter(p -> p.getData().effect() == effect).mapToInt(AdeptPower::getLevel).sum();
  }

  /** Wie {@link #level(AdeptPowerEffect)}, nur für Kräfte mit genau dieser Auswahl. */
  public int level(@NonNull AdeptPowerEffect effect, @NonNull String choice) {
    return powers.stream()
        .filter(p -> p.getData().effect() == effect && p.getChoice().equals(choice))
        .mapToInt(AdeptPower::getLevel)
        .sum();
  }
}
