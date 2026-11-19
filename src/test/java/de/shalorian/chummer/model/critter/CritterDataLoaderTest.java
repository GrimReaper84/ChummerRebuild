package de.shalorian.chummer.model.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.SkillDataLoader;
import de.shalorian.chummer.model.template.TemplateEntry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CritterDataLoaderTest {

  @Test
  void loadsAllRulebookCritters() {
    List<CritterData> all = CritterDataLoader.getAll();
    assertEquals(19, all.size());
    assertEquals(5, all.stream().filter(c -> c.category() == CritterCategory.ORDINARY).count());
    assertEquals(10, all.stream().filter(c -> c.category() == CritterCategory.PARACRITTER).count());
    assertEquals(4, all.stream().filter(c -> c.category() == CritterCategory.DRACOFORM).count());
  }

  @Test
  void barghestAsInTheRulebook() {
    CritterData barghest = CritterDataLoader.findByName("Barghest");
    assertNotNull(barghest);
    assertEquals(7, barghest.attribute(AttributeType.BOD));
    assertEquals(4, barghest.magic());
    assertEquals(2, barghest.passes());
    assertEquals("15/50", barghest.movement());
    assertTrue(barghest.powers().contains("Lähmendes Heulen"));
    assertEquals(344, barghest.page());
  }

  /** Initiative = Reaktion + Intuition steht im GRW bei jedem Critter; die Werte wurden daran gegengeprüft. */
  @Test
  void everyPowerWeaknessAndSkillIsKnown() {
    List<String> missing = new ArrayList<>();
    for (CritterData critter : CritterDataLoader.getAll()) {
      critter.powers().stream().filter(p -> CritterPowerDataLoader.findForEntry(p) == null)
          .forEach(p -> missing.add(critter.name() + ": Kraft " + p));
      critter.weaknesses().stream().filter(w -> CritterWeaknessDataLoader.findForEntry(w) == null)
          .forEach(w -> missing.add(critter.name() + ": Schwäche " + w));
      for (TemplateEntry skill : critter.skills()) {
        boolean known = switch (skill.kind()) {
          case "skill" -> SkillDataLoader.createFreshSkills().stream().anyMatch(s -> s.getName().equals(skill.name()));
          case "group" -> SkillDataLoader.createFreshSkills().stream()
              .anyMatch(s -> skill.name().equals(s.getGroupName()));
          default -> true;
        };
        if (!known) {
          missing.add(critter.name() + ": Fertigkeit " + skill.name());
        }
      }
    }
    assertTrue(missing.isEmpty(), String.join(", ", missing));
  }
}
