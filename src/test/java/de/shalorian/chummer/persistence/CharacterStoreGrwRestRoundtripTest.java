package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.reputation.Reputation;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Spielleiter-Eingaben zur Reputation und das Portrait überstehen Speichern und Laden (ADR-059). */
class CharacterStoreGrwRestRoundtripTest {

  @Test
  void reputationRoundtrip() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addKarmaEntry(new LogEntry("Run", 50, LocalDate.of(2026, 11, 15), LogCategory.MANUAL));
    c.getReputation().setStreetCredBonus(1);
    c.getReputation().setNotorietyBonus(3);
    c.burnStreetCred();
    c.getReputation().setAwarenessAdjustment(-1);

    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    Reputation r = loaded.getReputation();
    assertEquals(1, r.getStreetCredBonus());
    assertEquals(3, r.getNotorietyBonus());
    assertEquals(2, r.getBurnedStreetCred());
    assertEquals(-1, r.getAwarenessAdjustment());
    assertEquals(c.getStreetCred(), loaded.getStreetCred());
    assertEquals(c.getNotoriety(), loaded.getNotoriety());
  }

  @Test
  void portraitRoundtrip() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
    c.getBiography().setPortrait(png);
    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    assertArrayEquals(png, loaded.getBiography().getPortrait());
    c.getBiography().setPortrait(null);
    assertNull(CharacterStore.restore(CharacterStore.snapshot(c)).getBiography().getPortrait());
  }
}
