package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import org.junit.jupiter.api.Test;
import static de.shalorian.chummer.model.character.attribute.AttributeType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk S. 106: max. die Hälfte des Gesamtbudgets für Attribute, und höchstens ein
 * Attribut darf auf das natürliche Maximum gesteigert werden. Beide Caps gelten nur während der
 * Charaktererschaffung, nicht im Karrieremodus.
 */
class CharacterAttributeCapsTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void attributeBpIsSumOfAllAttributeCosts() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(4); // 30 BP
        c.getAttribute(STR).setValue(3); // 20 BP

        assertEquals(50, c.getAttributeBP());
    }

    @Test
    void noAttributeMaximizedInitially() {
        Character c = mensch("Test");
        assertEquals(0, c.getMaximizedAttributeCount());
    }

    @Test
    void onlyOneAttributeMayBeMaximizedAtCreation() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6); // Mensch-Maximum, BOD ist bereits maximiert
        c.getAttribute(AGI).setValue(5); // einen Schritt vor dem Maximum

        assertFalse(c.canRaiseAttributeAtCreation(AGI)); // würde ein zweites Attribut maximieren
    }

    @Test
    void raisingBelowMaximumIsAllowedRegardlessOfMaximizedCount() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6); // bereits maximiert
        c.getAttribute(AGI).setValue(2); // Steigerung 2 -> 3 maximiert AGI nicht

        assertTrue(c.canRaiseAttributeAtCreation(AGI));
    }

    @Test
    void attributeBudgetCapBlocksRaiseBeyondHalfOfTotalBp() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6); // 65 BP
        c.getAttribute(STR).setValue(6); // 65 BP
        c.getAttribute(REA).setValue(6); // 65 BP
        assertEquals(195, c.getAttributeBP());

        assertFalse(c.canRaiseAttributeAtCreation(CHA)); // 195 + 10 = 205 > 200
    }

    @Test
    void attributeBudgetCapAllowsRaiseWithinLimit() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6); // 65 BP
        c.getAttribute(STR).setValue(6); // 65 BP
        assertEquals(130, c.getAttributeBP());

        assertTrue(c.canRaiseAttributeAtCreation(CHA)); // 130 + 10 = 140 <= 200
    }

    @Test
    void capsDoNotApplyInCareerMode() {
        Character c = mensch("Test");
        c.getAttribute(BOD).setValue(6);
        c.getAttribute(STR).setValue(6);
        c.getAttribute(REA).setValue(6); // attributeBP = 195, würde CHA sonst blockieren
        c.setCreated(true);

        assertTrue(c.canRaiseAttributeAtCreation(CHA));
    }
}
