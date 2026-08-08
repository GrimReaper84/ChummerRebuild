package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CharacterBiographyTest {

  @Test
  void allFieldsDefaultToEmptyString() {
    CharacterBiography bio = new CharacterBiography();

    assertEquals("", bio.getAlias());
    assertEquals("", bio.getSex());
    assertEquals("", bio.getAge());
    assertEquals("", bio.getEyes());
    assertEquals("", bio.getHeight());
    assertEquals("", bio.getWeight());
    assertEquals("", bio.getSkin());
    assertEquals("", bio.getHair());
    assertEquals("", bio.getDescription());
    assertEquals("", bio.getBackground());
    assertEquals("", bio.getConcept());
    assertEquals("", bio.getNotes());
    assertEquals("", bio.getPlayerName());
    assertEquals("", bio.getGameNotes());
  }

  @Test
  void allFieldsAreSettableAndReadable() {
    CharacterBiography bio = new CharacterBiography();

    bio.setAlias("Razorwing");
    bio.setSex("weiblich");
    bio.setAge("27");
    bio.setEyes("grün");
    bio.setHeight("172 cm");
    bio.setWeight("61 kg");
    bio.setSkin("blass");
    bio.setHair("schwarz, kurz");
    bio.setDescription("Schlank, auffällige Cybertattoos");
    bio.setBackground("Ex-Konzernangestellte");
    bio.setConcept("Straßensamurai");
    bio.setNotes("Misstraut Konzernen");
    bio.setPlayerName("Alex");
    bio.setGameNotes("Sitzung 3: Kontakt zu Mr. Johnson verloren");

    assertEquals("Razorwing", bio.getAlias());
    assertEquals("weiblich", bio.getSex());
    assertEquals("27", bio.getAge());
    assertEquals("grün", bio.getEyes());
    assertEquals("172 cm", bio.getHeight());
    assertEquals("61 kg", bio.getWeight());
    assertEquals("blass", bio.getSkin());
    assertEquals("schwarz, kurz", bio.getHair());
    assertEquals("Schlank, auffällige Cybertattoos", bio.getDescription());
    assertEquals("Ex-Konzernangestellte", bio.getBackground());
    assertEquals("Straßensamurai", bio.getConcept());
    assertEquals("Misstraut Konzernen", bio.getNotes());
    assertEquals("Alex", bio.getPlayerName());
    assertEquals("Sitzung 3: Kontakt zu Mr. Johnson verloren", bio.getGameNotes());
  }
}
