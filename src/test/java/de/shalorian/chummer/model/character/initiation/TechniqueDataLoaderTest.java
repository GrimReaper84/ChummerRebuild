package de.shalorian.chummer.model.character.initiation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Metamagien (GRW S. 232/233) und Echos (S. 284) im Katalog. */
class TechniqueDataLoaderTest {

  private static TechniqueData technique(String name) {
    return Objects.requireNonNull(TechniqueDataLoader.findByName(name), name);
  }

  private static List<String> names(TechniqueKind kind) {
    return TechniqueDataLoader.getAllTechniques().values().stream()
        .filter(t -> t.kind() == kind)
        .map(TechniqueData::name)
        .toList();
  }

  @Test
  void loadsSixMetamagicsAndFiveEchoes() {
    assertEquals(List.of("Abschirmung", "Adeptenzentrierung", "Flexible Signatur", "Intensivierung", "Maskierung",
        "Zentrierung"), names(TechniqueKind.METAMAGIC));
    assertEquals(List.of("Firewall-Upgrade", "Neurofilter", "Prozessbeschleunigung", "Resonanzverbindung",
        "System-Upgrade"), names(TechniqueKind.ECHO));
    TechniqueDataLoader.getAllTechniques().values().forEach(t -> {
      assertEquals("SR4", t.source(), t.name());
      assertTrue(!t.page().isEmpty() && !t.effect().isEmpty(), t.name());
    });
  }

  @Test
  void adeptsLearnOnlyFourMetamagics() {
    // S. 232: Adepten nur Adeptenzentrierung, Flexible Signatur, Maskierung, Zentrierung
    assertEquals(List.of("Adeptenzentrierung", "Flexible Signatur", "Maskierung", "Zentrierung"),
        TechniqueDataLoader.getAllTechniques().values().stream()
            .filter(t -> t.kind() == TechniqueKind.METAMAGIC && !t.noAdept())
            .map(TechniqueData::name)
            .toList());
    assertTrue(technique("Adeptenzentrierung").adeptOnly());
  }

  @Test
  void someEchoesUpToThreeTimes() {
    assertEquals(3, technique("Firewall-Upgrade").maxTimes());
    assertEquals(3, technique("Neurofilter").maxTimes());
    assertEquals(3, technique("System-Upgrade").maxTimes());
    assertEquals(1, technique("Resonanzverbindung").maxTimes());
  }

  @Test
  void effectTextFillsGradeAndCount() {
    assertEquals("Firewall der Lebenden Persona +2.", technique("Firewall-Upgrade").effectText(3, 2));
    assertTrue(technique("Zentrierung").effectText(2, 1).startsWith("Entzugswiderstand +2 Würfel"));
  }
}
