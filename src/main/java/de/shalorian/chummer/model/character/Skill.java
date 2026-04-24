package de.shalorian.chummer.model.character;

import org.jspecify.annotations.Nullable;

public class Skill {

    public static final int MAX_RATING    = 6;
    private static final int BP_PER_RATING = 4;

    private final String        name;
    private final AttributeType linkedAttribute;
    private final SkillCategory category;
    private final @Nullable String groupName; // null wenn keine Gruppe

    private int rating = 0;

    public Skill(String name, AttributeType linkedAttribute,
                 SkillCategory category, @Nullable String groupName) {
        this.name            = name;
        this.linkedAttribute = linkedAttribute;
        this.category        = category;
        this.groupName       = groupName;
    }

    public String          getName()            { return name; }
    public AttributeType   getLinkedAttribute() { return linkedAttribute; }
    public SkillCategory   getCategory()        { return category; }
    public @Nullable String getGroupName()       { return groupName; }
    public int           getRating()          { return rating; }

    public void setRating(int rating) {
        if (rating < 0 || rating > MAX_RATING)
            throw new IllegalArgumentException(
                "%s: Rating %d liegt außerhalb [0/%d]".formatted(name, rating, MAX_RATING));
        this.rating = rating;
    }

    public int getBpCost() { return rating * BP_PER_RATING; }
}