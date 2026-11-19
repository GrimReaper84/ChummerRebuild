package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import de.shalorian.chummer.persistence.CharacterStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Käufe im Spielleiter-Modus bei der Erschaffung sind gratis (ADR-070). */
class GmAllowanceTest {

  private static Character human() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.setNuyenBP(10);
    return c;
  }

  private static Gear medkit() {
    return new Gear(gear("Medkit"), 4);
  }

  @Test
  void gearBoughtInGmModeCostsNothingAfterSwitchingOff() {
    Character c = human();
    int before = c.getRemainingNuyen();
    c.setGmMode(true);
    c.getEquipment().addGear(medkit());
    assertEquals(before, c.getRemainingNuyen());
    c.setGmMode(false);
    assertEquals(before, c.getRemainingNuyen());
  }

  @Test
  void entriesPaidBeforeGmModeStayPaid() {
    Character c = human();
    c.getEquipment().addGear(medkit());
    int paid = c.getRemainingNuyen();
    c.setGmMode(true);
    c.setGmMode(false);
    assertEquals(paid, c.getRemainingNuyen());
  }

  @Test
  void contactAddedInGmModeLaterRaisesCostNormally() {
    Character c = human();
    int before = c.getSpentBP();
    c.setGmMode(true);
    c.addContact("Schieber", null);
    c.getContacts().getFirst().setLoyalty(3);
    assertEquals(before, c.getSpentBP());
    c.setGmMode(false);
    c.getContacts().getFirst().setConnection(2);
    assertEquals(before + 1, c.getSpentBP());
  }

  @Test
  void loweringAndRaisingAgainInGmModeGivesNothingAway() {
    Character c = human();
    c.addContact("Schieber", null);
    c.getContacts().getFirst().setLoyalty(3);
    int paid = c.getSpentBP();
    c.setGmMode(true);
    c.getContacts().getFirst().setLoyalty(1);
    assertEquals(paid - 2, c.getSpentBP());
    c.getContacts().getFirst().setLoyalty(3);
    assertEquals(paid, c.getSpentBP());
  }

  @Test
  void spellLearnedInGmModeIsFreeUntilRemoved() {
    Character c = human();
    int before = c.getSpentBP();
    c.setGmMode(true);
    c.addSpell(Objects.requireNonNull(SpellDataLoader.findByName("Manablitz")));
    c.setGmMode(false);
    assertEquals(before, c.getSpentBP());
    assertTrue(c.getGmFreeSpells().contains("Manablitz"));
    c.removeSpell("Manablitz");
    assertTrue(c.getGmFreeSpells().isEmpty());
  }

  @Test
  void careerModeIsNotTracked() {
    Character c = human();
    c.setCreated(true);
    c.setGmMode(true);
    Gear kit = medkit();
    c.getEquipment().addGear(kit);
    c.setGmMode(false);
    assertEquals(0, kit.getGmFreeNuyen());
  }

  @Test
  void gratisSharesSurviveSaving() {
    Character c = human();
    int before = c.getRemainingNuyen();
    c.setGmMode(true);
    c.getEquipment().addGear(medkit());
    c.addContact("Schieber", null);
    c.addSpell(Objects.requireNonNull(SpellDataLoader.findByName("Manablitz")));
    // Speichern schreibt die Zuwächse gut, auch ohne vorheriges Ausschalten
    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    assertEquals(before, loaded.getRemainingNuyen());
    assertEquals(2, loaded.getContacts().getFirst().getGmFreeBp());
    assertTrue(loaded.getGmFreeSpells().contains("Manablitz"));
  }

  @Test
  void installIntoPaidItemOnlyMakesTheInstallFree() {
    Character c = human();
    Gear commlink = c.getEquipment().addGear(new Gear(gear("Renraku Sensei"), 0));
    int afterCommlink = c.getRemainingNuyen();
    c.setGmMode(true);
    Gear os = new Gear(gear("Mangadyne Deva"), 0);
    commlink.install(os);
    c.setGmMode(false);
    assertEquals(afterCommlink, c.getRemainingNuyen());
    assertEquals(os.getNuyenCost(), commlink.getGmFreeNuyen());
  }

  @Test
  void complexFormLearnedAndRaisedInGmModeIsFree() {
    Character c = AdeptTestSupport.withGift("Technomancer", 4);
    int before = c.getSpentBP();
    c.setGmMode(true);
    c.addComplexForm(Objects.requireNonNull(ComplexFormDataLoader.findByName("Analyse")));
    c.getComplexForms().getFirst().setRating(3);
    c.setGmMode(false);
    assertEquals(before, c.getSpentBP());
  }

  @Test
  void boundSpiritAndBoughtBoundFocusAreFreeInBpAndNuyen() {
    Character c = AdeptTestSupport.withGift("Zauberer", 4);
    Objects.requireNonNull(c.getSkill("Herbeirufen")).setRating(3);
    c.setNuyenBP(10);
    int bp = c.getSpentBP();
    int nuyen = c.getRemainingNuyen();
    c.setGmMode(true);
    c.getSpirits().add(new Spirit(Objects.requireNonNull(SpiritDataLoader.findByName("Erdgeist")), 4, 2, true));
    Gear focus = c.getEquipment().addGear(new Gear(gear("Kraftfokus"), 2));
    c.getFoci().bind(focus);
    c.setGmMode(false);
    assertEquals(bp, c.getSpentBP());
    assertEquals(nuyen, c.getRemainingNuyen());
  }

  @Test
  void lifestyleMonthsInGmModeAreFree() {
    Character c = human();
    int before = c.getRemainingNuyen();
    c.setGmMode(true);
    c.getEquipment().getLifestyle().setMonths(3);
    c.setGmMode(false);
    assertEquals(before, c.getRemainingNuyen());
  }

  @Test
  void creditIsReportedOnceForTheView() {
    Character c = human();
    c.setGmMode(true);
    c.getEquipment().addGear(medkit());
    assertTrue(c.consumeGmCredit());
    assertFalse(c.consumeGmCredit());
  }

  @Test
  void oldFilesWithoutGratisSharesCostAsBefore(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("alt.json");
    Files.writeString(file, """
        {"name":"Alt","metatype":"Mensch","contacts":[{"name":"Schieber","connection":2,"loyalty":2}]}
        """, StandardCharsets.UTF_8);
    Character loaded = CharacterStore.load(file.toFile());
    assertEquals(0, loaded.getContacts().getFirst().getGmFreeBp());
    assertEquals(4, loaded.getContactBP());
  }

  private static GearData gear(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }
}
