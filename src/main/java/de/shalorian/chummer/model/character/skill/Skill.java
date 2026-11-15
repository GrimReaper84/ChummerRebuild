package de.shalorian.chummer.model.character.skill;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Skill {

  public static final int MAX_RATING = 6;
  private static final int BP_PER_RATING = 4;

  private final @NonNull String name;
  private final @NonNull String linkedAttribute;
  private final @NonNull SkillCategory category;
  private final @Nullable String groupName; // null wenn keine Gruppe
  private final boolean defaultable;
  /** Waffe bzw. Fahrzeug einer Exotischen Fertigkeit (GRW S. 148, 155), sonst {@code null}. */
  private final @Nullable String exoticTarget;
  /** Name der Grundfertigkeit; bei Exotik-Instanzen z.B. „Exotische Nahkampfwaffe“, sonst der eigene Name. */
  private final @NonNull String baseName;

  private int maxRating = MAX_RATING;
  private int rating = 0;
  private @Nullable String specialization;

  /**
   * @param linkedAttribute Abkürzung des verknüpften Attributs (z.B. "AGI", "LOG", oder "MAG" für
   *     magische Fertigkeiten). Bewusst als reiner Anzeigetext modelliert statt als {@link
   *     AttributeType}, da {@code MAG} ein {@link SpecialAttributeType} ist, kein {@link
   *     AttributeType} — das Feld wird ausschließlich zur Anzeige genutzt, nie zur Berechnung.
   */
  public Skill(@NonNull String name, @NonNull String linkedAttribute,
      @NonNull SkillCategory category, @Nullable String groupName) {
    this(name, linkedAttribute, category, groupName, true);
  }

  /**
   * @param defaultable ob ohne Fertigkeitswert gewürfelt werden darf (SR4-Grundregelwerk S. 146–148: bei den in der
   *                    Fertigkeitstabelle kursiv gesetzten Fertigkeiten nicht)
   */
  public Skill(@NonNull String name, @NonNull String linkedAttribute,
      @NonNull SkillCategory category, @Nullable String groupName, boolean defaultable) {
    this.name = name;
    this.linkedAttribute = linkedAttribute;
    this.category = category;
    this.groupName = groupName;
    this.defaultable = defaultable;
    this.exoticTarget = null;
    this.baseName = name;
  }

  /**
   * Instanz einer Exotischen Fertigkeit für genau eine Waffe bzw. ein Fahrzeug (GRW S. 148, 155): übernimmt Attribut,
   * Kategorie und die Ohne-Wert-Regel der Grundfertigkeit, heißt „Grundfertigkeit (Ziel)“ und startet mit Wert 0.
   *
   * @throws IllegalArgumentException wenn {@code template} keine exotische Grundfertigkeit ist oder das Ziel leer ist
   */
  public Skill(@NonNull Skill template, @NonNull String target) {
    if (!template.isExoticTemplate() || target.isBlank()) {
      throw new IllegalArgumentException("Keine exotische Grundfertigkeit oder leeres Ziel: " + template.getName());
    }
    String trimmed = target.trim();
    this.name = ExoticSkills.instanceName(template.getName(), trimmed);
    this.linkedAttribute = template.linkedAttribute;
    this.category = template.category;
    this.groupName = template.groupName;
    this.defaultable = template.defaultable;
    this.exoticTarget = trimmed;
    this.baseName = template.getName();
  }

  public @NonNull String getName() {
    return name;
  }

  /** Grundfertigkeit: bei Exotik-Instanzen die exotische Grundfertigkeit, sonst der eigene Name. */
  public @NonNull String getBaseName() {
    return baseName;
  }

  /** Waffe bzw. Fahrzeug einer Exotik-Instanz, sonst {@code null}. */
  public @Nullable String getExoticTarget() {
    return exoticTarget;
  }

  /** Eine der drei exotischen Grundfertigkeiten, die selbst keinen Wert bekommt (Platzhalter für die Instanzen). */
  public boolean isExoticTemplate() {
    return exoticTarget == null && ExoticSkills.isBase(name);
  }

  /** Exotische Fertigkeit, Grundzeile oder Instanz; beide haben keine Spezialisierung (GRW S. 148). */
  public boolean isExotic() {
    return ExoticSkills.isBase(baseName);
  }

  public @NonNull String getLinkedAttribute() {
    return linkedAttribute;
  }

  public @NonNull SkillCategory getCategory() {
    return category;
  }

  public @Nullable String getGroupName() {
    return groupName;
  }

  /** Ob eine Probe ohne Fertigkeitswert (nur Attribut − 1) erlaubt ist, siehe GRW S. 147. */
  public boolean isDefaultable() {
    return defaultable;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    if (rating < 0 || rating > maxRating) {
      throw new IllegalArgumentException(
          "%s: Rating %d liegt außerhalb [0/%d]".formatted(name, rating, maxRating));
    }
    this.rating = rating;
  }

  /** Höchstwert: {@value #MAX_RATING}, mit der Gabe Talentiert 7 (GRW S. 117). */
  public int getMaxRating() {
    return maxRating;
  }

  /** Setzt den Höchstwert (Talentiert); liegt der Wert darüber, wird er darauf gesenkt. */
  public void setMaxRating(int maxRating) {
    this.maxRating = maxRating;
    rating = Math.min(rating, maxRating);
  }

  /** BP-Kosten des aktuellen Werts; Stufen über {@value #MAX_RATING} kosten doppelt (Talentiert, GRW S. 117). */
  public int getBpCost(boolean doubled) {
    int cost = rating * BP_PER_RATING + Math.max(0, rating - MAX_RATING) * BP_PER_RATING;
    return doubled ? cost * 2 : cost;
  }

  /** BP-Kosten des nächsten Steigerungsschritts. */
  public int getBpCostPerRating(boolean doubled) {
    int cost = rating + 1 > MAX_RATING ? BP_PER_RATING * 2 : BP_PER_RATING;
    return doubled ? cost * 2 : cost;
  }

  /** Karma-Kosten des nächsten Schritts; über {@value #MAX_RATING} doppelt (Talentiert, GRW S. 117). */
  public int getKarmaCost(boolean doubled) {
    int cost = rating == 0 ? 4 : (rating + 1) * 2;
    if (rating + 1 > MAX_RATING) {
      cost *= 2;
    }
    return doubled ? cost * 2 : cost;
  }

  /**
   * Freitext-Spezialisierung dieser Fertigkeit, oder {@code null} wenn keine gesetzt ist. Es gibt
   * maximal eine Spezialisierung pro Fertigkeit (SR4-Grundregelwerk S. 312) — ein neuer Wert überschreibt einen
   * vorhandenen. Für gruppierte, noch gekoppelte Fertigkeiten prüft {@link
   * de.shalorian.chummer.model.character.Character#canSpecializeSkill(Skill)} die Zulässigkeit; dieses Feld selbst erzwingt das nicht.
   */
  public @Nullable String getSpecialization() {
    return specialization;
  }

  /**
   * Setzt die Spezialisierung. Ein leerer oder nur aus Leerzeichen bestehender Wert wird als
   * "keine Spezialisierung" ({@code null}) normalisiert.
   */
  public void setSpecialization(@Nullable String specialization) {
    this.specialization = (specialization == null || specialization.isBlank())
        ? null
        : specialization.trim();
  }
}