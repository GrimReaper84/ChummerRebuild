package de.shalorian.chummer.model.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.weapon.Weapon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Beispiel-Schergen des GRW (S. 325–328), Werte am Seitenbild geprüft. Ganzkörperpanzerung über der Konstitution
 * senkt im Modell Geschicklichkeit und Reaktion (Panzerungsbehinderung); die Werteblöcke im GRW rechnen das nicht ein.
 */
class NpcTemplateLoaderTest {

  private static NpcGroup group(String name) {
    return Objects.requireNonNull(NpcTemplateLoader.findByName(name), name).create();
  }

  private static Character leader(String name) {
    NpcStatblock leader = group(name).getLeader();
    assertNotNull(leader, name);
    return leader.getCharacter();
  }

  @Test
  void sevenGroupsOnePerRating() {
    List<NpcTemplate> all = NpcTemplateLoader.getAll();
    assertEquals(7, all.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), all.stream().map(NpcTemplate::rating).sorted().toList());
  }

  @Test
  void everyTemplateBuildsWithoutProblems() {
    List<String> problems = new ArrayList<>();
    for (NpcTemplate template : NpcTemplateLoader.getAll()) {
      NpcGroup g = template.create(problems);
      assertNotNull(g.getLeader(), template.name());
    }
    assertEquals(List.of(), problems);
  }

  @Test
  void everyGruntMatchesMonitorAndHasAWeaponPool() {
    // ZM laut GRW: 10 für die Stufen 0–4, 11 für Rote Samurai und Tir Ghosts
    Map<String, Integer> monitor = Map.of("Humanis Policlub Schlägertrupp", 10, "Halloweener-Straßengang", 10,
        "Konzernsicherheitseinheit", 10, "Lone Star Polizeieinheit", 10, "Triadenbande", 10,
        "Abteilung der Roten Samurai", 11, "Tir Ghosts", 11);
    for (NpcTemplate template : NpcTemplateLoader.getAll()) {
      NpcGroup g = template.create();
      Character grunt = g.getGrunt().getCharacter();
      assertEquals(monitor.get(template.name()), g.getGrunt().getMonitorBoxes(), template.name());
      assertTrue(grunt.getEquipment().getWeapons().stream()
          .anyMatch(w -> grunt.getEquipment().getWeaponDicePool(w).isPresent()), template.name());
      assertEquals("SR4", template.source());
    }
  }

  @Test
  void initiativeWithoutArmorEncumbranceMatchesStatblock() {
    // INI laut GRW (Klammerwert, wo Implantate oder Kräfte wirken); ohne Ganzkörperpanzerung keine Behinderung
    Map<String, Integer> initiative = Map.of("Humanis Policlub Schlägertrupp", 4, "Halloweener-Straßengang", 6,
        "Konzernsicherheitseinheit", 7, "Lone Star Polizeieinheit", 8, "Triadenbande", 8);
    initiative.forEach((name, ini) -> assertEquals(ini, group(name).getGrunt().getCharacter().getInitiative(), name));
  }

  @Test
  void humanisGruntMatchesStatblock() {
    // K3 G2 R2 S4 C1 I2 L1 W2, ESS 6, INI 4, ID 1, ZM 10
    Character c = group("Humanis Policlub Schlägertrupp").getGrunt().getCharacter();
    assertEquals(4, c.getAttribute(AttributeType.STR).getValue());
    assertEquals(6.0, c.getEssence());
    assertEquals(4, c.getInitiative());
    assertEquals(1, c.getInitiativePasses());
    assertEquals(10, NpcRules.monitorBoxes(c));
  }

  @Test
  void implantsGiveEssenceAndInitiative() {
    assertEquals(4.7, leader("Halloweener-Straßengang").getEssence());
    Character loneStar = leader("Lone Star Polizeieinheit");
    assertEquals(4.3, loneStar.getEssence());
    assertEquals(6, loneStar.getAugmentedAttributeValue(AttributeType.REA));
    assertEquals(11, loneStar.getInitiative());
    Character samurai = group("Abteilung der Roten Samurai").getGrunt().getCharacter();
    assertEquals(2.05, samurai.getEssence());
    assertEquals(6, samurai.getAugmentedAttributeValue(AttributeType.STR));
    assertEquals(3, samurai.getInitiativePasses());
    assertEquals(11, NpcRules.monitorBoxes(samurai));
  }

  @Test
  void magicLeaders() {
    Character mage = leader("Konzernsicherheitseinheit");
    assertEquals(3, mage.getEffectiveMagicOrResonanceValue());
    assertEquals(6, mage.getSpells().size());
    Character adept = leader("Triadenbande");
    assertEquals(4, adept.getEffectiveMagicOrResonanceValue());
    assertEquals(6, adept.getAugmentedAttributeValue(AttributeType.REA));
    assertEquals(2, adept.getInitiativePasses());
  }

  @Test
  void tirGhostsAreElves() {
    Character ghost = group("Tir Ghosts").getGrunt().getCharacter();
    assertEquals("Elf", ghost.getMetatype().name());
    assertEquals(3.9, ghost.getEssence());
    assertEquals(3.8, leader("Tir Ghosts").getEssence());
  }

  @Test
  void weaponPoolComesFromSkillAndAgility() {
    // Lone Star: Pistolen 3 + Geschicklichkeit 4
    Character c = group("Lone Star Polizeieinheit").getGrunt().getCharacter();
    Weapon colt = c.getEquipment().getWeapons().stream().filter(w -> w.getData().name().equals("Colt America L36"))
        .findFirst().orElseThrow();
    assertEquals(7, c.getEquipment().getWeaponDicePool(colt).orElseThrow().dice());
    assertNull(group("Humanis Policlub Schlägertrupp").getLeaderHint());
  }
}
