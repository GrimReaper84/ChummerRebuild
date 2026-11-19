package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowerEffect;
import de.shalorian.chummer.model.character.adept.AdeptPowers;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttributeType;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.condition.CheatSheetItem;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.ImplantEffectType;
import de.shalorian.chummer.model.character.cyberware.Implants;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.MatrixFlag;
import de.shalorian.chummer.model.character.initiation.Initiation;
import de.shalorian.chummer.model.character.magic.Foci;
import de.shalorian.chummer.model.character.magic.FocusType;
import de.shalorian.chummer.model.character.magic.MentorData;
import de.shalorian.chummer.model.character.magic.MentorDataLoader;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.Movement;
import de.shalorian.chummer.model.character.gear.SkillsoftStatus;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.character.reputation.Reputation;
import de.shalorian.chummer.model.character.reputation.ReputationScore;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualityRules;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import de.shalorian.chummer.model.character.situation.SituationKind;
import de.shalorian.chummer.model.character.situation.SituationalBonus;
import de.shalorian.chummer.model.character.situation.SituationalPool;
import de.shalorian.chummer.model.character.situation.SituationalPools;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import de.shalorian.chummer.model.character.skill.SkillDataLoader;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.spirit.Spirits;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.model.character.technomancer.ComplexFormData;
import de.shalorian.chummer.model.character.technomancer.LivingPersona;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.critter.CritterMetatype;
import de.shalorian.chummer.model.critter.CritterTraits;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.utils.Numbers;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private static final String PERCEPTION_SKILL = "Wahrnehmung";
  /** Talentsofts (GRW S. 377). */
  private static final String ACTIVESOFT = "Aktionssoft";
  private static final String LINGUASOFT = "Linguasoft";
  private static final String KNOWSOFT = "Wissenssoft";
  /** Fertigkeit für das Sprinten (GRW S. 177). */
  private static final String RUNNING_SKILL = "Laufen";
  private static final String ASSENSING_SKILL = "Askennen";
  private static final String ASTRAL_COMBAT_SKILL = "Astralkampf";

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
  private final @NonNull AdeptPowers adeptPowers = new AdeptPowers(this);
  private final @NonNull Initiation initiation = new Initiation(this);
  private final @NonNull Spirits spirits = new Spirits(this);
  private final @NonNull Foci foci = new Foci(this);
  private final @NonNull Implants implants = new Implants(this);
  private final @NonNull Equipment equipment = new Equipment(this);
  private final @NonNull Reputation reputation = new Reputation();
  private final @NonNull RuleOptions ruleOptions = new RuleOptions();
  /** Spielleiter-Modus, siehe {@link #isGmMode()}; bewusst nicht in der Speicherdatei. */
  private boolean gmMode;
  private final List<LogEntry> karmaLog = new ArrayList<>();
  private final List<LogEntry> nuyenLog = new ArrayList<>();
  private final @NonNull CharacterBiography biography = new CharacterBiography();
  private @NonNull String name;
  private @NonNull MetatypeData metatype;
  private @NonNull SpecialAttribute edge;
  private @Nullable SpecialAttribute magicOrResonance;
  private @Nullable CritterTraits critter;
  private @NonNull Tradition tradition = Tradition.HERMETIC;
  private boolean created = false;
  private int nuyenBP = 0;
  /** Eingetragener Schaden im Zustandsmonitor (Kästchen), siehe {@link #getPhysicalDamage()}. */
  private int physicalDamage = 0;
  /** Ausgegebene Edgepunkte seit dem letzten Auffrischen (GRW S. 98/99). */
  private int edgeSpent = 0;
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

  /** Bewegungsweiten Gehen/Laufen des Metatyps (GRW S. 177). */
  public @NonNull Movement getMovement() {
    return metatype.movement();
  }

  /**
   * Pool zum Sprinten: Laufen + Stärke mit den üblichen Regeln einer Fertigkeitsprobe; jeder Erfolg verlängert die
   * Laufen-Weite um 2 m (GRW S. 177). Leer, wenn die Probe nicht erlaubt ist.
   */
  public @NonNull Optional<DicePool> getSprintPool() {
    return getSkillDicePool(Objects.requireNonNull(getSkill(RUNNING_SKILL), RUNNING_SKILL));
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
   * Maximum gesteigert werden. Im Karrieremodus und im Spielleiter-Modus gelten diese Erstellungs-Caps nicht.
   */
  public boolean canRaiseAttributeAtCreation(@NonNull AttributeType type) {
    if (created || gmMode) {
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
   * Wirksamer Attributwert inkl. aller modellierten Modifikatoren: Adeptenkräfte Gesteigertes körperliches Attribut
   * und Gesteigerte Reflexe (Reaktion, GRW S. 230) sowie Attributboni von Implantaten (S. 389, 393, 394), zusammen
   * höchstens bis zum augmentierten Maximum (S. 91), danach die
   * Panzerungsbehinderung auf Geschicklichkeit und Reaktion (siehe {@link Equipment#getArmorEncumbrancePenalty()}).
   * Der investierte Wert ({@link CharacterAttribute#getValue()}) bleibt unverändert, BP-Kosten richten sich weiter
   * nach ihm (analog {@link #getEffectiveMagicOrResonanceValue()}). Die Behinderung hängt selbst an der verstärkten
   * Konstitution; Konstitution wird von ihr nicht beeinflusst, daher entsteht kein Zyklus.
   *
   * <p>Ein durch Behinderung reduzierter Wert fällt nicht unter {@value
   * #MIN_ENCUMBERED_ATTRIBUTE_VALUE}. Das GRW nennt dafür keine Untergrenze; die Grenze ist eine
   * vorläufige Nutzerentscheidung bis zu einem Beleg in weiteren Regelwerken (siehe {@code
   * docs/plaene/archiv/nuyen_ruestung_plan.md} Abschnitt 13).
   */
  public int getAugmentedAttributeValue(@NonNull AttributeType type) {
    int natural = getAttribute(type).getValue();
    int boost = adeptPowers.level(AdeptPowerEffect.IMPROVED_ATTRIBUTE, type.name())
        + (type == AttributeType.REA ? adeptPowers.level(AdeptPowerEffect.IMPROVED_REFLEXES) : 0)
        + implants.level(ImplantEffectType.ATTRIBUTE, type.name());
    int value = boost == 0 ? natural : Math.min(natural + boost, getAugmentedMaximum(type));
    if (type != AttributeType.AGI && type != AttributeType.REA) {
      return value;
    }
    int penalty = equipment.getArmorEncumbrancePenalty();
    return penalty == 0 ? value : Math.max(MIN_ENCUMBERED_ATTRIBUTE_VALUE, value + penalty);
  }

  /**
   * Karma-Kosten im Karrieremodus, um ein Attribut um 1 zu steigern: neuer Wert × 5. Hat das Attribut Stufen von
   * Gesteigertes körperliches Attribut, zählen sie mit (GRW S. 230: Kosten nach dem Gesamtwert einschließlich der
   * magischen Steigerung).
   */
  public int getAttributeKarmaCost(@NonNull AttributeType type) {
    return getAttribute(type).getKarmaCost()
        + adeptPowers.level(AdeptPowerEffect.IMPROVED_ATTRIBUTE, type.name()) * 5;
  }

  // --- Sonderattribute (Edge/Magic/Resonance) & Essenz ---

  public @NonNull SpecialAttribute getEdge() {
    return edge;
  }

  // --- Edgepunkte (nur Karrieremodus) ---

  /** Verfügbare Edgepunkte: Edgeattribut minus ausgegebene (GRW S. 98/99). */
  public int getEdgePoints() {
    return edge.getValue() - getEdgeSpent();
  }

  /**
   * Ausgegebene Edgepunkte, höchstens das Attribut: die einzige Stelle der Grenze, weil das Attribut auch von außen
   * sinken kann (Verheizen, Gabe Glück entfernt).
   */
  public int getEdgeSpent() {
    return Math.min(edgeSpent, edge.getValue());
  }

  /** Gibt einen Edgepunkt aus (S. 98). */
  public void spendEdge() {
    requireCareer("Edge ausgeben");
    if (getEdgePoints() == 0) {
      throw new IllegalStateException("Keine Edgepunkte mehr");
    }
    edgeSpent = getEdgeSpent() + 1;
  }

  /** Gewinnt einen Edgepunkt zurück, höchstens bis zum vollen Attribut (S. 99). */
  public void regainEdge() {
    requireCareer("Edge zurückgewinnen");
    if (getEdgeSpent() == 0) {
      throw new IllegalStateException("Edgepunkte schon voll");
    }
    edgeSpent = getEdgeSpent() - 1;
  }

  /** Füllt die Edgepunkte auf das volle Attribut auf (S. 99, z.B. zu Beginn eines Spielabends). */
  public void refreshEdge() {
    requireCareer("Edge auffrischen");
    edgeSpent = 0;
  }

  /**
   * Verheizt einen Punkt Edge: das Attribut sinkt dauerhaft um 1 (S. 99), siehe {@link SpecialAttribute#burn()};
   * ausgegebene Punkte bleiben ausgegeben, soweit das neue Attribut reicht.
   */
  public void burnEdge() {
    requireCareer("Edge verheizen");
    edge.burn();
  }

  /** Setzt die ausgegebenen Edgepunkte beim Laden. */
  public void loadEdgeSpent(int spent) {
    edgeSpent = Math.max(0, spent);
  }

  private void requireCareer(@NonNull String action) {
    if (!created) {
      throw new IllegalStateException(action + " geht nur im Karrieremodus");
    }
  }

  /** Critter-Essenz aus dem Katalog; Würfelangaben wie „2W6 − 1“ (Vampir) gelten als 6, der Bogen zeigt den Text. */
  private static @NonNull BigDecimal critterEssence(@NonNull String essence) {
    String text = essence.trim().replace(',', '.');
    return text.matches("\\d+(\\.\\d+)?") ? new BigDecimal(text) : BASE_ESSENCE;
  }

  /** Was diesen Charakter zum Critter macht, {@code null} bei allen anderen (GRW S. 337–343). */
  public @Nullable CritterTraits getCritterTraits() {
    return critter;
  }

  /**
   * Macht den Charakter zum Critter, vor dem Setzen der Werte (auch beim Laden). Erwachte Critter haben Magie ohne die Gabe Zauberer (S. 337), in der Spanne ±3
   * wie die übrigen Attribute.
   */
  public void setCritterTraits(@NonNull CritterTraits traits) {
    this.critter = traits;
    // Fertigkeitsmaxima des Critters (Katalogwert + 3, sonst 3); hält auch über spätere Gaben-Änderungen
    skills.values().forEach(this::applyQualityLimits);
    int magic = traits.getData().magic();
    if (magic > 0) {
      AttributeRange range = CritterMetatype.range(magic);
      magicOrResonance = new SpecialAttribute(SpecialAttributeType.MAGIC, range.min(), range.max(), false);
      magicOrResonance.setValue(magic);
    }
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
    if (critter != null) {
      return critterEssence(critter.getData().essence());
    }
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
   * und Karrieremodus (kein modusabhängiger Sonderfall wie im C#-Original). Critter: kein Abzug, ihre Magie steht
   * fest im Katalog (GRW S. 337).
   */
  public int getEssencePenalty() {
    if (critter != null) {
      return 0;
    }
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
    } else if (!magic && !resonance && (critter == null || critter.getData().magic() == 0)) {
      magicOrResonance = null;
      initiation.clear();
    }
  }

  /**
   * Boni, die nur in bestimmten Lagen gelten: aktive Foki (Würfel = Kraft, S. 233/234), Schutzpatron (S. 234–237) und
   * die situativen Adeptenkräfte (S. 230/231).
   */
  public @NonNull List<SituationalBonus> getSituationalBonuses() {
    List<SituationalBonus> bonuses = new ArrayList<>();
    for (Gear focus : foci.getActive()) {
      FocusType type = Objects.requireNonNull(focus.getData().focusType());
      SituationKind kind = type.getSituation();
      if (kind != null) {
        bonuses.add(new SituationalBonus(focus.getName(), kind, focus.getExtra(), focus.getRating(), true));
      }
    }
    addMentorBonuses(bonuses);
    for (AdeptPower power : adeptPowers.getPowers()) {
      SituationKind kind = power.getData().effect().getSituation();
      if (kind != null && power.getLevel() > 0) {
        bonuses.add(new SituationalBonus(power.getData().name(), kind, "", power.getLevel(), false));
      }
    }
    return bonuses;
  }

  /**
   * Boni des Schutzpatrons aus der Gabe (GRW S. 117, 234–237); nichts ohne die Gabe oder bei einer Auswahl, die nicht
   * (mehr) im Katalog steht.
   */
  private void addMentorBonuses(@NonNull List<SituationalBonus> bonuses) {
    CharacterQuality gift = qualities.stream().filter(q -> q.data().selection() == QualitySelection.MENTOR)
        .findFirst().orElse(null);
    MentorData mentor = gift == null ? null : MentorDataLoader.findByChoice(gift.choice());
    if (gift == null || mentor == null) {
      return;
    }
    mentor.bonusesFor(gift.choice()).forEach(b ->
        bonuses.add(new SituationalBonus(mentor.name(), b.kind(), b.detail(), b.dice(), false)));
  }

  /** Situative Würfelpools je Anlass ({@link SituationalPools}). */
  public @NonNull List<SituationalPool> getSituationalPools() {
    return SituationalPools.of(this, getSituationalBonuses());
  }

  /** Foki: Bindung und Aktivierung (GRW S. 112, 233/234). */
  public @NonNull Foci getFoci() {
    return foci;
  }

  /** Gebundene und herbeigerufene Geister, registrierte und kompilierte Sprites (GRW S. 112/113, 222, 281). */
  public @NonNull Spirits getSpirits() {
    return spirits;
  }

  /** Initiation bzw. Wandlung: Grad, Metamagien bzw. Echos und das dadurch höhere Maximum (GRW S. 232, 284). */
  public @NonNull Initiation getInitiation() {
    return initiation;
  }

  // --- Skills ---

  public @NonNull List<Skill> getSkills() {
    return List.copyOf(skills.values());
  }

  public @Nullable Skill getSkill(@NonNull String name) {
    return skills.get(name);
  }

  /** Gelernte Instanz einer exotischen Grundfertigkeit für dieses Ziel (Groß-/Kleinschreibung egal), sonst null. */
  public @Nullable Skill getExoticSkill(@NonNull String base, @NonNull String target) {
    return skills.values().stream()
        .filter(s -> s.getBaseName().equals(base) && target.trim().equalsIgnoreCase(s.getExoticTarget()))
        .findFirst().orElse(null);
  }

  /** Warum für {@code base} kein Ziel {@code target} angelegt werden kann, oder {@code null}. */
  public @Nullable String getExoticSkillProblem(@NonNull String base, @NonNull String target) {
    Skill template = skills.get(base);
    if (template == null || !template.isExoticTemplate()) {
      return "Keine exotische Grundfertigkeit: " + base;
    }
    if (target.isBlank()) {
      return "Waffe bzw. Fahrzeug fehlt";
    }
    return getExoticSkill(base, target) != null ? "Für „%s“ schon vorhanden".formatted(target.trim()) : null;
  }

  /**
   * Legt eine Exotische Fertigkeit für eine Waffe bzw. ein Fahrzeug an (GRW S. 148, 155), mit Wert 0. Sie steht
   * direkt hinter ihrer Grundfertigkeit; GP- bzw. Karmakosten entstehen erst mit dem Wert.
   *
   * @throws IllegalArgumentException mit dem Grund aus {@link #getExoticSkillProblem}
   */
  public @NonNull Skill addExoticSkill(@NonNull String base, @NonNull String target) {
    String problem = getExoticSkillProblem(base, target);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    Skill instance = new Skill(Objects.requireNonNull(skills.get(base)), target);
    insertAfterBase(instance);
    // Beim Laden stehen die Qualities schon fest: Talentiert auf dieser Instanz muss das Maximum sofort heben.
    applyQualityLimits(instance);
    return instance;
  }

  /** Stellt eine gespeicherte Exotik-Instanz wieder her; unbekannte Grundfertigkeiten oder doppelte Ziele: null. */
  public @Nullable Skill loadExoticSkill(@NonNull String base, @NonNull String target) {
    return getExoticSkillProblem(base, target) == null ? addExoticSkill(base, target) : null;
  }

  /**
   * Warum die Exotik-Instanz nicht entfernt werden kann, oder {@code null}: im Karrieremodus wird nur gesteigert, und
   * eine Gabe bzw. ein Handicap mit ihr als Wahl (Talentiert, Inkompetenz) würde sonst ins Leere zeigen.
   */
  public @Nullable String getExoticSkillRemovalProblem(@NonNull Skill skill) {
    if (skill.getExoticTarget() == null) {
      return "Keine Exotik-Instanz: " + skill.getName();
    }
    if (created && !gmMode) {
      return "Im Karrieremodus wird nur gesteigert";
    }
    return getSelectedQualities().stream()
        .filter(q -> q.choice().equals(skill.getName()))
        .findFirst()
        .map(q -> "Zuerst „%s“ entfernen, es bezieht sich auf diese Fertigkeit".formatted(q.data().name()))
        .orElse(null);
  }

  /**
   * Entfernt eine Exotik-Instanz samt Wert.
   *
   * @throws IllegalStateException mit dem Grund aus {@link #getExoticSkillRemovalProblem}
   */
  public void removeExoticSkill(@NonNull Skill skill) {
    String problem = getExoticSkillRemovalProblem(skill);
    if (problem != null) {
      throw new IllegalStateException(problem);
    }
    skills.remove(skill.getName());
  }

  /** Ordnet die Fertigkeiten so, dass die Instanz alphabetisch hinter ihrer Grundfertigkeit steht. */
  private void insertAfterBase(@NonNull Skill instance) {
    List<Skill> ordered = new ArrayList<>(skills.values());
    int index = ordered.indexOf(skills.get(instance.getBaseName())) + 1;
    while (index < ordered.size() && ordered.get(index).getBaseName().equals(instance.getBaseName())
        && ordered.get(index).getName().compareTo(instance.getName()) < 0) {
      index++;
    }
    ordered.add(index, instance);
    skills.clear();
    ordered.forEach(sk -> skills.put(sk.getName(), sk));
  }

  /**
   * Prüft, ob eine Steigerung dieser aktiven Fertigkeit um 1 während der Charaktererschaffung
   * regelkonform wäre: entweder maximal eine Fertigkeit auf Wert 6, oder maximal zwei
   * Fertigkeiten auf Wert 5 — alle übrigen Fertigkeiten bleiben auf maximal 4 begrenzt, und
   * beide Sonderfälle schließen sich gegenseitig aus. Gekoppelte Gruppen-Skills (die gemeinsam
   * über {@link #raiseGroup(String)} gesteigert werden) zählen dabei nicht mit, da für Gruppen
   * ein eigener Cap gilt. Im Karrieremodus und im Spielleiter-Modus gilt dieser Erstellungs-Cap nicht.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: "eine andere Fertigkeit als diese"
  public boolean canRaiseSkillAtCreation(@NonNull Skill skill) {
    if (created || gmMode) {
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
   *   <li>Magische Fertigkeiten nur für Zauberer und Magieradepten (SR4-Grundregelwerk S. 148). Ausnahme Arcana
   *       (S. 148), die auch Mundane lernen dürfen. Askennen und Astralkampf setzen astrale Wahrnehmung voraus
   *       (S. 108, 149): Zauberer haben sie, Adepten und Magieradepten nur mit der Kraft Astrale Wahrnehmung
   *       (siehe {@link #hasAstralPerception()}).</li>
   *   <li>Resonanzfertigkeiten nur für Technomancer (S. 151, Gabe S. 117).</li>
   *   <li>Keine Fertigkeit, für die der Charakter Inkompetenz gewählt hat (S. 120).</li>
   * </ul>
   * Die Sperre gilt auch im Karrieremodus, da sie keine Erschaffungsregel, sondern eine
   * grundsätzliche Zugriffsvoraussetzung ist.
   */
  private boolean isSkillAccessible(@NonNull Skill skill) {
    return !hasQualityChoice(QualityBonusType.INCOMPETENT, skill.getName())
        && (critterMagicSkill(skill) || isSkillAccessible(skill, isMagician(), hasAstralPerception(), hasResonance()));
  }

  /**
   * Erwachte Critter haben ihre magischen Fertigkeiten auch ohne Gabe (GRW S. 337, wie im C#-Original); ohne die Gabe
   * Zauberer dürfen sie damit nur keine Zauber wirken, beschwören oder projizieren, das zeigt der Bogen als Hinweis.
   */
  private boolean critterMagicSkill(@NonNull Skill skill) {
    return critter != null && critter.getData().magic() > 0 && skill.getCategory() == SkillCategory.MAGICAL;
  }

  private static boolean isSkillAccessible(@NonNull Skill skill, boolean magician, boolean astral,
      boolean resonance) {
    return switch (skill.getCategory()) {
      case MAGICAL -> switch (skill.getName()) {
        case ARCANA_SKILL -> true;
        case ASSENSING_SKILL, ASTRAL_COMBAT_SKILL -> astral;
        default -> magician;
      };
      case RESONANCE -> resonance;
      default -> true;
    };
  }

  /**
   * Ob der Charakter astral wahrnehmen kann: Zauberer immer (GRW S. 118), Adepten und Magieradepten nur mit der
   * Kraft Astrale Wahrnehmung (S. 116, 229, 230). Schaltet Askennen und Astralkampf frei.
   */
  public boolean hasAstralPerception() {
    return astralPerception(hasAstralAccess(), hasAdeptPowers(), null);
  }

  /**
   * Gemeinsame Regel für {@link #hasAstralPerception()} und die Vorschau beim Entfernen: Astralzugang (Zauberer) oder
   * Adeptengabe mit der Kraft Astrale Wahrnehmung, wobei {@code without} nicht mitzählt.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: alle außer genau dieser Kraft
  private boolean astralPerception(boolean astralAccess, boolean adeptGift, @Nullable AdeptPower without) {
    return astralAccess || (adeptGift && adeptPowers.getPowers().stream()
        .anyMatch(p -> p != without && p.getData().effect() == AdeptPowerEffect.ASTRAL_PERCEPTION));
  }

  /**
   * Vollständige Steigerungs-Prüfung für eine aktive Fertigkeit: exotische Grundfertigkeiten nie (gelernt wird je
   * Waffe bzw. Fahrzeug, GRW S. 148); im Spielleiter-Modus sonst immer; ansonsten kombiniert sie den Erstellungs-Cap
   * aus {@link #canRaiseSkillAtCreation(Skill)} mit der Zugangsvoraussetzung aus
   * {@link #isSkillAccessible(Skill)}.
   */
  public boolean canRaiseSkill(@NonNull Skill skill) {
    return !skill.isExoticTemplate() && (gmMode || (isSkillAccessible(skill) && canRaiseSkillAtCreation(skill)));
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
   * String)}). Exotische Fertigkeiten sind schon auf ihre Waffe bzw. ihr Fahrzeug festgelegt und haben keine
   * Spezialisierung (GRW S. 148).
   */
  public boolean canSpecializeSkill(@NonNull Skill skill) {
    return !skill.isExotic() && skill.getRating() >= MIN_RATING_FOR_SPECIALIZATION
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
   * Im Karrieremodus und im Spielleiter-Modus gilt dieser Erstellungs-Cap nicht (dort bis
   * {@link Skill#MAX_RATING}).
   */
  public boolean canRaiseGroupAtCreation(@NonNull String groupName) {
    return created || gmMode || getGroupRating(groupName) < MAX_GROUP_RATING_AT_CREATION;
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
    // Doppelte Kosten (Ungebildet usw.) sperren Gruppen auch im Spielleiter-Modus: die BP-Rechnung bildet sie nicht ab.
    return members.stream().noneMatch(s -> isSkillCostDoubled(s.getCategory()))
        && (gmMode || (members.stream().allMatch(this::isSkillAccessible) && canRaiseGroupAtCreation(groupName)));
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
    // Spielleiter-Modus: Metatyp, Voraussetzungen, Ausschlüsse, Erwachen und BP-Grenzen entfallen; Stapelregeln
    // (Schmerz) und belegte Gruppen bei doppelten Kosten bleiben, weil die Rechnung sie sonst nicht abbildet.
    if (!gmMode && !fitsMetatype(rules)) {
      return "Nur für " + String.join(", ", rules.metatypes());
    }
    if (!gmMode && !rules.requires().isEmpty()
        && selected.stream().noneMatch(q -> rules.requires().contains(q.data().name()))) {
      return "Erfordert " + String.join(" oder ", rules.requires());
    }
    Optional<CharacterQuality> conflict = selected.stream()
        .filter(q -> rules.excludes().contains(q.data().name())
            || q.data().rules().excludes().contains(data.name()))
        .findFirst();
    if (!gmMode && conflict.isPresent()) {
      return "Nicht zusammen mit " + conflict.get().data().name();
    }
    if (data.bonuses().contains(QualityBonusType.HIGH_PAIN_TOLERANCE)
        && adeptPowers.level(AdeptPowerEffect.PAIN_RESISTANCE) > 0) {
      return "Nicht zusammen mit der Adeptenkraft Schmerzresistenz";
    }
    String painImplant = implants.getPainToleranceConflict();
    if (data.bonuses().contains(QualityBonusType.HIGH_PAIN_TOLERANCE) && painImplant != null) {
      return "Nicht zusammen mit " + painImplant;
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
    if (gmMode) {
      return null;
    }
    if (created && !ruleOptions.isLateAwakening() && isAwakeningQuality(data)) {
      return "Nur bei der Erschaffung wählbar (S. 313)";
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

  /**
   * Adept, Zauberer, Magieradept und Technomancer: Gaben, die Magie bzw. Resonanz verleihen und laut GRW S. 313 nur
   * bei der Erschaffung gewählt werden dürfen. Für latente Charaktere hebt {@link RuleOptions#isLateAwakening()} das
   * auf.
   */
  private static boolean isAwakeningQuality(@NonNull QualityData data) {
    return data.bonuses().contains(QualityBonusType.GRANTS_MAGIC)
        || data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE);
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
      case MENTOR -> MentorDataLoader.findByChoice(choice) == null ? "Schutzpatron wählen" : null;
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
    locked.forEach(Character::resetLockedSkill);
  }

  /** Setzt eine nicht mehr erlernbare Fertigkeit samt Spezialisierung auf 0. */
  private static void resetLockedSkill(@NonNull Skill skill) {
    skill.setRating(0);
    skill.setSpecialization(null);
  }

  /** Zeile für die Rückfrage, wenn eine Fertigkeit dadurch auf 0 fällt. */
  private static @NonNull String lockedSkillLine(@NonNull Skill skill) {
    return "%s %d → 0 (nicht mehr erlernbar)".formatted(skill.getName(), skill.getRating());
  }

  /**
   * Wendet die rechnerischen Wirkungen aller Qualities an: Magie/Resonanz freischalten, Maximum-Boni
   * (Außergewöhnliches Attribut, Glück), Höchstwert 7 (Talentiert) und gesperrte Fertigkeiten (Inkompetenz, auf
   * 0 gesetzt). Ohne Gabe Adept bzw. Magieradept fallen alle Adeptenkräfte weg.
   */
  private void applyQualityEffects() {
    syncMagicOrResonance();
    adeptPowers.syncWithGifts();
    spirits.syncWithGifts();
    for (CharacterAttribute attribute : attributes.values()) {
      attribute.setMaxBonus(
          hasQualityChoice(QualityBonusType.EXCEPTIONAL_ATTRIBUTE, attribute.getType().name()) ? 1 : 0);
    }
    edge.setMaxBonus(hasQualityBonus(QualityBonusType.LUCKY) ? 1 : 0);
    for (Skill skill : skills.values()) {
      applyQualityLimits(skill);
    }
  }

  /** Höchstwert durch Talentiert (S. 117) und Sperre durch Inkompetenz (S. 120) für eine Fertigkeit. */
  private void applyQualityLimits(@NonNull Skill skill) {
    if (critter != null) {
      skill.setMaxRating(critter.getSkillMaximum(skill));
    } else {
      skill.setMaxRating(hasQualityChoice(QualityBonusType.APTITUDE, skill.getName())
          ? Skill.MAX_RATING + 1 : Skill.MAX_RATING);
    }
    if (hasQualityChoice(QualityBonusType.INCOMPETENT, skill.getName())) {
      resetLockedSkill(skill);
    }
  }

  /** Ob eine Quality des Charakters diese Wirkung hat. */
  public boolean hasQualityBonus(@NonNull QualityBonusType bonus) {
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
    if (!hasQualityBonus(remaining, QualityBonusType.ADEPT_POWERS)) {
      adeptPowers.getPowers().forEach(p -> lines.add("Adeptenkraft " + p.displayName() + " wird mit entfernt"));
    }
    if (!hasQualityBonus(remaining, QualityBonusType.GRANTS_SPELLCASTING)) {
      spirits.getEntries(SpiritKind.SPIRIT).forEach(s -> lines.add("Geist " + s.getName() + " wird mit entfernt"));
    }
    if (!hasQualityBonus(remaining, QualityBonusType.GRANTS_RESONANCE)) {
      spirits.getEntries(SpiritKind.SPRITE).forEach(s -> lines.add("Sprite " + s.getName() + " wird mit entfernt"));
    }
    String initiationLoss = initiation.getLossLine();
    if (initiationLoss != null && !hasQualityBonus(remaining, QualityBonusType.GRANTS_MAGIC)
        && !hasQualityBonus(remaining, QualityBonusType.GRANTS_RESONANCE)) {
      lines.add(initiationLoss);
    }
    List<Skill> locked = getSkillsLockedWithout(removed);
    locked.forEach(s -> lines.add(lockedSkillLine(s)));
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
    boolean astral = astralPerception(hasQualityBonus(remaining, QualityBonusType.ASTRAL_ACCESS),
        hasQualityBonus(remaining, QualityBonusType.ADEPT_POWERS), null);
    return lockedSkills(magician, astral, resonance);
  }

  private @NonNull List<Skill> lockedSkills(boolean magician, boolean astral, boolean resonance) {
    return skills.values().stream()
        .filter(s -> s.getRating() > 0 && !isSkillAccessible(s, magician, astral, resonance))
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
   * ist von beiden Regeln ausgenommen. Im Karrieremodus und im Spielleiter-Modus gilt dieser Erstellungs-Cap nicht.
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: "eine andere Fertigkeit als diese"
  public boolean canRaiseKnowledgeSkillAtCreation(@NonNull KnowledgeSkill skill) {
    if (created || gmMode || skill.isNativeLanguage()) {
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
    if (created || gmMode) {
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
   * Ob eine (neue) Spezialisierung einer aktiven Fertigkeit bezahlbar ist: im Karrieremodus genug Karma, bei der
   * Erschaffung genug BP (Umbenennen einer schon gekauften kostet nichts); im Spielleiter-Modus immer.
   *
   * @param currentSpec die bisherige Spezialisierung, sonst {@code null}
   */
  public boolean canAffordSkillSpecialization(@Nullable String currentSpec) {
    return canAffordSpecialization(currentSpec, getActiveSkillSpecializationBpCost());
  }

  /** Wie {@link #canAffordSkillSpecialization} für Wissensfertigkeiten (BP-Preis aus dem Wissenspool). */
  public boolean canAffordKnowledgeSkillSpecialization(@Nullable String currentSpec) {
    return canAffordSpecialization(currentSpec, getKnowledgeSkillSpecializationBP());
  }

  private boolean canAffordSpecialization(@Nullable String currentSpec, int bpCost) {
    if (gmMode) {
      return true;
    }
    return created ? getKarmaTotal() >= getSpecializationKarmaCost()
        : currentSpec != null || getRemainingBP() >= bpCost;
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
    int powers = adeptPowerDice(skill);
    int implantDice = implantDice(skill);
    int shield = shieldPenalty(isPhysicalAttribute(skill.getLinkedAttribute()));
    String bonusText = (powers == 0 ? "" : " + Adeptenkräfte " + powers)
        + (implantDice == 0 ? "" : " + Implantate " + implantDice)
        + shieldText(shield);
    int bonus = powers + implantDice + shield;
    int rating = getEffectiveSkillRating(skill);
    if (rating > 0) {
      return Optional.of(dicePool(rating + attribute + bonus,
          ratingBreakdown(ratingName(skill), rating, attributeName, attribute) + bonusText + softSuffix(skill)));
    }
    if (!skill.isDefaultable() || isSkillCostDoubled(skill.getCategory())) {
      return Optional.empty();
    }
    return Optional.of(defaultingPool(attributeName, attribute, bonus, bonusText));
  }

  /**
   * Zusatzwürfel aus Implantaten für eine Fertigkeit: Synthacardium für ihre Gruppe (Athletik, S. 393), Pheromone für
   * soziale Fertigkeiten (S. 393), Hochleistungsgelenke für Fertigkeiten mit körperlichem verbundenem Attribut (S. 393)
   * und Reflexrekorder für die gewählte Fertigkeit oder Gruppe (S. 394).
   */
  private int implantDice(@NonNull Skill skill) {
    String group = skill.getGroupName();
    int dice = group == null ? 0 : implants.level(ImplantEffectType.SKILL_GROUP_DICE, group);
    if (skill.getCategory() == SkillCategory.SOCIAL) {
      dice += implants.level(ImplantEffectType.SOCIAL_SKILL_DICE);
    }
    if (isPhysicalAttribute(skill.getLinkedAttribute())) {
      dice += implants.level(ImplantEffectType.PHYSICAL_SKILL_DICE);
    }
    return dice + implants.selectedSkillDice(skill);
  }

  private static boolean isPhysicalAttribute(@NonNull String abbreviation) {
    return Arrays.stream(AttributeType.values())
        .anyMatch(t -> t.isPhysical() && t.name().equals(abbreviation));
  }

  /**
   * Zusatzwürfel aus Adeptenkräften für eine Fertigkeit: Verbesserte Fertigkeit für genau diese Fertigkeit, Verbesserte
   * Wahrnehmung für Wahrnehmung und Askennen (GRW S. 232).
   */
  private int adeptPowerDice(@NonNull Skill skill) {
    int dice = adeptPowers.level(AdeptPowerEffect.IMPROVED_SKILL, skill.getName());
    if (PERCEPTION_SKILL.equals(skill.getName()) || ASSENSING_SKILL.equals(skill.getName())) {
      dice += adeptPowers.level(AdeptPowerEffect.IMPROVED_PERCEPTION);
    }
    return dice;
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
    int implantDice = implants.level(ImplantEffectType.KNOWLEDGE_SKILL_DICE);
    String bonusText = implantDice == 0 ? "" : " + Implantate " + implantDice;
    int rating = getEffectiveKnowledgeSkillRating(knowledgeSkill);
    if (rating > 0) {
      boolean bySoft = rating > knowledgeSkill.getRating();
      String name = !bySoft ? knowledgeSkill.getName()
          : category == KnowledgeSkillCategory.LANGUAGE ? LINGUASOFT : KNOWSOFT;
      return Optional.of(dicePool(rating + attribute + implantDice,
          ratingBreakdown(name, rating, type.getDisplayName(), attribute) + bonusText
              + (bySoft ? SKILLSOFT_NO_EDGE : "")));
    }
    if (category == KnowledgeSkillCategory.LANGUAGE || isKnowledgeSkillCostDoubled(category)) {
      return Optional.empty();
    }
    return Optional.of(defaultingPool(type.getDisplayName(), attribute, implantDice, bonusText));
  }

  /**
   * Pool aus Fertigkeit + einem fremden Wert statt des verbundenen Attributs, z.B. Prozessor oder Sensor einer Drohne
   * beim Hineinspringen (S. 286/287) oder die Stufe von Befehl beim Fernsteuern. Dieselben Sperren wie {@link
   * #getSkillDicePool(Skill)} (Inkompetenz, ohne Fertigkeitswert nur wenn erlaubt und nicht ahnungslos), aber ohne
   * Implantat- und Adeptenwürfel und ohne Schild-Malus, weil nicht der Körper des Charakters handelt
   * (Nutzerentscheidung 2026-11-15). Verletzungsmodifikator eingerechnet.
   */
  public @NonNull Optional<DicePool> getSkillDicePool(@NonNull Skill skill, @NonNull String valueName, int value) {
    if (!isSkillAccessible(skill)) {
      return Optional.empty();
    }
    int rating = getEffectiveSkillRating(skill);
    if (rating > 0) {
      return Optional.of(dicePool(value + rating,
          ratingBreakdown(valueName, value, ratingName(skill), rating) + softSuffix(skill)));
    }
    if (!skill.isDefaultable() || isSkillCostDoubled(skill.getCategory())) {
      return Optional.empty();
    }
    return Optional.of(defaultingPool(valueName, value, 0, ""));
  }

  /** Pool aus {@code dice} Würfeln mit dem Verletzungsmodifikator des Charakters (S. 193), nicht unter 0. */
  public @NonNull DicePool getWoundedPool(int dice, @NonNull String breakdown) {
    return dicePool(dice, breakdown);
  }

  /** Würfel mit Spezialisierung: +{@value #SPECIALIZATION_DICE} im Spezialgebiet (S. 147). */
  public static int withSpecialization(@NonNull DicePool pool) {
    return pool.dice() + SPECIALIZATION_DICE;
  }

  private static @NonNull String ratingBreakdown(@NonNull String name, int rating, @NonNull String attributeName,
      int attribute) {
    return "%s %d + %s %d".formatted(name, rating, attributeName, attribute);
  }

  /** Pool ohne Fertigkeitswert: Attribut − 1, plus {@code bonus} Würfel mit dem Zusatztext {@code bonusText}. */
  private @NonNull DicePool defaultingPool(@NonNull String attributeName, int attribute, int bonus,
      @NonNull String bonusText) {
    return dicePool(attribute - 1 + bonus,
        "%s %d − 1 ohne Fertigkeitswert%s".formatted(attributeName, attribute, bonusText));
  }

  /**
   * Pool eines Eintrags im Cheat-Sheet (Wahrnehmung, Ausweichen); leer, wenn die Probe nicht möglich ist (z.B. kein
   * Defaulting), und für Bewegung, die keinen eigenen Pool hat (ADR-065).
   */
  public @NonNull Optional<DicePool> getCheatSheetPool(@NonNull CheatSheetItem item) {
    String skillName = item.getSkillName();
    if (skillName == null) {
      return Optional.empty();
    }
    Skill skill = getSkill(skillName);
    return skill == null ? Optional.empty() : getSkillDicePool(skill);
  }

  /** Würfelpool einer reinen Attributprobe (S. 166/167), Verletzungsmodifikator eingerechnet. */
  public @NonNull DicePool getAttributeTestPool(@NonNull AttributeTest test) {
    int first = getAugmentedAttributeValue(test.getFirst());
    int second = getAugmentedAttributeValue(test.getSecond());
    int shield = shieldPenalty(test.getFirst().isPhysical() || test.getSecond().isPhysical());
    return dicePool(first + second + shield,
        ratingBreakdown(test.getFirst().getDisplayName(), first, test.getSecond().getDisplayName(), second)
            + shieldText(shield));
  }

  /**
   * Malus eines angelegten Schilds: −1 auf alle körperlichen Proben einschließlich Angriffen (GRW S. 372). Als
   * körperlich gilt eine Probe mit körperlichem Attribut (Fertigkeit mit körperlichem verbundenem Attribut bzw.
   * Attributprobe mit einem körperlichen Attribut).
   */
  private int shieldPenalty(boolean physical) {
    return physical && equipment.hasShieldEquipped() ? -1 : 0;
  }

  private static @NonNull String shieldText(int shield) {
    return shield == 0 ? "" : " − Schild " + -shield;
  }

  private @NonNull DicePool dicePool(int dice, @NonNull String breakdown) {
    int wound = getWoundModifier();
    String text = wound == 0 ? breakdown : "%s − %d Verletzung".formatted(breakdown, -wound);
    return new DicePool(Math.max(0, dice + wound), text);
  }

  /**
   * Wirksamer Wert des verbundenen Attributs einer Fertigkeit; MAG/RES nach Essenzverlust, beim Magieradepten nur die
   * Magie für magische Fertigkeiten ({@link #getSpellcastingMagic()}).
   */
  private int linkedAttributeValue(@NonNull String abbreviation) {
    return switch (abbreviation) {
      case "MAG" -> getSpellcastingMagic();
      case "RES" -> getEffectiveMagicOrResonanceValue();
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
   * Initiativedurchgänge: bei Crittern wie im Katalog; sonst einer, plus die Stufe von Gesteigerte Reflexe (S. 172, 230) bzw. von Reflexbooster oder
   * Synapsenbeschleuniger (S. 389, 394). Die Quellen schließen einander aus ({@link Implants#getBlockReason}).
   */
  public int getInitiativePasses() {
    if (critter != null) {
      return critter.getData().passes();
    }
    return 1 + adeptPowers.level(AdeptPowerEffect.IMPROVED_REFLEXES)
        + implants.level(ImplantEffectType.INITIATIVE_PASSES);
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

  // --- Talentsofts ---

  private static final String SKILLSOFT_NO_EDGE = "; Talentsoft: kein Edge";

  /** Stufe der Talentleitungen, 0 ohne (GRW S. 389). */
  public int getSkillwireRating() {
    return implants.maxLevel(ImplantEffectType.SKILLWIRES);
  }

  /**
   * Alle Talentsofts des Charakters: lose, in Geräten eingebaut (z.B. Kommlink) oder im Kommlink der Headware; nicht
   * in Fahrzeugen.
   */
  public @NonNull List<Gear> getSkillsofts() {
    return Stream.concat(equipment.getGear().stream(), implants.getCommlinks().stream())
        .flatMap(g -> Stream.concat(Stream.of(g), g.getInstalled().stream()))
        .filter(Gear::isSkillsoft)
        .distinct()
        .toList();
  }

  /** Summe der Stufen aller geladenen Aktionssofts. */
  public int getLoadedActivesoftRating() {
    return getSkillsofts().stream().filter(g -> isActivesoft(g) && g.isRunning()).mapToInt(Gear::getRating).sum();
  }

  /** Wie viele Aktionssoft-Stufen die Talentleitungen gleichzeitig verarbeiten: das Doppelte ihrer Stufe (S. 389). */
  public int getActivesoftCapacity() {
    return getSkillwireRating() * 2;
  }

  /**
   * Warum die Talentsoft nicht geladen werden kann, oder {@code null}: Aktionssofts nur mit Talentleitungen und nur,
   * solange die geladenen zusammen das Doppelte der Leitungsstufe nicht übersteigen (S. 389). Lingua- und Wissenssofts
   * lassen sich immer laden; ob sie wirken, sagt {@link #getSkillsoftStatus}.
   */
  public @Nullable String getSkillsoftLoadProblem(@NonNull Gear soft) {
    if (!isActivesoft(soft) || soft.isRunning()) {
      return null;
    }
    if (getSkillwireRating() == 0) {
      return "Aktionssofts brauchen Talentleitungen (S. 389)";
    }
    int loaded = getLoadedActivesoftRating();
    if (loaded + soft.getRating() > getActivesoftCapacity()) {
      return "Talentleitungen ausgelastet: %d von %d Stufen geladen (S. 389)".formatted(loaded, getActivesoftCapacity());
    }
    return null;
  }

  /**
   * Ob und mit welcher Stufe die Talentsoft wirkt (S. 377, 389): nur geladen; Aktionssofts nur mit Talentleitungen,
   * höchstens bis zu deren Stufe (eine höhere Soft wirkt gar nicht, anders als im C#-Original, das deckelt) und nur,
   * solange die geladenen zusammen das Doppelte der Leitungsstufe nicht übersteigen; Lingua-
   * und Wissenssofts nur über ein Direktes Neuralinterface (Datenbuchse, Sim-Modul).
   */
  public @NonNull SkillsoftStatus getSkillsoftStatus(@NonNull Gear soft) {
    if (!soft.isRunning()) {
      return new SkillsoftStatus(0, "nicht geladen");
    }
    if (isActivesoft(soft)) {
      int wires = getSkillwireRating();
      if (wires == 0) {
        return new SkillsoftStatus(0, "keine Talentleitungen");
      }
      if (soft.getRating() > wires) {
        return new SkillsoftStatus(0, "Stufe über den Talentleitungen (%d)".formatted(wires));
      }
      int loaded = getLoadedActivesoftRating();
      if (loaded > getActivesoftCapacity()) {
        // z.B. nach dem Senken der Leitungsstufe oder aus einer Datei: überlastet wirkt keine Aktionssoft (S. 389)
        return new SkillsoftStatus(0, "Talentleitungen überlastet: %d von %d Stufen geladen"
            .formatted(loaded, getActivesoftCapacity()));
      }
      return new SkillsoftStatus(soft.getRating(), null);
    }
    if (!hasNeuralInterface()) {
      return new SkillsoftStatus(0, "kein Neuralinterface (Datenbuchse oder Sim-Modul)");
    }
    return new SkillsoftStatus(soft.getRating(), null);
  }

  /**
   * Pool, mit dem die Talentsoft gewürfelt wird: Aktionssoft über ihre Fertigkeit, Lingua- bzw. Wissenssoft über eine
   * gleichnamige eigene Sprache bzw. Wissensfertigkeit; eine Linguasoft ohne eigene Sprache mit Intuition wie jede
   * Sprache (S. 110). Eine Wissenssoft ohne eigene Fertigkeit hat keinen Pool, weil ihre Kategorie und damit das
   * Attribut offen ist (Plan-Annahme A4). Leer auch, wenn die Soft nicht wirkt.
   */
  public @NonNull Optional<DicePool> getSkillsoftPool(@NonNull Gear soft) {
    SkillsoftStatus status = getSkillsoftStatus(soft);
    if (status.rating() == 0) {
      return Optional.empty();
    }
    if (isActivesoft(soft)) {
      Skill skill = getSkill(soft.getExtra());
      return skill == null ? Optional.empty() : getSkillDicePool(skill);
    }
    Optional<KnowledgeSkill> own = knowledgeSkills.stream()
        .filter(k -> k.getName().equalsIgnoreCase(soft.getExtra()))
        .findFirst();
    if (own.isPresent()) {
      return getKnowledgeSkillDicePool(own.get());
    }
    if (!LINGUASOFT.equals(soft.getName())) {
      return Optional.empty();
    }
    int intuition = getAugmentedAttributeValue(AttributeType.INT);
    return Optional.of(dicePool(status.rating() + intuition,
        ratingBreakdown(LINGUASOFT, status.rating(), AttributeType.INT.getDisplayName(), intuition) + SKILLSOFT_NO_EDGE));
  }

  /**
   * Wirksamer Wert einer aktiven Fertigkeit: der eigene Wert oder, wenn höher, die Stufe einer wirksamen Aktionssoft
   * (S. 377). Einzige Stelle für diese Regel; Pools und Anzeige nutzen sie.
   */
  public int getEffectiveSkillRating(@NonNull Skill skill) {
    return Math.max(skill.getRating(), activesoftRating(skill));
  }

  /** Wie {@link #getEffectiveSkillRating} für Wissens- und Sprachfertigkeiten mit Wissens- bzw. Linguasoft. */
  public int getEffectiveKnowledgeSkillRating(@NonNull KnowledgeSkill skill) {
    return Math.max(skill.getRating(),
        knowledgesoftRating(skill.getName(), skill.getCategory() == KnowledgeSkillCategory.LANGUAGE));
  }

  /** Bezeichnung des wirksamen Werts für die Aufschlüsselung: die Fertigkeit oder „Aktionssoft“. */
  private @NonNull String ratingName(@NonNull Skill skill) {
    return activesoftRating(skill) > skill.getRating() ? ACTIVESOFT : skill.getName();
  }

  /** Hinweis „kein Edge“, wenn die Aktionssoft den Wert stellt (S. 377). */
  private @NonNull String softSuffix(@NonNull Skill skill) {
    return activesoftRating(skill) > skill.getRating() ? SKILLSOFT_NO_EDGE : "";
  }

  /** Höchste wirksame Stufe einer Aktionssoft für diese Fertigkeit, 0 ohne. */
  private int activesoftRating(@NonNull Skill skill) {
    return getSkillsofts().stream()
        .filter(g -> isActivesoft(g) && g.getExtra().equals(skill.getName()))
        .mapToInt(g -> getSkillsoftStatus(g).rating())
        .max().orElse(0);
  }

  /** Höchste wirksame Stufe einer Lingua- ({@code language}) bzw. Wissenssoft mit diesem Namen, 0 ohne. */
  private int knowledgesoftRating(@NonNull String name, boolean language) {
    String softName = language ? LINGUASOFT : KNOWSOFT;
    return getSkillsofts().stream()
        .filter(g -> g.getName().equals(softName) && g.getExtra().equalsIgnoreCase(name))
        .mapToInt(g -> getSkillsoftStatus(g).rating())
        .max().orElse(0);
  }

  private static boolean isActivesoft(@NonNull Gear soft) {
    return ACTIVESOFT.equals(soft.getName());
  }

  // --- Matrix ---

  /** Durchgänge in VR mit kaltem Sim (S. 276). */
  public static final int VR_COLD_PASSES = 2;
  /** Durchgänge in VR mit heißem Sim, auch für Technomancer (S. 276, 279). */
  public static final int VR_HOT_PASSES = 3;

  /**
   * Matrixzugang: Technomancer über die Lebende Persona immer heißes Sim (S. 279); sonst braucht es ein aktives
   * Kommlink ({@link Equipment#getActiveCommlink()}), für VR dazu ein Sim-Modul und ein Direktes Neuralinterface
   * (S. 258). Das Sim-Modul steckt im aktiven Kommlink, ist implantiert oder Teil eines Simrigs; das Interface kommt
   * aus einem Implantat (Datenbuchse, Kommlink, Sim-Modul) oder aus Elektroden bzw. Simrig (S. 374, 386).
   */
  public @NonNull MatrixAccess getMatrixAccess() {
    if (hasResonance()) {
      return MatrixAccess.VR_HOT;
    }
    Gear commlink = equipment.getActiveCommlink();
    if (commlink == null) {
      return MatrixAccess.NONE;
    }
    int sim = simModuleLevel(commlink);
    if (sim == 0 || !hasNeuralInterface()) {
      return MatrixAccess.AR;
    }
    return sim > 1 ? MatrixAccess.VR_HOT : MatrixAccess.VR_COLD;
  }

  /**
   * 0 ohne Sim-Modul, 1 kaltes, 2 heißes Sim. Es zählt ein Sim-Modul im aktiven Kommlink, ein implantiertes oder das
   * eines getragenen Simrigs; ein lose gekauftes Sim-Modul ist mit keinem Kommlink verbunden und zählt nicht
   * (Plan-Annahme A3). Ein Simrig erkennt man daran, dass er Neuralinterface und Sim-Modul zugleich ist.
   */
  private int simModuleLevel(@NonNull Gear commlink) {
    int level = implants.maxLevel(ImplantEffectType.SIM_MODULE);
    for (Gear part : commlink.getInstalled()) {
      level = Math.max(level, part.getData().simLevel());
    }
    for (Gear item : equipment.getGear()) {
      if (item.getData().matrixFlags().contains(MatrixFlag.DNI)) {
        level = Math.max(level, item.getData().simLevel());
      }
    }
    return level;
  }

  private boolean hasNeuralInterface() {
    return implants.maxLevel(ImplantEffectType.DNI) > 0
        || equipment.getGear().stream().anyMatch(g -> g.getData().matrixFlags().contains(MatrixFlag.DNI));
  }

  /** Was für den Matrixzugang bzw. für VR fehlt, oder {@code null} bei VR. */
  public @Nullable String getMatrixAccessHint() {
    return switch (getMatrixAccess()) {
      case NONE -> equipment.getCommlinks().isEmpty() ? "Kein Kommlink" : "Aktives Kommlink wählen";
      case AR -> {
        Gear commlink = Objects.requireNonNull(equipment.getActiveCommlink());
        List<String> missing = new ArrayList<>();
        if (simModuleLevel(commlink) == 0) {
          missing.add("Sim-Modul (im aktiven Kommlink, implantiert oder im Simrig)");
        }
        if (!hasNeuralInterface()) {
          missing.add("Direktes Neuralinterface");
        }
        yield "Für VR fehlt: " + String.join(", ", missing);
      }
      case VR_COLD, VR_HOT -> null;
    };
  }

  /**
   * Matrixinitiative mit Verletzungsmodifikator, nicht unter 0: Technomancer über die Lebende Persona (S. 279), sonst
   * in AR die Körperliche Initiative (S. 265), in VR Prozessor des aktiven Kommlinks (nach Prozessorlimit) + Intuition,
   * mit heißem Sim + 1 (S. 276).
   */
  public int getMatrixInitiative() {
    LivingPersona persona = getLivingPersona();
    int intuition = getAugmentedAttributeValue(AttributeType.INT);
    int base = persona != null ? persona.matrixInitiative() : switch (getMatrixAccess()) {
      case NONE, AR -> getInitiativeAttribute();
      case VR_COLD -> activeCommlinkProcessor() + intuition;
      case VR_HOT -> activeCommlinkProcessor() + intuition + 1;
    };
    return Math.max(0, base + getWoundModifier());
  }

  private int activeCommlinkProcessor() {
    Gear commlink = Objects.requireNonNull(equipment.getActiveCommlink());
    return Objects.requireNonNull(commlink.getMatrixStats()).processor();
  }

  /** Durchgänge der Matrixinitiative: in AR die körperlichen, in VR 2 (kalt) bzw. 3 (heiß) (S. 276). */
  public int getMatrixInitiativePasses() {
    return switch (getMatrixAccess()) {
      case NONE, AR -> getInitiativePasses();
      case VR_COLD -> VR_COLD_PASSES;
      case VR_HOT -> VR_HOT_PASSES;
    };
  }

  /** Lebende Persona aus den wirksamen Werten (S. 279), oder {@code null} ohne Resonanz. */
  public @Nullable LivingPersona getLivingPersona() {
    if (!hasResonance()) {
      return null;
    }
    return LivingPersona.of(getAugmentedAttributeValue(AttributeType.WIL), getAugmentedAttributeValue(AttributeType.INT),
        getAugmentedAttributeValue(AttributeType.LOG), getAugmentedAttributeValue(AttributeType.CHA),
        getEffectiveMagicOrResonanceValue());
  }

  /**
   * Kästchen der Körperlichen Spalte: 8 plus halbe verstärkte Konstitution, aufgerundet (S. 94), plus 1 je
   * angeschlossener Cybergliedmaße (S. 390).
   */
  public int getPhysicalBoxes() {
    return CONDITION_MONITOR_BASE + (getAugmentedAttributeValue(AttributeType.BOD) + 1) / 2
        + implants.getAttachedLimbs().size();
  }

  /** Kästchen der Geistigen Spalte: 8 plus halbe verstärkte Willenskraft, aufgerundet (S. 94). */
  public int getStunBoxes() {
    return CONDITION_MONITOR_BASE + (getAugmentedAttributeValue(AttributeType.WIL) + 1) / 2;
  }

  /**
   * Kästchen für überzähligen körperlichen Schaden: so viele wie die verstärkte Konstitution (darüber stirbt der
   * Charakter, S. 192), plus die Stufe von Überlebenswille (S. 117).
   */
  public int getPhysicalOverflowBoxes() {
    return getAugmentedAttributeValue(AttributeType.BOD) + qualityLevel(QualityBonusType.WILL_TO_LIVE);
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
   * Hohe Schmerztoleranz, die Adeptenkraft Schmerzresistenz und der Schadenskompensator lassen je Stufe ein Kästchen
   * pro Spalte unberücksichtigt (S. 116, 231, 394; Hohe Schmerztoleranz schließt die beiden anderen aus, ob
   * Schmerzresistenz und Schadenskompensator sich addieren, sagt das GRW nicht: Annahme ja), Niedrige Schmerztoleranz
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
   * #getWoundModifier()}. Dient auch den Trennern im Zustandsmonitor des Status-Panels.
   *
   * @return 0 oder negativ
   */
  public int getColumnWoundModifier(int damagedBoxes) {
    int ignored = qualityLevel(QualityBonusType.HIGH_PAIN_TOLERANCE)
        + adeptPowers.level(AdeptPowerEffect.PAIN_RESISTANCE)
        + implants.level(ImplantEffectType.WOUND_BOXES_IGNORED);
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
   * Ritualzauberei nicht überschreiten (SR4-Grundregelwerk). Im Karrieremodus und im Spielleiter-Modus gilt dieser
   * Erstellungs-Cap nicht.
   */
  public boolean canAddSpellAtCreation() {
    if (created || gmMode) {
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
   * der natürliche Logikwert, unabhängig von ihrer Stufe (SR4-Grundregelwerk S. 112). Im Karrieremodus und im
   * Spielleiter-Modus ohne Grenze.
   */
  public boolean canAddComplexFormAtCreation() {
    return created || gmMode
        || complexForms.size() < COMPLEX_FORM_CAP_MULTIPLIER * getAttribute(AttributeType.LOG).getValue();
  }

  /** Wie {@link #canRaiseComplexForm(ComplexForm)}, aber für den Erwerb einer noch nicht gelernten Form (Stufe 1). */
  public boolean canAddComplexForm() {
    return hasResonance() && getEffectiveMagicOrResonanceValue() >= ComplexForm.MIN_RATING;
  }

  // --- Adeptenkräfte ---

  /** Ob der Charakter Adeptenkräfte kaufen kann: Gabe Adept oder Magieradept (GRW S. 115/116). */
  public boolean hasAdeptPowers() {
    return hasQualityBonus(QualityBonusType.ADEPT_POWERS);
  }

  /** Magieradept: Adeptenkräfte und Zauberei zugleich, Magie wird aufgeteilt (GRW S. 116, 229). */
  public boolean isMysticAdept() {
    return hasAdeptPowers() && isMagician();
  }

  /**
   * Die Adeptenkräfte des Charakters (Kauf, Stufen, Kraftpunkte, Magieradept-Aufteilung) — immer vorhanden, leer ohne
   * Adeptengabe (analog {@link #getEquipment()}). Entfernen läuft über {@link #removeAdeptPower}.
   */
  public @NonNull AdeptPowers getAdeptPowers() {
    return adeptPowers;
  }

  /**
   * Kaufregeln und Wirkungen der Implantate (Unverträglichkeiten, Auswahl, Summen der Wirkungen); die Implantate selbst
   * liegen in {@link Equipment#getCyberware()}.
   */
  public @NonNull Implants getImplants() {
    return implants;
  }

  /**
   * Magie für magische Fertigkeiten (Zauberei, Beschwören): beim Magieradepten die wirksame Magie ohne den Kräfte-Anteil
   * (GRW S. 229, Beispiel Roxanne: Magie 4, ein Punkt in Kräften, Spruchzauberei mit 3), sonst die wirksame Magie.
   */
  public int getSpellcastingMagic() {
    return getEffectiveMagicOrResonanceValue() - adeptPowers.getAdeptMagic();
  }

  /**
   * Entfernt eine Kraft. Fällt damit die astrale Wahrnehmung weg, werden Askennen und Astralkampf auf 0 gesetzt (wie
   * beim Entfernen einer Gabe); {@link #getAdeptPowerRemovalConsequences} listet das vorher auf.
   */
  public void removeAdeptPower(@NonNull AdeptPower power) {
    List<Skill> locked = getSkillsLockedByRemovingPower(power);
    adeptPowers.remove(power);
    locked.forEach(Character::resetLockedSkill);
  }

  /** Folgen des Entfernens einer Kraft als Zeilen für die Rückfrage; leer, wenn nur die Kraft wegfällt. */
  public @NonNull List<String> getAdeptPowerRemovalConsequences(@NonNull AdeptPower power) {
    return getSkillsLockedByRemovingPower(power).stream()
        .map(Character::lockedSkillLine)
        .toList();
  }

  private @NonNull List<Skill> getSkillsLockedByRemovingPower(@NonNull AdeptPower power) {
    if (power.getData().effect() != AdeptPowerEffect.ASTRAL_PERCEPTION) {
      return List.of();
    }
    return lockedSkills(isMagician(), astralPerception(hasAstralAccess(), hasAdeptPowers(), power), hasResonance());
  }

  /**
   * Ballistik-Schutz: angelegte Rüstung ({@link Equipment#getArmorBallistic()}) plus Mystischer Panzer (S. 231) plus
   * Implantatpanzerung (Dermalpanzerung, Titan-Kompositknochen S. 388; Orthoskin S. 393) plus Gliedpanzerung
   * ({@link Implants#getLimbArmor()}, S. 390) plus Panzer eines Critters (S. 340).
   */
  public int getArmorBallistic() {
    return equipment.getArmorBallistic() + adeptPowers.level(AdeptPowerEffect.MYSTIC_ARMOR)
        + implants.level(ImplantEffectType.ARMOR_BALLISTIC) + implants.getLimbArmor()
        + (critter == null ? 0 : critter.getArmorBallistic());
  }

  /**
   * Stoß-Schutz: angelegte Rüstung plus Mystischer Panzer plus Implantat- und Gliedpanzerung. Panzer und Implantate
   * werden nicht getragen und zählen deshalb nicht zur Panzerungsbehinderung, die nur getragene Panzerung rechnet
   * (S. 190).
   */
  public int getArmorImpact() {
    return equipment.getArmorImpact() + adeptPowers.level(AdeptPowerEffect.MYSTIC_ARMOR)
        + implants.level(ImplantEffectType.ARMOR_IMPACT) + implants.getLimbArmor()
        + (critter == null ? 0 : critter.getArmorImpact());
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
      problems.add("Es wurden %s mehr ausgegeben, als an Ressourcen zur Verfügung stehen."
          .formatted(Numbers.nuyen(-remaining)));
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
    return finalizeCreation(diceSum, LocalDate.now(ZoneId.systemDefault()));
  }

  /** Wie {@link #finalizeCreation(int)} mit festem Buchungsdatum (z.B. für reproduzierbare Testfälle). */
  public int finalizeCreation(int diceSum, @NonNull LocalDate date) {
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
        startingCapital, date, LogCategory.START));
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
        + getActiveSkillSpecializationBP() + getContactBP() + getSpellBP() + getComplexFormBP()
        + spirits.getBpCost() + foci.getBpCost();
  }

  /**
   * Ob bei der Erschaffung BP oder Ressourcengeld überzogen sind (Fußzeile als Warnung, Hinweise im Status); im
   * Karrieremodus nie.
   */
  public boolean isCreationOverspent() {
    return !created && (getRemainingBP() < 0 || getRemainingNuyen() < 0);
  }

  public int getRemainingBP() {
    return TOTAL_BP - getSpentBP();
  }

  // --- Karma & Nuyen Log ---

  /**
   * Spielleiter-Modus (ADR-060): Korrekturen ohne Kosten und ohne Regelsperren. Es entfallen die Erschaffungsgrenzen
   * (Attribute, 6/5-Regel, Gruppen-Cap, Wissensfertigkeiten, Zauberzahl, 35 BP für Gaben und Handicaps, Grad und
   * Verfügbarkeit, Geisterkraft), „nur steigern“ im Karrieremodus, Zugangs- und Gabensperren (magische Fertigkeiten,
   * Metatyp, Voraussetzungen, Erwachen). Es bleiben die Obergrenzen aus dem GRW (Attribut-, Fertigkeits-, Magie-
   * Maximum), strukturelle Regeln (Gaben nur durch den Metatyp, schon gewählt, exotische Grundzeile) und der einseitige
   * Abschluss der Erschaffung. Kosten entfallen in der Oberfläche ({@code TabServices}). Nie gespeichert.
   */
  public boolean isGmMode() {
    return gmMode;
  }

  /**
   * Kürzel abgeschalteter Regelwerke, aus denen der Charakter Einträge besitzt (ADR-061). Solche Einträge bleiben und
   * wirken weiter; der Status zeigt dazu nur einen Hinweis.
   */
  public @NonNull SortedSet<String> getSourcesFromDisabledBooks() {
    List<Sourced> owned = new ArrayList<>();
    for (CharacterQuality quality : qualities) {
      owned.add(quality.data());
      MentorData mentor = quality.data().selection() == QualitySelection.MENTOR
          ? MentorDataLoader.findByChoice(quality.choice()) : null;
      if (mentor != null) {
        owned.add(mentor);
      }
    }
    owned.addAll(spells);
    complexForms.forEach(form -> owned.add(form.getData()));
    owned.addAll(initiation.getTechniques());
    spirits.getAll().forEach(spirit -> owned.add(spirit.getData()));
    equipment.getCyberware().forEach(implant -> addImplantSources(implant, owned));
    for (Armor armor : equipment.getArmor()) {
      owned.add(armor.getData());
      armor.getMods().forEach(mod -> owned.add(mod.getData()));
    }
    equipment.getWeapons().forEach(weapon -> addWeaponSources(weapon, owned));
    equipment.getGear().forEach(gear -> addGearSources(gear, owned));
    for (Vehicle vehicle : equipment.getVehicles()) {
      owned.add(vehicle.getData());
      vehicle.getGear().forEach(gear -> addGearSources(gear, owned));
      vehicle.getMountedWeapons().forEach(weapon -> addWeaponSources(weapon, owned));
    }
    return owned.stream().map(Sourced::source).filter(code -> !ruleOptions.isBookEnabled(code))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private static void addImplantSources(@NonNull Cyberware implant, @NonNull List<Sourced> owned) {
    owned.add(implant.getData());
    Gear commlink = implant.getCommlink();
    if (commlink != null) {
      addGearSources(commlink, owned);
    }
    implant.getInstalled().forEach(part -> addImplantSources(part, owned));
  }

  private static void addWeaponSources(@NonNull Weapon weapon, @NonNull List<Sourced> owned) {
    owned.add(weapon.getData());
    weapon.getAccessories().forEach(accessory -> owned.add(accessory.data()));
  }

  private static void addGearSources(@NonNull Gear gear, @NonNull List<Sourced> owned) {
    owned.add(gear.getData());
    gear.getInstalled().forEach(part -> addGearSources(part, owned));
  }

  /** Regel-Einstellungen dieses Charakters (gespeichert, nie {@code null}). */
  public @NonNull RuleOptions getRuleOptions() {
    return ruleOptions;
  }

  /** Schaltet den Spielleiter-Modus; wird nicht gespeichert. */
  public void setGmMode(boolean gmMode) {
    this.gmMode = gmMode;
  }

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

  // --- Reputation ---

  /** Eingaben des Spielleiters zur Reputation (S. 308/309). */
  public @NonNull Reputation getReputation() {
    return reputation;
  }

  /**
   * Insgesamt verdientes Karma: Summe aller Gutschriften im Karma-Log; Ausgaben zählen nicht (wie im C#-Original,
   * GRW S. 308 verweist auf das verdiente Karma).
   */
  public int getEarnedKarma() {
    return karmaLog.stream().mapToInt(LogEntry::amount).filter(a -> a > 0).sum();
  }

  /**
   * Straßenruf (S. 308): verdientes Karma ÷ 10, kaufmännisch gerundet, plus Punkte vom Spielleiter, minus geopferter
   * Straßenruf; nie unter 0. Als Würfelbonus höchstens Charisma (Anzeige).
   */
  public @NonNull ReputationScore getStreetCred() {
    int earned = getEarnedKarma();
    int fromKarma = (earned + 5) / 10;
    StringBuilder text = new StringBuilder("verdientes Karma %d ÷ 10 = %d".formatted(earned, fromKarma));
    if (reputation.getStreetCredBonus() > 0) {
      text.append(" + Spielleiter ").append(reputation.getStreetCredBonus());
    }
    if (reputation.getBurnedStreetCred() > 0) {
      text.append(" − geopfert ").append(reputation.getBurnedStreetCred());
    }
    return new ReputationScore(
        Math.max(0, fromKarma + reputation.getStreetCredBonus() - reputation.getBurnedStreetCred()), text.toString());
  }

  /**
   * Schlechter Ruf (S. 308): Start 0, +1 je Handicap und −1 je Gabe der Liste (Wert in {@link QualityData#notoriety()}),
   * plus Punkte vom Spielleiter, minus 1 je 2 geopfertem Straßenruf. Kann durch Gaben negativ werden (Plan-Annahme A2).
   */
  public @NonNull ReputationScore getNotoriety() {
    List<String> parts = new ArrayList<>();
    int value = 0;
    for (CharacterQuality q : qualities) {
      int n = q.data().notoriety();
      if (n != 0) {
        value += n;
        parts.add("%s %s%d".formatted(q.data().name(), n > 0 ? "+" : "−", Math.abs(n)));
      }
    }
    if (reputation.getNotorietyBonus() > 0) {
      value += reputation.getNotorietyBonus();
      parts.add("Spielleiter +" + reputation.getNotorietyBonus());
    }
    int burned = reputation.getBurnedStreetCred() / Reputation.STREET_CRED_PER_NOTORIETY;
    if (burned > 0) {
      value -= burned;
      parts.add("geopferter Straßenruf −" + burned);
    }
    return new ReputationScore(value, parts.isEmpty() ? "keine Gaben oder Handicaps mit Einfluss" : String.join(", ",
        parts));
  }

  /**
   * Prominenz (S. 309): (Straßenruf + Schlechter Ruf) ÷ 3, abgerundet, plus Anpassung des Spielleiters; nie unter 0.
   */
  public @NonNull ReputationScore getPublicAwareness() {
    int streetCred = getStreetCred().value();
    int notoriety = getNotoriety().value();
    int base = Math.floorDiv(streetCred + notoriety, 3);
    int adjustment = reputation.getAwarenessAdjustment();
    String text = "(%d + %d) ÷ 3 = %d".formatted(streetCred, notoriety, base)
        + (adjustment == 0 ? "" : (adjustment > 0 ? " + Spielleiter " : " − Spielleiter ") + Math.abs(adjustment));
    return new ReputationScore(Math.max(0, base + adjustment), text);
  }

  /** Ob 2 Straßenruf geopfert werden können: genug Straßenruf und Schlechter Ruf über 0 (S. 308). */
  public boolean canBurnStreetCred() {
    return getStreetCred().value() >= Reputation.STREET_CRED_PER_NOTORIETY && getNotoriety().value() > 0;
  }

  /**
   * Opfert dauerhaft 2 Straßenruf für 1 Punkt weniger Schlechten Ruf (S. 308).
   *
   * @throws IllegalStateException wenn {@link #canBurnStreetCred()} nicht gilt
   */
  public void burnStreetCred() {
    if (!canBurnStreetCred()) {
      throw new IllegalStateException("Zu wenig Straßenruf oder kein Schlechter Ruf zum Abbauen");
    }
    reputation.burn();
  }
}