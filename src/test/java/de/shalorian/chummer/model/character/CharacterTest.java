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
    void zwergCostsTwentyFiveBp() {
        // SR4-Grundregelwerk S. 105/106: Metatyp-Attribute-Tabelle und durchgerechnetes
        // Beispiel ("Ein Zwerg kostet 25 GP") bestätigen 25, nicht 40.
        Character zwerg = new Character("Test", MetatypeDataLoader.find("Zwerg"));
        assertEquals(25, zwerg.getSpentBP());
    }

    @Test
    void zwergBodyRangeMatchesRulebookTable() {
        // GRW-Tabelle "Metatyp-Attribute" S. 105: Zwerg KON = 2-7, nicht 3-8.
        Character zwerg = new Character("Test", MetatypeDataLoader.find("Zwerg"));
        assertEquals(2, zwerg.getAttribute(BOD).getMin());
        assertEquals(7, zwerg.getAttribute(BOD).getMax());
    }

    @Test
    void orkIntuitionRangeMatchesRulebookTable() {
        // GRW-Tabelle "Metatyp-Attribute" S. 105: Ork INT = 1-6, nicht 1-5.
        Character ork = new Character("Test", MetatypeDataLoader.find("Ork"));
        assertEquals(1, ork.getAttribute(INT).getMin());
        assertEquals(6, ork.getAttribute(INT).getMax());
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
    void groupKarmaCostIsNewRatingTimesFive() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf"); // Rating 0 → 1
        assertEquals(10, c.getGroupKarmaCost("Nahkampf")); // (1 + 1) * 5 = 10
        c.raiseGroup("Nahkampf"); // Rating 1 → 2
        assertEquals(15, c.getGroupKarmaCost("Nahkampf")); // (2 + 1) * 5 = 15
    }

    @Test
    void spentBpIncludesSkillRating() {
        Character c = mensch("Test");
        c.getSkill("Bogenschießen").setRating(3); // 3 × 4 BP = 12 BP
        assertEquals(12, c.getSpentBP());
    }

    @Test
    void groupSkillUsesGroupPrice() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf"); // Rating 1, 3 Skills in Gruppe → 1 × 10 BP
        assertEquals(10, c.getSpentBP());
    }

    @Test
    void decouplingChangesGroupToIndividualPrice() {
        Character c = mensch("Test");
        c.raiseGroup("Nahkampf"); // Rating 1 → 10 BP als Gruppe
        c.decoupleGroup("Nahkampf");
        // 3 Skills × Rating 1 × 4 BP = 12 BP
        assertEquals(12, c.getSpentBP());
    }

    @Test
    void karmaAndNuyenTotalSumAllEntries() {
        Character c = mensch("Test");
        c.addKarmaEntry(new LogEntry("Missionserfolg", 10, java.time.LocalDate.now()));
        c.addKarmaEntry(new LogEntry("Attribut erhöht", -5, java.time.LocalDate.now()));
        c.addNuyenEntry(new LogEntry("Startkapital", 6000, java.time.LocalDate.now()));
        c.addNuyenEntry(new LogEntry("Ausrüstung", -2000, java.time.LocalDate.now()));
        assertEquals(5, c.getKarmaTotal());
        assertEquals(4000, c.getNuyenTotal());
    }

    @Test
    void findUnknownMetatypeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> MetatypeDataLoader.find("GibtsNicht"));
    }

    @Test
    void remainingBpDecreasesWithAttributeSpending() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(3);  // 2 Punkte × 10 BP = 20 BP
        c.getAttribute(AGI).setValue(4);  // 3 Punkte × 10 BP = 30 BP
        assertEquals(400 - 50, c.getRemainingBP());
    }
}