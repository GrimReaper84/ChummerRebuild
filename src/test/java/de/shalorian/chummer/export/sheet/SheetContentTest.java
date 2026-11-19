package de.shalorian.chummer.export.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.template.ArchetypeLoader;
import de.shalorian.chummer.utils.Numbers;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Texte des Charakterbogens (ADR-067). */
class SheetContentTest {

  private static Character samurai() {
    return Objects.requireNonNull(ArchetypeLoader.findByName("Straßensamurai")).create();
  }

  private static Character runner() {
    Character c = new Character("Kira", MetatypeDataLoader.find("Mensch"));
    c.getAttribute(AttributeType.AGI).setValue(4);
    c.getAttribute(AttributeType.INT).setValue(3);
    c.getAttribute(AttributeType.BOD).setValue(4);
    c.getAttribute(AttributeType.WIL).setValue(3);
    return c;
  }

  private static String field(List<SheetField> fields, String label) {
    return fields.stream().filter(f -> f.label().equals(label)).findFirst().orElseThrow().value();
  }

  @Test
  void headerAndAttributes() {
    Character c = samurai();
    SheetContent content = SheetContent.of(c);
    assertEquals(c.getName(), content.name());
    assertEquals(c.getMetatype().name(), field(content.identity(), "Metatyp"));
    assertEquals(Numbers.integer(c.getKarmaTotal()), field(content.resources(), "Karma gesamt"));
    assertEquals(Numbers.essence(c.getEssence()), field(content.attributes().get(2).fields(), "Essenz"));
    assertEquals(4, content.attributes().size());
    for (AttributeType type : AttributeType.values()) {
      List<SheetField> block = content.attributes().get(type.isPhysical() ? 0 : 1).fields();
      int natural = c.getAttribute(type).getValue();
      int augmented = c.getAugmentedAttributeValue(type);
      String expected = natural == augmented ? String.valueOf(natural) : natural + " (" + augmented + ")";
      assertEquals(expected, field(block, type.getDisplayName()), type.name());
    }
  }

  /** Nutzerentscheidung 2026-11-19: Nuyen, Karma aktuell, aktuelles Edge und Schaden bleiben leer. */
  @Test
  void pencilFieldsEmpty() {
    Character c = samurai();
    c.setPhysicalDamage(4);
    SheetContent content = SheetContent.of(c);
    assertEquals("", field(content.resources(), "Nuyen"));
    assertEquals("", field(content.resources(), "Karma aktuell"));
    assertEquals("", field(content.attributes().get(3).fields(), "Aktuelles Edge"));
    assertEquals(4, c.getPhysicalDamage(), "der Export ändert den Charakter nicht");
  }

  @Test
  void damageDoesNotLowerPools() {
    Character c = runner();
    Skill pistols = Objects.requireNonNull(c.getSkill("Pistolen"));
    pistols.setRating(3);
    String unhurt = SheetContent.of(c).activeSkills().getFirst().get(3);
    c.setPhysicalDamage(6);
    assertEquals(unhurt, SheetContent.of(c).activeSkills().getFirst().get(3));
  }

  @Test
  void skillsOnlyWithRatingAndPool() {
    Character c = runner();
    Skill pistols = Objects.requireNonNull(c.getSkill("Pistolen"));
    pistols.setRating(3);
    c.setSkillSpecialization(pistols, "Revolver");
    List<List<String>> skills = SheetContent.of(c).activeSkills();
    assertEquals(1, skills.size(), "nur Fertigkeiten mit Wert");
    assertEquals(List.of("Pistolen (Revolver)", "3", pistols.getLinkedAttribute(), "7 (9)"), skills.getFirst());
  }

  @Test
  void weaponsSplitRangedMelee() {
    SheetContent content = SheetContent.of(samurai());
    assertTrue(content.meleeWeapons().stream().anyMatch(row -> row.getFirst().equals("Waffenloser Angriff")));
    assertFalse(content.rangedWeapons().isEmpty());
    assertTrue(content.rangedWeapons().stream().noneMatch(row -> row.getFirst().equals("Waffenloser Angriff")));
    assertEquals(SheetLayout.RANGED_COLUMNS.size(), content.rangedWeapons().getFirst().size());
    assertEquals(SheetLayout.MELEE_COLUMNS.size(), content.meleeWeapons().getFirst().size());
  }

  /** GRW S. 193: je drei Kästchen eine Stufe des Verletzungsmodifikators. */
  @Test
  void woundMarkersFollowColumnModifier() {
    Character c = runner();
    SheetContent content = SheetContent.of(c);
    List<String> physical = content.physicalMarkers();
    assertEquals(c.getPhysicalBoxes(), physical.size());
    assertEquals("−1", physical.get(2));
    assertEquals("−2", physical.get(5));
    assertEquals("", physical.get(3));
    assertEquals(c.getPhysicalOverflowBoxes(), content.overflowBoxes());
    assertEquals(c.getStunBoxes(), content.stunMarkers().size());
  }

  /** Hohe Schmerztoleranz 2 (Straßensamurai) verschiebt den ersten Verletzungsmodifikator um zwei Kästchen. */
  @Test
  void woundMarkersFollowPainTolerance() {
    List<String> physical = SheetContent.of(samurai()).physicalMarkers();
    assertEquals("", physical.get(2));
    assertEquals("−1", physical.get(4));
  }

  /** Deutsche Sortierung: „Überreden“ steht vor „Wahrnehmung“, nicht hinter „Wurfwaffen“. */
  @Test
  void skillsSortedGerman() {
    Character c = runner();
    Objects.requireNonNull(c.getSkill("Überreden")).setRating(1);
    Objects.requireNonNull(c.getSkill("Wahrnehmung")).setRating(1);
    Objects.requireNonNull(c.getSkill("Wurfwaffen")).setRating(1);
    assertEquals(List.of("Überreden", "Wahrnehmung", "Wurfwaffen"),
        SheetContent.of(c).activeSkills().stream().map(List::getFirst).toList());
  }

  /** Eingebaute Teile stehen eingerückt unter ihrem Gerät; Foki nur in ihrer eigenen Tabelle. */
  @Test
  void gearWithInstalledPartsWithoutFoci() {
    Character c = runner();
    Gear glasses = c.getEquipment().addGear(gear("Brille", 2));
    glasses.install(gear("Smartlink", 0));
    c.getEquipment().addGear(gear("Kraftfokus", 2));
    SheetContent content = SheetContent.of(c);
    List<String> names = content.gear().stream().map(List::getFirst).toList();
    assertEquals(List.of(glasses.getDisplayName(), "– Smartlink"), names);
    assertEquals(1, content.foci().size());
  }

  private static Gear gear(String name, int rating) {
    return new Gear(Objects.requireNonNull(GearDataLoader.findByName(name), name), rating);
  }

  @Test
  void columnsMatchLayout() {
    SheetContent content = SheetContent.of(samurai());
    content.activeSkills().forEach(row -> assertEquals(SheetLayout.SKILL_COLUMNS.size(), row.size()));
    content.knowledgeSkills().forEach(row -> assertEquals(SheetLayout.SKILL_COLUMNS.size(), row.size()));
    content.implants().forEach(row -> assertEquals(SheetLayout.IMPLANT_COLUMNS.size(), row.size()));
    content.gear().forEach(row -> assertEquals(SheetLayout.GEAR_COLUMNS.size(), row.size()));
    content.armor().forEach(row -> assertEquals(SheetLayout.ARMOR_COLUMNS.size(), row.size()));
  }

  @Test
  void spiritRowShowsChosenOptions() {
    Character c = Objects.requireNonNull(ArchetypeLoader.findByName("Kampfmagier")).create();
    SpiritData fire = Objects.requireNonNull(SpiritDataLoader.findByName("Feuergeist"));
    c.getSpirits().add(new Spirit(fire, c.getSpirits().getCreationForce(), 1, true, List.of("Grauen")));
    assertEquals("Feuergeist (Grauen)", SheetContent.of(c).spirits().getFirst().getFirst());
  }
}
