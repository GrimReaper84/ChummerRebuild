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

  public int getBpCost(boolean doubled) {
    int cost = rating * BP_PER_RATING;
    return doubled ? cost * 2 : cost;
  }

  public int getBpCostPerRating(boolean doubled) {
    return doubled ? BP_PER_RATING * 2 : BP_PER_RATING;
  }

  public int getKarmaCost(boolean doubled) {
    int cost = rating == 0 ? 4 : (rating + 1) * 2;
    return doubled ? cost * 2 : cost;
  }
}