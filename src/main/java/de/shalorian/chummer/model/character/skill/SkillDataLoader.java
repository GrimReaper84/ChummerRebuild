package de.shalorian.chummer.model.character.skill;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SkillDataLoader {

  private SkillDataLoader() {
  }

  private static @Nullable List<Skill> cache;

  public static synchronized @NonNull List<Skill> createFreshSkills() {
    if (cache == null) {
      cache = Collections.unmodifiableList(loadFromXml());
    }
    return cache.stream()
        .map(s -> new Skill(s.getName(), s.getLinkedAttribute(), s.getCategory(), s.getGroupName()))
        .toList();
  }

  private static @NonNull List<Skill> loadFromXml() {
    try (InputStream in = SkillDataLoader.class.getResourceAsStream(DataPaths.SKILLS_XML)) {
      if (in == null) {
        throw new IllegalStateException("skills.xml nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      List<Skill> list = new ArrayList<>();
      NodeList children = doc.getDocumentElement().getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        Element el = (Element) node;

        if ("group".equals(el.getTagName())) {
          String groupName = el.getAttribute("name");
          SkillCategory category = SkillCategory.valueOf(el.getAttribute("category"));
          NodeList skillNodes = el.getElementsByTagName("skill");
          for (int j = 0; j < skillNodes.getLength(); j++) {
            list.add(parseSkill((Element) skillNodes.item(j), category, groupName));
          }
        } else if ("skill".equals(el.getTagName())) {
          SkillCategory category = SkillCategory.valueOf(el.getAttribute("category"));
          list.add(parseSkill(el, category, null));
        }
      }
      return list;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von skills.xml", e);
    }
  }

  private static @NonNull Skill parseSkill(@NonNull Element el, @NonNull SkillCategory category, @Nullable String groupName) {
    return new Skill(el.getAttribute("name"), el.getAttribute("attribute"), category, groupName);
  }
}