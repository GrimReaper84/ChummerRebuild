package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Spezialisieren einer noch gekoppelten aktiven Fertigkeit im Karrieremodus löst die
 * Fertigkeitsgruppe auf ("Attach an EventHandler for the RatingChanged and SpecializationChanged
 * Events" / "If the Skill is Grouped, verify that the user wants to break the Group" —
 * frmCareer.cs:4337-4364). Die Mitglieder behalten ihr aktuelles Gruppen-Rating als Einzelrating.
 */
class CharacterSpecializationGroupDecoupleTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    private static Skill skill(Character c, String name) {
        Skill s = c.getSkill(name);
        assertNotNull(s, "Skill \"" + name + "\" nicht gefunden");
        return s;
    }

    @Test
    void specializingCoupledSkillInCareerModeDecouplesGroup() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf"); // Rating 0 -> 1
        c.setCreated(true);
        Skill klingen = skill(c, "Klingenwaffen");

        assertTrue(c.isGroupCoupled("Nahkampf"));

        c.setSkillSpecialization(klingen, "Katana");

        assertFalse(c.isGroupCoupled("Nahkampf"));
        assertTrue(c.getDecoupledGroups().contains("Nahkampf"));
    }

    @Test
    void groupMembersKeepRatingAfterDecoupleViaSpecialization() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf");
        c.raiseGroup("Nahkampf"); // Rating 0 -> 2
        c.setCreated(true);
        Skill klingen = skill(c, "Klingenwaffen");

        c.setSkillSpecialization(klingen, "Katana");

        assertEquals(2, skill(c, "Klingenwaffen").getRating());
        assertEquals(2, skill(c, "Knüppel").getRating());
        assertEquals(2, skill(c, "Waffenloser Kampf").getRating());
    }

    @Test
    void specializingUncoupledSkillDoesNotAffectOtherGroups() {
        Character c = mensch("Test");
        c.raiseGroup("Feuerwaffen");
        c.setCreated(true);
        Skill bogen = skill(c, "Projektilwaffen"); // kein Gruppen-Mitglied
        bogen.setRating(1);

        c.setSkillSpecialization(bogen, "Langbogen");

        assertTrue(c.isGroupCoupled("Feuerwaffen"));
    }

    @Test
    void specializingDuringCreationDoesNotDecoupleBecauseItIsNotAllowedAtAll() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf");
        Skill klingen = skill(c, "Klingenwaffen");

        assertFalse(c.canSpecializeSkill(klingen));
        // Kein setSkillSpecialization-Aufruf hier — die UI verdrahtet den Button ohnehin nur,
        // wenn canSpecializeSkill() true liefert (siehe SkillTabBuilder.addSkillRow()).
    }
}
