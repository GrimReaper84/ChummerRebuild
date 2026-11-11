package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttributeType;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public void setMetatype(@NonNull MetatypeData metatype) {
    this.metatype = metatype;
    initAttributes(metatype);
    this.edge = initEdge(metatype);
    initMetatypeQualities(metatype);
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
   * #hasResonance()}). SR4-Grundregelwerk: "Magie und Resonanz sind Attribute, die sich
   * gegenseitig ausschließen" — ein Charakter kann nie beide gleichzeitig besitzen.
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
   * Prüft, ob der Charakter speziell die Gabe "Magier" besitzt — anders als {@link #hasMagic()}
   * (liefert auch für Adepten {@code true}, da diese ebenfalls ein Magie-Attribut besitzen), grenzt
   * dies auf Charaktere ein, die tatsächlich Zauber wirken dürfen. SR4-Grundregelwerk S. 148: "Wenn
   * nicht anders angegeben, können nur Charaktere mit einer der Gaben Zauberer oder Magieradept und
   * einem Magieattribut von mindestens 1 magische Fertigkeiten erwerben oder anwenden." Steuert die
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
   * GRW-Kombinationsregel abgezogen (S. 111): "Essenzverlust durch Cyberware und Bioware wird getrennt berechnet
   * und notiert. Der höhere der beiden Werte wird vollständig von der Essenz abgezogen, der andere Wert
   * nur zur Hälfte" — also {@code 6 − max(ΣCyber, ΣBio) − min(ΣCyber, ΣBio) / 2}. GRW-Beispiel: 1,5
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
    return BASE_ESSENCE.subtract(higher).subtract(lower.divide(TWO))
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
   * SR4-Grundregelwerk S. 208: "Für jeden angefangenen Punkt Essenzverlust werden das Magieattribut
   * des Charakters und sein maximaler Magiewert um jeweils 1 Punkt reduziert. Bei einem Charakter mit
   * Magie 4, dessen Essenz auf 5,8 sinkt, beträgt die Magie 3 und das Magiemaximum 5" — also
   * {@code ceiling(6 − Essenz)}. Gilt ebenso für Resonanz (GRW S. 111: "Jeder Essenzverlust
   * beeinflusst auch die Magie bzw. die Resonanz eines Charakters"). Einheitlich für Erstellungs-
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
   * Quality-Änderung noch eine freischaltende Quality vorhanden ist. GRW: Charaktere mit der
   * entsprechenden Gabe "erhalten automatisch einen Magiewert von 1" bzw. "einen Resonanzwert von
   * 1" — daher Startwert 1, nicht 0. Der Reset auf {@code null} verwirft automatisch die BP-Kosten
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
        .filter(s -> s != skill && !isCoupledGroupMember(s) && s.getRating() == 6)
        .count();
    long count5 = skills.values().stream()
        .filter(s -> s != skill && !isCoupledGroupMember(s) && s.getRating() == 5)
        .count();
    if (newValue == 5) {
      return count6 == 0 && count5 < 2;
    }
    return count5 == 0 && count6 < 1; // newValue == 6
  }

  /**
   * Prüft, ob der Charakter diese aktive Fertigkeit überhaupt erwerben darf:
   * <ul>
   *   <li>Magische Fertigkeiten nur mit der Gabe "Magier" (SR4-Grundregelwerk S. 148: "Wenn nicht
   *       anders angegeben, können nur Charaktere mit einer der Gaben Zauberer oder Magieradept und
   *       einem Magieattribut von mindestens 1 magische Fertigkeiten erwerben oder anwenden.").
   *       Ausnahme Arcana (S. 148): "Die Fertigkeit kann nicht nur von Erwachten, sondern auch von
   *       Mundanen erlernt werden". Dass Adepten mit der Kraft Astrale Wahrnehmung auch Askennen und
   *       Astralkampf nutzen dürfen (S. 109), ist mangels Adeptenkräften noch nicht abgebildet.</li>
   *   <li>Resonanzfertigkeiten nur für Technomancer (S. 151: "Diese Kategorie beinhaltet
   *       Fertigkeiten, die nur von Charakteren mit der Gabe Technomancer (S. 117) gewählt werden
   *       können.").</li>
   * </ul>
   * Die Sperre gilt auch im Karrieremodus, da sie keine Erschaffungsregel, sondern eine
   * grundsätzliche Zugriffsvoraussetzung ist.
   */
  private boolean isSkillAccessible(@NonNull Skill skill) {
    return isSkillAccessible(skill, isMagician(), hasResonance());
  }

  private static boolean isSkillAccessible(@NonNull Skill skill, boolean magician,
      boolean resonance) {
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
   * regelkonform wäre: "Bei der Charaktererschaffung beträgt der Maximalwert für
   * Fertigkeitsgruppen 4." Im Karrieremodus gilt dieser Erstellungs-Cap nicht (dort bis
   * {@link Skill#MAX_RATING}).
   */
  public boolean canRaiseGroupAtCreation(@NonNull String groupName) {
    return created || getGroupRating(groupName) < MAX_GROUP_RATING_AT_CREATION;
  }

  /**
   * Vollständige Steigerungs-Prüfung für eine Fertigkeitsgruppe: Erstellungs-Cap aus {@link
   * #canRaiseGroupAtCreation(String)} plus Zugangsvoraussetzung aller Mitglieder (siehe {@link
   * #isSkillAccessible(Skill)}), z.B. Hexerei/Beschwören nur für Magier, Tasken nur für
   * Technomancer.
   */
  public boolean canRaiseGroup(@NonNull String groupName) {
    return skills.values().stream()
        .filter(s -> groupName.equals(s.getGroupName()))
        .allMatch(this::isSkillAccessible)
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

  /**
   * Fügt eine gewählte Quality hinzu. Gratis-Qualities umgehen das BP-Limit und kosten 0 BP.
   *
   * @param gratis wenn true, wird die Limit-Prüfung übersprungen und die Quality kostet 0 BP
   */
  public void addQuality(@NonNull QualityData data, boolean gratis) {
    if (!created && !gratis && data.contributeToLimit()) {
      if (data.type() == QualityType.POSITIVE
          && getPositiveQualityBP() + data.bp() > MAX_POSITIVE_QUALITY_BP) {
        throw new IllegalArgumentException(
            "Positives Quality-Limit von %d BP überschritten".formatted(MAX_POSITIVE_QUALITY_BP));
      }
      if (data.type() == QualityType.NEGATIVE
          && getNegativeQualityBP() + Math.abs(data.bp()) > MAX_NEGATIVE_QUALITY_BP) {
        throw new IllegalArgumentException(
            "Negatives Quality-Limit von %d BP überschritten".formatted(MAX_NEGATIVE_QUALITY_BP));
      }
    }
    if (data.bonuses().contains(QualityBonusType.GRANTS_MAGIC) && grantsResonance()) {
      throw new IllegalArgumentException(
          "Magie und Resonanz schließen sich gegenseitig aus (SR4-Grundregelwerk)");
    }
    if (data.bonuses().contains(QualityBonusType.GRANTS_RESONANCE) && grantsMagic()) {
      throw new IllegalArgumentException(
          "Magie und Resonanz schließen sich gegenseitig aus (SR4-Grundregelwerk)");
    }
    if (data.bonuses().contains(QualityBonusType.GRANTS_MAGIC) && hasSelectedGrantsMagicQuality()) {
      throw new IllegalArgumentException(
          "Magier, Adept und Mystischer Adept schließen sich gegenseitig aus (SR4-Grundregelwerk)");
    }
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis));
    syncMagicOrResonance();
  }

  /**
   * Prüft, ob bereits eine gewählte (nicht metatyp-vererbte) Quality mit {@link
   * QualityBonusType#GRANTS_MAGIC} aktiv ist — verhindert die gleichzeitige Wahl von Magier, Adept
   * und Mystischem Adept (SR4-Grundregelwerk: diese Gaben schließen sich gegenseitig aus).
   */
  private boolean hasSelectedGrantsMagicQuality() {
    return qualities.stream()
        .anyMatch(q -> q.source() == QualitySource.SELECTED
            && q.data().bonuses().contains(QualityBonusType.GRANTS_MAGIC));
  }

  /** Lädt eine gewählte Quality ohne Limit-Prüfung — nur für den Persistenz-Pfad. */
  public void loadSelectedQuality(@NonNull QualityData data, boolean gratis) {
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis));
    syncMagicOrResonance();
  }

  /**
   * Entfernt eine gewählte Quality. Falls dies die letzte Quality war, die Magic bzw. Resonance
   * freigeschaltet hat, wird das Sonderattribut zurückgesetzt (siehe {@link
   * #syncMagicOrResonance()}) — in der Erschaffungsphase entspricht das einer automatischen
   * Rückerstattung der dafür ausgegebenen BP, im Karrieremodus gibt es keine Karma-Rückerstattung.
   * Fertigkeiten, die dadurch nicht mehr zugänglich sind, werden auf 0 gesetzt (siehe {@link
   * #getSkillsLockedByRemovingQuality(String)}).
   */
  public void removeQuality(@NonNull String name) {
    List<Skill> locked = getSkillsLockedByRemovingQuality(name);
    qualities.removeIf(q -> isSelectedQuality(q, name));
    syncMagicOrResonance();
    locked.forEach(s -> {
      s.setRating(0);
      s.setSpecialization(null);
    });
  }

  /**
   * Aktive Fertigkeiten mit Wert > 0, die nach dem Entfernen dieser Quality nicht mehr zugänglich
   * wären (siehe {@link #isSkillAccessible(Skill)}), z.B. Hexerei nach Entfernen der Gabe "Magier"
   * oder Tasken nach Entfernen der Gabe "Technomant". {@link #removeQuality(String)} setzt genau
   * diese Fertigkeiten samt Spezialisierung auf 0, damit keine Werte stehen bleiben, die BP kosten,
   * aber nicht mehr nutzbar sind. Die UI fragt vorher nach, wenn die Liste nicht leer ist.
   */
  public @NonNull List<Skill> getSkillsLockedByRemovingQuality(@NonNull String name) {
    List<CharacterQuality> remaining = qualities.stream()
        .filter(q -> !isSelectedQuality(q, name))
        .toList();
    boolean magician = remaining.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.GRANTS_SPELLCASTING));
    boolean resonance = remaining.stream()
        .anyMatch(q -> q.data().bonuses().contains(QualityBonusType.GRANTS_RESONANCE));
    return skills.values().stream()
        .filter(s -> s.getRating() > 0 && !isSkillAccessible(s, magician, resonance))
        .toList();
  }

  private static boolean isSelectedQuality(@NonNull CharacterQuality q, @NonNull String name) {
    return q.source() == QualitySource.SELECTED && q.data().name().equals(name);
  }

  /**
   * Karma-Kosten im Karrieremodus, um eine neue Quality zu erwerben bzw. ein bestehendes
   * Handicap wieder loszuwerden: die doppelten GP-Kosten der Quality ("der Charakter muss
   * Karmapunkte in Höhe der doppelten Generierungspunkte der Gabe zahlen" / "durch die
   * Bezahlung der doppelten Generierungspunkte des Handicaps").
   */
  public int getQualityKarmaCost(@NonNull QualityData data) {
    return Math.abs(data.bp()) * 2;
  }

  public int getPositiveQualityBP() {
    return qualities.stream()
        .filter(q -> q.source() == QualitySource.SELECTED
            && q.data().type() == QualityType.POSITIVE
            && q.data().contributeToLimit()
            && !q.gratis())
        .mapToInt(q -> q.data().bp())
        .sum();
  }

  public int getNegativeQualityBP() {
    return qualities.stream()
        .filter(q -> q.source() == QualitySource.SELECTED
            && q.data().type() == QualityType.NEGATIVE
            && q.data().contributeToLimit()
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
    return switch (category) {
      case TECHNICAL -> isUneducated();
      case SOCIAL -> isUncouth();
      case PHYSICAL -> isInfirm();
      default -> false;
    };
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
   * verdoppeln ("Ein Charakter kann die Gesamtzahl an Wissensfertigkeitspunkten zu Beginn des
   * Spiels maximal verdoppeln"), und wie bei aktiven Fertigkeiten gilt entweder maximal eine
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
    return count5 == 0 && count6 < 1; // newValue == 6
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
   * {@value #MIN_RATING_FOR_SPECIALIZATION} (SR4-Grundregelwerk S. 109: "Genau wie bei den
   * Aktionsfertigkeiten kann sich ein Charakter auch bei Wissensfertigkeiten spezialisieren" —
   * dieselbe Mindestrating-Voraussetzung wie bei aktiven Fertigkeiten, siehe {@link
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
   * zu setzen oder zu ändern. SR4-Grundregelwerk S. 312: "Eine Spezialisierung zu erwerben kostet
   * 2 Karmapunkte" — gilt identisch für beide Fertigkeitsarten. Bewusst unabhängig von
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
   * Resonanzattribut nicht überschreiten (SR4-Grundregelwerk: "Die Grundstufe einer Komplexen Form kann
   * das Resonanzattribut des Technomancers nicht überschreiten"). Anders als die reinen
   * Erstellungs-Caps bei Fertigkeiten gilt dieser Cap **durchgehend**, auch im Karrieremodus, da er
   * keine Erschaffungsregel ist, sondern eine grundsätzliche Grenze. Maßgeblich ist der durch
   * Essenzverlust ggf. reduzierte Resonanzwert ({@link #getEffectiveMagicOrResonanceValue()}).
   */
  public boolean canRaiseComplexForm(@NonNull ComplexForm complexForm) {
    return hasResonance()
        && complexForm.getRating() + 1 <= getEffectiveMagicOrResonanceValue();
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
   * ¥. SR4-Grundregelwerk S. 110: "Für jeden GP, den der Spieler in Ausrüstung investiert, kann der
   * Charakter Waffen, Panzerung, Cyberware und andere Ausrüstung im Wert von 5.000 ¥ erwerben, bis zu
   * einem Maximum von 50 GP oder 250.000 ¥."
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
   * darf. Derzeit nur negatives Restgeld (GRW S. 111: "Wenn der Spieler zum Schluss einfach nicht
   * genügend Nuyen hat […], muss er dies bis zum Ende der Charaktererschaffung tun"). Essenz ≤ 0 ist
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
   * erster Nuyen-Log-Eintrag gebucht. Übriges Ressourcengeld verfällt ("Nuyen, die bei der Zuteilung
   * von Ressourcen nicht für Ausrüstung ausgegeben werden, werden nicht ins Spiel übernommen") und
   * fließt nur als Bonus in den Wurf ein. Ersetzt die frühere Controller-Logik, die das volle
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