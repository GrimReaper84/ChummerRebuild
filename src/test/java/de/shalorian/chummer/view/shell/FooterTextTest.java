package de.shalorian.chummer.view.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Fußzeile: BP bei der Erschaffung, Karma im Karrieremodus, dazu Nuyen und Essenz. */
class FooterTextTest {

  private static Character mensch() {
    return new Character("Test", MetatypeDataLoader.find("Mensch"));
  }

  @Test
  void creationShowsRemainingBpAndResourceNuyen() {
    Character c = mensch();
    c.setNuyenBP(2);
    assertEquals("BP: " + c.getRemainingBP() + "  |  Nuyen: 10000 ¥  |  Essenz: 6,00", FooterText.of(c));
    assertFalse(c.isCreationOverspent());
  }

  @Test
  void careerShowsKarmaAndNuyenFromLog() {
    Character c = mensch();
    c.setCreated(true);
    c.addKarmaEntry(new LogEntry("Run", 7, LocalDate.of(2072, 1, 1)));
    c.addNuyenEntry(new LogEntry("Bezahlung", 2350, LocalDate.of(2072, 1, 1)));
    assertEquals("Karma: 7  |  Nuyen: 2350 ¥  |  Essenz: 6,00", FooterText.of(c));
  }

  @Test
  void negativeNuyenAtCreationIsOverspent() {
    Character c = mensch();
    // ohne Ressourcen-BP kostet schon die Datenbuchse mehr, als da ist
    c.getEquipment().addCyberware(new Cyberware(
        Objects.requireNonNull(CyberwareDataLoader.findByName("Datenbuchse")), CyberwareGrade.STANDARD, 0));
    assertTrue(c.isCreationOverspent());
  }

  @Test
  void negativeBpAreShownAndCountAsOverspent() {
    Character c = mensch();
    for (AttributeType type : AttributeType.values()) {
      c.getAttribute(type).setValue(c.getAttribute(type).getMax());
    }
    c.setNuyenBP(50);
    int remaining = c.getRemainingBP();
    assertTrue(remaining < 0);
    assertTrue(FooterText.of(c).startsWith("BP: " + remaining + "  |"));
    assertTrue(c.isCreationOverspent());
  }

  @Test
  void careerIsNeverOverspent() {
    Character c = mensch();
    c.setCreated(true);
    assertFalse(c.isCreationOverspent());
  }
}
