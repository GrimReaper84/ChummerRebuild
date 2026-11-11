package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
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
        QualityData gedaechtnis = QualityDataLoader.findByName("Fotografisches Gedächtnis");
        assertNotNull(gedaechtnis);

        assertEquals(20, c.getQualityKarmaCost(gedaechtnis)); // 10 GP * 2
    }

    @Test
    void karmaCostIsDoubleTheAbsoluteHandicapBpCost() {
        Character c = mensch("Test");
        QualityData ungebildet = QualityDataLoader.findByName("Ungebildet");
        assertNotNull(ungebildet);

        assertEquals(40, c.getQualityKarmaCost(ungebildet)); // |-20| GP * 2
    }
}
