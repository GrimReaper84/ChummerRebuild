package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static de.shalorian.chummer.model.character.AttributeType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mindestrating-1-Voraussetzung für Spezialisierungen bei aktiven Fertigkeiten UND
 * Wissensfertigkeiten (User-Entscheidung 2026-08-06: GRW-konform für beide Fertigkeitsarten
 * durchgesetzt, auch wenn das C#-Original das nur bei aktiven Fertigkeiten tut). Zusätzlich:
 * keine Spezialisierung für noch gekoppelte aktive Fertigkeiten während der Erstellung
 * (SR4-Grundregelwerk: Fertigkeitsgruppen selbst sind nicht spezialisierbar).
 */
class CharacterSpecializationEligibilityTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    private static Skill skill(Character c, String name) {
        Skill s = c.getSkill(name);
        assertNotNull(s, "Skill \"" + name + "\" nicht gefunden");
        return s;
    }

    @Test
    void activeSkillWithRatingZeroCannotBeSpecialized() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Bogenschießen");

        assertFalse(c.canSpecializeSkill(bogen));
    }

    @Test
    void activeSkillWithRatingOneCanBeSpecialized() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Bogenschießen");
        bogen.setRating(1);

        assertTrue(c.canSpecializeSkill(bogen));
    }

    @Test
    void coupledGroupSkillCannotBeSpecializedDuringCreation() {
        Character c = mensch("Test");
        Skill klingen = skill(c, "Klingen"); // Mitglied der Gruppe "Nahkampf"
        c.raiseGroup("Nahkampf");

        assertFalse(c.canSpecializeSkill(klingen));
    }

    @Test
    void coupledGroupSkillCanBeSpecializedInCareerMode() {
        Character c = mensch("Test");
        Skill klingen = skill(c, "Klingen");
        c.raiseGroup("Nahkampf");
        c.setCreated(true);

        assertTrue(c.canSpecializeSkill(klingen));
    }

    @Test
    void decoupledGroupSkillCanBeSpecializedDuringCreation() {
        Character c = mensch("Test");
        Skill klingen = skill(c, "Klingen");
        c.raiseGroup("Nahkampf");
        c.decoupleGroup("Nahkampf");

        assertTrue(c.canSpecializeSkill(klingen));
    }

    @Test
    void knowledgeSkillWithRatingZeroCannotBeSpecialized() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);

        assertFalse(c.canSpecializeKnowledgeSkill(c.getKnowledgeSkills().get(0)));
    }

    @Test
    void knowledgeSkillWithRatingOneCanBeSpecialized() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(3);
        c.getAttribute(LOG).setValue(3);
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        c.getKnowledgeSkills().get(0).setRating(1);

        assertTrue(c.canSpecializeKnowledgeSkill(c.getKnowledgeSkills().get(0)));
    }

    @Test
    void nativeLanguageCannotBeSpecialized() {
        Character c = mensch("Test");
        c.setNativeLanguage("Deutsch");

        assertFalse(c.canSpecializeKnowledgeSkill(c.getNativeLanguage()));
    }

    @Test
    void knowledgeSkillSpecializationBlockedWhenPoolCapExceeded() {
        Character c = mensch("Test");
        c.getAttribute(INT).setValue(1);
        c.getAttribute(LOG).setValue(1); // Pool = 6, Obergrenze = 12
        c.addKnowledgeSkill("A", KnowledgeSkillCategory.STREET);
        c.addKnowledgeSkill("B", KnowledgeSkillCategory.STREET);
        c.addKnowledgeSkill("C", KnowledgeSkillCategory.STREET);
        c.getKnowledgeSkills().get(0).setRating(4);
        c.getKnowledgeSkills().get(1).setRating(4);
        KnowledgeSkill dritte = c.getKnowledgeSkills().get(2);
        dritte.setRating(4); // Summe bereits 12 (Maximum)

        assertFalse(c.canSpecializeKnowledgeSkill(dritte)); // 12 + 1 = 13 > 12
    }

    @Test
    void eligibilityCapsDoNotApplyInCareerMode() {
        Character c = mensch("Test");
        c.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
        KnowledgeSkill skill = c.getKnowledgeSkills().get(0);
        skill.setRating(1);
        c.setCreated(true);

        assertTrue(c.canSpecializeKnowledgeSkill(skill));
    }
}
