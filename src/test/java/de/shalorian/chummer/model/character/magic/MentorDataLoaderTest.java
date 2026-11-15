package de.shalorian.chummer.model.character.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.situation.SituationKind;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Schutzpatrone des GRW (S. 234–237, am Bild geprüft). */
class MentorDataLoaderTest {

  private static MentorData find(String name) {
    return Objects.requireNonNull(MentorDataLoader.findByName(name), name);
  }

  @Test
  void nineteenMentorsFromTheRulebook() {
    assertEquals(19, MentorDataLoader.getAll().size());
  }

  @Test
  void bearHealsAndResistsPhysicalDamage() {
    assertEquals(List.of(new MentorBonus(SituationKind.SPELL_CATEGORY, "Heilung", 2),
        new MentorBonus(SituationKind.DAMAGE_RESISTANCE, "", 2)), find("Bär").bonuses());
  }

  @Test
  void catChoosesAcrobaticsOrInfiltration() {
    MentorData cat = find("Katze");
    assertEquals(List.of("Katze (Akrobatik)", "Katze (Infiltration)"), cat.choiceLabels());
    assertEquals(new MentorBonus(SituationKind.SKILL, "Infiltration", 2),
        cat.bonusesFor("Katze (Infiltration)").getLast());
    assertEquals(cat, MentorDataLoader.findByChoice("Katze (Akrobatik)"));
    assertNull(MentorDataLoader.findByChoice("Katze"));
  }

  @Test
  void describeListsAdvantagesAndDrawbacks() {
    // rechnender Nachteil (Feuerbringer, S. 236) und gewählter Vorteil (Katze)
    assertEquals("Vorteile: +2 Spruch- und Ritualzauberei (Manipulation), +2 Herbeirufen, Binden, Verbannen "
        + "(Feuergeist)\nNachteile: −1 Spruch- und Ritualzauberei (Illusion)", find("Feuerbringer").describe("Feuerbringer"));
    assertTrue(find("Katze").describe("Katze (Infiltration)").contains("+2 Infiltration"));
    assertTrue(find("Adler").describe("Adler").contains("\nNachteile: Bekommt das Handicap Allergie"));
  }

  @Test
  void moonMaidenHasAPenaltyOnCombatSpells() {
    assertEquals(new MentorBonus(SituationKind.SPELL_CATEGORY, "Kampf", -1), find("Mondin").bonuses().getLast());
  }
}
