package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSkillTest {

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new KnowledgeSkill("  ", KnowledgeSkillCategory.STREET));
    }

    @Test
    void ratingWithinBoundsIsAccepted() {
        KnowledgeSkill skill = new KnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        skill.setRating(KnowledgeSkill.MAX_RATING);

        assertEquals(KnowledgeSkill.MAX_RATING, skill.getRating());
    }

    @Test
    void ratingOutsideBoundsIsRejected() {
        KnowledgeSkill skill = new KnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertThrows(IllegalArgumentException.class, () -> skill.setRating(-1));
        assertThrows(IllegalArgumentException.class,
                () -> skill.setRating(KnowledgeSkill.MAX_RATING + 1));
    }

    @Test
    void defaultConstructorIsNotNativeLanguage() {
        KnowledgeSkill skill = new KnowledgeSkill("Deutsch", KnowledgeSkillCategory.LANGUAGE);

        assertFalse(skill.isNativeLanguage());
    }

    @Test
    void nativeLanguageFlagIsStored() {
        KnowledgeSkill skill = new KnowledgeSkill("Englisch", KnowledgeSkillCategory.LANGUAGE, true);

        assertTrue(skill.isNativeLanguage());
    }
}
