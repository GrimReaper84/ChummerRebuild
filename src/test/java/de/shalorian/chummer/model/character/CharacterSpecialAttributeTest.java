package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import org.junit.jupiter.api.Test;
import static de.shalorian.chummer.model.character.QualityTestSupport.selected;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SR4-Grundregelwerk (Sonderattribute): Edge ist für jeden Charakter sofort verfügbar, mit
 * rassenabhängiger Spanne (Mensch 2-7, sonst 1-6; laut GRW S. 105 bekommt nur der Mensch +1 Edge).
 * Magic/Resonance sind 0/nicht vorhanden, bis eine passende Quality gewählt wird (dann Startwert 1),
 * und schließen einander aus.
 */
class CharacterSpecialAttributeTest {

  private static Character mensch(String name) {
    return new Character(name, MetatypeDataLoader.find("Mensch"));
  }

  private static Character zwerg(String name) {
    return new Character(name, MetatypeDataLoader.find("Zwerg"));
  }

  @Test
  void humanEdgeRangeIsTwoToSeven() {
    Character c = mensch("Test");
    assertEquals(2, c.getEdge().getMin());
    assertEquals(7, c.getEdge().getMax());
    assertEquals(2, c.getEdge().getValue());
  }

  @Test
  void dwarfEdgeRangeIsOneToSix() {
    Character c = zwerg("Test");
    assertEquals(1, c.getEdge().getMin());
    assertEquals(6, c.getEdge().getMax());
  }

  @Test
  void edgeBpDoesNotCountTowardsAttributeBpCap() {
    Character c = mensch("Test");
    c.getEdge().setValue(7); // (7-2)*10 = 50 BP, kein Rassenmax-Bonus (Edge-Ausnahme)
    assertEquals(0, c.getAttributeBP(), "Edge darf nicht in getAttributeBP() einfließen");
    assertEquals(50, c.getSpecialAttributeBP());
    assertEquals(50, c.getSpentBP() - c.getMetatype().bpCost());
  }

  @Test
  void characterHasNoMagicOrResonanceByDefault() {
    Character c = mensch("Test");
    assertFalse(c.hasMagic());
    assertFalse(c.hasResonance());
    assertNull(c.getMagicOrResonance());
  }

  @Test
  void magicianQualityUnlocksMagicAtValueOne() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    assertNotNull(magier);

    c.addQuality(magier, false);

    assertTrue(c.hasMagic());
    assertFalse(c.hasResonance());
    assertNotNull(c.getMagicOrResonance());
    assertEquals(1, c.getMagicOrResonance().getValue());
    assertEquals(1, c.getMagicOrResonance().getMin());
    assertEquals(6, c.getMagicOrResonance().getMax());
  }

  @Test
  void technomancerQualityUnlocksResonanceAtValueOne() {
    Character c = mensch("Test");
    QualityData technomant = QualityDataLoader.findByName("Technomancer");
    assertNotNull(technomant);

    c.addQuality(technomant, false);

    assertTrue(c.hasResonance());
    assertFalse(c.hasMagic());
    assertEquals(1, c.getMagicOrResonance().getValue());
  }

  @Test
  void magicAndResonanceAreMutuallyExclusive() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    QualityData technomant = QualityDataLoader.findByName("Technomancer");
    assertNotNull(magier);
    assertNotNull(technomant);

    c.addQuality(magier, false);

    assertThrows(IllegalArgumentException.class, () -> c.addQuality(technomant, false));
    // Ursprünglicher Zustand bleibt unverändert
    assertTrue(c.hasMagic());
    assertFalse(c.hasResonance());
  }

  @Test
  void magicianAndAdeptAreMutuallyExclusive() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    QualityData adept = QualityDataLoader.findByName("Adept");
    assertNotNull(magier);
    assertNotNull(adept);

    c.addQuality(magier, false);

    assertThrows(IllegalArgumentException.class, () -> c.addQuality(adept, false));
    assertTrue(c.isMagician());
  }

  @Test
  void resonanceBlocksMagicQualityToo() {
    Character c = mensch("Test");
    QualityData technomant = QualityDataLoader.findByName("Technomancer");
    QualityData adept = QualityDataLoader.findByName("Adept");
    assertNotNull(technomant);
    assertNotNull(adept);

    c.addQuality(technomant, false);

    assertThrows(IllegalArgumentException.class, () -> c.addQuality(adept, false));
  }

  @Test
  void removingLastGrantingQualityDuringCreationResetsAndRefundsMagic() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    assertNotNull(magier);

    c.addQuality(magier, false);
    c.getMagicOrResonance().setValue(4); // (4-1)*10 = 30 BP ausgegeben
    assertEquals(30, c.getSpecialAttributeBP());

    c.removeQuality(selected(c, "Zauberer"));

    assertFalse(c.hasMagic());
    assertNull(c.getMagicOrResonance());
    // Automatische Rückerstattung: Sonderattribut existiert nicht mehr, kostet daher 0 BP.
    assertEquals(0, c.getSpecialAttributeBP());
  }

  @Test
  void removingLastGrantingQualityInCareerModeResetsWithoutKarmaRefund() {
    Character c = mensch("Test");
    QualityData magier = QualityDataLoader.findByName("Zauberer");
    assertNotNull(magier);

    c.addQuality(magier, false);
    c.setCreated(true);
    c.getMagicOrResonance().setValue(4);
    c.addKarmaEntry(new LogEntry("Magie gesteigert", -75,
        java.time.LocalDate.now(java.time.ZoneId.systemDefault()), LogCategory.MANUAL));
    int karmaBefore = c.getKarmaTotal();

    c.removeQuality(selected(c, "Zauberer"));

    assertFalse(c.hasMagic());
    assertNull(c.getMagicOrResonance());
    // Keine Karma-Rückerstattung: der Karma-Log bleibt unverändert.
    assertEquals(karmaBefore, c.getKarmaTotal());
  }

  @Test
  void essenceIsFixedAtSixRegardlessOfMetatype() {
    assertEquals(6.0, mensch("Test").getEssence());
    assertEquals(6.0, zwerg("Test").getEssence());
  }

  @Test
  void metatypeChangeResetsEdgeToNewRacialRange() {
    Character c = mensch("Test");
    c.getEdge().setValue(5);
    c.setMetatype(MetatypeDataLoader.find("Troll"));
    assertEquals(1, c.getEdge().getMin());
    assertEquals(6, c.getEdge().getMax());
    assertEquals(1, c.getEdge().getValue());
  }
}
