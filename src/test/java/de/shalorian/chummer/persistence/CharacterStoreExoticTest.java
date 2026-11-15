package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.ExoticSkills;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Exotische Fertigkeiten mit Ziel und Wert überstehen Speichern und Laden (ADR-059). */
class CharacterStoreExoticTest {

  @Test
  void exoticSkillRoundtrip() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche").setRating(3);
    c.addExoticSkill(ExoticSkills.VEHICLE, "Jetpack");

    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    Skill whip = loaded.getExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    assertNotNull(whip);
    assertEquals(3, whip.getRating());
    Skill jetpack = loaded.getExoticSkill(ExoticSkills.VEHICLE, "Jetpack");
    assertNotNull(jetpack);
    assertEquals(0, jetpack.getRating());
  }

  @Test
  void aptitudeOnAnExoticSkillSurvivesLoading() {
    // Review-Befund 1: Talentiert hebt das Maximum der Instanz auch beim Laden (Qualities kommen dort zuerst)
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Skill whip = c.addExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche");
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Talentiert")), false, whip.getName());
    whip.setRating(7);

    Character loaded = CharacterStore.restore(CharacterStore.snapshot(c));
    assertEquals(7, Objects.requireNonNull(loaded.getExoticSkill(ExoticSkills.MELEE, "Monofilamentpeitsche"))
        .getRating());
  }
}
