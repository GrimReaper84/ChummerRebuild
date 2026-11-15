package de.shalorian.chummer.model.character.skill;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Lädt die Vorschläge für Wissens- und Sprachfertigkeiten (ADR-032) beim ersten Zugriff und cacht sie. Die Listen
 * sind nur Vorschläge; eigene Namen und Spezialisierungen bleiben möglich.
 */
public final class KnowledgeSuggestionLoader {

  private static @Nullable List<KnowledgeSuggestion> cache;

  private KnowledgeSuggestionLoader() {
  }

  /** Alle Vorschläge in Dateireihenfolge. */
  static synchronized @NonNull List<KnowledgeSuggestion> getAll() {
    if (cache == null) {
      cache = Collections.unmodifiableList(loadFromXml());
    }
    return cache;
  }

  /** Namen der Vorschläge einer Kategorie, alphabetisch. */
  public static @NonNull List<String> names(@NonNull KnowledgeSkillCategory category) {
    return getAll().stream()
        .filter(s -> s.category() == category)
        .map(KnowledgeSuggestion::name)
        .distinct()
        .sorted(Collator.getInstance(Locale.GERMAN))
        .toList();
  }

  /** Vorgeschlagene Spezialisierungen für eine Fertigkeit dieses Namens (Groß-/Kleinschreibung egal), sonst leer. */
  public static @NonNull List<String> specializations(@NonNull String name, @NonNull KnowledgeSkillCategory category) {
    return getAll().stream()
        .filter(s -> s.category() == category && s.name().equalsIgnoreCase(name.trim()))
        .findFirst()
        .map(KnowledgeSuggestion::specializations)
        .orElse(List.of());
  }

  private static @NonNull List<KnowledgeSuggestion> loadFromXml() {
    try (InputStream in = KnowledgeSuggestionLoader.class.getResourceAsStream(DataPaths.KNOWLEDGE_SKILLS_XML)) {
      if (in == null) {
        throw new IllegalStateException("knowledge_skills.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      List<KnowledgeSuggestion> list = new ArrayList<>();
      for (Element categoryEl : children(doc.getDocumentElement(), "category")) {
        KnowledgeSkillCategory category = KnowledgeSkillCategory.valueOf(categoryEl.getAttribute("name"));
        List<String> categorySpecs = texts(categoryEl, "spec");
        for (Element skillEl : children(categoryEl, "skill")) {
          List<String> specs = texts(skillEl, "spec");
          list.add(new KnowledgeSuggestion(skillEl.getAttribute("name"), category,
              specs.isEmpty() ? categorySpecs : specs));
        }
      }
      return list;
    } catch (Exception e) {
      throw new IllegalStateException("Fehler beim Laden von knowledge_skills.xml", e);
    }
  }

  private static @NonNull List<Element> children(@NonNull Element parent, @NonNull String tag) {
    List<Element> result = new ArrayList<>();
    NodeList nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node instanceof Element el && el.getTagName().equals(tag)) {
        result.add(el);
      }
    }
    return result;
  }

  private static @NonNull List<String> texts(@NonNull Element parent, @NonNull String tag) {
    return children(parent, tag).stream().map(e -> e.getTextContent().trim()).toList();
  }
}
