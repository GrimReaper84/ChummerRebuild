package de.shalorian.chummer.model.character.spirit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.magic.Tradition;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Geister (GRW S. 347/348) und Sprites (S. 282) im Katalog, Werte am Seitenbild gelesen. */
class SpiritDataLoaderTest {

  private static SpiritData data(String name) {
    return Objects.requireNonNull(SpiritDataLoader.findByName(name), name);
  }

  private static int stat(SpiritData data, String label, int force) {
    return data.stats().stream().filter(s -> s.label().equals(label)).findFirst().orElseThrow().valueFor(force);
  }

  @Test
  void loadsSixSpiritsAndFiveSprites() {
    assertEquals(List.of("Erdgeist", "Feuergeist", "Luftgeist", "Geist des Menschen", "Geist des Tieres",
        "Wassergeist"), SpiritDataLoader.getAll().values().stream()
        .filter(d -> d.kind() == SpiritKind.SPIRIT).map(SpiritData::name).toList());
    assertEquals(List.of("Daten-Sprite", "Infiltrator-Sprite", "Kurier-Sprite", "Maschinen-Sprite", "Stör-Sprite"),
        SpiritDataLoader.getAll().values().stream()
            .filter(d -> d.kind() == SpiritKind.SPRITE).map(SpiritData::name).toList());
  }

  @Test
  void fireSpiritForceFour() {
    // S. 347: K+1, G+2, R+3, S−2, INI (K×2)+3
    SpiritData fire = data("Feuergeist");
    assertEquals(5, stat(fire, "K", 4));
    assertEquals(6, stat(fire, "G", 4));
    assertEquals(7, stat(fire, "R", 4));
    assertEquals(2, stat(fire, "S", 4));
    assertEquals(11, stat(fire, "INI", 4));
    assertEquals(8, stat(fire, "Astrale INI", 4));
    assertEquals(List.of("Allergie (Wasser, Schwer)"), fire.weaknesses());
  }

  @Test
  void physicalAttributesAtLeastOne() {
    // Luftgeist S−3 bei Kraft 2 wäre −1; körperliche Attribute mindestens 1 (S. 347)
    assertEquals(1, stat(data("Luftgeist"), "S", 2));
  }

  @Test
  void spritesRatingThree() {
    // S. 282: Daten-Sprite Firewall S−2, Matrix-INI S×2; Stör-Sprite Prozessor S+2
    assertEquals(1, stat(data("Daten-Sprite"), "Firewall", 3));
    assertEquals(6, stat(data("Daten-Sprite"), "Matrix-INI", 3));
    assertEquals(5, stat(data("Stör-Sprite"), "Prozessor", 3));
    assertEquals(4, stat(data("Kurier-Sprite"), "Prozessor", 3));
  }

  @Test
  void traditionsCallTheirFiveSpirits() {
    // S. 213: hermetisch Kampf Feuer, schamanistisch Kampf Tier
    assertEquals("Kampf", data("Feuergeist").categoryIn(Tradition.HERMETIC));
    assertNull(data("Feuergeist").categoryIn(Tradition.SHAMANIC));
    assertEquals("Kampf", data("Geist des Tieres").categoryIn(Tradition.SHAMANIC));
    for (Tradition tradition : Tradition.values()) {
      assertEquals(5, SpiritDataLoader.getAll().values().stream()
          .filter(d -> d.categoryIn(tradition) != null).count(), tradition.name());
    }
  }

  @Test
  void optionalPowersPerThreeFullPoints() {
    assertEquals(0, data("Erdgeist").optionalCount(2));
    assertEquals(1, data("Erdgeist").optionalCount(5));
    assertEquals(2, data("Kurier-Sprite").optionalCount(6));
    assertTrue(data("Daten-Sprite").complexForms().contains("Schmöker"));
  }
}
