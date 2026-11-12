package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.spirit.Spirits;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Geister und Sprites am Charakter (GRW S. 112/113, 213, 222, 281). */
class CharacterSpiritsTest {

  private static SpiritData data(String name) {
    return Objects.requireNonNull(SpiritDataLoader.findByName(name), name);
  }

  private static Character mage(int magic, int summoning, int charisma) {
    Character c = withGift("Zauberer", magic);
    Objects.requireNonNull(c.getSkill("Herbeirufen")).setRating(summoning);
    c.getAttribute(AttributeType.CHA).setValue(charisma);
    return c;
  }

  private static Character technomancer(int resonance, int compiling, int charisma) {
    Character c = withGift("Technomancer", resonance);
    Objects.requireNonNull(c.getSkill("Kompilieren")).setRating(compiling);
    c.getAttribute(AttributeType.CHA).setValue(charisma);
    return c;
  }

  @Test
  void creationExampleCostsOneBpPerService() {
    // S. 112: Magie 3, Luft- und Feuergeist mit je 3 Diensten, beide Kraft 3 → 6 GP
    Character c = mage(3, 3, 3);
    int before = c.getSpentBP();
    c.getSpirits().add(new Spirit(data("Luftgeist"), 3, 3, true));
    c.getSpirits().add(new Spirit(data("Feuergeist"), 3, 3, true));
    assertEquals(6, c.getSpentBP() - before);
  }

  @Test
  void creationLimits() {
    Character c = mage(4, 2, 2);
    Spirits spirits = c.getSpirits();
    assertEquals("Kraft bei der Erschaffung = Magie (4)", spirits.getAddProblem(data("Erdgeist"), 3, 1, true));
    assertEquals("Bei der Erschaffung 1 bis 2 Dienste (Herbeirufen 2)",
        spirits.getAddProblem(data("Erdgeist"), 4, 3, true));
    spirits.add(new Spirit(data("Erdgeist"), 4, 2, true));
    spirits.add(new Spirit(data("Wassergeist"), 4, 1, true));
    assertEquals("Höchstens 2 Geister gebunden (Charisma)", spirits.getAddProblem(data("Luftgeist"), 4, 1, true));
    // herbeigerufene zählen nicht und kosten nichts
    int bp = c.getSpentBP();
    spirits.add(new Spirit(data("Luftgeist"), 4, 0, false));
    assertEquals(bp, c.getSpentBP());
    Spirit summoned = spirits.getEntries(SpiritKind.SPIRIT).get(2);
    assertEquals("Höchstens 2 Geister gebunden (Charisma)", spirits.getBindProblem(summoned));
  }

  @Test
  void summoningSkillZeroBlocksBoundSpirits() {
    Character c = mage(3, 0, 3);
    assertEquals("Erst Herbeirufen steigern", c.getSpirits().getAddProblem(data("Erdgeist"), 3, 1, true));
  }

  @Test
  void traditionDecidesSpiritTypes() {
    Character c = mage(3, 3, 3);
    assertEquals("Die Tradition Hermetisch ruft keinen Geist des Tieres",
        c.getSpirits().getAddProblem(data("Geist des Tieres"), 3, 1, true));
    c.setTradition(Tradition.SHAMANIC);
    assertNull(c.getSpirits().getAddProblem(data("Geist des Tieres"), 3, 1, true));
    assertEquals(5, c.getSpirits().getTypes(SpiritKind.SPIRIT).size());
    assertTrue(c.getSpirits().getTypes(SpiritKind.SPIRIT).stream().noneMatch(d -> d.name().equals("Feuergeist")));
  }

  @Test
  void spritesFollowResonanceCompilingAndCharisma() {
    Character c = technomancer(4, 3, 1);
    Spirits spirits = c.getSpirits();
    assertEquals("Stufe bei der Erschaffung = Resonanz (4)", spirits.getAddProblem(data("Daten-Sprite"), 3, 1, true));
    assertEquals("Bei der Erschaffung 1 bis 3 Dienste (Kompilieren 3)",
        spirits.getAddProblem(data("Daten-Sprite"), 4, 4, true));
    spirits.add(new Spirit(data("Daten-Sprite"), 4, 3, true));
    assertEquals("Höchstens 1 Sprites registriert (Charisma)",
        spirits.getAddProblem(data("Kurier-Sprite"), 4, 1, true));
    assertEquals("Nur für Zauberer und Magieradepten", spirits.getAddProblem(data("Erdgeist"), 4, 1, true));
  }

  @Test
  void careerOnlyRecords() {
    Character c = mage(5, 1, 2);
    c.setCreated(true);
    Spirits spirits = c.getSpirits();
    assertNull(spirits.getAddProblem(data("Feuergeist"), 7, 4, true));
    Spirit fire = new Spirit(data("Feuergeist"), 7, 4, true);
    spirits.add(fire);
    assertEquals(0, spirits.getBpCost());
    spirits.setServices(fire, 0);
    spirits.setServices(fire, 6);
    assertEquals(6, fire.getServices());
    assertThrows(IllegalArgumentException.class, () -> spirits.setServices(fire, -1));
    spirits.setBound(fire, false);
    spirits.setBound(fire, true);
  }

  @Test
  void losingTheGiftRemovesSpiritsAndSaysSoBefore() {
    Character c = mage(3, 3, 3);
    c.getSpirits().add(new Spirit(data("Erdgeist"), 3, 2, true));
    CharacterQuality gift = c.getQualities().stream().filter(q -> q.data().name().equals("Zauberer")).findFirst()
        .orElseThrow();
    assertTrue(c.getQualityRemovalConsequences(gift).contains("Geist Erdgeist wird mit entfernt"));
    c.removeQuality(gift);
    assertTrue(c.getSpirits().getAll().isEmpty());
  }
}
