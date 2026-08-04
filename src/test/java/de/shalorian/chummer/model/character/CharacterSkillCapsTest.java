package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 109: bei der Charaktererschaffung entweder eine einzige Fertigkeit auf
 * Wert 6 (übrige max. 4), oder zwei Fertigkeiten auf Wert 5 (übrige max. 4) — nicht beides
 * gleichzeitig. Gilt nur für ungruppierte/entkoppelte Fertigkeiten, nicht im Karrieremodus.
 */
class CharacterSkillCapsTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    private static Skill skill(Character c, String name) {
        Skill s = c.getSkill(name);
        assertNotNull(s, "Skill \"" + name + "\" nicht gefunden");
        return s;
    }

    @Test
    void raisingUpToFourIsAlwaysAllowed() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Bogenschießen");
        bogen.setRating(3);

        assertTrue(c.canRaiseSkillAtCreation(bogen)); // 3 -> 4
    }

    @Test
    void firstSkillMayReachSix() {
        Character c = mensch("Test");
        Skill bogen = skill(c, "Bogenschießen");
        bogen.setRating(5);

        assertTrue(c.canRaiseSkillAtCreation(bogen)); // 5 -> 6, noch keine andere Fertigkeit auf 5/6
    }

    @Test
    void secondSkillMayNotAlsoReachSix() {
        Character c = mensch("Test");
        skill(c, "Bogenschießen").setRating(6);
        Skill sprengstoff = skill(c, "Sprengstoff");
        sprengstoff.setRating(5);

        assertFalse(c.canRaiseSkillAtCreation(sprengstoff)); // bereits eine Fertigkeit auf 6
    }

    @Test
    void twoSkillsMayReachFive() {
        Character c = mensch("Test");
        skill(c, "Bogenschießen").setRating(5);
        Skill wurfwaffen = skill(c, "Wurfwaffen");
        wurfwaffen.setRating(4);

        assertTrue(c.canRaiseSkillAtCreation(wurfwaffen)); // 4 -> 5, erst die zweite Fertigkeit auf 5
    }

    @Test
    void thirdSkillMayNotReachFive() {
        Character c = mensch("Test");
        skill(c, "Bogenschießen").setRating(5);
        skill(c, "Wurfwaffen").setRating(5);
        Skill exotisch = skill(c, "Exotische Fernkampfwaffe");
        exotisch.setRating(4);

        assertFalse(c.canRaiseSkillAtCreation(exotisch)); // schon zwei Fertigkeiten auf 5
    }

    @Test
    void raisingToSixIsBlockedWhenAnotherSkillIsAtFive() {
        Character c = mensch("Test");
        skill(c, "Bogenschießen").setRating(5);
        Skill wurfwaffen = skill(c, "Wurfwaffen");
        wurfwaffen.setRating(5);

        // Regeltext: "kann das Spiel also nicht gleichzeitig mit einer Fertigkeit auf Wert 6 und
        // zwei Fertigkeiten auf Wert 5 beginnen" -> 5->6 ist ausgeschlossen, solange eine andere
        // Fertigkeit noch auf 5 steht (der 5er-Modus ist bereits "gewählt").
        assertFalse(c.canRaiseSkillAtCreation(wurfwaffen));
    }

    @Test
    void capsDoNotApplyInCareerMode() {
        Character c = mensch("Test");
        skill(c, "Bogenschießen").setRating(6);
        Skill sprengstoff = skill(c, "Sprengstoff");
        sprengstoff.setRating(5);
        c.setCreated(true);

        assertTrue(c.canRaiseSkillAtCreation(sprengstoff));
    }
}
