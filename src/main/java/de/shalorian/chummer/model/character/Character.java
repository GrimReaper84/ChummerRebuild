package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttributeType;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualityRules;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import de.shalorian.chummer.model.character.skill.SkillDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.model.character.technomancer.ComplexFormData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class Character {

  public static final int TOTAL_BP = 400;
  public static final int MAX_NUYEN_BP = 50;
  public static final int NUYEN_PER_BP = 5_000;
  public static final int MAX_POSITIVE_QUALITY_BP = 35;
  public static final int MAX_NEGATIVE_QUALITY_BP = 35;
  public static final int MAX_ATTRIBUTE_BP = TOTAL_BP / 2;
  public static final int MAX_GROUP_RATING_AT_CREATION = 4;
  private static final int KARMA_NEW_SKILL_GROUP = 10;
  private static final int KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT = 2;
  private static final int KNOWLEDGE_SKILL_KARMA_NEW = 2;
  private static final int KNOWLEDGE_SKILL_KARMA_PER_RATING = 1;
  private static final int ACTIVE_SKILL_SPECIALIZATION_BP = 2;
  private static final int SPECIALIZATION_KARMA_COST = 2;
  private static final int MIN_RATING_FOR_SPECIALIZATION = 1;
  private static final int CONTACT_BP_PER_POINT = 1;
  private static final int SPELL_BP_COST = 3;
  private static final int SPELL_KARMA_COST = 5;
  private static final int SPELL_CAP_MULTIPLIER = 2;
  private static final String SPELLCASTING_SKILL = "Spruchzauberei";
  private static final String RITUAL_SPELLCASTING_SKILL = "Ritualzauberei";
  private static final String ARCANA_SKILL = "Arcana";

  private static final BigDecimal BASE_ESSENCE = BigDecimal.valueOf(6);
  private static final int ESSENCE_DECIMALS = 2;
  private static final BigDecimal TWO = BigDecimal.valueOf(2);
  private static final int MIN_ENCUMBERED_ATTRIBUTE_VALUE = 1;
  private static final int COMPLEX_FORM_CAP_MULTIPLIER = 2;
  private static final int SPECIALIZATION_DICE = 2;
  private static final int CONDITION_MONITOR_BASE = 8;
  private static final int BOXES_PER_WOUND_MODIFIER = 3;
  private static final int BOXES_PER_WOUND_MODIFIER_LOW_PAIN_TOLERANCE = 2;
  /** Astrale Initiativedurchgänge (SR4-Grundregelwerk S. 227). */
  public static final int ASTRAL_INITIATIVE_PASSES = 3;

  private final Map<AttributeType, CharacterAttribute> attributes;
  private final Map<String, Skill> skills;
  private final Set<String> decoupledGroups;
  private final List<CharacterQuality> qualities = new ArrayList<>();
  private final List<KnowledgeSkill> knowledgeSkills = new ArrayList<>();
  private final List<Contact> contacts = new ArrayList<>();
  private final List<SpellData> spells = new ArrayList<>();
  private final List<ComplexForm> complexForms = new ArrayList<>();
  private final @NonNull Equipment equipment = new Equipment(this);
  private final List<LogEntry> karmaLog = new ArrayList<>();
  private final List<LogEntry> nuyenLog = new ArrayList<>();
  private final @NonNull CharacterBiography biography = new CharacterBiography();
  private @NonNull String name;
  private @NonNull MetatypeData metatype;
  private @NonNull SpecialAttribute edge;
  private @Nullable SpecialAttribute magicOrResonance;
  private @NonNull Tradition tradition = Tradition.HERMETIC;
  private boolean created = false;
  private int nuyenBP = 0;
  /** Eingetragener Schaden im Zustandsmonitor (Kästchen), siehe {@link #getPhysicalDamage()}. */
  private int physicalDamage = 0;
  private int stunDamage = 0;

  public Character(@NonNull String name, @NonNull MetatypeData metatype) {
    this.name = name;
    this.metatype = metatype;
    this.attributes = new EnumMap<>(AttributeType.class);
    this.decoupledGroups = new HashSet<>();
    initAttributes(metatype);
    this.edge = initEdge(metatype);
    initMetatypeQualities(metatype);
    this.skills = new LinkedHashMap<>();
    for (Skill skill : SkillDataLoader.createFreshSkills()) {
      this.skills.put(skill.getName(), skill);
    }
  }

  private void initAttributes(@NonNull MetatypeData metatype) {
    attributes.clear();
    for (AttributeType type : AttributeType.values()) {
      attributes.put(type, new CharacterAttribute(type, metatype.getRangeFor(type)));
    }
  }

  private static @NonNull SpecialAttribute initEdge(@NonNull MetatypeData metatype) {
    AttributeRange range = metatype.edgeRange();
    return new SpecialAttribute(SpecialAttributeType.EDGE, range.min(), range.max(), false);
  }

  private void initMetatypeQualities(@NonNull MetatypeData metatype) {
    qualities.removeIf(q -> q.source() == QualitySource.METATYPE);
    for (String qName : metatype.grantedPositiveQualityNames()) {
      QualityData data = QualityDataLoader.findByName(qName);
      if (data != null) {
        qualities.add(new CharacterQuality(data, QualitySource.METATYPE));
      }
    }
    for (String qName : metatype.grantedNegativeQualityNames()) {
      QualityData data = QualityDataLoader.findByName(qName);
      if (data != null) {
        qualities.add(new CharacterQuality(data, QualitySource.METATYPE));
      }
    }
    syncMagicOrResonance();
  }

  // --- Attribute ---

  public @NonNull String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  public @NonNull MetatypeData getMetatype() {
    return metatype;
  }

  /**
   * Liefert die Biografie-Metadaten dieses Charakters (Aussehen, Hintergrund, Spieler-Metadaten,
   * Spielnotizen) — immer initialisiert, nie {@code null}. Kein eigener Setter auf {@code
   * Character}-Ebene: Aufrufer mutieren das zurückgegebene Objekt direkt über dessen eigene Setter
   * (analog {@link #getEdge()}).
   */
  public @NonNull CharacterBiography getBiography() {
    return biography;
  }

  /**
   * Wechselt den Metatyp. Gewählte Qualities, die nur für bestimmte Metatypen gelten (z.B. Elfenposer), werden
   * entfernt, wenn der neue Metatyp nicht passt, samt ihren Folgen (siehe {@link #removeQuality}).
   *
   * @return die dabei entfernten Qualities, für den Hinweis in der UI
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau diese Einträge
  public @NonNull List<CharacterQuality> setMetatype(@NonNull MetatypeData metatype) {
    this.metatype = metatype;
    initAttributes(metatype);
    this.edge = initEdge(metatype);
    initMetatypeQualities(metatype);
    List<CharacterQuality> before = getSelectedQualities();
    before.stream().filter(q -> !fitsMetatype(q.data().rules())).forEach(this::removeQuality);
    applyQualityEffects();
    List<CharacterQuality> after = getSelectedQualities();
    return before.stream().filter(q -> after.stream().noneMatch(a -> a == q)).toList();
  }

  public @NonNull Map<AttributeType, CharacterAttribute> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public @NonNull CharacterAttribute getAttribute(@NonNull AttributeType type) {
    return Objects.requireNonNull(attributes.get(type));
  }

  /**
   * Summe der BP-Kosten aller Körperlichen und Geistigen Attribute.
   */
  public int getAttributeBP() {
    return attributes.values().stream().mapToInt(CharacterAttribute::getBpCost).sum();
  }

  /**
   * Anzahl der Attribute, die aktuell auf ihr natürliches (Rassen-)Maximum gesteigert sind.
   */
  public long getMaximizedAttributeCount() {
    return attributes.values().stream()
        .filter(a -> a.getMin() != a.getMax() && a.getValue() == a.getMax())
        .count();
  }

  /**
   * Prüft, ob eine Steigerung des angegebenen Attributs um 1 während der Charaktererschaffung
   * regelkonform wäre: max. {@link #MAX_ATTRIBUTE_BP} (die Hälfte des Gesamtbudgets) für
   * Körperliche/Geistige Attribute in Summe, und höchstens ein Attribut darf auf das natürliche
   * Maximum gesteigert werden. Im Karrieremodus gelten diese Erstellungs-Caps nicht.
   */
  public boolean canRaiseAttributeAtCreation(@NonNull AttributeType type) {
    if (created) {
      return true;
    }
    CharacterAttribute attr = getAttribute(type);
    int newValue = attr.getValue() + 1;
    if (newValue > attr.getMax()) {
      return false;
    }
    int projectedAttributeBP = getAttributeBP() - attr.getBpCost() + attr.getBpCostForValue(newValue);
    if (projectedAttributeBP > MAX_ATTRIBUTE_BP) {
      return false;
    }
    boolean wouldMaximize = attr.getMin() != attr.getMax() && newValue == attr.getMax();
    return !wouldMaximize || getMaximizedAttributeCount() == 0;
  }

  /**
   * Wirksamer Attributwert inkl. aller modellierten Modifikatoren — derzeit ausschließlich die
   * Panzerungsbehinderung auf Geschicklichkeit und Reaktion (siehe {@link
   * Equipment#getArmorEncumbrancePenalty()}). Der investierte Wert ({@link CharacterAttribute#getValue()})
   * bleibt unverändert, BP- und Karma-Kosten richten sich weiter nach ihm (analog {@link
   * #getEffectiveMagicOrResonanceValue()}).
   *
   * <p>Ein durch Behinderung reduzierter Wert fällt nicht unter {@value
   * #MIN_ENCUMBERED_ATTRIBUTE_VALUE}. Das GRW nennt dafür keine Untergrenze; die Grenze ist eine
   * vorläufige Nutzerentscheidung bis zu einem Beleg in weiteren Regelwerken (siehe {@code
   * docs/plaene/archiv/nuyen_ruestung_plan.md} Abschnitt 13).
   */
  public int getAugmentedAttributeValue(@NonNull AttributeType type) {
    int base = getAttribute(type).getValue();
    int penalty = equipment.getArmorEncumbrancePenalty();
    if (penalty == 0 || (type != AttributeType.AGI && type != AttributeType.REA)) {
      return base;
    }
    return Math.max(MIN_ENCUMBERED_ATTRIBUTE_VALUE, base + penalty);
  }

  // --- Sonderattribute (Edge/Magic/Resonance) & Essenz ---

  public @NonNull SpecialAttribute getEdge() {
    return edge;
  }

  /**
   * Liefert das freigeschaltete Magic- oder Resonance-Sonderattribut, oder {@code null}, wenn der
   * Charakter keine entsprechende Quality besitzt (siehe {@link #hasMagic()}/{@link
   * #hasResonance()}). Laut SR4-Grundregelwerk schließen Magie und Resonanz einander aus — ein
   * Charakter kann nie beide gleichzeitig besitzen.
   */
  public @Nullable SpecialAttribute getMagicOrResonance() {
    return magicOrResonance;
  }

  public boolean hasMagic() {
    return magicOrResonance != null && magicOrResonance.getType() == SpecialAttributeType.MAGIC;
  }

  public boolean hasResonance() {
    return magicOrResonance != null
        && magicOrResonance.getType() == SpecialAttributeType.RESONANCE;
  }

  /**
   * Prüft, ob der Charakter speziell die Gabe "Zauberer" besitzt — anders als {@link #hasMagic()}
   * (liefert auch für Adepten {@code true}, da diese ebenfalls ein Magie-Attribut besitzen), grenzt
   * dies auf Charaktere ein, die tatsächlich Zauber wirken dürfen. SR4-Grundregelwerk S. 148: Magische
   * Fertigkeiten grundsätzlich nur für Zauberer und Magieradepten mit Magie ab 1. Steuert die
   * Sichtbarkeit des Magie-Tabs sowie den Zugang zu den magischen Aktionsfertigkeiten (siehe
   * {@link #isSkillAccessible(Skill)}).
   */
  public boolean isMagician() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING));
  }

  /**
   * Die gewählte magische Tradition (GRW-Kern: Hermetisch/Schamanistisch), Default {@link
   * Tradition#HERMETIC}. Es gibt bewusst keinen "nicht gewählt"-Zustand — SR4-Grundregelwerk kennt
   * keine harte Voraussetzung, eine Tradition vor dem Zauber-Erlernen festzulegen.
   */
  public @NonNull Tradition getTradition() {
    return tradition;
  }

  public void setTradition(@NonNull Tradition tradition) {
    this.tradition = tradition;
  }

  /**
   * Essenz des Charakters, auf {@value #ESSENCE_DECIMALS} Nachkommastellen gerundet. Alle Charaktere
   * beginnen mit Essenz 6 (metatypunabhängig); davon wird der Essenzverlust durch Implantate nach der
   * GRW-Kombinationsregel abgezogen (S. 111): Cyberware- und Bioware-Verlust werden getrennt summiert,
   * der größere zählt voll, der kleinere halb — also {@code 6 − max(ΣCyber, ΣBio) − min(ΣCyber, ΣBio) / 2}.
   * GRW-Beispiel: 1,5
   * Cyberware + 2,5 Bioware ergibt 6 − 2,5 − 0,75 = 2,75.
   *
   * <p>Dies ist die <b>einzige</b> Stelle der Essenzberechnung — bewusste Abweichung vom C#-Original,
   * das dieselbe Summe in vier Gettern dupliziert. Die Cyberware-Summe ist bei "Immunabstoßung"
   * bereits verdoppelt (siehe {@link #getCyberwareEssenceLoss()}). Die Rundung erfolgt hier (nicht
   * erst in der Anzeige), damit Anzeige und {@link #getEssencePenalty()} dieselbe Grundlage haben.
   */
  public double getEssence() {
    return getExactEssence().doubleValue();
  }

  private @NonNull BigDecimal getExactEssence() {
    BigDecimal cyber = getExactCyberwareEssenceLoss();
    BigDecimal bio = equipment.getBiowareEssenceCost();
    BigDecimal higher = cyber.max(bio);
    BigDecimal lower = cyber.min(bio);
    BigDecimal burnout = hasQualityBonus(QualityBonusType.BURNOUT_ESSENCE) ? BigDecimal.ONE : BigDecimal.ZERO;
    return BASE_ESSENCE.subtract(higher).subtract(lower.divide(TWO)).subtract(burnout)
        .setScale(ESSENCE_DECIMALS, RoundingMode.HALF_UP);
  }

  /**
   * Ob die Essenz während der Charaktererschaffung auf 0 oder darunter gefallen ist, was das GRW für
   * Startcharaktere nicht erlaubt. Wird bewusst nur als Warnung angezeigt, nicht als harte Sperre
   * (konsistent mit der übrigen Validierung, siehe {@code docs/plaene/archiv/cyberware_plan.md} Abschnitt 13).
   * Im Karrieremodus immer {@code false}.
   */
  public boolean isEssenceBelowCreationMinimum() {
    return !created && getExactEssence().signum() <= 0;
  }

  /**
   * Essenzverlust durch Cyberware (vor Anwendung der Kombinationsregel) — bei der Quality
   * "Immunabstoßung" ({@link QualityBonusType#DOUBLES_CYBERWARE_ESSENCE}) verdoppelt. Das GRW
   * empfiehlt, Cyberware- und Bioware-Verlust getrennt zu notieren; dient daher auch der Anzeige.
   */
  public double getCyberwareEssenceLoss() {
    return getExactCyberwareEssenceLoss().doubleValue();
  }

  /** Essenzverlust durch Bioware (vor Anwendung der Kombinationsregel), Gegenstück zu {@link #getCyberwareEssenceLoss()}. */
  public double getBiowareEssenceLoss() {
    return equipment.getBiowareEssenceCost().doubleValue();
  }

  private @NonNull BigDecimal getExactCyberwareEssenceLoss() {
    BigDecimal loss = equipment.getCyberwareEssenceCost();
    return hasDoubledCyberwareEssence() ? loss.multiply(TWO) : loss;
  }

  private boolean hasDoubledCyberwareEssence() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.DOUBLES_CYBERWARE_ESSENCE));
  }

  /**
   * Um wie viele Punkte Magie bzw. Resonanz (Wert UND Maximum) durch Essenzverlust sinken.
   * SR4-Grundregelwerk S. 208: Jeder angebrochene Punkt Essenzverlust senkt Magie und Magiemaximum um
   * je 1 (GRW-Beispiel: Magie 4 bei Essenz 5,8 ergibt Magie 3, Maximum 5) — also
   * {@code ceiling(6 − Essenz)}. Gilt laut GRW S. 111 ebenso für Resonanz. Einheitlich für Erstellungs-
   * und Karrieremodus (kein modusabhängiger Sonderfall wie im C#-Original).
   */
  public int getEssencePenalty() {
    return BASE_ESSENCE.subtract(getExactEssence()).setScale(0, RoundingMode.CEILING).intValue();
  }

  /**
   * Der tatsächlich wirksame Magie- bzw. Resonanzwert: investierter Wert abzüglich {@link
   * #getEssencePenalty()}, nicht unter 0. Der investierte Wert ({@link SpecialAttribute#getValue()})
   * bleibt bewusst unverändert, weil sich BP-Kosten und Rückerstattung bei der Erstellung nach ihm
   * richten; die Essenz-Reduktion wird ausschließlich hier abgeleitet (keine Mutation von {@link
   * SpecialAttribute}, siehe {@code docs/plaene/archiv/cyberware_plan.md} Abschnitt 13).
   *
   * @return wirksamer Wert, oder 0 wenn weder Magie noch Resonanz freigeschaltet sind
   */
  public int getEffectiveMagicOrResonanceValue() {
    if (magicOrResonance == null) {
      return 0;
    }
    return Math.max(0, magicOrResonance.getValue() - getEssencePenalty());
  }

  /** Wirksames Magie-/Resonanzmaximum: Maximum abzüglich {@link #getEssencePenalty()}, nicht unter 0. */
  public int getEffectiveMagicOrResonanceMax() {
    if (magicOrResonance == null) {
      return 0;
    }
    return Math.max(0, magicOrResonance.getMax() - getEssencePenalty());
  }

  /**
   * Summe der BP-Kosten für Edge sowie — falls freigeschaltet — Magic/Resonance. Bewusst getrennt
   * von {@link #getAttributeBP()}: Sonderattribute zählen nicht in den {@link #MAX_ATTRIBUTE_BP}-
   * Deckel für Körperliche/Geistige Attribute.
   */
  public int getSpecialAttributeBP() {
    int cost = edge.getBpCost();
    if (magicOrResonance != null) {
      cost += magicOrResonance.getBpCost();
    }
    return cost;
  }

  private boolean grantsMagic() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.GRANTS_MAGIC));
  }

  private boolean grantsResonance() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.GRANTS_RESONANCE));
  }

  /**
   * Schaltet Magic/Resonance frei bzw. setzt es zurück, je nachdem ob nach der letzten
   * Quality-Änderung noch eine freischaltende Quality vorhanden ist. Laut GRW beginnt Magie bzw.
   * Resonanz mit der entsprechenden Gabe bei 1 — daher Startwert 1, nicht 0. Der Reset auf {@code null} verwirft automatisch die BP-Kosten
   * des Sonderattributs (siehe {@link #getSpecialAttributeBP()}): In der Erschaffungsphase
   * entspricht das einer Rückerstattung der ausgegebenen Punkte, im Karrieremodus bleibt bereits
   * verbuchtes Karma dagegen unrückerstattet (siehe {@link #getKarmaLog()}).
   */
  private void syncMagicOrResonance() {
    boolean magic = grantsMagic();
    boolean resonance = grantsResonance();
    if (magic && magicOrResonance == null) {
      magicOrResonance = new SpecialAttribute(SpecialAttributeType.MAGIC, 1, 6, true);
    } else if (resonance && magicOrResonance == null) {
      magicOrResonance = new SpecialAttribute(SpecialAttributeType.RESONANCE, 1, 6, true);
    } else if (!magic && !resonance) {
      magicOrResonance = null;
    }
  }

  // --- Skills ---

  public @NonNull List<Skill> getSkills() {
    return List.copyOf(skills.values());
  }

  public @Nullable Skill getSkill(@NonNull String name) {
    return skills.get(name);
  }

  /**
   * Prüft, ob eine Steigerung dieser aktiven Fertigkeit um 1 während der Charaktererschaffung
   * regelkonform wäre: entweder maximal eine Fertigkeit auf Wert 6, oder maximal zwei
   * Fertigkeiten auf Wert 5 — alle übrigen Fertigkeiten bleiben auf maximal 4 begrenzt, und
   * beide Sonderfälle schließen sich gegenseitig aus. Gekoppelte Gruppen-Skills (die gemeinsam
   * über {@link #raiseGroup(String)} gesteigert werden) zählen dabei nicht mit, da für Gruppen
   * ein eigener Cap gilt. Im Karrieremodus gilt dieser Erstellungs-Cap nicht.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: "eine andere Fertigkeit als diese"
  public boolean canRaiseSkillAtCreation(@NonNull Skill skill) {
    if (created) {
      return true;
    }
    int newValue = skill.getRating() + 1;
    if (newValue <= 4) {
      return true;
    }
    long count6 = skills.values().stream()
        .filter(s -> s != skill && !isCoupledGroupMember(s) && s.getRating() >= 6)
        .count();
    long count5 = skills.values().stream()
        .filter(s -> s != skill && !isCoupledGroupMember(s) && s.getRating() == 5)
        .count();
    if (newValue == 5) {
      return count6 == 0 && count5 < 2;
    }
    // newValue 6 oder (Talentiert, GRW S. 117) 7: zählt als die eine Fertigkeit auf 6
    return count5 == 0 && count6 < 1;
  }

  /**
   * Prüft, ob der Charakter diese aktive Fertigkeit überhaupt erwerben darf:
   * <ul>
   *   <li>Magische Fertigkeiten nur mit der Gabe "Zauberer" (SR4-Grundregelwerk S. 148: grundsätzlich nur
   *       für Zauberer und Magieradepten mit Magie ab 1). Ausnahme Arcana (S. 148), die auch Mundane
   *       lernen dürfen. Dass Adepten mit der Kraft Astrale Wahrnehmung auch Askennen und
   *       Astralkampf nutzen dürfen (S. 109), ist mangels Adeptenkräften noch nicht abgebildet.</li>
   *   <li>Resonanzfertigkeiten nur für Technomancer (S. 151, Gabe S. 117).</li>
   *   <li>Keine Fertigkeit, für die der Charakter Inkompetenz gewählt hat (S. 120).</li>
   * </ul>
   * Die Sperre gilt auch im Karrieremodus, da sie keine Erschaffungsregel, sondern eine
   * grundsätzliche Zugriffsvoraussetzung ist.
   */
  private boolean isSkillAccessible(@NonNull Skill skill) {
    return !hasQualityChoice(QualityBonusType.INCOMPETENT, skill.getName())
        && isSkillAccessible(skill, isMagician(), hasResonance());
  }

  private static boolean isSkillAccessible(@NonNull Skill skill, boolean magician, boolean resonance) {
    return switch (skill.getCategory()) {
      case MAGICAL -> magician || ARCANA_SKILL.equals(skill.getName());
      case RESONANCE -> resonance;
      default -> true;
    };
  }

  /**
   * Vollständige Steigerungs-Prüfung für eine aktive Fertigkeit: kombiniert den Erstellungs-Cap
   * aus {@link #canRaiseSkillAtCreation(Skill)} mit der Zugangsvoraussetzung aus
   * {@link #isSkillAccessible(Skill)}.
   */
  public boolean canRaiseSkill(@NonNull Skill skill) {
    return isSkillAccessible(skill) && canRaiseSkillAtCreation(skill);
  }

  private boolean isCoupledGroupMember(@NonNull Skill s) {
    String g = s.getGroupName();
    return g != null && !decoupledGroups.contains(g);
  }

  /**
   * Summe der BP-Kosten aller spezialisierten aktiven Fertigkeiten: fixer Preis
   * {@value #ACTIVE_SKILL_SPECIALIZATION_BP} BP pro Spezialisierung (SR4-Grundregelwerk S. 109),
   * unabhängig vom Rating der Fertigkeit. Gekoppelte Gruppen-Skills tragen nie bei, da sie laut
   * {@link #canSpecializeSkill(Skill)} während der Erstellung ohnehin nicht spezialisierbar sind.
   */
  public int getActiveSkillSpecializationBP() {
    return (int) skills.values().stream()
        .filter(s -> s.getSpecialization() != null)
        .count() * ACTIVE_SKILL_SPECIALIZATION_BP;
  }

  /**
   * Fixpreis in BP, um einer noch nicht spezialisierten aktiven Fertigkeit während der
   * Charaktererschaffung eine Spezialisierung zu geben (SR4-Grundregelwerk S. 109).
   */
  public int getActiveSkillSpecializationBpCost() {
    return ACTIVE_SKILL_SPECIALIZATION_BP;
  }

  /**
   * Prüft, ob diese aktive Fertigkeit spezialisiert werden darf: Mindestrating
   * {@value #MIN_RATING_FOR_SPECIALIZATION} (SR4-Grundregelwerk: Spezialisierung setzt voraus,
   * dass der Charakter die Fertigkeit bereits besitzt), und während der Charaktererschaffung darf
   * die Fertigkeit nicht Teil einer noch gekoppelten Fertigkeitsgruppe sein (Fertigkeitsgruppen
   * selbst sind nicht spezialisierbar). Im Karrieremodus ist eine gekoppelte Fertigkeit dagegen
   * spezialisierbar — das bricht die Gruppe auf (siehe {@link #setSkillSpecialization(Skill,
   * String)}).
   */
  public boolean canSpecializeSkill(@NonNull Skill skill) {
    return skill.getRating() >= MIN_RATING_FOR_SPECIALIZATION
        && (created || !isCoupledGroupMember(skill));
  }

  /**
   * Setzt die Spezialisierung einer aktiven Fertigkeit. Ist die Fertigkeit im Karrieremodus noch
   * Teil einer gekoppelten Fertigkeitsgruppe, wird die Gruppe dabei aufgebrochen (siehe {@link
   * #decoupleGroup(String)}) — die Mitglieder behalten ihr aktuelles Gruppen-Rating als
   * Einzelrating.
   */
  public void setSkillSpecialization(@NonNull Skill skill, @Nullable String specialization) {
    skill.setSpecialization(specialization);
    String groupName = skill.getGroupName();
    if (created && groupName != null && isCoupledGroupMember(skill)) {
      decoupleGroup(groupName);
    }
  }

  // --- Skillgruppen ---

  /**
   * Liefert true, solange die Gruppe noch nicht aufgebrochen wurde.
   */
  public boolean isGroupCoupled(@NonNull String groupName) {
    return !decoupledGroups.contains(groupName);
  }

  /**
   * Das aktuelle Rating einer gekoppelten Gruppe (Minimum aller Skills).
   */
  public int getGroupRating(@NonNull String groupName) {
    return skills.values().stream()
        .filter(s -> groupName.equals(s.getGroupName()))
        .mapToInt(Skill::getRating)
        .min().orElse(0);
  }

  public void raiseGroup(@NonNull String groupName) {
    int newRating = getGroupRating(groupName) + 1;
    skills.values().stream()
        .filter(s -> groupName.equals(s.getGroupName()))
        .forEach(s -> s.setRating(newRating));
  }

  public void lowerGroup(@NonNull String groupName) {
    int newRating = Math.max(0, getGroupRating(groupName) - 1);
    skills.values().stream()
        .filter(s -> groupName.equals(s.getGroupName()))
        .forEach(s -> s.setRating(newRating));
  }

  /**
   * Prüft, ob eine Steigerung dieser Fertigkeitsgruppe um 1 während der Charaktererschaffung
   * regelkonform wäre: Bei der Erschaffung sind Fertigkeitsgruppen auf Stufe 4 begrenzt (GRW S. 109).
   * Im Karrieremodus gilt dieser Erstellungs-Cap nicht (dort bis
   * {@link Skill#MAX_RATING}).
   */
  public boolean canRaiseGroupAtCreation(@NonNull String groupName) {
    return created || getGroupRating(groupName) < MAX_GROUP_RATING_AT_CREATION;
  }

  /**
   * Vollständige Steigerungs-Prüfung für eine Fertigkeitsgruppe: Erstellungs-Cap aus {@link
   * #canRaiseGroupAtCreation(String)} plus Zugangsvoraussetzung aller Mitglieder (siehe {@link
   * #isSkillAccessible(Skill)}), z.B. Hexerei/Beschwören nur für Magier, Tasken nur für
   * Technomancer. Ungebildet, Ungehobelt und Körperliche Schwäche sperren außerdem die Gruppen ihrer Kategorie
   * (GRW S. 120–122).
   */
  public boolean canRaiseGroup(@NonNull String groupName) {
    List<Skill> members = skills.values().stream()
        .filter(s -> groupName.equals(s.getGroupName()))
        .toList();
    return members.stream().allMatch(this::isSkillAccessible)
        && members.stream().noneMatch(s -> isSkillCostDoubled(s.getCategory()))
        && canRaiseGroupAtCreation(groupName);
  }

  /**
   * Karma-Kosten im Karrieremodus, um diese Fertigkeitsgruppe um 1 zu steigern. Eine komplett
   * neue Gruppe (Rating 0 → 1) kostet pauschal 10 Karma, danach gilt die Steigerungs-Formel
   * (neue Stufe × 5) — analog zum Muster bei einzelnen aktiven Fertigkeiten (neu = 4, sonst
   * (neue Stufe) × 2).
   */
  public int getGroupKarmaCost(@NonNull String groupName) {
    int rating = getGroupRating(groupName);
    return rating == 0 ? KARMA_NEW_SKILL_GROUP : (rating + 1) * 5;
  }

  /**
   * Bricht die Gruppe auf – Skills behalten ihre Ratings, werden ab jetzt einzeln bepreist.
   */
  public void decoupleGroup(@NonNull String groupName) {
    decoupledGroups.add(groupName);
  }

  public @NonNull Set<String> getDecoupledGroups() {
    return Collections.unmodifiableSet(decoupledGroups);
  }

  /**
   * Alle aktiven Fertigkeiten, die aktuell nicht Teil einer noch gekoppelten Fertigkeitsgruppe sind —
   * also nie gruppierte Fertigkeiten sowie im Karrieremodus entkoppelte ehemalige Gruppenmitglieder
   * (siehe {@link #decoupleGroup(String)}), alphabetisch nach Name sortiert. Für die UI-Sortierung nach
   * Alphabet gedacht, als Gegenstück zu {@link #getCoupledGroupNames()}.
   */
  public @NonNull List<Skill> getIndividualSkills() {
    return skills.values().stream()
        .filter(s -> !isCoupledGroupMember(s))
        .sorted(Comparator.comparing(Skill::getName))
        .toList();
  }

  /**
   * Namen aller aktuell noch gekoppelten Fertigkeitsgruppen, alphabetisch sortiert — Gegenstück zu
   * {@link #getDecoupledGroups()} (dort stehen die bereits aufgebrochenen Gruppen).
   */
  public @NonNull List<String> getCoupledGroupNames() {
    return skills.values().stream()
        .map(Skill::getGroupName)
        .filter(Objects::nonNull)
        .distinct()
        .filter(this::isGroupCoupled)
        .sorted()
        .toList();
  }

  public int getNuyenBP() {
    return nuyenBP;
  }

  public void setNuyenBP(int bp) {
    if (bp < 0 || bp > MAX_NUYEN_BP) {
      throw new IllegalArgumentException(
          "Nuyen-BP muss zwischen 0 und %d liegen".formatted(MAX_NUYEN_BP));
    }
    this.nuyenBP = bp;
  }

  // --- Qualities ---

  public @NonNull List<CharacterQuality> getQualities() {
    return Collections.unmodifiableList(qualities);
  }

  /** Fügt eine gewählte Quality ohne Auswahl hinzu, siehe {@link #addQuality(QualityData, boolean, String)}. */
  public void addQuality(@NonNull QualityData data, boolean gratis) {
    addQuality(data, gratis, "");
  }

  /**
   * Fügt eine gewählte Quality hinzu. Gratis-Qualities umgehen das GP-Limit und kosten 0 GP.
   *
   * @param choice Auswahl laut {@link QualityData#selection()} (Fertigkeitsname, {@link AttributeType}-Name oder
   *               Freitext), sonst leer
   * @throws IllegalArgumentException mit dem Grund aus {@link #getQualityAddProblem}
   */
  public void addQuality(@NonNull QualityData data, boolean gratis, @NonNull String choice) {
    String reason = getQualityAddProblem(data, gratis, choice);
    if (reason != null) {
      throw new IllegalArgumentException(reason);
    }
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis, choice));
    applyQualityEffects();
  }

  /**
   * Warum {@code data} mit dieser Auswahl nicht hinzugefügt werden kann, oder {@code null}: erst {@link
   * #getQualityBlockReason}, dann {@link #getQualityChoiceProblem}. Einzige Prüfung für Modell, Dialog und Tab.
   */
  public @Nullable String getQualityAddProblem(@NonNull QualityData data, boolean gratis, @NonNull String choice) {
    String reason = getQualityBlockReason(data, gratis);
    return reason != null ? reason : getQualityChoiceProblem(data, choice);
  }

  /**
   * Warum {@code data} gerade nicht gewählt werden kann (unabhängig von der Auswahl), oder {@code null}. Prüft die
   * Regeln aus GRW S. 114–122: nur durch den Metatyp, schon gewählt (außer mehrfach wählbare), eine Stufe je
   * Gruppe, Metatyp, Voraussetzung und Ausschluss, keine schon gekaufte Fertigkeitsgruppe bei Ungebildet, Ungehobelt
   * und Körperlicher Schwäche (S. 120–122) sowie bei der Erschaffung die Grenzen von 35 BP (S. 114, 118; nicht für
   * Gratis-Qualities).
   */
  public @Nullable String getQualityBlockReason(@NonNull QualityData data, boolean gratis) {
    QualityRules rules = data.rules();
    if (rules.metatypeOnly()) {
      return "Kommt nur durch den Metatyp";
    }
    List<CharacterQuality> selected = getSelectedQualities();
    boolean sameName = selected.stream().anyMatch(q -> q.data().name().equals(data.name()));
    if (sameName && !rules.multiple()) {
      return "Bereits gewählt";
    }
    String group = rules.group();
    if (group != null && selected.stream().anyMatch(q -> group.equals(q.data().rules().group()))) {
      return "Nur eine Stufe bzw. Variante von „%s“".formatted(group);
    }
    if (!fitsMetatype(rules)) {
      return "Nur für " + String.join(", ", rules.metatypes());
    }
    if (!rules.requires().isEmpty()
        && selected.stream().noneMatch(q -> rules.requires().contains(q.data().name()))) {
      return "Erfordert " + String.join(" oder ", rules.requires());
    }
    Optional<CharacterQuality> conflict = selected.stream()
        .filter(q -> rules.excludes().contains(q.data().name())
            || q.data().rules().excludes().contains(data.name()))
        .findFirst();
    if (conflict.isPresent()) {
      return "Nicht zusammen mit " + conflict.get().data().name();
    }
    SkillCategory doubled = doubledCategory(data);
    if (doubled != null) {
      for (Skill s : skills.values()) {
        String g = s.getGroupName();
        if (g != null && s.getCategory() == doubled && isGroupCoupled(g) && getGroupRating(g) > 0) {
          return "Fertigkeitsgruppe %s hat bereits einen Wert".formatted(g);
        }
      }
    }
    if (!created && !gratis) {
      if (data.type() == QualityType.POSITIVE
          && getPositiveQualityBP() + data.bp() > MAX_POSITIVE_QUALITY_BP) {
        return "Höchstens %d BP für Gaben bei der Erschaffung".formatted(MAX_POSITIVE_QUALITY_BP);
      }
      if (data.type() == QualityType.NEGATIVE
          && getNegativeQualityBP() + Math.abs(data.bp()) > MAX_NEGATIVE_QUALITY_BP) {
        return "Höchstens %d BP aus Handicaps bei der Erschaffung".formatted(MAX_NEGATIVE_QUALITY_BP);
      }
    }
    return null;
  }

  private boolean fitsMetatype(@NonNull QualityRules rules) {
    return rules.metatypes().isEmpty() || rules.metatypes().contains(metatype.baseName());
  }

  /** Kategorie, deren Fertigkeiten diese Quality verteuert und deren Gruppen sie sperrt (GRW S. 120–122). */
  private static @Nullable SkillCategory doubledCategory(@NonNull QualityData data) {
    if (data.bonuses().contains(QualityBonusType.UNEDUCATED)) {
      return SkillCategory.TECHNICAL;
    }
    if (data.bonuses().contains(QualityBonusType.UNCOUTH)) {
      return SkillCategory.SOCIAL;
    }
    if (data.bonuses().contains(QualityBonusType.INFIRM)) {
      return SkillCategory.PHYSICAL;
    }
    return null;
  }

  /**
   * Warum {@code choice} als Auswahl für {@code data} nicht passt, oder {@code null}: fehlende Auswahl, unbekannte
   * Fertigkeit bzw. unbekanntes Attribut, Inkompetenz für eine schon erlernte Fertigkeit, doppelte Auswahl.
   */
  public @Nullable String getQualityChoiceProblem(@NonNull QualityData data, @NonNull String choice) {
    if (getSelectedQualities().stream()
        .anyMatch(q -> q.data().name().equals(data.name()) && q.choice().equals(choice))) {
      return "Mit dieser Auswahl bereits gewählt";
    }
    return switch (data.selection()) {
      case NONE -> null;
      case TEXT -> choice.isBlank() ? data.selectLabel() + " angeben" : null;
      case ATTRIBUTE -> Arrays.stream(AttributeType.values()).anyMatch(t -> t.name().equals(choice))
          ? null : "Attribut wählen";
      case SKILL -> {
        Skill skill = getSkill(choice);
        if (skill == null) {
          yield "Fertigkeit wählen";
        }
        // Inkompetenz: die Fertigkeit darf nicht erworben sein (GRW S. 120).
        yield data.bonuses().contains(QualityBonusType.INCOMPETENT) && skill.getRating() > 0
            ? "Fertigkeit hat bereits einen Wert" : null;
      }
    };
  }

  /** Gewählte (nicht vom Metatyp vergebene) Qualities. */
  public @NonNull List<CharacterQuality> getSelectedQualities() {
    return qualities.stream().filter(q -> q.source() == QualitySource.SELECTED).toList();
  }

  /** Lädt eine gewählte Quality ohne Regelprüfung — nur für den Persistenz-Pfad. */
  public void loadSelectedQuality(@NonNull QualityData data, boolean gratis, @NonNull String choice) {
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis, choice));
    applyQualityEffects();
  }

  /**
   * Entfernt eine gewählte Quality. Gaben, deren Voraussetzung damit wegfällt (z.B. Schutzpatron ohne Zauberer),
   * werden mit entfernt. Falls dies die letzte Quality war, die Magie bzw. Resonanz freigeschaltet hat, wird das
   * Sonderattribut zurückgesetzt (siehe {@link #syncMagicOrResonance()}) — in der Erschaffungsphase entspricht das
   * einer automatischen Rückerstattung, im Karrieremodus gibt es keine Karma-Rückerstattung. Fertigkeiten, die
   * dadurch nicht mehr zugänglich sind, werden auf 0 gesetzt (siehe {@link
   * #getSkillsLockedByRemovingQuality(CharacterQuality)}); Werte über einem gesunkenen Maximum (Talentiert,
   * Außergewöhnliches Attribut, Glück) sinken darauf. {@link #getQualityRemovalConsequences} listet diese Folgen
   * vorher auf.
   */
  public void removeQuality(@NonNull CharacterQuality quality) {
    List<CharacterQuality> removed = getRemovalSet(quality);
    List<Skill> locked = getSkillsLockedWithout(removed);
    qualities.removeIf(q -> containsSame(removed, q));
    applyQualityEffects();
    locked.forEach(s -> {
      s.setRating(0);
      s.setSpecialization(null);
    });
  }

  /**
   * Wendet die rechnerischen Wirkungen aller Qualities an: Magie/Resonanz freischalten, Maximum-Boni
   * (Außergewöhnliches Attribut, Glück), Höchstwert 7 (Talentiert) und gesperrte Fertigkeiten (Inkompetenz, auf
   * 0 gesetzt).
   */
  private void applyQualityEffects() {
    syncMagicOrResonance();
    for (CharacterAttribute attribute : attributes.values()) {
      attribute.setMaxBonus(
          hasQualityChoice(QualityBonusType.EXCEPTIONAL_ATTRIBUTE, attribute.getType().name()) ? 1 : 0);
    }
    edge.setMaxBonus(hasQualityBonus(QualityBonusType.LUCKY) ? 1 : 0);
    for (Skill skill : skills.values()) {
      skill.setMaxRating(hasQualityChoice(QualityBonusType.APTITUDE, skill.getName())
          ? Skill.MAX_RATING + 1 : Skill.MAX_RATING);
      if (hasQualityChoice(QualityBonusType.INCOMPETENT, skill.getName())) {
        skill.setRating(0);
        skill.setSpecialization(null);
      }
    }
  }

  private boolean hasQualityBonus(@NonNull QualityBonusType bonus) {
    return hasQualityBonus(qualities, bonus);
  }

  private static boolean hasQualityBonus(@NonNull List<CharacterQuality> from, @NonNull QualityBonusType bonus) {
    return from.stream().anyMatch(q -> q.data().bonuses().contains(bonus));
  }

  private boolean hasQualityChoice(@NonNull QualityBonusType bonus, @NonNull String choice) {
    return hasQualityChoice(qualities, bonus, choice);
  }

  private static boolean hasQualityChoice(@NonNull List<CharacterQuality> from, @NonNull QualityBonusType bonus,
      @NonNull String choice) {
    return from.stream().anyMatch(q -> q.data().bonuses().contains(bonus) && q.choice().equals(choice));
  }

  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau dieser Eintrag
  private static boolean containsSame(@NonNull List<CharacterQuality> list, @NonNull CharacterQuality quality) {
    return list.stream().anyMatch(q -> q == quality);
  }

  /** Die Quality selbst plus alle gewählten Gaben, deren Voraussetzung damit wegfällt (auch über mehrere Stufen). */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau dieser Eintrag
  private @NonNull List<CharacterQuality> getRemovalSet(@NonNull CharacterQuality quality) {
    List<CharacterQuality> remaining = new ArrayList<>(getSelectedQualities());
    remaining.removeIf(q -> q == quality);
    boolean removedDependent = true;
    while (removedDependent) {
      List<CharacterQuality> current = List.copyOf(remaining);
      removedDependent = remaining.removeIf(q -> !q.data().rules().requires().isEmpty()
          && current.stream().noneMatch(other -> q.data().rules().requires().contains(other.data().name())));
    }
    return qualities.stream()
        .filter(q -> q == quality || (q.source() == QualitySource.SELECTED && !containsSame(remaining, q)))
        .toList();
  }

  /**
   * Was das Entfernen dieser Quality zusätzlich bewirkt, als Zeilen für die Rückfrage: mit entfernte Gaben, nicht mehr
   * erlernbare Fertigkeiten (auf 0) und Werte über einem gesunkenen Maximum (Talentiert, Außergewöhnliches
   * Attribut, Glück). Leer, wenn nur die Quality selbst wegfällt.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau dieser Eintrag
  public @NonNull List<String> getQualityRemovalConsequences(@NonNull CharacterQuality quality) {
    List<CharacterQuality> removed = getRemovalSet(quality);
    List<CharacterQuality> remaining = qualities.stream().filter(q -> !containsSame(removed, q)).toList();
    List<String> lines = new ArrayList<>();
    removed.stream().filter(q -> q != quality).forEach(q -> lines.add(q.displayName() + " wird mit entfernt"));
    List<Skill> locked = getSkillsLockedWithout(removed);
    locked.forEach(s -> lines.add("%s %d → 0 (nicht mehr erlernbar)".formatted(s.getName(), s.getRating())));
    for (Skill s : skills.values()) {
      if (!locked.contains(s) && s.getRating() > Skill.MAX_RATING
          && !hasQualityChoice(remaining, QualityBonusType.APTITUDE, s.getName())) {
        lines.add("%s %d → %d".formatted(s.getName(), s.getRating(), Skill.MAX_RATING));
      }
    }
    for (CharacterAttribute a : attributes.values()) {
      String type = a.getType().name();
      if (a.getValue() == a.getMax()
          && hasQualityChoice(QualityBonusType.EXCEPTIONAL_ATTRIBUTE, type)
          && !hasQualityChoice(remaining, QualityBonusType.EXCEPTIONAL_ATTRIBUTE, type)) {
        lines.add("%s %d → %d".formatted(a.getType().getDisplayName(), a.getValue(), a.getMax() - 1));
      }
    }
    if (edge.getValue() == edge.getMax() && hasQualityBonus(QualityBonusType.LUCKY)
        && !hasQualityBonus(remaining, QualityBonusType.LUCKY)) {
      lines.add("%s %d → %d".formatted(edge.getType().getDisplayName(), edge.getValue(), edge.getMax() - 1));
    }
    return lines;
  }

  /**
   * Aktive Fertigkeiten mit Wert > 0, die nach dem Entfernen dieser Quality nicht mehr zugänglich
   * wären (siehe {@link #isSkillAccessible(Skill)}), z.B. Hexerei nach Entfernen der Gabe "Zauberer"
   * oder Tasken nach Entfernen der Gabe "Technomancer"; mit entfernte Gaben zählen mit. {@link
   * #removeQuality(CharacterQuality)} setzt genau
   * diese Fertigkeiten samt Spezialisierung auf 0, damit keine Werte stehen bleiben, die BP kosten,
   * aber nicht mehr nutzbar sind. Die UI fragt vorher nach, wenn die Liste nicht leer ist.
   */
  public @NonNull List<Skill> getSkillsLockedByRemovingQuality(@NonNull CharacterQuality quality) {
    return getSkillsLockedWithout(getRemovalSet(quality));
  }

  private @NonNull List<Skill> getSkillsLockedWithout(@NonNull List<CharacterQuality> removed) {
    List<CharacterQuality> remaining = qualities.stream().filter(q -> !containsSame(removed, q)).toList();
    boolean magician = hasQualityBonus(remaining, QualityBonusType.GRANTS_SPELLCASTING);
    boolean resonance = hasQualityBonus(remaining, QualityBonusType.GRANTS_RESONANCE);
    return skills.values().stream()
        .filter(s -> s.getRating() > 0 && !isSkillAccessible(s, magician, resonance))
        .toList();
  }

  /**
   * Karma-Kosten im Karrieremodus, um eine neue Quality zu erwerben bzw. ein bestehendes
   * Handicap wieder loszuwerden: laut GRW in beiden Fällen das Doppelte der GP-Kosten der Quality in
   * Karma.
   */
  public int getQualityKarmaCost(@NonNull QualityData data) {
    return Math.abs(data.bp()) * 2;
  }

  public int getPositiveQualityBP() {
    return qualities.stream()
        .filter(q -> q.source() == QualitySource.SELECTED
            && q.data().type() == QualityType.POSITIVE
            && !q.gratis())
        .mapToInt(q -> q.data().bp())
        .sum();
  }

  public int getNegativeQualityBP() {
    return qualities.stream()
        .filter(q -> q.source() == QualitySource.SELECTED
            && q.data().type() == QualityType.NEGATIVE
            && !q.gratis())
        .mapToInt(q -> Math.abs(q.data().bp()))
        .sum();
  }

  public boolean isUneducated() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.UNEDUCATED));
  }

  public boolean isUncouth() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.UNCOUTH));
  }

  public boolean isInfirm() {
    return qualities.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.INFIRM));
  }

  public boolean isSkillCostDoubled(@NonNull SkillCategory category) {
    return qualities.stream().anyMatch(q -> doubledCategory(q.data()) == category);
  }

  // --- Wissensfertigkeiten ---

  public @NonNull List<KnowledgeSkill> getKnowledgeSkills() {
    return Collections.unmodifiableList(knowledgeSkills);
  }

  /**
   * Fügt eine neue, mit Rating 0 startende Wissensfertigkeit hinzu.
   *
   * @throws IllegalArgumentException wenn bereits eine Wissensfertigkeit mit demselben Namen existiert
   */
  public void addKnowledgeSkill(@NonNull String name, @NonNull KnowledgeSkillCategory category) {
    boolean duplicate = knowledgeSkills.stream().anyMatch(k -> k.getName().equals(name));
    if (duplicate) {
      throw new IllegalArgumentException(
          "Wissensfertigkeit \"%s\" existiert bereits".formatted(name));
    }
    knowledgeSkills.add(new KnowledgeSkill(name, category));
  }

  public void removeKnowledgeSkill(@NonNull String name) {
    knowledgeSkills.removeIf(k -> k.getName().equals(name));
  }

  /**
   * Liefert die deklarierte Muttersprache, falls gesetzt. Die Muttersprache ist laut SR4-Regeln
   * kostenlos und kein regulärer Wissensfertigkeitspunkte-Verbrauch (siehe {@link
   * KnowledgeSkill#isNativeLanguage()}).
   */
  public @Nullable KnowledgeSkill getNativeLanguage() {
    return knowledgeSkills.stream().filter(KnowledgeSkill::isNativeLanguage).findFirst()
        .orElse(null);
  }

  /**
   * Setzt (bzw. ersetzt) die Muttersprache des Charakters. Anders als {@link
   * #addKnowledgeSkill(String, KnowledgeSkillCategory)} kostet dies keine Wissensfertigkeitspunkte
   * und es kann immer nur eine Muttersprache geben — ein vorheriger Eintrag wird ersetzt.
   */
  public void setNativeLanguage(@NonNull String name) {
    knowledgeSkills.removeIf(KnowledgeSkill::isNativeLanguage);
    knowledgeSkills.add(new KnowledgeSkill(name, KnowledgeSkillCategory.LANGUAGE, true));
  }

  /**
   * Der freie Wissensfertigkeitspunkte-Pool zur Charaktererstellung: (INT + LOG) × 3.
   */
  public int getKnowledgeSkillPointPool() {
    return (getAttribute(AttributeType.INT).getValue()
        + getAttribute(AttributeType.LOG).getValue()) * 3;
  }

  /**
   * Verdoppelt bei Kategorien Academic/Professional, wenn der Charakter Uneducated ist — Original
   * verdoppelt hier bewusst den Pool-Verbrauch (BP-Modus), nicht direkt den Karma-Preis wie im
   * Karrieremodus (siehe {@link #getKnowledgeSkillKarmaCost(KnowledgeSkill)}).
   */
  public boolean isKnowledgeSkillCostDoubled(@NonNull KnowledgeSkillCategory category) {
    return (category == KnowledgeSkillCategory.ACADEMIC
        || category == KnowledgeSkillCategory.PROFESSIONAL) && isUneducated();
  }

  /**
   * Summe der verbrauchten Wissensfertigkeitspunkte. Ratings in den Kategorien Academic/
   * Professional zählen bei Uneducated doppelt in diese Summe ein. Eine gesetzte Spezialisierung
   * trägt zusätzlich flach mit +1 Punkt bei (SR4-Grundregelwerk S. 109/154: "Jede Spezialisierung
   * einer Wissensfertigkeit kostet 1 Wissens-Fertigkeitspunkt") — dieser Beitrag ist bewusst
   * **nicht** von der Uneducated-Verdopplung betroffen, anders als der Rating-Beitrag. Die
   * Muttersprache ({@link KnowledgeSkill#isNativeLanguage()}) ist kostenlos und zählt nicht mit.
   */
  public int getKnowledgeSkillPointsUsed() {
    int used = 0;
    for (KnowledgeSkill skill : knowledgeSkills) {
      if (skill.isNativeLanguage()) {
        continue;
      }
      int contribution = skill.getRating();
      if (isKnowledgeSkillCostDoubled(skill.getCategory())) {
        contribution *= 2;
      }
      if (skill.getSpecialization() != null) {
        contribution += 1;
      }
      used += contribution;
    }
    return used;
  }

  /**
   * BP-Kosten für Wissensfertigkeitspunkte über dem freien Pool — flacher Preis pro Punkt, nicht
   * gestaffelt wie bei aktiven Fertigkeiten.
   */
  public int getKnowledgeSkillOverflowBP() {
    int overflow = getKnowledgeSkillPointsUsed() - getKnowledgeSkillPointPool();
    return Math.max(0, overflow) * KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT;
  }

  /**
   * BP-Kosten, die eine Ratingsteigerung dieser einen Wissensfertigkeit um 1 zusätzlich auslöst.
   * Da der Pool gemeinsam über alle Wissensfertigkeiten verrechnet wird, ist dies keine feste
   * Pro-Fertigkeit-Konstante, sondern hängt vom bereits verbrauchten Gesamtpool ab.
   */
  public int getKnowledgeSkillIncrementBP(@NonNull KnowledgeSkill skill) {
    int pointsUsed = getKnowledgeSkillPointsUsed();
    int pool = getKnowledgeSkillPointPool();
    int contribution = isKnowledgeSkillCostDoubled(skill.getCategory()) ? 2 : 1;
    int overflowBefore = Math.max(0, pointsUsed - pool) * KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT;
    int overflowAfter =
        Math.max(0, pointsUsed + contribution - pool) * KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT;
    return overflowAfter - overflowBefore;
  }

  /**
   * Prüft, ob eine Steigerung dieser Wissensfertigkeit um 1 während der Charaktererschaffung
   * regelkonform wäre: die Gesamtpunktzahl (Pool + Zusatzpunkte) darf den freien Pool höchstens
   * verdoppeln (GRW S. 109), und wie bei aktiven Fertigkeiten gilt entweder maximal eine
   * Wissensfertigkeit auf Wert 6 oder maximal zwei auf Wert 5 (übrige max. 4). Die Muttersprache
   * ist von beiden Regeln ausgenommen. Im Karrieremodus gilt dieser Erstellungs-Cap nicht.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: "eine andere Fertigkeit als diese"
  public boolean canRaiseKnowledgeSkillAtCreation(@NonNull KnowledgeSkill skill) {
    if (created || skill.isNativeLanguage()) {
      return true;
    }
    int newValue = skill.getRating() + 1;
    if (newValue > KnowledgeSkill.MAX_RATING) {
      return false;
    }
    int contribution = isKnowledgeSkillCostDoubled(skill.getCategory()) ? 2 : 1;
    if (getKnowledgeSkillPointsUsed() + contribution > getKnowledgeSkillPointPool() * 2) {
      return false;
    }
    if (newValue <= 4) {
      return true;
    }
    long count6 = knowledgeSkills.stream()
        .filter(k -> k != skill && !k.isNativeLanguage() && k.getRating() == 6)
        .count();
    long count5 = knowledgeSkills.stream()
        .filter(k -> k != skill && !k.isNativeLanguage() && k.getRating() == 5)
        .count();
    if (newValue == 5) {
      return count6 == 0 && count5 < 2;
    }
    // newValue 6 oder (Talentiert, GRW S. 117) 7: zählt als die eine Fertigkeit auf 6
    return count5 == 0 && count6 < 1;
  }

  /**
   * BP-Kosten, die das Setzen einer weiteren Wissensfertigkeiten-Spezialisierung gerade jetzt
   * zusätzlich auslösen würde. Der Spezialisierungs-Beitrag zum Pool ist stets flach +1 (siehe
   * {@link #getKnowledgeSkillPointsUsed()}) und — anders als {@link
   * #getKnowledgeSkillIncrementBP(KnowledgeSkill)} für Rating-Steigerungen — unabhängig davon,
   * welche Wissensfertigkeit oder Kategorie betroffen ist; daher ohne Skill-Parameter.
   */
  public int getKnowledgeSkillSpecializationBP() {
    int pointsUsed = getKnowledgeSkillPointsUsed();
    int pool = getKnowledgeSkillPointPool();
    int overflowBefore = Math.max(0, pointsUsed - pool) * KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT;
    int overflowAfter = Math.max(0, pointsUsed + 1 - pool) * KNOWLEDGE_SKILL_BP_PER_OVERFLOW_POINT;
    return overflowAfter - overflowBefore;
  }

  /**
   * Prüft, ob diese Wissensfertigkeit spezialisiert werden darf: Mindestrating
   * {@value #MIN_RATING_FOR_SPECIALIZATION} (SR4-Grundregelwerk S. 109: Wissensfertigkeiten lassen sich
   * wie Aktionsfertigkeiten spezialisieren — dieselbe Mindestrating-Voraussetzung wie bei aktiven Fertigkeiten, siehe {@link
   * #canSpecializeSkill(Skill)}), keine Spezialisierung für die Muttersprache. Während der
   * Charaktererschaffung darf der Spezialisierungs-Beitrag den Pool nicht über die
   * Verdopplungsgrenze hinaus belasten (analog zu {@link
   * #canRaiseKnowledgeSkillAtCreation(KnowledgeSkill)}). Im Karrieremodus gilt dieser
   * Erstellungs-Cap nicht.
   */
  public boolean canSpecializeKnowledgeSkill(@NonNull KnowledgeSkill skill) {
    if (skill.isNativeLanguage() || skill.getRating() < MIN_RATING_FOR_SPECIALIZATION) {
      return false;
    }
    if (created) {
      return true;
    }
    return getKnowledgeSkillPointsUsed() + 1 <= getKnowledgeSkillPointPool() * 2;
  }

  /**
   * Setzt die Spezialisierung einer Wissensfertigkeit. Anders als bei aktiven Fertigkeiten gibt es
   * hier keinen Gruppenauflösungs-Seiteneffekt (Wissensfertigkeiten kennen keine Gruppen).
   */
  public void setKnowledgeSkillSpecialization(@NonNull KnowledgeSkill skill,
      @Nullable String specialization) {
    skill.setSpecialization(specialization);
  }

  /**
   * Karrieremodus-Kosten, um eine Spezialisierung (aktive Fertigkeit oder Wissensfertigkeit) neu
   * zu setzen oder zu ändern. SR4-Grundregelwerk S. 312: 2 Karma je Spezialisierung — gilt identisch für beide Fertigkeitsarten. Bewusst unabhängig von
   * Uneducated/Uncouth/Infirm und daher **nicht** über {@link Skill#getKarmaCost(boolean)} bzw.
   * {@link #getKnowledgeSkillKarmaCost(KnowledgeSkill)} zu berechnen — jene Methoden verdoppeln bei
   * Rating-Steigerungen, das GRW nennt für Spezialisierungen aber keine solche Sonderregel.
   */
  public int getSpecializationKarmaCost() {
    return SPECIALIZATION_KARMA_COST;
  }

  /**
   * Karma-Kosten für eine Ratingsteigerung im Karrieremodus. Anders als im BP-Modus verdoppelt
   * Uneducated hier den Karma-Endpreis direkt, nicht den Poolverbrauch (es existiert kein Pool
   * mehr im Karrieremodus).
   */
  public int getKnowledgeSkillKarmaCost(@NonNull KnowledgeSkill skill) {
    int rating = skill.getRating();
    int cost = rating == 0
        ? KNOWLEDGE_SKILL_KARMA_NEW
        : (rating + 1) * KNOWLEDGE_SKILL_KARMA_PER_RATING;
    return isKnowledgeSkillCostDoubled(skill.getCategory()) ? cost * 2 : cost;
  }

  // --- Würfelpools, Zustandsmonitor, Initiative ---

  /**
   * Würfelpool einer aktiven Fertigkeit (SR4-Grundregelwerk S. 146–148): Fertigkeitswert plus wirksamer Wert des
   * verbundenen Attributs. Ohne Fertigkeitswert nur das Attribut mit −1, und das nur, wenn die Fertigkeit es erlaubt
   * ({@link Skill#isDefaultable()}) und der Charakter in ihrer Kategorie nicht als ahnungslos gilt (Ungebildet,
   * Ungehobelt, Körperliche Schwäche, S. 120/122). Der Verletzungsmodifikator ist eingerechnet, Spezialisierungen
   * (+2 im Spezialgebiet, S. 147) nicht.
   *
   * @return leer, wenn keine Probe möglich ist (Fertigkeit nicht erlernbar, z.B. Inkompetenz oder Hexerei ohne Gabe,
   *         oder Probe ohne Fertigkeitswert nicht erlaubt)
   */
  public @NonNull Optional<DicePool> getSkillDicePool(@NonNull Skill skill) {
    if (!isSkillAccessible(skill)) {
      return Optional.empty();
    }
    String attributeName = linkedAttributeName(skill.getLinkedAttribute());
    int attribute = linkedAttributeValue(skill.getLinkedAttribute());
    if (skill.getRating() > 0) {
      return Optional.of(dicePool(skill.getRating() + attribute,
          ratingBreakdown(skill.getName(), skill.getRating(), attributeName, attribute)));
    }
    if (!skill.isDefaultable() || isSkillCostDoubled(skill.getCategory())) {
      return Optional.empty();
    }
    return Optional.of(defaultingPool(attributeName, attribute));
  }

  /**
   * Würfelpool einer Wissensfertigkeit: Wert plus verbundenes Attribut, Verletzungsmodifikator eingerechnet. Ohne
   * Wert wie bei aktiven Fertigkeiten Attribut − 1, außer bei Sprachen (in der Tabelle S. 146 als einzige
   * Wissensfertigkeit kursiv) und bei Akademischem und Berufswissen mit Ungebildet (S. 121). Die Muttersprache hat
   * keinen Wert und damit keinen Pool.
   */
  public @NonNull Optional<DicePool> getKnowledgeSkillDicePool(@NonNull KnowledgeSkill knowledgeSkill) {
    if (knowledgeSkill.isNativeLanguage()) {
      return Optional.empty();
    }
    KnowledgeSkillCategory category = knowledgeSkill.getCategory();
    AttributeType type = category.getLinkedAttribute();
    int attribute = getAugmentedAttributeValue(type);
    if (knowledgeSkill.getRating() > 0) {
      return Optional.of(dicePool(knowledgeSkill.getRating() + attribute,
          ratingBreakdown(knowledgeSkill.getName(), knowledgeSkill.getRating(), type.getDisplayName(), attribute)));
    }
    if (category == KnowledgeSkillCategory.LANGUAGE || isKnowledgeSkillCostDoubled(category)) {
      return Optional.empty();
    }
    return Optional.of(defaultingPool(type.getDisplayName(), attribute));
  }

  /** Würfel mit Spezialisierung: +{@value #SPECIALIZATION_DICE} im Spezialgebiet (S. 147). */
  public static int withSpecialization(@NonNull DicePool pool) {
    return pool.dice() + SPECIALIZATION_DICE;
  }

  private static @NonNull String ratingBreakdown(@NonNull String name, int rating, @NonNull String attributeName,
      int attribute) {
    return "%s %d + %s %d".formatted(name, rating, attributeName, attribute);
  }

  private @NonNull DicePool defaultingPool(@NonNull String attributeName, int attribute) {
    return dicePool(attribute - 1, "%s %d − 1 ohne Fertigkeitswert".formatted(attributeName, attribute));
  }

  /** Würfelpool einer reinen Attributprobe (S. 166/167), Verletzungsmodifikator eingerechnet. */
  public @NonNull DicePool getAttributeTestPool(@NonNull AttributeTest test) {
    int first = getAugmentedAttributeValue(test.getFirst());
    int second = getAugmentedAttributeValue(test.getSecond());
    return dicePool(first + second,
        ratingBreakdown(test.getFirst().getDisplayName(), first, test.getSecond().getDisplayName(), second));
  }

  private @NonNull DicePool dicePool(int dice, @NonNull String breakdown) {
    int wound = getWoundModifier();
    String text = wound == 0 ? breakdown : "%s − %d Verletzung".formatted(breakdown, -wound);
    return new DicePool(Math.max(0, dice + wound), text);
  }

  /** Wirksamer Wert des verbundenen Attributs einer Fertigkeit; MAG/RES nach Essenzverlust. */
  private int linkedAttributeValue(@NonNull String abbreviation) {
    return switch (abbreviation) {
      case "MAG", "RES" -> getEffectiveMagicOrResonanceValue();
      default -> getAugmentedAttributeValue(AttributeType.valueOf(abbreviation));
    };
  }

  private static @NonNull String linkedAttributeName(@NonNull String abbreviation) {
    return Arrays.stream(SpecialAttributeType.values())
        .filter(t -> t.getAbbreviation().equals(abbreviation))
        .map(SpecialAttributeType::getDisplayName)
        .findFirst()
        .orElseGet(() -> AttributeType.valueOf(abbreviation).getDisplayName());
  }

  /**
   * Augmentiertes Maximum eines körperlichen oder geistigen Attributs: das 1,5-fache des natürlichen Maximums,
   * abgerundet (SR4-Grundregelwerk S. 91). Das natürliche Maximum schließt Außergewöhnliches Attribut ein.
   */
  public int getAugmentedMaximum(@NonNull AttributeType type) {
    return getAttribute(type).getMax() * 3 / 2;
  }

  /**
   * Initiativeattribut: Reaktion plus Intuition, je mit wirksamem Wert (S. 172), höchstens {@link
   * #getInitiativeMaximum()}. Ohne Verletzungsmodifikator, siehe {@link #getInitiative()}.
   */
  public int getInitiativeAttribute() {
    int sum = getAugmentedAttributeValue(AttributeType.REA) + getAugmentedAttributeValue(AttributeType.INT);
    return Math.min(sum, getInitiativeMaximum());
  }

  /** Augmentiertes Maximum der Initiative: 1,5-fache Summe der Maxima von Reaktion und Intuition (S. 91). */
  public int getInitiativeMaximum() {
    return (getAttribute(AttributeType.REA).getMax() + getAttribute(AttributeType.INT).getMax()) * 3 / 2;
  }

  /** Initiative mit Verletzungsmodifikator (S. 174), nicht unter 0. */
  public int getInitiative() {
    return Math.max(0, getInitiativeAttribute() + getWoundModifier());
  }

  /**
   * Initiativedurchgänge: einer; mehr gibt es nur durch Boni wie Reflexbooster oder Adeptenkräfte (S. 172), die noch
   * nicht modelliert sind.
   */
  public int getInitiativePasses() {
    return 1;
  }

  /** Zugang zum Astralraum und damit eine astrale Initiative: derzeit nur über die Gabe Zauberer (S. 116, 118). */
  public boolean hasAstralAccess() {
    return hasQualityBonus(QualityBonusType.ASTRAL_ACCESS);
  }

  /**
   * Astrale Initiative: doppelte Intuition (S. 227), mit Verletzungsmodifikator, nicht unter 0; {@value
   * #ASTRAL_INITIATIVE_PASSES} Durchgänge. Nur sinnvoll mit {@link #hasAstralAccess()}.
   */
  public int getAstralInitiative() {
    return Math.max(0, getAugmentedAttributeValue(AttributeType.INT) * 2 + getWoundModifier());
  }

  /** Kästchen der Körperlichen Spalte: 8 plus halbe Konstitution, aufgerundet (S. 94). */
  public int getPhysicalBoxes() {
    return CONDITION_MONITOR_BASE + (getAttribute(AttributeType.BOD).getValue() + 1) / 2;
  }

  /** Kästchen der Geistigen Spalte: 8 plus halbe Willenskraft, aufgerundet (S. 94). */
  public int getStunBoxes() {
    return CONDITION_MONITOR_BASE + (getAttribute(AttributeType.WIL).getValue() + 1) / 2;
  }

  /**
   * Kästchen für überzähligen körperlichen Schaden: so viele wie die Konstitution (darüber stirbt der Charakter,
   * S. 192), plus die Stufe von Überlebenswille (S. 117).
   */
  public int getPhysicalOverflowBoxes() {
    return getAttribute(AttributeType.BOD).getValue() + qualityLevel(QualityBonusType.WILL_TO_LIVE);
  }

  /**
   * Körperlicher Schaden in Kästchen inklusive Überzahl. Schrumpft die Spalte nach dem Eintragen (z.B. niedrigere
   * Konstitution), wird beim Lesen auf die neue Größe begrenzt; der eingetragene Wert bleibt bewusst erhalten und gilt
   * wieder, wenn die Spalte wächst.
   */
  public int getPhysicalDamage() {
    return Math.min(physicalDamage, getPhysicalBoxes() + getPhysicalOverflowBoxes());
  }

  /** Setzt den körperlichen Schaden, begrenzt auf 0 bis Spalte plus Überzahl (S. 192). */
  public void setPhysicalDamage(int damage) {
    physicalDamage = Math.clamp(damage, 0, getPhysicalBoxes() + getPhysicalOverflowBoxes());
  }

  /** Geistiger Schaden in Kästchen; Begrenzung beim Lesen wie bei {@link #getPhysicalDamage()}. */
  public int getStunDamage() {
    return Math.min(stunDamage, getStunBoxes());
  }

  /** Setzt den geistigen Schaden, begrenzt auf 0 bis Spaltengröße. */
  public void setStunDamage(int damage) {
    stunDamage = Math.clamp(damage, 0, getStunBoxes());
  }

  /**
   * Verletzungsmodifikator (S. 193): je volle 3 Schadenskästchen −1, beide Spalten getrennt gezählt und addiert.
   * Hohe Schmerztoleranz lässt je Stufe ein Kästchen pro Spalte unberücksichtigt (S. 116), Niedrige Schmerztoleranz
   * zählt schon je 2 Kästchen (S. 120). Überzählige Kästchen zählen nicht mit.
   *
   * @return 0 oder negativ
   */
  public int getWoundModifier() {
    return getColumnWoundModifier(Math.min(getPhysicalDamage(), getPhysicalBoxes()))
        + getColumnWoundModifier(getStunDamage());
  }

  /**
   * Verletzungsmodifikator einer einzelnen Spalte bei {@code damagedBoxes} gefüllten Kästchen, siehe {@link
   * #getWoundModifier()}. Dient auch der Beschriftung der Kästchen im Zustand-Tab.
   *
   * @return 0 oder negativ
   */
  public int getColumnWoundModifier(int damagedBoxes) {
    int ignored = qualityLevel(QualityBonusType.HIGH_PAIN_TOLERANCE);
    int step = hasQualityBonus(QualityBonusType.LOW_PAIN_TOLERANCE)
        ? BOXES_PER_WOUND_MODIFIER_LOW_PAIN_TOLERANCE : BOXES_PER_WOUND_MODIFIER;
    return -(Math.max(0, damagedBoxes - ignored) / step);
  }

  /** Höchste Stufe einer gewählten Quality mit diesem Bonus, 0 ohne. */
  private int qualityLevel(@NonNull QualityBonusType bonus) {
    return qualities.stream()
        .filter(q -> q.data().bonuses().contains(bonus))
        .mapToInt(q -> q.data().level())
        .max()
        .orElse(0);
  }

  // --- Kontakte ---

  public @NonNull List<Contact> getContacts() {
    return Collections.unmodifiableList(contacts);
  }

  /**
   * Fügt einen neuen Kontakt hinzu. Connection und Loyalty starten auf {@link
   * Contact#MIN_RATING} (siehe {@link Contact#Contact(String, String)}). Namensduplikate sind
   * bewusst erlaubt (zwei Kontakte können denselben Namen tragen) — {@link
   * #removeContact(Contact)} arbeitet deshalb über die Objektreferenz statt über den Namen.
   */
  public void addContact(@NonNull String name, @Nullable String description) {
    contacts.add(new Contact(name, description));
  }

  public void removeContact(@NonNull Contact contact) {
    contacts.remove(contact);
  }

  /**
   * BP-Kosten aller Kontakte zusammen: (Connection + Loyalty) × 1 Punkt je Kontakt, aus dem
   * allgemeinen Budget, ohne Freibetrag (die ursprünglich angenommene CHA×2-Freimenge ist im
   * C#-Original nur eine standardmäßig deaktivierte Hausregel, siehe
   * {@code docs/kontakte_analyse.md}). Wird nur während der Erstellung angezeigt/geprüft — im
   * Karrieremodus bleiben Kontakte kostenlos änderbar (SR4-Grundregelwerk: Connections werden
   * nicht mit Karma gekauft, siehe {@code docs/kontakte_java_analyse.md}).
   */
  public int getContactBP() {
    return contacts.stream()
        .mapToInt(c -> (c.getConnection() + c.getLoyalty()) * CONTACT_BP_PER_POINT)
        .sum();
  }

  // --- Zauber ---

  public @NonNull List<SpellData> getSpells() {
    return Collections.unmodifiableList(spells);
  }

  /**
   * Lernt einen Zauber aus dem GRW-Kern-Katalog ({@link SpellDataLoader}). Ein Zauber kann pro
   * Charakter nur einmal gelernt werden (analog {@link #addKnowledgeSkill(String,
   * KnowledgeSkillCategory)}) — anders als bei Kontakten sind hier keine Namensduplikate erlaubt, da
   * ein Zauber bereits durch seinen Namen fachlich eindeutig ist.
   *
   * @throws IllegalArgumentException wenn der Zauber bereits gelernt ist
   */
  public void addSpell(@NonNull SpellData data) {
    boolean duplicate = spells.stream().anyMatch(s -> s.name().equals(data.name()));
    if (duplicate) {
      throw new IllegalArgumentException("Zauber \"%s\" ist bereits gelernt".formatted(data.name()));
    }
    spells.add(data);
  }

  public void removeSpell(@NonNull String name) {
    spells.removeIf(s -> s.name().equals(name));
  }

  /**
   * BP-Kosten aller gelernten Zauber: fixer Preis {@value #SPELL_BP_COST} BP pro Zauber
   * (SR4-Grundregelwerk), unabhängig von Kategorie oder Kraftstufe.
   */
  public int getSpellBP() {
    return spells.size() * SPELL_BP_COST;
  }

  /** Karma-Kosten im Karrieremodus, um einen weiteren Zauber zu lernen — fixer Preis, keine Staffelung. */
  public int getSpellKarmaCost() {
    return SPELL_KARMA_COST;
  }

  /**
   * Prüft, ob ein weiterer Zauber während der Charaktererschaffung gelernt werden darf: die
   * Gesamtzahl gelernter Zauber darf das Doppelte des höchsten Ratings von Spruchzauberei/
   * Ritualzauberei nicht überschreiten (SR4-Grundregelwerk). Im Karrieremodus gilt dieser
   * Erstellungs-Cap nicht.
   */
  public boolean canAddSpellAtCreation() {
    if (created) {
      return true;
    }
    return spells.size() < SPELL_CAP_MULTIPLIER * highestSpellcastingRating();
  }

  private int highestSpellcastingRating() {
    Skill spellcasting = getSkill(SPELLCASTING_SKILL);
    Skill ritual = getSkill(RITUAL_SPELLCASTING_SKILL);
    int spellcastingRating = spellcasting != null ? spellcasting.getRating() : 0;
    int ritualRating = ritual != null ? ritual.getRating() : 0;
    return Math.max(spellcastingRating, ritualRating);
  }

  // --- Komplexe Formen ---

  public @NonNull List<ComplexForm> getComplexForms() {
    return Collections.unmodifiableList(complexForms);
  }

  /**
   * Lernt eine Komplexe Form aus dem GRW-Kern-Katalog ({@link ComplexFormDataLoader}). Wird laut
   * SR4-Grundregelwerk immer mit Stufe 1 gelernt (siehe {@link ComplexForm#MIN_RATING}). Eine Komplexe
   * Form kann pro Charakter nur einmal gelernt werden (analog {@link #addSpell(SpellData)}).
   *
   * @throws IllegalArgumentException wenn die Komplexe Form bereits gelernt ist
   */
  public void addComplexForm(@NonNull ComplexFormData data) {
    boolean duplicate = complexForms.stream().anyMatch(cf -> cf.getName().equals(data.name()));
    if (duplicate) {
      throw new IllegalArgumentException(
          "Komplexe Form \"%s\" ist bereits gelernt".formatted(data.name()));
    }
    complexForms.add(new ComplexForm(data));
  }

  public void removeComplexForm(@NonNull String name) {
    complexForms.removeIf(cf -> cf.getName().equals(name));
  }

  /** BP-Kosten aller gelernten Komplexen Formen: Rating × {@link ComplexForm#getBpCostPerRating()}. */
  public int getComplexFormBP() {
    return complexForms.stream().mapToInt(cf -> cf.getRating() * cf.getBpCostPerRating()).sum();
  }

  /**
   * Prüft, ob das Rating einer Komplexen Form um 1 gesteigert werden darf (bzw., bei einer noch nicht
   * gelernten Form, ob sie überhaupt gelernt werden darf): die neue Stufe darf das aktuelle
   * Resonanzattribut nicht überschreiten (SR4-Grundregelwerk: Stufe einer Komplexen Form höchstens
   * Resonanz). Anders als die reinen
   * Erstellungs-Caps bei Fertigkeiten gilt dieser Cap **durchgehend**, auch im Karrieremodus, da er
   * keine Erschaffungsregel ist, sondern eine grundsätzliche Grenze. Maßgeblich ist der durch
   * Essenzverlust ggf. reduzierte Resonanzwert ({@link #getEffectiveMagicOrResonanceValue()}).
   */
  public boolean canRaiseComplexForm(@NonNull ComplexForm complexForm) {
    return hasResonance()
        && complexForm.getRating() + 1 <= getEffectiveMagicOrResonanceValue();
  }

  /**
   * Ob bei der Erschaffung noch eine Komplexe Form gelernt werden darf: zu Spielbeginn höchstens doppelt so viele wie
   * der natürliche Logikwert, unabhängig von ihrer Stufe (SR4-Grundregelwerk S. 112). Im Karrieremodus ohne Grenze.
   */
  public boolean canAddComplexFormAtCreation() {
    return created
        || complexForms.size() < COMPLEX_FORM_CAP_MULTIPLIER * getAttribute(AttributeType.LOG).getValue();
  }

  /** Wie {@link #canRaiseComplexForm(ComplexForm)}, aber für den Erwerb einer noch nicht gelernten Form (Stufe 1). */
  public boolean canAddComplexForm() {
    return hasResonance() && getEffectiveMagicOrResonanceValue() >= ComplexForm.MIN_RATING;
  }

  // --- Ausrüstung & Nuyen-Budget ---

  /**
   * Die Ausrüstung des Charakters (Cyber-/Bioware, Rüstung, Waffen, Lebensstil) — immer vorhanden,
   * Aufrufer kaufen und entfernen direkt dort (analog {@link #getBiography()}).
   */
  public @NonNull Equipment getEquipment() {
    return equipment;
  }

  /**
   * Ressourcengeld für Ausrüstungskäufe bei der Erschaffung: Ressourcen-BP × {@value #NUYEN_PER_BP}
   * ¥. SR4-Grundregelwerk S. 110: 5.000 ¥ Ausrüstung je GP, höchstens 50 GP (250.000 ¥).
   */
  public int getResourceNuyen() {
    return nuyenBP * NUYEN_PER_BP;
  }

  /**
   * Nicht ausgegebenes Ressourcengeld — eine reine Ableitung ohne Buchungsliste, Entfernen eines
   * Gegenstands "erstattet" also automatisch. Darf zwischenzeitlich negativ sein; ein Abschluss ist
   * dann nicht möglich (siehe {@link #getFinalizeProblems()}).
   */
  public int getRemainingNuyen() {
    return getResourceNuyen() - equipment.getSpentNuyen();
  }

  // --- Abschluss der Charaktererschaffung ---

  /**
   * Gründe, die einen Abschluss der Charaktererschaffung verhindern; leer, wenn abgeschlossen werden
   * darf. Derzeit nur negatives Restgeld (GRW S. 111: Fehlendes Geld muss vor Ende der Erschaffung
   * ausgeglichen werden). Essenz ≤ 0 ist
   * bewusst kein Hinderungsgrund, sondern bleibt eine weiche Warnung (Nutzerentscheidung, siehe
   * {@link #isEssenceBelowCreationMinimum()}).
   */
  public @NonNull List<String> getFinalizeProblems() {
    List<String> problems = new ArrayList<>();
    int remaining = getRemainingNuyen();
    if (remaining < 0) {
      problems.add("Es wurden %d ¥ mehr ausgegeben, als an Ressourcen zur Verfügung stehen."
          .formatted(-remaining));
    }
    return problems;
  }

  /**
   * Schließt die Charaktererschaffung ab und wechselt in den Karrieremodus. Das Startkapital wird nach
   * SR4-Grundregelwerk S. 114 aus dem Lebensstil erwürfelt ({@link
   * de.shalorian.chummer.model.character.lifestyle.LifestyleLevel#startingCapital(int, int)}) und als
   * erster Nuyen-Log-Eintrag gebucht. Übriges Ressourcengeld verfällt (GRW S. 114: nicht ausgegebenes
   * Ressourcengeld geht nicht ins Spiel über) und fließt nur als Bonus in den Wurf ein. Ersetzt die frühere Controller-Logik, die das volle
   * Ressourcengeld als Startkapital gebucht hat. Die Ressourcen-BP werden wie bisher auf 0 gesetzt.
   *
   * @param diceSum vom Spieler bzw. der App gewürfelte Summe der Lebensstil-W6
   * @return das gebuchte Startkapital in Nuyen
   * @throws IllegalStateException    wenn bereits abgeschlossen oder {@link #getFinalizeProblems()}
   *                                  nicht leer ist
   * @throws IllegalArgumentException bei einer mit der Würfelzahl unmöglichen Würfelsumme
   */
  public int finalizeCreation(int diceSum) {
    if (created) {
      throw new IllegalStateException("Charaktererschaffung ist bereits abgeschlossen");
    }
    List<String> problems = getFinalizeProblems();
    if (!problems.isEmpty()) {
      throw new IllegalStateException(String.join(" ", problems));
    }
    int startingCapital = equipment.getLifestyle().getLevel().startingCapital(diceSum, getRemainingNuyen());
    nuyenLog.add(new LogEntry(
        "Startkapital (Lebensstil " + equipment.getLifestyle().getLevel().getDisplayName() + ")",
        startingCapital, LocalDate.now(ZoneId.systemDefault())));
    nuyenBP = 0;
    created = true;
    return startingCapital;
  }

  // --- BP ---
  public int getSpentBP() {
    int attrBP = getAttributeBP();

    int skillBP = 0;
    Set<String> counted = new HashSet<>();
    for (Skill skill : skills.values()) {
      String g = skill.getGroupName();
      if (g != null && !decoupledGroups.contains(g)) {
        // Gekoppelte Gruppe: einmal mit Gruppenpreis (10 BP/Rating)
        if (counted.add(g)) {
          skillBP += getGroupRating(g) * 10;
        }
      } else {
        // Entkoppelt oder kein Gruppen-Skill: Einzelpreis (4 BP/Rating)
        skillBP += skill.getBpCost(isSkillCostDoubled(skill.getCategory()));
      }
    }

    int qualityBP = qualities.stream()
        .filter(q -> q.source() == QualitySource.SELECTED && !q.gratis())
        .mapToInt(q -> q.data().bp())
        .sum();

    return metatype.bpCost() + attrBP + skillBP + nuyenBP + qualityBP
        + getKnowledgeSkillOverflowBP() + getSpecialAttributeBP()
        + getActiveSkillSpecializationBP() + getContactBP() + getSpellBP() + getComplexFormBP();
  }

  public int getRemainingBP() {
    return TOTAL_BP - getSpentBP();
  }

  // --- Karma & Nuyen Log ---

  public boolean isCreated() {
    return created;
  }

  public void setCreated(boolean v) {
    this.created = v;
  }

  public void addKarmaEntry(@NonNull LogEntry entry) {
    karmaLog.add(entry);
  }

  public void addNuyenEntry(@NonNull LogEntry entry) {
    nuyenLog.add(entry);
  }

  public @NonNull List<LogEntry> getKarmaLog() {
    return Collections.unmodifiableList(karmaLog);
  }

  public @NonNull List<LogEntry> getNuyenLog() {
    return Collections.unmodifiableList(nuyenLog);
  }

  public int getKarmaTotal() {
    return karmaLog.stream().mapToInt(LogEntry::amount).sum();
  }

  public int getNuyenTotal() {
    return nuyenLog.stream().mapToInt(LogEntry::amount).sum();
  }
}