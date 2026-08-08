package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 109: Fertigkeitsgruppen sind bei der Erstellung auf Rating 4 gedeckelt.
 * Karrieremodus S. 314: eine komplett neue Fertigkeitsgruppe (Rating 0 -> 1) kostet pauschal
 * 10 Karma, keine Formel.
 */
class CharacterSkillGroupCapsTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void groupMayBeRaisedUpToFourAtCreation() {
        Character c = mensch("Test");
        for (int i = 0; i < 4; i++) {
            assertTrue(c.canRaiseGroupAtCreation("Nahkampf"));
            c.raiseGroup("Nahkampf");
        }

        assertEquals(4, c.getGroupRating("Nahkampf"));
    }

    @Test
    void groupCannotExceedFourAtCreation() {
        Character c = mensch("Test");
        for (int i = 0; i < 4; i++) {
            c.raiseGroup("Nahkampf");
        }

        assertFalse(c.canRaiseGroupAtCreation("Nahkampf")); // 4 -> 5 nicht erlaubt
    }

    @Test
    void groupCapDoesNotApplyInCareerMode() {
        Character c = mensch("Test");
        for (int i = 0; i < 4; i++) {
            c.raiseGroup("Nahkampf");
        }
        c.setCreated(true);

        assertTrue(c.canRaiseGroupAtCreation("Nahkampf"));
    }

    @Test
    void newGroupCostsTenKarmaFlat() {
        Character c = mensch("Test");

        assertEquals(10, c.getGroupKarmaCost("Nahkampf")); // Rating 0 -> 1, pauschal 10, nicht (0+1)*5=5
    }

    @Test
    void existingGroupUsesRatingFormula() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf"); // Rating 0 -> 1

        assertEquals(10, c.getGroupKarmaCost("Nahkampf")); // (1+1)*5 = 10
        c.raiseGroup("Nahkampf"); // Rating 1 -> 2
        assertEquals(15, c.getGroupKarmaCost("Nahkampf")); // (2+1)*5 = 15
    }
}
