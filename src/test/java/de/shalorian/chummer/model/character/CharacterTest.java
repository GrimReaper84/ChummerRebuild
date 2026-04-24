package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static de.shalorian.chummer.model.character.AttributeType.*;
import static org.junit.jupiter.api.Assertions.*;

class CharacterTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void newCharacterStartsAtRacialMinimum() {
        Character c = mensch("Test");
        assertEquals(1, c.getAttribute(BOD).getValue());
        assertEquals(1, c.getAttribute(CHA).getValue());
    }

    @Test
    void elfStartsWithRacialMinimumForAgiAndCha() {
        Character elf = new Character("Elara", MetatypeDataLoader.find("Elf"));
        assertEquals(2, elf.getAttribute(AGI).getValue(), "Elf AGI Minimum ist 2");
        assertEquals(3, elf.getAttribute(CHA).getValue(), "Elf CHA Minimum ist 3");
    }

    @Test
    void attributeBpCostIsZeroAtRacialMinimum() {
        Character c = mensch("Test");
        assertEquals(0, c.getAttribute(BOD).getBpCost());
    }

    @Test
    void attributeBpCostIsTenPerPointAboveMinimum() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(4);
        assertEquals(30, c.getAttribute(BOD).getBpCost()); // (4-1) * 10
    }

    @Test
    void metatypeBpCostIsIncludedInSpentBP() {
        Character elf = new Character("Test", MetatypeDataLoader.find("Elf"));
        assertEquals(30, elf.getSpentBP()); // Nur Metatyp-Kosten, keine Attributpunkte
    }

    @Test
    void totalBpIs400() {
        Character c = mensch("Test");
        assertEquals(400, c.getRemainingBP()); // Mensch 0 BP, Attribute alle auf Min
    }

    @Test
    void trollAttributeLimitsAreRespected() {
        Character troll = new Character("Grog", MetatypeDataLoader.find("Troll"));
        CharacterAttribute bod = troll.getAttribute(BOD);
        assertEquals(5,  bod.getMin());
        assertEquals(10, bod.getMax());
        assertThrows(IllegalArgumentException.class, () -> bod.setValue(4));
        assertThrows(IllegalArgumentException.class, () -> bod.setValue(11));
    }

    @Test
    void metatypeChangeResetsAttributes() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6);
        c.setMetatype(MetatypeDataLoader.find("Troll"));
        assertEquals(5, c.getAttribute(BOD).getValue(), "Nach Metatyp-Wechsel startet BOD neu bei Troll-Minimum");
    }

    @Test
    void remainingBpDecreasesWithAttributeSpending() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(3);  // 2 Punkte × 10 BP = 20 BP
        c.getAttribute(AGI).setValue(4);  // 3 Punkte × 10 BP = 30 BP
        assertEquals(400 - 50, c.getRemainingBP());
    }
}