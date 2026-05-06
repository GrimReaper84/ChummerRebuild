package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Skill {

  public static final int MAX_RATING = 6;
  private static final int BP_PER_RATING = 4;

  private final @NonNull String name;
  private final @NonNull AttributeType linkedAttribute;
  private final @NonNull SkillCategory category;
  private final @Nullable String groupName; // null wenn keine Gruppe

  private int rating = 0;

  public Skill(@NonNull String name, @NonNull AttributeType linkedAttribute,
      @NonNull SkillCategory category, @Nullable String groupName) {
    this.name = name;
    this.linkedAttribute = linkedAttribute;
    this.category = category;
    this.groupName = groupName;
  }

  public @NonNull String getName() {
    return name;
  }

  public @NonNull AttributeType getLinkedAttribute() {
    return linkedAttribute;
  }

  public @NonNull SkillCategory getCategory() {
    return category;
  }

  public @Nullable String getGroupName() {
    return groupName;
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

  public int getBpCost() {
    return rating * BP_PER_RATING;
  }

  /**
   * Karma-Kosten um diesen Skill um 1 zu erhöhen (neuer Rating × 2).
   */
  public int getKarmaCost() {
    return (rating + 1) * 2;
  }
}