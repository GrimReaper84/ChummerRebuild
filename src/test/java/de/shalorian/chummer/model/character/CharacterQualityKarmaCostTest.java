package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk (Karrieremodus): eine neue Gabe erwerben bzw. ein bestehendes Handicap
 * loswerden kostet die doppelten GP-Kosten der Quality in Karma.
 */
class CharacterQualityKarmaCostTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void karmaCostIsDoubleThePositiveQualityBpCost() {
        Character c = mensch("Test");
        QualityData nachtsicht = QualityDataLoader.findByName("Nachtsicht");
        assertNotNull(nachtsicht);

        assertEquals(20, c.getQualityKarmaCost(nachtsicht)); // 10 GP * 2
    }

    @Test
    void karmaCostIsDoubleTheAbsoluteHandicapBpCost() {
        Character c = mensch("Test");
        QualityData ungebildet = QualityDataLoader.findByName("Ungebildet");
        assertNotNull(ungebildet);

        assertEquals(40, c.getQualityKarmaCost(ungebildet)); // |-20| GP * 2
    }
}
