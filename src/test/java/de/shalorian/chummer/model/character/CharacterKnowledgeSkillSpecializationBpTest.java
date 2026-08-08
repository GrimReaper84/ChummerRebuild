package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import org.junit.jupiter.api.Test;
import static de.shalorian.chummer.model.character.attribute.AttributeType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 109/154: "Jede Spezialisierung einer Wissensfertigkeit kostet 1
 * Wissens-Fertigkeitspunkt (oder 2 GP)" — flacher +1-Beitrag zum gemeinsamen Pool, unabhängig von
 * der Uneducated-Verdopplung des Rating-Beitrags.
 */
class CharacterKnowledgeSkillSpecializationBpTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void specializationAddsOnePointToPoolUsage() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(3); // Pool = 18
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(2);

        assertEquals(2, c.getKnowledgeSkillPointsUsed());
        c.setKnowledgeSkillSpecialization(skill, "Bandenkriminalität");
        assertEquals(3, c.getKnowledgeSkillPointsUsed());
    }

    @Test
    void specializationContributionIsNotDoubledByUneducated() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(2); // zählt doppelt: 4 Punkte
        c.setKnowledgeSkillSpecialization(skill, "Ares Macrotechnology");

        // Rating-Beitrag verdoppelt (2*2=4), Spezialisierungs-Beitrag bleibt flach +1 -> 5
        assertEquals(5, c.getKnowledgeSkillPointsUsed());
    }

    @Test
    void specializationCostsNoBpWhilePoolHasRoom() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(3); // Pool = 18
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(2);

        assertEquals(0, c.getKnowledgeSkillSpecializationBP());
    }

    @Test
    void specializationCostsTwoBpOncePoolIsExhausted() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(6); // Pool exakt ausgeschöpft

        assertEquals(2, c.getKnowledgeSkillSpecializationBP());
    }

    @Test
    void overflowBpReflectsSpecializationOnceApplied() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(6); // Pool exakt ausgeschöpft
        c.setKnowledgeSkillSpecialization(skill, "Bandenkriminalität");

        assertEquals(2, c.getKnowledgeSkillOverflowBP()); // 1 Überschusspunkt * 2 BP
    }
}
