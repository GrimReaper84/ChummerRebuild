package de.shalorian.chummer.model.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.npc.NpcTemplate;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CharacterSpecTest {

  private static TemplateEntry entry(String kind, String name, int rating, int amount, String detail) {
    return new TemplateEntry(kind, name, rating, amount, detail, List.of());
  }

  private static CharacterSpec magician(List<TemplateEntry> entries) {
    Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      attributes.put(type, 3);
    }
    List<TemplateEntry> all = new ArrayList<>();
    all.add(entry("quality", "Zauberer", 0, 0, ""));
    all.addAll(entries);
    return new CharacterSpec("Mensch", attributes, 4, all, "");
  }

  @Test
  void creationModeBuildsUnfinishedCharacterWithNewEntryKinds() {
    CharacterSpec spec = magician(List.of(
        entry("contact", "Schieber", 3, 2, ""),
        entry("language", "Deutsch", 0, 0, "muttersprache"),
        entry("language", "Englisch", 2, 0, ""),
        entry("lifestyle", "MIDDLE", 0, 1, ""),
        entry("resources", "", 10, 0, ""),
        entry("edge", "", 3, 0, ""),
        entry("focus", "Bindungsfokus", 1, 0, "Erdgeist"),
        entry("spirit", "Erdgeist", 4, 2, "")));
    List<String> problems = new ArrayList<>();

    Character c = spec.build("Test", BuildMode.CREATION, problems);

    assertEquals(List.of(), problems);
    assertFalse(c.isCreated());
    assertFalse(c.isGmMode());
    Contact contact = c.getContacts().getLast();
    assertEquals(3, contact.getConnection());
    assertEquals(2, contact.getLoyalty());
    assertEquals("Deutsch", c.getNativeLanguage().getName());
    assertTrue(c.getKnowledgeSkills().stream().anyMatch(k -> k.getName().equals("Englisch") && k.getRating() == 2));
    assertEquals(LifestyleLevel.MIDDLE, c.getEquipment().getLifestyle().getLevel());
    assertEquals(10, c.getNuyenBP());
    assertEquals(3, c.getEdge().getValue());
    Gear focus = c.getEquipment().getGear().getLast();
    assertTrue(focus.isBound());
    assertEquals(1, c.getSpirits().getAll().size());
    assertTrue(c.getSpentBP() > 0);
  }

  @Test
  void singleSkillOfAGroupBreaksTheGroupOnlyInCreation() {
    CharacterSpec spec = magician(List.of(entry("skill", "Pistolen", 3, 0, "")));

    assertTrue(spec.build("Erschaffung", BuildMode.CREATION, new ArrayList<>()).getDecoupledGroups()
        .contains("Feuerwaffen"));
    assertTrue(spec.build("Scherge", BuildMode.GM, new ArrayList<>()).getDecoupledGroups().isEmpty());
  }

  @Test
  void npcTemplatesKeepTheirGroups() {
    for (NpcTemplate template : NpcTemplateLoader.getAll()) {
      assertTrue(template.create().getGrunt().getCharacter().getDecoupledGroups().isEmpty(), template.name());
    }
  }

  @Test
  void gmModeMarksCharacterAsFinishedStatblock() {
    Character c = magician(List.of()).build("Scherge", BuildMode.GM, new ArrayList<>());

    assertTrue(c.isCreated());
    assertTrue(c.isGmMode());
  }
}
