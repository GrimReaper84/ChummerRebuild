package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class SkillEntry {

  public @NonNull String name = "";
  public int rating = 0;
  public @Nullable String specialization = null;

  public SkillEntry() {
  }

  public SkillEntry(@NonNull String name, int rating, @Nullable String specialization) {
    this.name = name;
    this.rating = rating;
    this.specialization = specialization;
  }
}
