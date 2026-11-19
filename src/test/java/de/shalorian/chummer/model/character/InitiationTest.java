package de.shalorian.chummer.model.character;

import static de.shalorian.chummer.model.character.AdeptTestSupport.cyberware;
import static de.shalorian.chummer.model.character.AdeptTestSupport.withGift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.initiation.Initiation;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.initiation.TechniqueDataLoader;
import de.shalorian.chummer.model.character.initiation.TechniqueKind;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Initiation (GRW S. 232/233) und Wandlung (S. 284) am Charakter. */
class InitiationTest {

  private static TechniqueData technique(String name) {
    return Objects.requireNonNull(TechniqueDataLoader.findByName(name), name);
  }

  /** Charakter mit Gabe und Magie bzw. Resonanz im Karrieremodus. */
  private static Character career(String gift, int value) {
    Character c = withGift(gift, value);
    c.setCreated(true);
    return c;
  }

  @Test
  void costsTenPlusThreeTimesNewGrade() {
    Initiation initiation = career("Zauberer", 6).getInitiation();
    assertEquals(13, initiation.getNextGradeKarmaCost());
    initiation.raiseGrade();
    assertEquals(16, initiation.getNextGradeKarmaCost());
    initiation.raiseGrade();
    assertEquals(19, initiation.getNextGradeKarmaCost());
  }

  @Test
  void onlyInCareerMode() {
    Character c = withGift("Zauberer", 3);
    assertEquals("Initiation erst im Karrieremodus (GRW S. 112)", c.getInitiation().getRaiseProblem());
    assertThrows(IllegalStateException.class, () -> c.getInitiation().raiseGrade());
    Character noMagic = new Character("Test", MetatypeDataLoader.find("Mensch"));
    assertNull(noMagic.getInitiation().getKind());
  }

  @Test
  void gradeLimitedByEffectiveMagic() {
    Character c = career("Zauberer", 2);
    c.getInitiation().raiseGrade();
    c.getInitiation().raiseGrade();
    assertEquals("Initiationsgrad 3 darf Magie 2 nicht übersteigen", c.getInitiation().getRaiseProblem());
  }

  @Test
  void essenceLossCountsForTheLimitAndIsReported() {
    Character c = career("Zauberer", 2);
    c.getInitiation().raiseGrade();
    c.getInitiation().raiseGrade();
    c.getInitiation().learn(technique("Zentrierung"));
    assertFalse(c.getInitiation().isAboveAttribute());
    // 0,5 Essenz verloren → ein Punkt Magie weniger (S. 91); Grad und Metamagie bleiben, werden aber gemeldet
    c.getEquipment().addCyberware(cyberware("0.5"));
    assertTrue(c.getInitiation().isAboveAttribute());
    assertEquals(2, c.getInitiation().getGrade());
    assertEquals(1, c.getInitiation().getTechniques().size());
  }

  @Test
  void raiseLimitUsesMagicAfterEssenceLoss() {
    Character c = career("Zauberer", 3);
    c.getInitiation().raiseGrade();
    c.getInitiation().raiseGrade();
    c.getEquipment().addCyberware(cyberware("0.5"));
    assertEquals("Initiationsgrad 3 darf Magie 2 nicht übersteigen", c.getInitiation().getRaiseProblem());
  }

  @Test
  void restoreDropsTechniquesOfTheOtherKind() {
    Character c = career("Zauberer", 4);
    c.getInitiation().restore(2, List.of(technique("Neurofilter"), technique("Zentrierung")));
    assertEquals(List.of(technique("Zentrierung")), c.getInitiation().getTechniques());
  }

  @Test
  void raisesNaturalMaximum() {
    Character mage = career("Zauberer", 6);
    mage.getInitiation().raiseGrade();
    mage.getInitiation().raiseGrade();
    assertEquals(8, Objects.requireNonNull(mage.getMagicOrResonance()).getMax());
    Character techno = career("Technomancer", 5);
    techno.getInitiation().raiseGrade();
    assertEquals(TechniqueKind.ECHO, techno.getInitiation().getKind());
    assertEquals(7, Objects.requireNonNull(techno.getMagicOrResonance()).getMax());
  }

  @Test
  void oneTechniquePerGrade() {
    Initiation initiation = career("Zauberer", 4).getInitiation();
    assertEquals("Je Grad eine Metamagie; zuerst den Grad steigern",
        initiation.getLearnProblem(technique("Zentrierung")));
    initiation.raiseGrade();
    initiation.learn(technique("Zentrierung"));
    assertEquals("Bereits gelernt", initiation.getLearnProblem(technique("Zentrierung")));
    assertEquals("Je Grad eine Metamagie; zuerst den Grad steigern",
        initiation.getLearnProblem(technique("Abschirmung")));
    initiation.raiseGrade();
    assertNull(initiation.getLearnProblem(technique("Abschirmung")));
    assertEquals("Nur bei der Wandlung", initiation.getLearnProblem(technique("Neurofilter")));
    Initiation submersion = career("Technomancer", 3).getInitiation();
    assertEquals("Je Grad ein Echo; zuerst den Grad steigern",
        submersion.getLearnProblem(technique("Neurofilter")));
  }

  @Test
  void adeptRestrictions() {
    Initiation adept = career("Adept", 4).getInitiation();
    adept.raiseGrade();
    assertEquals("Adepten können diese Metamagie nicht lernen.", adept.getLearnProblem(technique("Abschirmung")));
    assertNull(adept.getLearnProblem(technique("Adeptenzentrierung")));

    Initiation mystic = career("Magieradept", 4).getInitiation();
    mystic.raiseGrade();
    assertNull(mystic.getLearnProblem(technique("Abschirmung")));
    assertNull(mystic.getLearnProblem(technique("Adeptenzentrierung")));

    Initiation mage = career("Zauberer", 4).getInitiation();
    mage.raiseGrade();
    assertEquals("Nur für Adepten und Magieradepten", mage.getLearnProblem(technique("Adeptenzentrierung")));
  }

  @Test
  void echoesUpToThreeTimes() {
    Initiation submersion = career("Technomancer", 6).getInitiation();
    for (int i = 0; i < 4; i++) {
      submersion.raiseGrade();
    }
    submersion.learn(technique("Firewall-Upgrade"));
    submersion.learn(technique("Firewall-Upgrade"));
    assertEquals("Firewall der Lebenden Persona +2.", technique("Firewall-Upgrade")
        .effectText(submersion.getGrade(), submersion.count(technique("Firewall-Upgrade"))));
    submersion.learn(technique("Firewall-Upgrade"));
    assertEquals("Höchstens 3-mal", submersion.getLearnProblem(technique("Firewall-Upgrade")));
    submersion.learn(technique("Resonanzverbindung"));
    assertEquals(0, submersion.getFreeChoices());
  }

  @Test
  void losingTheGiftClearsInitiationAndSaysSoBefore() {
    Character c = career("Zauberer", 3);
    c.getInitiation().raiseGrade();
    c.getInitiation().learn(technique("Zentrierung"));
    CharacterQuality gift = c.getQualities().stream().filter(q -> q.data().name().equals("Zauberer")).findFirst()
        .orElseThrow();
    assertTrue(c.getQualityRemovalConsequences(gift).contains("Initiationsgrad 1 samt 1 Metamagie geht verloren"));
    c.removeQuality(gift);
    assertEquals(0, c.getInitiation().getGrade());
    assertTrue(c.getInitiation().getTechniques().isEmpty());
  }
}
