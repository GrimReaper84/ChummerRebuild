package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 312: "Eine Spezialisierung zu erwerben kostet 2 Karmapunkte" — gilt
 * identisch für aktive Fertigkeiten und Wissensfertigkeiten, ohne Uneducated/Uncouth/Infirm-
 * Verdopplung (anders als reguläre Rating-Steigerungen).
 */
class CharacterSpecializationKarmaCostTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void specializationKarmaCostIsFlatTwo() {
        Character c = mensch("Test");

        assertEquals(2, c.getSpecializationKarmaCost());
    }

    @Test
    void specializationKarmaCostIsNotDoubledByUneducated() {
        Character c = mensch("Test");
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);

        // Regressionstest: darf NICHT die Verdopplungslogik von Skill.getKarmaCost()/
        // getKnowledgeSkillKarmaCost() erben.
        assertEquals(2, c.getSpecializationKarmaCost());
    }

    @Test
    void specializationKarmaCostIsIndependentOfSkillCategory() {
        Character c = mensch("Test");
        c.addQuality(QualityDataLoader.findByName("Ungebildet"), false);
        c.addKnowledgeSkill("Firmenkunde", KnowledgeSkillCategory.PROFESSIONAL);

        // Zum Vergleich: die reguläre Rating-Karma-Kosten-Methode verdoppelt hier tatsächlich.
        assertEquals(4, c.getKnowledgeSkillKarmaCost(c.getKnowledgeSkills().getFirst()));
        // Die Spezialisierungskosten bleiben davon unberührt.
        assertEquals(2, c.getSpecializationKarmaCost());
    }
}
