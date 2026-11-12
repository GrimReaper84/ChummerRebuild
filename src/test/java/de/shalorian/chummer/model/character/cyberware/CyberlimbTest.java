package de.shalorian.chummer.model.character.cyberware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Ein einzelnes Cyberglied: Maßanfertigung, Verbesserungen, Kapazität und Passung (GRW S. 390/391). */
class CyberlimbTest {

  private static CyberwareData data(String name) {
    return Objects.requireNonNull(CyberwareDataLoader.findByName(name), name);
  }

  private static Cyberware limb(String name, BodyLocation location, LimbAttributes base) {
    return new Cyberware(data(name), CyberwareGrade.STANDARD, location, base);
  }

  private static Cyberware part(String name, int rating) {
    return new Cyberware(data(name), CyberwareGrade.STANDARD, rating);
  }

  @Test
  void carlosCustomizedArm() {
    // S. 390: maßgefertigter Arm 8/6/4 → Erhöhung 9, Verfügbarkeit 4 + 9 = 13, Preis 15.000 + 13.500 = 28.500 ¥
    Cyberware arm = limb("Ganzer Arm (offensichtlich)", BodyLocation.ARM_RIGHT, new LimbAttributes(8, 6, 4));
    assertEquals(13, arm.getAvailability().evaluate(0));
    assertEquals(28500, arm.getNuyenCost());
    assertEquals(8, arm.getLimbAttribute(AttributeType.BOD));
  }

  @Test
  void carlosEnhancements() {
    // S. 390: Stärke 3 + Konstitution 1 = 4 Kapazität, 11 von 15 frei, +950 ¥; Arm danach KON 9, STR 9, GES 4
    Cyberware arm = limb("Ganzer Arm (offensichtlich)", BodyLocation.ARM_RIGHT, new LimbAttributes(8, 6, 4));
    int before = arm.getNuyenCost();
    arm.install(part("Stärkeverbesserung", 3));
    arm.install(part("Konstitutionsverbesserung", 1));
    assertEquals(4, arm.getUsedCapacity());
    assertEquals(11, arm.getCapacity() - arm.getUsedCapacity());
    assertEquals(950, arm.getNuyenCost() - before);
    assertEquals(9, arm.getLimbAttribute(AttributeType.BOD));
    assertEquals(9, arm.getLimbAttribute(AttributeType.STR));
    assertEquals(4, arm.getLimbAttribute(AttributeType.AGI));
  }

  @Test
  void fitsOnlyMatchingLimbsAndCapacity() {
    Cyberware hand = limb("Hand/Fuß (offensichtlich)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    assertEquals("Cyber-Holster passt nicht in Hand/Fuß (offensichtlich)",
        hand.installProblem(data("Cyber-Holster"), 0));
    assertEquals("Katzenaugen lässt sich nicht in Cybergliedmaßen einbauen",
        hand.installProblem(data("Katzenaugen"), 0));
    assertEquals("Kunstmuskeln lässt sich nicht in Cybergliedmaßen einbauen",
        hand.installProblem(data("Kunstmuskeln"), 1));
    assertNull(hand.installProblem(data("Sporn (einziehbar)"), 0));
    hand.install(part("Sporn (einziehbar)", 0));
    assertEquals("Kapazität reicht nicht: Handklinge (einziehbar) braucht 3, frei sind 1 von 4",
        hand.installProblem(data("Handklinge (einziehbar)"), 0));
    Cyberware skull = limb("Schädel (offensichtlich)", BodyLocation.SKULL, LimbAttributes.STANDARD);
    assertEquals("Schädel (offensichtlich) hat keine eigenen Attribute",
        skull.installProblem(data("Stärkeverbesserung"), 1));
    assertNull(skull.installProblem(data("Panzerungsverbesserung"), 1));
    assertNull(skull.installProblem(data("Datenbuchse"), 0));
  }

  @Test
  void enhancementOnlyOncePerLimb() {
    Cyberware arm = limb("Ganzer Arm (synthetisch)", BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    arm.install(part("Stärkeverbesserung", 1));
    assertEquals("Stärkeverbesserung ist bereits eingebaut", arm.installProblem(data("Stärkeverbesserung"), 2));
  }

  @Test
  void limbNeedsMatchingLocationAndValidBase() {
    assertThrows(IllegalArgumentException.class,
        () -> limb("Ganzer Arm (offensichtlich)", BodyLocation.LEG_LEFT, LimbAttributes.STANDARD));
    assertThrows(IllegalArgumentException.class,
        () -> limb("Schädel (offensichtlich)", BodyLocation.SKULL, new LimbAttributes(4, 3, 3)));
    assertThrows(IllegalArgumentException.class,
        () -> new Cyberware(data("Ganzer Arm (offensichtlich)"), CyberwareGrade.STANDARD, 0));
  }

  @Test
  void limbPartCodesAndLocations() {
    assertEquals(LimbPart.HAND_FOOT, LimbPart.fromCode("handfoot"));
    assertEquals(4, LimbPart.HAND_FOOT.locations().size());
    assertEquals(BodyLocation.TORSO, LimbPart.TORSO.locations().getFirst());
    assertThrows(IllegalArgumentException.class, () -> LimbPart.fromCode("tail"));
  }
}
