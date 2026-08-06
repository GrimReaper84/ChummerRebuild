package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

class KnowledgeSkillEntry {

  public @NonNull String name = "";
  public @NonNull String category = "";
  public int rating = 0;
  public boolean nativeLanguage = false;

  public KnowledgeSkillEntry() {
  }

  public KnowledgeSkillEntry(@NonNull String name, @NonNull String category, int rating,
      boolean nativeLanguage) {
    this.name = name;
    this.category = category;
    this.rating = rating;
    this.nativeLanguage = nativeLanguage;
  }
}
