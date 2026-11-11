package de.shalorian.chummer.model.character.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Abgleich mit der Tabelle Gaben/Handicaps (GRW S. 106, am Seitenbild geprüft): Je Grundeintrag die möglichen GP
 * (Stufen und Varianten aus S. 114–122) und die Seite. Handicaps mit negativen GP.
 */
class QualityDataLoaderGrwTableTest {

  private static final Map<String, Object[]> GRW = Map.ofEntries(
      Map.entry("Adept", new Object[] {115, Set.of(5)}),
      Map.entry("Astrales Chamäleon", new Object[] {115, Set.of(5)}),
      Map.entry("Außergewöhnliches Attribut", new Object[] {115, Set.of(20)}),
      Map.entry("Beidhändigkeit", new Object[] {115, Set.of(5)}),
      Map.entry("Erhöhte Konzentrationsfähigkeit", new Object[] {115, Set.of(10, 20)}),
      Map.entry("Fotografisches Gedächtnis", new Object[] {115, Set.of(10)}),
      Map.entry("Freundliche Geister", new Object[] {115, Set.of(10)}),
      Map.entry("Gelöscht", new Object[] {115, Set.of(5, 10)}),
      Map.entry("Glück", new Object[] {115, Set.of(20)}),
      Map.entry("Gummi-Gelenke", new Object[] {115, Set.of(5)}),
      Map.entry("Hohe Schmerztoleranz", new Object[] {116, Set.of(5, 10, 15)}),
      Map.entry("Magieradept", new Object[] {116, Set.of(10)}),
      Map.entry("Magieresistenz", new Object[] {116, Set.of(5, 10, 15, 20)}),
      Map.entry("Menschliches Aussehen", new Object[] {116, Set.of(5)}),
      Map.entry("Mut", new Object[] {116, Set.of(5)}),
      Map.entry("Natürliche Immunität", new Object[] {116, Set.of(5, 15)}),
      Map.entry("Pathogen- und Toxinresistenz", new Object[] {116, Set.of(5, 10)}),
      Map.entry("Programmier-Genie", new Object[] {117, Set.of(10)}),
      Map.entry("Resistenz gegen Signalspitzen", new Object[] {117, Set.of(10)}),
      Map.entry("Schlechte Verbindung", new Object[] {117, Set.of(10)}),
      Map.entry("Schnellheilung", new Object[] {117, Set.of(10)}),
      Map.entry("Schutzpatron", new Object[] {117, Set.of(5)}),
      Map.entry("Soziales Chamäleon", new Object[] {117, Set.of(5)}),
      Map.entry("Talentiert", new Object[] {117, Set.of(10)}),
      Map.entry("Technomancer", new Object[] {117, Set.of(5)}),
      Map.entry("Tierempathie", new Object[] {117, Set.of(10)}),
      Map.entry("Überlebenswille", new Object[] {117, Set.of(5, 10, 15)}),
      Map.entry("Unauffälligkeit", new Object[] {117, Set.of(10)}),
      Map.entry("Vertrautes Terrain", new Object[] {118, Set.of(10)}),
      Map.entry("Zähigkeit", new Object[] {118, Set.of(10)}),
      Map.entry("Zauberer", new Object[] {118, Set.of(15)}),
      Map.entry("Abhängigkeit", new Object[] {118, Set.of(-5, -10, -20, -30)}),
      Map.entry("Allergie", new Object[] {119, Set.of(-5, -10, -15, -20)}),
      Map.entry("Astrales Leuchtfeuer", new Object[] {119, Set.of(-5)}),
      Map.entry("Elfenposer", new Object[] {119, Set.of(-5)}),
      Map.entry("Empfindliches Nervensystem", new Object[] {119, Set.of(-5, -10)}),
      Map.entry("Feindliche Geister", new Object[] {119, Set.of(-10)}),
      Map.entry("Gezeichnet", new Object[] {119, Set.of(-5, -10)}),
      Map.entry("Gremlins", new Object[] {120, Set.of(-5, -10, -15, -20)}),
      Map.entry("Immunabstoßung", new Object[] {120, Set.of(-15)}),
      Map.entry("Inkompetenz", new Object[] {120, Set.of(-5)}),
      Map.entry("Kampflähmung", new Object[] {120, Set.of(-20)}),
      Map.entry("Körperliche Schwäche", new Object[] {120, Set.of(-20)}),
      Map.entry("Niedrige Schmerztoleranz", new Object[] {120, Set.of(-10)}),
      Map.entry("Orkposer", new Object[] {120, Set.of(-5)}),
      Map.entry("Pazifist", new Object[] {120, Set.of(-5, -10)}),
      Map.entry("Programmier-Niete", new Object[] {121, Set.of(-5)}),
      Map.entry("Schwaches Immunsystem", new Object[] {121, Set.of(-5)}),
      Map.entry("SimSinn-Desorientierung", new Object[] {121, Set.of(-10, -15)}),
      Map.entry("SIN-Mensch", new Object[] {121, Set.of(-5, -10)}),
      Map.entry("Ungebildet", new Object[] {121, Set.of(-20)}),
      Map.entry("Ungehobelt", new Object[] {122, Set.of(-20)}),
      Map.entry("Unglück", new Object[] {122, Set.of(-20)}));

  @Test
  void selectableQualitiesMatchTheGrwTable() {
    Map<String, Set<Integer>> found = new TreeMap<>();
    for (QualityData q : QualityDataLoader.getAllQualities().values()) {
      if (q.rules().metatypeOnly()) {
        continue;
      }
      String base = baseName(q.name());
      Object[] expected = GRW.get(base);
      assertTrue(expected != null, "nicht in der GRW-Tabelle: " + q.name());
      assertEquals(expected[0], q.page(), q.name());
      assertTrue(!q.note().isEmpty(), "Kurzwirkung fehlt: " + q.name());
      found.computeIfAbsent(base, _ -> new TreeSet<>()).add(q.bp());
    }
    assertEquals(GRW.keySet(), found.keySet());
    GRW.forEach((name, expected) -> assertEquals(new TreeSet<>((Set<?>) expected[1]), found.get(name), name));
  }

  /** Grundname ohne Stufe ("Gremlins 2") und ohne Variante in Klammern ("Allergie (Gewöhnlich, Leicht)"). */
  private static String baseName(String name) {
    return name.replaceAll(" \\(.*\\)$", "").replaceAll(" \\d$", "");
  }
}
