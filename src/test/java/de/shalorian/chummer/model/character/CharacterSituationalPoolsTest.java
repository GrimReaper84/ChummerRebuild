package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.buy;
import static de.shalorian.chummer.model.character.AdeptTestSupport.quality;
import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.situation.SituationalPool;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Situative Würfelpools aus Foki, Schutzpatron und Adeptenkräften (GRW S. 181, 230–237, 294, 297). */
class CharacterSituationalPoolsTest {

  private static Gear activeFocus(Character c, String name, int force, String extra) {
    Gear focus = c.getEquipment().addGear(new Gear(Objects.requireNonNull(GearDataLoader.findByName(name)), force, 1,
        extra, null));
    c.getFoci().bind(focus);
    c.getFoci().setActive(focus, true);
    return focus;
  }

  private static SituationalPool pool(Character c, String label) {
    return c.getSituationalPools().stream().filter(p -> p.label().equals(label)).findFirst()
        .orElseThrow(() -> new AssertionError(label + " fehlt in " + c.getSituationalPools()));
  }

  private static boolean hasPool(Character c, String label) {
    return c.getSituationalPools().stream().anyMatch(p -> p.label().equals(label));
  }

  private static Character mage() {
    Character c = withGift("Zauberer", 5);
    c.getAttribute(AttributeType.LOG).setValue(4);
    Objects.requireNonNull(c.getSkill("Spruchzauberei")).setRating(4);
    return c;
  }

  @Test
  void spellCategoryWithFocusAndMentor() {
    // Spruchzauberei 4 + Magie 5, Hai +2 auf Kampfzauber, Zauberspruchfokus (Kampf) 3 → 14
    Character c = mage();
    c.addQuality(quality("Schutzpatron"), false, "Hai");
    activeFocus(c, "Zauberspruchfokus", 3, "Kampf");
    assertEquals(14, pool(c, "Spruchzauberei · Kampfzauber").dice());
    assertFalse(hasPool(c, "Ritualzauberei · Kampfzauber")); // ohne Ritualzauberei kein Grundpool
  }

  @Test
  void onlyOneFocusPerTest() {
    // S. 233: zwei passende Foki → nur der stärkere zählt
    Character c = mage();
    activeFocus(c, "Zauberspruchfokus", 2, "Kampf");
    activeFocus(c, "Kraftfokus", 3, "");
    assertEquals(4 + 5 + 3, pool(c, "Spruchzauberei · Kampfzauber").dice());
  }

  @Test
  void powerFocusAllMagicButCounterspelling() {
    Character c = mage();
    Objects.requireNonNull(c.getSkill("Antimagie")).setRating(3);
    activeFocus(c, "Kraftfokus", 2, "");
    assertEquals(4 + 5 + 2, pool(c, "Spruchzauberei").dice());
    assertFalse(hasPool(c, "Antimagie"));
  }

  @Test
  void inactiveFocusGivesNothing() {
    Character c = mage();
    activeFocus(c, "Kraftfokus", 2, "");
    c.getFoci().getActive().forEach(f -> c.getFoci().setActive(f, false));
    assertTrue(c.getSituationalPools().isEmpty());
  }

  @Test
  void mentorChoiceAndPenalty() {
    Character cat = withGift("Zauberer", 4);
    Objects.requireNonNull(cat.getSkill("Infiltration")).setRating(3);
    cat.getAttribute(AttributeType.AGI).setValue(4);
    cat.addQuality(quality("Schutzpatron"), false, "Katze (Infiltration)");
    assertEquals(3 + 4 + 2, pool(cat, "Infiltration").dice());

    Character moon = mage();
    moon.addQuality(quality("Schutzpatron"), false, "Mondin");
    assertEquals(4 + 5 - 1, pool(moon, "Spruchzauberei · Kampfzauber").dice());
  }

  @Test
  void bearResistsDamageWithArmorButWithoutWoundModifier() {
    // S. 181: Konstitution + Panzerung; S. 192: kein Verletzungsmodifikator
    Character c = withGift("Zauberer", 3);
    c.getAttribute(AttributeType.BOD).setValue(4);
    c.addQuality(quality("Schutzpatron"), false, "Bär");
    c.setStunDamage(3);
    assertEquals(4 + 2, pool(c, "Schadenswiderstand · ballistisch").dice());
  }

  @Test
  void adeptSituationalPowers() {
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.REA).setValue(3);
    buy(c, "Kampfsinn", "", 2);
    buy(c, "Körpersprache", "", 1);
    assertEquals(3 + 2, pool(c, "Verteidigung").dice());
    SituationalPool social = pool(c, "Soziale Proben");
    assertTrue(social.bonusOnly());
    assertEquals(1, social.dice());
  }

  @Test
  void criticalStrikeRaisesUnarmedDamage() {
    Character c = withGift("Adept", 4);
    Weapon unarmed = WeaponDataLoader.getAllWeapons().values().stream()
        .filter(d -> d.category() == WeaponCategory.UNARMED).findFirst().map(d -> new Weapon(d, 0)).orElseThrow();
    String before = c.getEquipment().getWeaponDamageText(unarmed);
    buy(c, "Kritischer Schlag", "", 1);
    String after = c.getEquipment().getWeaponDamageText(unarmed);
    assertEquals(Integer.parseInt(before.replaceAll("\\D.*", "")) + 1, Integer.parseInt(after.replaceAll("\\D.*", "")));
  }

  @Test
  void everyCharacterHasTheUnarmedAttackWithCriticalStrike() {
    // S. 188: halbe Stärke als Geistiger Schaden; Kritischer Schlag +1 je Stufe (S. 231)
    Character c = withGift("Adept", 4);
    c.getAttribute(AttributeType.STR).setValue(4);
    Weapon unarmed = c.getEquipment().getUnarmedAttack();
    assertTrue(unarmed.getData().builtIn());
    assertEquals("2G", c.getEquipment().getWeaponDamageText(unarmed));
    buy(c, "Kritischer Schlag", "", 2);
    assertEquals("4G", c.getEquipment().getWeaponDamageText(c.getEquipment().getUnarmedAttack()));
  }
}
