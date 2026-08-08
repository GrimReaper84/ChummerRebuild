package de.shalorian.chummer.model.character.skill;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class KnowledgeSkill {

  public static final int MAX_RATING = 6;

  private final @NonNull String name;
  private final @NonNull KnowledgeSkillCategory category;
  private final boolean nativeLanguage;

  private int rating = 0;
  private @Nullable String specialization;

  public KnowledgeSkill(@NonNull String name, @NonNull KnowledgeSkillCategory category) {
    this(name, category, false);
  }

  public KnowledgeSkill(@NonNull String name, @NonNull KnowledgeSkillCategory category,
      boolean nativeLanguage) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("Name einer Wissensfertigkeit darf nicht leer sein");
    }
    this.name = name;
    this.category = category;
    this.nativeLanguage = nativeLanguage;
  }

  public @NonNull String getName() {
    return name;
  }

  public @NonNull KnowledgeSkillCategory getCategory() {
    return category;
  }

  /**
   * true, wenn dies die kostenlos deklarierte Muttersprache des Charakters ist (kein Pool- Verbrauch, kein Rating, Anzeige als „M").
   */
  public boolean isNativeLanguage() {
    return nativeLanguage;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    if (rating < 0 || rating > MAX_RATING) {
      throw new IllegalArgumentException(
          "%s: Rating %d liegt außerhalb [0/%d]".formatted(name, rating, MAX_RATING));
    }
    this.rating = rating;
  }

  /**
   * Freitext-Spezialisierung dieser Wissensfertigkeit, oder {@code null} wenn keine gesetzt ist.
   * Maximal eine Spezialisierung pro Fertigkeit (siehe {@link Skill#getSpecialization()} für die
   * identische Regel bei aktiven Fertigkeiten). Für die Muttersprache ({@link #isNativeLanguage()})
   * ist dieses Feld nicht vorgesehen.
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
