package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static de.shalorian.chummer.model.character.AttributeType.*;
import static org.junit.jupiter.api.Assertions.*;

class CharacterKnowledgeSkillTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void addAndRemoveKnowledgeSkill() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertEquals(1, c.getKnowledgeSkills().size());

        c.removeKnowledgeSkill("Straßenkultur");
        assertTrue(c.getKnowledgeSkills().isEmpty());
    }

    @Test
    void duplicateNameIsRejected() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertThrows(IllegalArgumentException.class,
                () -> c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.INTEREST));
    }

    @Test
    void pointPoolIsIntPlusLogTimesThree() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(4);

        assertEquals(21, c.getKnowledgeSkillPointPool()); // (3 + 4) * 3
    }

    @Test
    void noOverflowBpWithinPool() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(3); // Pool = 18
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        c.getKnowledgeSkills().get(0).setRating(6);

        assertEquals(0, c.getKnowledgeSkillOverflowBP());
    }

    @Test
    void overflowCostsTwoBpPerPointFlat() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);
        c.getKnowledgeSkills().get(0).setRating(3);
        c.getKnowledgeSkills().get(1).setRating(4); // Summe 7, Überschuss 1

        assertEquals(2, c.getKnowledgeSkillOverflowBP()); // 1 Punkt * 2 BP
    }

    @Test
    void uneducatedDoublesPoolConsumptionForAcademicAndProfessional() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);
        c.getKnowledgeSkills().get(0).setRating(4); // zählt doppelt: 8 Punkte verbraucht

        assertEquals(8, c.getKnowledgeSkillPointsUsed());
        assertEquals(4, c.getKnowledgeSkillOverflowBP()); // Überschuss 2 * 2 BP
    }

    @Test
    void uneducatedDoesNotAffectStreetOrLanguageCategories() {
        Character c = mensch("Test");
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertFalse(c.isKnowledgeSkillCostDoubled(KnowledgeSkillCategory.STREET));
    }

    @Test
    void spentBpIncludesKnowledgeSkillOverflow() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        int baseline = c.getSpentBP();

        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        c.getKnowledgeSkills().get(0).setRating(6);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);
        c.getKnowledgeSkills().get(1).setRating(2); // Summe 8, Überschuss 2 -> 4 BP

        assertEquals(baseline + 4, c.getSpentBP());
    }

    @Test
    void incrementBpIsZeroWhilePoolHasRoom() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(3); // Pool = 18
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertEquals(0, c.getKnowledgeSkillIncrementBP(c.getKnowledgeSkills().get(0)));
    }

    @Test
    void incrementBpIsTwoOncePoolIsExhausted() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        c.getKnowledgeSkills().get(0).setRating(6); // Pool exakt ausgeschöpft
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.INTEREST);

        assertEquals(2, c.getKnowledgeSkillIncrementBP(c.getKnowledgeSkills().get(1)));
    }

    @Test
    void karmaCostForNewSkillIsTwo() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);

        assertEquals(2, c.getKnowledgeSkillKarmaCost(c.getKnowledgeSkills().get(0)));
    }

    @Test
    void karmaCostFollowsRatingPlusOneStaffel() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);

        skill.setRating(1);
        assertEquals(2, c.getKnowledgeSkillKarmaCost(skill)); // (1+1)*1

        skill.setRating(2);
        assertEquals(3, c.getKnowledgeSkillKarmaCost(skill)); // (2+1)*1
    }

    @Test
    void uneducatedDoublesKarmaPriceNotPoolInCareerMode() {
        Character c = mensch("Test");
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);

        assertEquals(4, c.getKnowledgeSkillKarmaCost(c.getKnowledgeSkills().get(0))); // 2 * 2
    }

    @Test
    void nativeLanguageIsFreeAndSeparateFromPool() {
        Character c = mensch("Test");
        c.setNativeLanguage("Deutsch");

        assertEquals("Deutsch", c.getNativeLanguage().getName());
        assertTrue(c.getNativeLanguage().isNativeLanguage());
        assertEquals(0, c.getKnowledgeSkillPointsUsed());
    }

    @Test
    void settingNativeLanguageTwiceReplacesIt() {
        Character c = mensch("Test");
        c.setNativeLanguage("Deutsch");
        c.setNativeLanguage("Englisch");

        assertEquals(1, c.getKnowledgeSkills().stream().filter(KnowledgeSkill::isNativeLanguage).count());
        assertEquals("Englisch", c.getNativeLanguage().getName());
    }

    @Test
    void nativeLanguageHasNoInitialValueWhenUnset() {
        Character c = mensch("Test");

        assertNull(c.getNativeLanguage());
    }
}
