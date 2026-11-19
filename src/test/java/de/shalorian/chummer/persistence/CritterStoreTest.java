package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.critter.CritterData;
import de.shalorian.chummer.model.critter.CritterDataLoader;
import de.shalorian.chummer.model.critter.CritterTemplate;
import de.shalorian.chummer.model.critter.CritterTraits;
import de.shalorian.chummer.model.npc.NpcGroup;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CritterStoreTest {

  /** Akzeptanzkriterium: jeder GRW-Critter lässt sich anlegen, speichern und laden. */
  @Test
  void everyCritterBuildsAndSurvivesARoundTrip(@TempDir File dir) throws IOException {
    for (CritterData data : CritterDataLoader.getAll()) {
      NpcGroup group = new CritterTemplate(data).create();
      File file = new File(dir, "critter.json");
      NpcGroupStore.save(group, file);
      Character loaded = NpcGroupStore.load(file).getGrunt().getCharacter();
      Character original = group.getGrunt().getCharacter();
      for (AttributeType type : AttributeType.values()) {
        assertEquals(original.getAttribute(type).getValue(), loaded.getAttribute(type).getValue(), data.name());
      }
      assertEquals(original.getCritterTraits().getPowers(), loaded.getCritterTraits().getPowers(), data.name());
      for (var skill : original.getSkills()) {
        assertEquals(skill.getRating(), loaded.getSkill(skill.getName()).getRating(), data.name() + " " + skill.getName());
        assertEquals(skill.getMaxRating(), loaded.getSkill(skill.getName()).getMaxRating(), data.name());
      }
      assertEquals(original.isMagician(), loaded.isMagician(), data.name());
    }
  }

  @Test
  void critterPackSurvivesSaveAndLoad(@TempDir File dir) throws IOException {
    NpcGroup pack = new CritterTemplate(Objects.requireNonNull(CritterDataLoader.findByName("Donnervogel"))).create();
    pack.setMemberCount(3);
    pack.getMembers().getFirst().setDamage(4, pack.getGrunt().getMonitorBoxes());
    Character bird = pack.getGrunt().getCharacter();
    bird.getAttribute(AttributeType.REA).setValue(8);
    Objects.requireNonNull(bird.getCritterTraits()).addPower("Grauen");
    File file = new File(dir, "donnervoegel.json");

    NpcGroupStore.save(pack, file);
    NpcGroup loaded = NpcGroupStore.load(file);

    Character c = loaded.getGrunt().getCharacter();
    CritterTraits traits = c.getCritterTraits();
    assertNotNull(traits);
    assertEquals("Donnervogel", c.getMetatype().name());
    assertEquals(8, c.getAttribute(AttributeType.REA).getValue());
    assertEquals(9, c.getAttribute(AttributeType.REA).getMax());
    assertTrue(traits.getPowers().contains("Grauen"));
    assertEquals(3, traits.getSpecialSkills().get("Fliegen"));
    assertEquals(4, c.getMagicOrResonance().getValue());
    assertEquals(4, loaded.getMembers().getFirst().getDamage());
    assertEquals(3, loaded.getMemberCount());
    assertEquals(6, c.getSkill("Waffenloser Kampf").getMaxRating());
  }
}
