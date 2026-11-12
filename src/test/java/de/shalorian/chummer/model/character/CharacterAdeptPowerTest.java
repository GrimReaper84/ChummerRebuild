package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.buy;
import static de.shalorian.chummer.model.character.AdeptTestSupport.cyberware;
import static de.shalorian.chummer.model.character.AdeptTestSupport.points;
import static de.shalorian.chummer.model.character.AdeptTestSupport.power;
import static de.shalorian.chummer.model.character.AdeptTestSupport.quality;
import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static de.shalorian.chummer.model.character.QualityTestSupport.selected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowers;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Kraftpunkte, Kosten, Höchststufen und Grenzen der Adeptenkräfte (GRW S. 115/116, 229–232). */
class CharacterAdeptPowerTest {

  private static Skill skill(Character c, String name) {
    return Objects.requireNonNull(c.getSkill(name), name);
  }

  @Test
  void adeptHasPowerPointsEqualToMagic() {
    // GRW S. 229: Kraftpunkte = Magie
    Character c = withGift("Adept", 4);
    assertTrue(c.hasAdeptPowers());
    assertFalse(c.isMysticAdept());
    assertEquals(0, points("4").compareTo(c.getAdeptPowers().getPowerPoints()));
  }

  @Test
  void essenceLossReducesPowerPoints() {
    // GRW S. 208: Essenz 5 → Magie −1
    Character c = withGift("Adept", 4);
    c.getEquipment().addCyberware(cyberware("1"));
    assertEquals(0, points("3").compareTo(c.getAdeptPowers().getPowerPoints()));
  }

  @Test
  void magicianHasNoPowers() {
    Character c = withGift("Zauberer", 4);
    assertFalse(c.hasAdeptPowers());
    assertEquals(0, c.getAdeptPowers().getPowerPoints().signum());
    assertEquals("Nur für Adepten und Magieradepten", c.getAdeptPowers().getBlockReason(power("Kampfsinn")));
  }

  @Test
  void rulebookExampleRoxanne() {
    // GRW S. 229: Magie 4, ein Punkt in Kräfte → Beschleunigte Heilung 4, Spruchzauberei mit 3 Würfeln Magie
    Character c = withGift("Magieradept", 4);
    assertTrue(c.isMysticAdept());
    assertEquals(0, c.getAdeptPowers().getPowerPoints().signum());
    c.getAdeptPowers().setAdeptMagic(1);
    assertEquals(0, points("1").compareTo(c.getAdeptPowers().getPowerPoints()));
    assertEquals(4, c.getAdeptPowers().getMaxLevel(power("Beschleunigte Heilung"), ""));

    AdeptPower healing = buy(c, "Beschleunigte Heilung", "", 4);
    assertEquals(0, points("1").compareTo(c.getAdeptPowers().getCost(healing)));
    assertEquals(0, c.getAdeptPowers().getRemainingPowerPoints().signum());
    assertEquals(3, c.getSpellcastingMagic());

    skill(c, "Spruchzauberei").setRating(2);
    assertEquals(5, c.getSkillDicePool(skill(c, "Spruchzauberei")).orElseThrow().dice());
  }

  @Test
  void mysticAdeptSplitIsLimitedToMagic() {
    Character c = withGift("Magieradept", 3);
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().setAdeptMagic(4));
    assertThrows(IllegalArgumentException.class, () -> withGift("Adept", 3).getAdeptPowers().setAdeptMagic(1));
    c.getAdeptPowers().setAdeptMagic(3);
    assertEquals(0, c.getSpellcastingMagic());
  }

  @Test
  void essenceLossReducesSpellcastingMagicFirst() {
    Character c = withGift("Magieradept", 4);
    c.getAdeptPowers().setAdeptMagic(2);
    c.getEquipment().addCyberware(cyberware("1"));
    assertEquals(2, c.getAdeptPowers().getAdeptMagic());
    assertEquals(1, c.getSpellcastingMagic());
    c.getEquipment().addCyberware(cyberware("2"));
    assertEquals(1, c.getAdeptPowers().getAdeptMagic());
    assertEquals(0, c.getSpellcastingMagic());
  }

  @Test
  void levelAboveMagicIsRejected() {
    // GRW S. 229: Stufe höchstens Magie
    Character c = withGift("Adept", 2);
    AdeptPower sense = buy(c, "Kampfsinn", "", 2);
    assertFalse(c.getAdeptPowers().canRaise(sense));
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().raise(sense));
  }

  @Test
  void fixedMaximumLevelApplies() {
    Character c = withGift("Adept", 6);
    AdeptPower body = buy(c, "Körpersprache", "", 3);
    assertFalse(c.getAdeptPowers().canRaise(body));
  }

  @Test
  void notEnoughPowerPointsBlocksPurchaseAndRaise() {
    Character c = withGift("Adept", 1);
    AdeptPower combat = buy(c, "Kampfsinn", "", 1); // 0,5
    buy(c, "Geschossparade", "", 1);                // 0,25 → 0,25 übrig
    assertFalse(c.getAdeptPowers().canRaise(combat));
    assertEquals("Nicht genug Kraftpunkte (1 nötig, 0,25 übrig)",
        c.getAdeptPowers().getBlockReason(power("Astrale Wahrnehmung")));
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().add(power("Astrale Wahrnehmung"), ""));
  }

  @Test
  void fractionsAreKept() {
    // GRW S. 229: Bruchteile bleiben stehen
    Character c = withGift("Adept", 2);
    buy(c, "Kritischer Schlag", "", 1);
    buy(c, "Kampfsinn", "", 1);
    assertEquals(0, points("1.25").compareTo(c.getAdeptPowers().getRemainingPowerPoints()));
    assertEquals("1,25", AdeptPowers.formatPoints(c.getAdeptPowers().getRemainingPowerPoints()));
  }

  @Test
  void improvedReflexesUseTable() {
    Character c = withGift("Adept", 4);
    AdeptPower reflexes = buy(c, "Gesteigerte Reflexe", "", 2);
    assertEquals(0, points("2.5").compareTo(c.getAdeptPowers().getCost(reflexes)));
    c.getAdeptPowers().raise(reflexes);
    assertEquals(0, points("4").compareTo(c.getAdeptPowers().getCost(reflexes)));
    assertFalse(c.getAdeptPowers().canRaise(reflexes));
  }

  @Test
  void improvedAttributeCostsDoubleAboveNaturalMaximum() {
    // GRW S. 230: Mensch STR 5 (Maximum 6), Stufe 2 → 0,75 + 1,5 = 2,25; Grenze augmentiertes Maximum 9
    Character c = withGift("Adept", 6);
    c.getAttribute(AttributeType.STR).setValue(5);
    AdeptPower strength = buy(c, "Gesteigertes körperliches Attribut", "STR", 2);
    assertEquals(0, points("2.25").compareTo(c.getAdeptPowers().getCost(strength)));
    assertEquals(4, c.getAdeptPowers().getMaxLevel(strength.getData(), "STR"));
  }

  @Test
  void improvedAttributeOnlyForPhysicalAttributes() {
    Character c = withGift("Adept", 4);
    assertEquals("Körperliches Attribut wählen",
        c.getAdeptPowers().getAddProblem(power("Gesteigertes körperliches Attribut"), "LOG"));
    assertNull(c.getAdeptPowers().getAddProblem(power("Gesteigertes körperliches Attribut"), "AGI"));
  }

  @Test
  void improvedSkillCostsByCategoryAndIsCappedAtHalfRating() {
    // GRW S. 232: Kampf 0,5 je Stufe, sonst 0,25; modifizierter Wert höchstens 1,5 × Grundwert
    Character c = withGift("Adept", 4);
    skill(c, "Pistolen").setRating(3);
    skill(c, "Wahrnehmung").setRating(4);
    assertEquals(0, points("0.5").compareTo(c.getAdeptPowers().getCost(power("Verbesserte Fertigkeit"), "Pistolen", 1)));
    assertEquals(0,
        points("0.25").compareTo(c.getAdeptPowers().getCost(power("Verbesserte Fertigkeit"), "Wahrnehmung", 1)));
    assertEquals(1, c.getAdeptPowers().getMaxLevel(power("Verbesserte Fertigkeit"), "Pistolen"));
    assertEquals(2, c.getAdeptPowers().getMaxLevel(power("Verbesserte Fertigkeit"), "Wahrnehmung"));

    skill(c, "Entfesseln").setRating(1);
    assertEquals("Fertigkeitswert mindestens 2 nötig",
        c.getAdeptPowers().getAddProblem(power("Verbesserte Fertigkeit"), "Entfesseln"));
  }

  @Test
  void improvedSkillNotForMagicalSkills() {
    Character c = withGift("Magieradept", 4);
    c.getAdeptPowers().setAdeptMagic(2);
    skill(c, "Spruchzauberei").setRating(4);
    assertEquals("Nur Kampf-, körperliche, soziale, technische und Fahrzeugfertigkeiten",
        c.getAdeptPowers().getAddProblem(power("Verbesserte Fertigkeit"), "Spruchzauberei"));
  }

  @Test
  void improvedPerceptionCappedAtIntuition() {
    // GRW S. 232: Zusatzwürfel höchstens Intuition
    Character c = withGift("Adept", 5);
    c.getAttribute(AttributeType.INT).setValue(2);
    assertEquals(2, c.getAdeptPowers().getMaxLevel(power("Verbesserte Wahrnehmung"), ""));
  }

  @Test
  void duplicatesOnlyForMultiplePowersWithOtherChoice() {
    Character c = withGift("Adept", 4);
    buy(c, "Kampfsinn", "", 1);
    assertEquals("Bereits gekauft", c.getAdeptPowers().getBlockReason(power("Kampfsinn")));
    buy(c, "Geschärfter Sinn", "Infrarotsicht", 1);
    assertEquals("Mit dieser Auswahl bereits gekauft",
        c.getAdeptPowers().getAddProblem(power("Geschärfter Sinn"), "Infrarotsicht"));
    assertNull(c.getAdeptPowers().getAddProblem(power("Geschärfter Sinn"), "Richtungssinn"));
    assertEquals("Sinn angeben", c.getAdeptPowers().getAddProblem(power("Geschärfter Sinn"), " "));
  }

  @Test
  void painResistanceExcludesHighPainTolerance() {
    // GRW S. 116
    Character c = withGift("Adept", 4);
    c.addQuality(quality("Hohe Schmerztoleranz 1"), false);
    assertEquals("Nicht zusammen mit Hohe Schmerztoleranz", c.getAdeptPowers().getBlockReason(power("Schmerzresistenz")));

    Character other = withGift("Adept", 4);
    buy(other, "Schmerzresistenz", "", 1);
    assertEquals("Nicht zusammen mit der Adeptenkraft Schmerzresistenz",
        other.getQualityBlockReason(quality("Hohe Schmerztoleranz 1"), false));
  }

  @Test
  void loweredMagicReportsOverspentPoints() {
    Character c = withGift("Adept", 2);
    buy(c, "Kampfsinn", "", 2);
    buy(c, "Astrale Wahrnehmung", "", 1);
    assertNull(c.getAdeptPowers().getProblem());
    c.getEquipment().addCyberware(cyberware("0.5"));
    assertEquals("Kraftpunkte um 1 überzogen; Kampfsinn über der Höchststufe 1", c.getAdeptPowers().getProblem());
    assertEquals(2, c.getAdeptPowers().getPowers().size());
  }

  @Test
  void removingAdeptGiftRemovesPowers() {
    Character c = withGift("Adept", 3);
    buy(c, "Kampfsinn", "", 1);
    buy(c, "Geschärfter Sinn", "Infrarotsicht", 1);
    List<String> lines = c.getQualityRemovalConsequences(selected(c, "Adept"));
    assertTrue(lines.contains("Adeptenkraft Kampfsinn wird mit entfernt"), lines.toString());
    assertTrue(lines.contains("Adeptenkraft Geschärfter Sinn (Infrarotsicht) wird mit entfernt"), lines.toString());

    c.removeQuality(selected(c, "Adept"));
    assertTrue(c.getAdeptPowers().getPowers().isEmpty());
    assertNotNull(c.getAdeptPowers().getBlockReason(power("Kampfsinn")));
  }

  @Test
  void removingMysticAdeptGiftResetsSplit() {
    Character c = withGift("Magieradept", 3);
    c.getAdeptPowers().setAdeptMagic(2);
    c.removeQuality(selected(c, "Magieradept"));
    c.addQuality(quality("Magieradept"), false);
    assertEquals(0, c.getAdeptPowers().getAdeptMagic());
  }

  @Test
  void powersCostNoBp() {
    // GRW S. 115, 229: Kräfte kosten nur Kraftpunkte
    Character c = withGift("Adept", 3);
    int before = c.getSpentBP();
    buy(c, "Kampfsinn", "", 2);
    assertEquals(before, c.getSpentBP());
  }
  @Test
  void levelAboveMaximumAfterLoweredSkillIsReported() {
    // GRW S. 232: Grenze halber Grundwert; sinkt der Wert, bleibt die Stufe mit Hinweis stehen
    Character c = withGift("Adept", 4);
    skill(c, "Pistolen").setRating(4);
    buy(c, "Verbesserte Fertigkeit", "Pistolen", 2);
    assertNull(c.getAdeptPowers().getProblem());
    skill(c, "Pistolen").setRating(2);
    assertEquals("Verbesserte Fertigkeit (Pistolen) über der Höchststufe 1", c.getAdeptPowers().getProblem());
  }

  @Test
  void reflexesAndImprovedReactionShareAugmentedMaximum() {
    // Reaktion 5, augmentiertes Maximum 9: Reflexe 3 lassen Gesteigerter Reaktion nur noch eine Stufe
    Character c = withGift("Adept", 6);
    c.getAttribute(AttributeType.REA).setValue(5);
    buy(c, "Gesteigerte Reflexe", "", 3);
    assertEquals(1, c.getAdeptPowers().getMaxLevel(power("Gesteigertes körperliches Attribut"), "REA"));

    Character other = withGift("Adept", 6);
    other.getAttribute(AttributeType.REA).setValue(5);
    buy(other, "Gesteigertes körperliches Attribut", "REA", 3);
    assertEquals(1, other.getAdeptPowers().getMaxLevel(power("Gesteigerte Reflexe"), ""));
  }

  @Test
  void lowerPowerBelowOneIsRejected() {
    Character c = withGift("Adept", 2);
    AdeptPower sense = buy(c, "Kampfsinn", "", 1);
    assertFalse(c.getAdeptPowers().canLower(sense));
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().lower(sense));
  }

  @Test
  void splitCannotDropBelowSpentPointsAtCreation() {
    Character c = withGift("Magieradept", 4);
    c.getAdeptPowers().setAdeptMagic(2);
    buy(c, "Kampfsinn", "", 3); // 1,5 Punkte → mindestens 2
    assertEquals(2, c.getAdeptPowers().getMinAdeptMagic());
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().setAdeptMagic(1));
  }

  @Test
  void splitCanOnlyGrowInCareer() {
    // GRW S. 116, 229: Entscheidung je Magiepunkt, kein Umverteilen
    Character c = withGift("Magieradept", 4);
    c.getAdeptPowers().setAdeptMagic(1);
    c.setCreated(true);
    assertEquals(1, c.getAdeptPowers().getMinAdeptMagic());
    assertThrows(IllegalArgumentException.class, () -> c.getAdeptPowers().setAdeptMagic(0));
    c.getAdeptPowers().setAdeptMagic(2);
    assertEquals(2, c.getAdeptPowers().getAdeptMagic());
  }

  @Test
  void loweredMagicAttributeKeepsPowersAndWarns() {
    Character c = withGift("Adept", 3);
    buy(c, "Gesteigerte Reflexe", "", 1); // 1,5
    buy(c, "Kampfsinn", "", 2);           // 1
    Objects.requireNonNull(c.getMagicOrResonance()).setValue(2);
    assertEquals("Kraftpunkte um 0,5 überzogen", c.getAdeptPowers().getProblem());
    assertEquals(2, c.getAdeptPowers().getPowers().size());
  }
}
