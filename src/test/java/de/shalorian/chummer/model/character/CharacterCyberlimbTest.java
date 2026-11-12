package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.LimbAttributes;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.weapon.Weapon;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Cybergliedmaßen am Charakter (GRW S. 389–391): Durchschnitt, Torso-Regel, Essenz je Stelle, Monitor, Waffen. */
class CharacterCyberlimbTest {

  private static CyberwareData data(String name) {
    return Objects.requireNonNull(CyberwareDataLoader.findByName(name), name);
  }

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  private static Cyberware addLimb(Character c, String name, BodyLocation location, LimbAttributes base) {
    Cyberware limb = new Cyberware(data(name), CyberwareGrade.STANDARD, location, base);
    c.getImplants().add(limb);
    return limb;
  }

  private static Cyberware part(String name, int rating) {
    return new Cyberware(data(name), CyberwareGrade.STANDARD, rating);
  }

  /** Critical George (S. 390): KON 3, STR 4, GES 2; Torso 6/5/3, linker Arm 3/7/3, linkes Bein 5/3/3. */
  private static Character criticalGeorge() {
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(3);
    c.getAttribute(AttributeType.STR).setValue(4);
    c.getAttribute(AttributeType.AGI).setValue(2);
    addLimb(c, "Torso (offensichtlich)", BodyLocation.TORSO, new LimbAttributes(6, 5, 3));
    Cyberware arm = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(arm, part("Stärkeverbesserung", 4));
    addLimb(c, "Ganzes Bein (offensichtlich)", BodyLocation.LEG_LEFT, new LimbAttributes(5, 3, 3));
    return c;
  }

  @Test
  void criticalGeorgeAverages() {
    Character c = criticalGeorge();
    Cyberware arm = c.getImplants().getLimbs().get(1);
    assertEquals(7, arm.getLimbAttribute(AttributeType.STR));
    // beschossen: (6 + 3 + 5 + 3 + 3) / 5 = 4; STR (5 + 7 + 3 + 4 + 4) / 5 = 4; GES 13 / 5 = 2
    assertEquals(new LimbAttributes(4, 4, 2), c.getImplants().getLimbAverages());
  }

  @Test
  void enhancementAboveThreeNeedsAttachedTorso() {
    Character c = mensch();
    Cyberware arm = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    assertNull(c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 3));
    assertEquals("Über Stufe 3 nur mit angeschlossenem Cybertorso",
        c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 4));
    Cyberware torso = addLimb(c, "Torso (synthetisch)", BodyLocation.TORSO, LimbAttributes.STANDARD);
    assertNull(c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 4));
    torso.setAttached(false);
    assertEquals("Über Stufe 3 nur mit angeschlossenem Cybertorso",
        c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 4));
  }

  @Test
  void armorEnhancementAboveThreeAlsoNeedsTorso() {
    // S. 390: jede Cyberglied-Verbesserung über Stufe 3 nur mit Cybertorso, auch die Panzerung
    Character c = mensch();
    Cyberware leg = addLimb(c, "Ganzes Bein (offensichtlich)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD);
    assertEquals("Über Stufe 3 nur mit angeschlossenem Cybertorso",
        c.getImplants().getLimbInstallProblem(leg, data("Panzerungsverbesserung"), 4));
    addLimb(c, "Torso (offensichtlich)", BodyLocation.TORSO, LimbAttributes.STANDARD);
    assertNull(c.getImplants().getLimbInstallProblem(leg, data("Panzerungsverbesserung"), 4));
  }

  @Test
  void headwareInLimbsCountsForDuplicatesAndExclusions() {
    Character c = mensch();
    Cyberware skull = addLimb(c, "Schädel (offensichtlich)", BodyLocation.SKULL, LimbAttributes.STANDARD);
    c.getImplants().add(part("Datenbuchse", 0));
    assertEquals("Bereits eingebaut", c.getImplants().getLimbInstallProblem(skull, data("Datenbuchse"), 0));
    c.getImplants().installInLimb(skull, part("Sim-Modul", 0));
    assertEquals("Nicht zusammen mit Sim-Modul",
        c.getImplants().getBlockReason(data("Sim-Modul (modifiziert für heißes Sim)")));
    assertEquals("Bereits eingebaut", c.getImplants().getBlockReason(data("Sim-Modul")));
  }

  @Test
  void limbKnowsItsValuesAndUnarmedDamage() {
    Character c = mensch();
    addLimb(c, "Torso (offensichtlich)", BodyLocation.TORSO, LimbAttributes.STANDARD);
    Cyberware arm = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, new LimbAttributes(4, 3, 5));
    c.getImplants().installInLimb(arm, part("Stärkeverbesserung", 4));
    assertEquals(new LimbAttributes(4, 7, 5), arm.getLimbAttributes());
    // S. 390: waffenlos (STR÷2)K, aufgerundet → 4K
    assertEquals("4K", arm.getUnarmedDamageText());
    Cyberware skull = addLimb(c, "Schädel (synthetisch)", BodyLocation.SKULL, LimbAttributes.STANDARD);
    assertNull(skull.getUnarmedDamageText());
    assertTrue(c.getImplants().isOccupied(BodyLocation.ARM_LEFT));
    assertFalse(c.getImplants().isOccupied(BodyLocation.ARM_RIGHT));
  }

  @Test
  void limbValuesRespectMaxima() {
    // Mensch: natürliches Maximum 6, augmentiertes 9
    Character c = mensch();
    assertEquals("STR höchstens 6 (natürliches Maximum)",
        c.getImplants().getLimbBaseProblem(new LimbAttributes(3, 7, 3)));
    addLimb(c, "Torso (offensichtlich)", BodyLocation.TORSO, LimbAttributes.STANDARD);
    Cyberware arm = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, new LimbAttributes(3, 6, 3));
    assertNull(c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 3));
    assertEquals("STR des Glieds höchstens 9 (augmentiertes Maximum)",
        c.getImplants().getLimbInstallProblem(arm, data("Stärkeverbesserung"), 4));
  }

  @Test
  void eachAttachedLimbAddsAPhysicalBox() {
    // Archetyp Straßensamurai S. 135: KON 4 mit zwei Cyberarmen → 10 (12)
    Character c = mensch();
    c.getAttribute(AttributeType.BOD).setValue(4);
    assertEquals(10, c.getPhysicalBoxes());
    Cyberware left = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_RIGHT, LimbAttributes.STANDARD);
    assertEquals(12, c.getPhysicalBoxes());
    left.setAttached(false);
    assertEquals(11, c.getPhysicalBoxes());
  }

  @Test
  void essenceCountsHighestLimbPerLocation() {
    Character c = mensch();
    addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    Cyberware spare = new Cyberware(data("Ganzer Arm (synthetisch)"), CyberwareGrade.ALPHAWARE,
        BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    spare.setAttached(false);
    c.getImplants().add(spare);
    assertEquals(0, BigDecimal.ONE.compareTo(c.getEquipment().getCyberwareEssenceCost()));
    addLimb(c, "Hand/Fuß (offensichtlich)", BodyLocation.ARM_RIGHT, LimbAttributes.STANDARD);
    assertEquals(0, new BigDecimal("1.25").compareTo(c.getEquipment().getCyberwareEssenceCost()));
  }

  @Test
  void installedPartsCostCapacityNotEssence() {
    Character c = mensch();
    Cyberware skull = addLimb(c, "Schädel (offensichtlich)", BodyLocation.SKULL, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(skull, part("Datenbuchse", 0));
    assertEquals(1, skull.getUsedCapacity());
    assertEquals(0, new BigDecimal("0.75").compareTo(c.getEquipment().getCyberwareEssenceCost()));
  }

  @Test
  void weaponsUseLimbStrength() {
    Character c = mensch();
    c.getAttribute(AttributeType.STR).setValue(2);
    addLimb(c, "Torso (offensichtlich)", BodyLocation.TORSO, LimbAttributes.STANDARD);
    Cyberware arm = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(arm, part("Stärkeverbesserung", 4));
    c.getImplants().installInLimb(arm, part("Sporn (einziehbar)", 0));
    c.getImplants().add(part("Nagelmesser (einziehbar)", 0));
    List<Weapon> weapons = c.getEquipment().getImplantWeapons();
    assertEquals(List.of("Sporn", "Nagelmesser"), weapons.stream().map(Weapon::getName).toList());
    // Sporn (STR÷2+3)K mit Arm-STR 7 → 4 + 3 = 7K (aufgerundet); Nagelmesser (STR÷2+1)K mit Charakter-STR 2 → 2K
    assertEquals("7K", weapons.getFirst().getDamageText(2));
    assertEquals("Implantat, linker Arm", weapons.getFirst().getBuiltInSource());
    assertEquals("2K", weapons.get(1).getDamageText(2));
    arm.setAttached(false);
    assertEquals(List.of("Nagelmesser"), c.getEquipment().getImplantWeapons().stream().map(Weapon::getName).toList());
  }

  @Test
  void overOccupiedLocationsAreReported() {
    Character c = mensch();
    addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    Cyberware hand = addLimb(c, "Hand/Fuß (synthetisch)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    // an einer belegten Stelle kommt das neue Glied abgenommen dazu
    assertFalse(hand.isAttached());
    assertTrue(c.getImplants().getOverOccupiedLocations().isEmpty());
    hand.setAttached(true);
    assertEquals(List.of(BodyLocation.ARM_LEFT), c.getImplants().getOverOccupiedLocations());
    hand.setAttached(false);
    assertTrue(c.getImplants().getOverOccupiedLocations().isEmpty());
  }

  @Test
  void limbArmorOfAttachedLimbsIsSummed() {
    // wie im C#: alle angeschlossenen Glieder zusammen, zusätzlich zur getragenen Panzerung, ohne Behinderung
    Character c = mensch();
    Cyberware left = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    Cyberware right = addLimb(c, "Ganzer Arm (offensichtlich)", BodyLocation.ARM_RIGHT, LimbAttributes.STANDARD);
    Cyberware foot = addLimb(c, "Hand/Fuß (offensichtlich)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(left, part("Panzerungsverbesserung", 2));
    c.getImplants().installInLimb(right, part("Panzerungsverbesserung", 3));
    c.getImplants().installInLimb(foot, part("Panzerungsverbesserung", 1));
    assertEquals(6, c.getArmorBallistic());
    assertEquals(6, c.getArmorImpact());
    assertEquals(0, c.getEquipment().getArmorEncumbrancePenalty());
    foot.setAttached(false);
    assertEquals(5, c.getArmorBallistic());
  }

  @Test
  void jumpHydraulicsGoIntoBothLegs() {
    Character c = mensch();
    Cyberware left = addLimb(c, "Ganzes Bein (offensichtlich)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD);
    assertEquals("Braucht ein zweites Cyberbein",
        c.getImplants().getLimbInstallProblem(left, data("Sprunghydraulik"), 3));
    Cyberware right = addLimb(c, "Unterschenkel (synthetisch)", BodyLocation.LEG_RIGHT, LimbAttributes.STANDARD);
    Cyberware container = part("Großer Schmuggelbehälter", 0);
    c.getImplants().installInLimb(right, container);
    assertEquals("Im zweiten Bein: Kapazität reicht nicht: Sprunghydraulik braucht 3, frei sind 1 von 6",
        c.getImplants().getLimbInstallProblem(left, data("Sprunghydraulik"), 3));
    c.getImplants().uninstallFromLimb(right, container);
    int before = left.getNuyenCost() + right.getNuyenCost();
    c.getImplants().installInLimb(left, part("Sprunghydraulik", 3));
    assertEquals(3, left.getUsedCapacity());
    assertEquals(3, right.getUsedCapacity());
    assertEquals(6000, left.getNuyenCost() + right.getNuyenCost() - before);
    c.getImplants().uninstallFromLimb(right, right.getInstalled().getFirst());
    assertTrue(left.getInstalled().isEmpty());
    assertTrue(right.getInstalled().isEmpty());
    c.getImplants().installInLimb(right, part("Sprunghydraulik", 2));
    c.getImplants().remove(right);
    assertTrue(left.getInstalled().isEmpty());
  }

  @Test
  void jumpHydraulicsOnlyRemoveTheirOwnCounterpart() {
    // zwei Paare über Ersatzbeine: links 1 + rechts 1, dann links 2 + rechts 2; Ausbau des ersten Paars lässt das
    // zweite stehen
    Character c = mensch();
    Cyberware left1 = addLimb(c, "Ganzes Bein (offensichtlich)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD);
    Cyberware right1 = addLimb(c, "Ganzes Bein (offensichtlich)", BodyLocation.LEG_RIGHT, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(left1, part("Sprunghydraulik", 2));
    left1.setAttached(false);
    right1.setAttached(false);
    Cyberware left2 = addLimb(c, "Ganzes Bein (synthetisch)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD);
    Cyberware right2 = addLimb(c, "Ganzes Bein (synthetisch)", BodyLocation.LEG_RIGHT, LimbAttributes.STANDARD);
    c.getImplants().installInLimb(left2, part("Sprunghydraulik", 1));
    assertEquals(1, right2.getUsedCapacity());

    c.getImplants().uninstallFromLimb(left1, left1.getInstalled().getFirst());
    assertTrue(right1.getInstalled().isEmpty());
    assertEquals(1, left2.getUsedCapacity());
    assertEquals(1, right2.getUsedCapacity());
  }
}
