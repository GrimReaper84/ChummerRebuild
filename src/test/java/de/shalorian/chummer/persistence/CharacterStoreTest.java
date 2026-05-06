package de.shalorian.chummer.persistence;

import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CharacterStoreTest {

    @TempDir
    File tempDir;

    private File tempFile() {
        return new File(tempDir, "charakter.json");
    }

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void roundtripPreservesBasicFields() throws IOException {
        Character original = mensch("Razorwing");
        original.setCreated(true);

        CharacterStore.save(original, tempFile());
        Character loaded = CharacterStore.load(tempFile());

        assertEquals("Razorwing", loaded.getName());
        assertEquals("Mensch", loaded.getMetatype().name());
        assertTrue(loaded.isCreated());
    }

    @Test
    void roundtripPreservesAttributeValues() throws IOException {
        Character original = mensch("Test");
        original.getAttribute(AttributeType.BOD).setValue(4);
        original.getAttribute(AttributeType.LOG).setValue(5);

        CharacterStore.save(original, tempFile());
        Character loaded = CharacterStore.load(tempFile());

        assertEquals(4, loaded.getAttribute(AttributeType.BOD).getValue());
        assertEquals(5, loaded.getAttribute(AttributeType.LOG).getValue());
    }

    @Test
    void roundtripPreservesSkillRatings() throws IOException {
        Character original = mensch("Test");
        original.getSkill("Bogenschießen").setRating(3);

        CharacterStore.save(original, tempFile());
        Character loaded = CharacterStore.load(tempFile());

        assertEquals(3, loaded.getSkill("Bogenschießen").getRating());
    }

    @Test
    void roundtripPreservesDecoupledGroups() throws IOException {
        Character original = mensch("Test");
        original.raiseGroup("Nahkampf");
        original.decoupleGroup("Nahkampf");

        CharacterStore.save(original, tempFile());
        Character loaded = CharacterStore.load(tempFile());

        assertFalse(loaded.isGroupCoupled("Nahkampf"));
    }

    @Test
    void roundtripPreservesLogs() throws IOException {
        Character original = mensch("Test");
        original.addKarmaEntry(new LogEntry("Mission", 10, LocalDate.of(2026, 1, 15)));
        original.addKarmaEntry(new LogEntry("Attribut", -6, LocalDate.of(2026, 2, 1)));
        original.addNuyenEntry(new LogEntry("Startkapital", 8000, LocalDate.of(2026, 1, 15)));

        CharacterStore.save(original, tempFile());
        Character loaded = CharacterStore.load(tempFile());

        assertEquals(2, loaded.getKarmaLog().size());
        assertEquals(4, loaded.getKarmaTotal());
        assertEquals(1, loaded.getNuyenLog().size());
        assertEquals(8000, loaded.getNuyenTotal());
    }
}