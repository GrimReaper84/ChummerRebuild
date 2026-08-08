package de.shalorian.chummer.model.character.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 312: "Ein Charakter darf nur eine Spezialisierung pro Fertigkeit
 * besitzen" — strukturell durch ein Einzelfeld erzwungen.
 */
class SkillSpecializationTest {

    private static Skill skill() {
        return new Skill("Bogenschießen", "AGI", SkillCategory.COMBAT, null);
    }

    @Test
    void specializationIsInitiallyAbsent() {
        assertNull(skill().getSpecialization());
    }

    @Test
    void blankSpecializationIsNormalizedToNull() {
        Skill s = skill();
        s.setSpecialization("   ");

        assertNull(s.getSpecialization());
    }

    @Test
    void nullSpecializationClearsIt() {
        Skill s = skill();
        s.setSpecialization("Langbogen");
        s.setSpecialization(null);

        assertNull(s.getSpecialization());
    }

    @Test
    void specializationIsTrimmedAndOverwritable() {
        Skill s = skill();
        s.setSpecialization("  Langbogen  ");

        assertEquals("Langbogen", s.getSpecialization());

        s.setSpecialization("Kurzbogen");
        assertEquals("Kurzbogen", s.getSpecialization()); // max. 1 Spezialisierung, wird ersetzt
    }
}
