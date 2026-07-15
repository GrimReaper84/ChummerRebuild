package de.shalorian.chummer.model.character;

import java.util.ArrayList;
import java.util.Collections;
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

  private final Map<AttributeType, CharacterAttribute> attributes;
  private final Map<String, Skill> skills;
  private final Set<String> decoupledGroups;
  private final List<CharacterQuality> qualities = new ArrayList<>();
  private final List<LogEntry> karmaLog = new ArrayList<>();
  private final List<LogEntry> nuyenLog = new ArrayList<>();
  private @NonNull String name;
  private @NonNull MetatypeData metatype;
  private boolean created = false;
  private int nuyenBP = 0;

  public Character(@NonNull String name, @NonNull MetatypeData metatype) {
    this.name = name;
    this.metatype = metatype;
    this.attributes = new EnumMap<>(AttributeType.class);
    this.decoupledGroups = new HashSet<>();
    initAttributes(metatype);
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

  public void setMetatype(@NonNull MetatypeData metatype) {
    this.metatype = metatype;
    initAttributes(metatype);
    initMetatypeQualities(metatype);
  }

  public @NonNull Map<AttributeType, CharacterAttribute> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public @NonNull CharacterAttribute getAttribute(@NonNull AttributeType type) {
    return Objects.requireNonNull(attributes.get(type));
  }

  // --- Skills ---

  public @NonNull List<Skill> getSkills() {
    return List.copyOf(skills.values());
  }

  public @Nullable Skill getSkill(@NonNull String name) {
    return skills.get(name);
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

  public int getGroupKarmaCost(@NonNull String groupName) {
    return (getGroupRating(groupName) + 1) * 5;
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
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis));
  }

  /** Lädt eine gewählte Quality ohne Limit-Prüfung — nur für den Persistenz-Pfad. */
  public void loadSelectedQuality(@NonNull QualityData data, boolean gratis) {
    qualities.add(new CharacterQuality(data, QualitySource.SELECTED, gratis));
  }

  public void removeQuality(@NonNull String name) {
    qualities.removeIf(q -> q.source() == QualitySource.SELECTED && q.data().name().equals(name));
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

  // --- BP ---
  public int getSpentBP() {
    int attrBP = attributes.values().stream().mapToInt(CharacterAttribute::getBpCost).sum();

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

    return metatype.bpCost() + attrBP + skillBP + nuyenBP + qualityBP;
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