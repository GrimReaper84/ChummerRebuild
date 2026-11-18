package de.shalorian.chummer.model.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Regel A1 (GRW S. 105, 124–139): Jeder Archetyp ist nach den normalen Erschaffungsregeln gebaut und geht genau auf
 * 400 GP auf; die Teilsummen der GRW-Überschriften stehen in {@code <gp>} von {@code archetypes.xml}.
 */
class ArchetypeLoaderTest {

  @Test
  void loadsAllSixteenArchetypes() {
    assertEquals(16, ArchetypeLoader.getAll().size());
    assertNotNull(ArchetypeLoader.findByName("Straßensamurai"));
  }

  @Test
  void everyArchetypeBuildsExactlyAsInTheRulebook() throws Exception {
    Map<String, Map<String, Integer>> expected = expectedPoints();
    List<String> findings = new ArrayList<>();
    for (ArchetypeTemplate archetype : ArchetypeLoader.getAll()) {
      List<String> problems = new ArrayList<>();
      Character c = archetype.create(problems);
      problems.forEach(p -> findings.add(archetype.name() + ": Bauproblem " + p));
      Map<String, Integer> actual = points(c);
      expected.get(archetype.name()).forEach((part, gp) -> {
        if (!gp.equals(actual.get(part))) {
          findings.add(archetype.name() + ": " + part + " GRW " + gp + ", Modell " + actual.get(part));
        }
      });
      if (c.getSpentBP() != 400) {
        findings.add(archetype.name() + ": gesamt " + c.getSpentBP() + " statt 400 " + actual);
      }
      if (c.getRemainingNuyen() < 0) {
        findings.add(archetype.name() + ": Ressourcen überzogen um " + -c.getRemainingNuyen() + " ¥");
      }
    }
    assertEquals(List.of(), findings, String.join("\n", findings));
  }

  private static Map<String, Integer> points(Character c) {
    Map<String, Integer> points = new LinkedHashMap<>();
    points.put("metatype", c.getMetatype().bpCost());
    points.put("attributes", c.getAttributeBP() + c.getSpecialAttributeBP());
    points.put("qualities", c.getPositiveQualityBP() - c.getNegativeQualityBP());
    points.put("resources", c.getNuyenBP());
    points.put("contacts", c.getContactBP());
    points.put("magic", c.getSpellBP() + c.getComplexFormBP() + c.getSpirits().getBpCost() + c.getFoci().getBpCost());
    int known = points.values().stream().mapToInt(Integer::intValue).sum();
    points.put("skills", c.getSpentBP() - known);
    return points;
  }

  private static Map<String, Map<String, Integer>> expectedPoints() throws Exception {
    Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
    try (InputStream in = ArchetypeLoaderTest.class.getResourceAsStream(DataPaths.ARCHETYPES_XML)) {
      NodeList archetypes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in)
          .getElementsByTagName("archetype");
      for (int i = 0; i < archetypes.getLength(); i++) {
        Element el = (Element) archetypes.item(i);
        Element gp = (Element) el.getElementsByTagName("gp").item(0);
        Map<String, Integer> parts = new LinkedHashMap<>();
        for (int a = 0; a < gp.getAttributes().getLength(); a++) {
          parts.put(gp.getAttributes().item(a).getNodeName(),
              Integer.parseInt(gp.getAttributes().item(a).getNodeValue()));
        }
        result.put(el.getElementsByTagName("name").item(0).getTextContent(), parts);
      }
    }
    return result;
  }
}
