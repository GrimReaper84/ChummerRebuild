package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.KnowledgeSkill;
import de.shalorian.chummer.model.character.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.QualityData;
import de.shalorian.chummer.model.character.QualityDataLoader;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreTest {

  @TempDir
  File tempDir;

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private File tempFile() {
    return new File(tempDir, "charakter.json");
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
  void roundtripPreservesKnowledgeSkills() throws IOException {
    Character original = mensch("Test");
    original.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
    original.getKnowledgeSkills().get(0).setRating(3);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(1, loaded.getKnowledgeSkills().size());
    KnowledgeSkill loadedSkill = loaded.getKnowledgeSkills().get(0);
    assertEquals("Straßenkultur", loadedSkill.getName());
    assertEquals(KnowledgeSkillCategory.STREET, loadedSkill.getCategory());
    assertEquals(3, loadedSkill.getRating());
  }

  @Test
  void roundtripPreservesNativeLanguage() throws IOException {
    Character original = mensch("Test");
    original.setNativeLanguage("Deutsch");

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals("Deutsch", loaded.getNativeLanguage().getName());
    assertTrue(loaded.getNativeLanguage().isNativeLanguage());
    assertEquals(0, loaded.getKnowledgeSkillPointsUsed());
  }

  @Test
  void roundtripPreservesEdgeValue() throws IOException {
    Character original = mensch("Test");
    original.getEdge().setValue(5);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(5, loaded.getEdge().getValue());
  }

  @Test
  void roundtripPreservesUnlockedMagic() throws IOException {
    Character original = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Magier");
    assertNotNull(magier);
    original.addQuality(magier, false);
    original.getMagicOrResonance().setValue(4);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertTrue(loaded.hasMagic());
    assertFalse(loaded.hasResonance());
    assertEquals(4, loaded.getMagicOrResonance().getValue());
  }

  @Test
  void roundtripOmitsMagicWhenNotUnlocked() throws IOException {
    Character original = mensch("Test");

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertFalse(loaded.hasMagic());
    assertFalse(loaded.hasResonance());
  }

  @Test
  void roundtripPreservesActiveSkillSpecialization() throws IOException {
    Character original = mensch("Test");
    original.getSkill("Bogenschießen").setRating(1);
    original.setSkillSpecialization(original.getSkill("Bogenschießen"), "Langbögen (Öko-Kunststoff)");

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals("Langbögen (Öko-Kunststoff)", loaded.getSkill("Bogenschießen").getSpecialization());
  }

  @Test
  void roundtripOmitsSpecializationWhenUnset() throws IOException {
    Character original = mensch("Test");
    original.getSkill("Bogenschießen").setRating(1);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertNull(loaded.getSkill("Bogenschießen").getSpecialization());
  }

  @Test
  void roundtripPreservesKnowledgeSkillSpecialization() throws IOException {
    Character original = mensch("Test");
    original.addKnowledgeSkill("Straßenkultur", KnowledgeSkillCategory.STREET);
    KnowledgeSkill skill = original.getKnowledgeSkills().get(0);
    skill.setRating(2);
    original.setKnowledgeSkillSpecialization(skill, "Bandenkriminalität");

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals("Bandenkriminalität", loaded.getKnowledgeSkills().get(0).getSpecialization());
  }

  @Test
  void roundtripPreservesContacts() throws IOException {
    Character original = mensch("Test");
    original.addContact("Sam", "Fixer im Barrens, mag keine Überraschungen");
    Contact contact = original.getContacts().get(0);
    contact.setConnection(4);
    contact.setLoyalty(3);
    original.addContact("Sam", null); // Duplikat-Name bewusst erlaubt

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(2, loaded.getContacts().size());
    Contact loadedFirst = loaded.getContacts().get(0);
    assertEquals("Sam", loadedFirst.getName());
    assertEquals("Fixer im Barrens, mag keine Überraschungen", loadedFirst.getDescription());
    assertEquals(4, loadedFirst.getConnection());
    assertEquals(3, loadedFirst.getLoyalty());
    assertNull(loaded.getContacts().get(1).getDescription());
  }

  @Test
  void roundtripPreservesLogs() throws IOException {
    Character original = mensch("Test");
    original.addKarmaEntry(new LogEntry("Mission", 10, LocalDate.of(2026, Month.JANUARY, 15)));
    original.addKarmaEntry(new LogEntry("Attribut", -6, LocalDate.of(2026, Month.FEBRUARY, 1)));
    original.addNuyenEntry(new LogEntry("Startkapital", 8000, LocalDate.of(2026, Month.JANUARY, 15)));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(2, loaded.getKarmaLog().size());
    assertEquals(4, loaded.getKarmaTotal());
    assertEquals(1, loaded.getNuyenLog().size());
    assertEquals(8000, loaded.getNuyenTotal());
  }
}