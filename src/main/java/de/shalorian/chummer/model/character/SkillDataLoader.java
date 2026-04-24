package de.shalorian.chummer.model.character;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SkillDataLoader {

    private static @Nullable List<Skill> cache;

    public static synchronized List<Skill> createFreshSkills() {
        if (cache == null) {
            cache = Collections.unmodifiableList(loadFromXml());
        }
        return cache.stream()
            .map(s -> new Skill(s.getName(), s.getLinkedAttribute(), s.getCategory(), s.getGroupName()))
            .collect(Collectors.toList());
    }

    private static List<Skill> loadFromXml() {
        try (InputStream in = SkillDataLoader.class.getResourceAsStream(
                "/de/shalorian/chummer/data/skills.xml")) {
            if (in == null) throw new IllegalStateException("skills.xml nicht gefunden");

            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(in);
            doc.getDocumentElement().normalize();

            List<Skill> list = new ArrayList<>();
            NodeList children = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;
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

    private static Skill parseSkill(Element el, SkillCategory category, @Nullable String groupName) {
        return new Skill(
            el.getAttribute("name"),
            AttributeType.valueOf(el.getAttribute("attribute")),
            category,
            groupName
        );
    }
}