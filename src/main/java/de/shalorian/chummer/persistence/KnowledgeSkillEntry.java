package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class KnowledgeSkillEntry {

  public @NonNull String name = "";
  public @NonNull String category = "";
  public int rating = 0;
  public boolean nativeLanguage = false;
  public @Nullable String specialization = null;

  public KnowledgeSkillEntry() {
  }

  public KnowledgeSkillEntry(@NonNull String name, @NonNull String category, int rating,
      boolean nativeLanguage, @Nullable String specialization) {
    this.name = name;
    this.category = category;
    this.rating = rating;
    this.nativeLanguage = nativeLanguage;
    this.specialization = specialization;
  }
}
