package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 109: 2 GP je Spezialisierung — fixer Preis pro spezialisierter
 * aktiver Fertigkeit, unabhängig vom Rating oder dem übrigen Budget.
 */
class CharacterActiveSkillSpecializationBpTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    private static Skill skill(Character c, String name) {
        Skill s = c.getSkill(name);
        assertNotNull(s, "Skill \"" + name + "\" nicht gefunden");
        return s;
    }

    @Test
    void unspecializedSkillsCostNothing() {
        Character c = mensch("Test");

        assertEquals(0, c.getActiveSkillSpecializationBP());
    }

    @Test
    void oneSpecializationCostsFlatTwoBp() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Projektilwaffen");
        bogen.setRating(1);
        c.setSkillSpecialization(bogen, "Langbogen");

        assertEquals(2, c.getActiveSkillSpecializationBP());
    }

    @Test
    void costIsFlatRegardlessOfRating() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Projektilwaffen");
        bogen.setRating(6);
        c.setSkillSpecialization(bogen, "Langbogen");

        assertEquals(2, c.getActiveSkillSpecializationBP()); // nicht rating-abhängig
    }

    @Test
    void multipleSpecializationsAddUp() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Projektilwaffen");
        bogen.setRating(1);
        c.setSkillSpecialization(bogen, "Langbogen");
        Skill wurfwaffen = skill(c, "Wurfwaffen");
        wurfwaffen.setRating(1);
        c.setSkillSpecialization(wurfwaffen, "Messer");

        assertEquals(4, c.getActiveSkillSpecializationBP());
    }

    @Test
    void spentBpIncludesActiveSkillSpecialization() {
        Character c = mensch("Test");
        int baseline = c.getSpentBP();
        Skill bogen = skill(c, "Projektilwaffen");
        bogen.setRating(1);
        c.setSkillSpecialization(bogen, "Langbogen");

        // +1 Rating (4 BP) + Spezialisierung (2 BP)
        assertEquals(baseline + 4 + 2, c.getSpentBP());
    }

    @Test
    void clearingSpecializationRemovesCost() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Projektilwaffen");
        bogen.setRating(1);
        c.setSkillSpecialization(bogen, "Langbogen");
        c.setSkillSpecialization(bogen, null);

        assertEquals(0, c.getActiveSkillSpecializationBP());
    }
}
