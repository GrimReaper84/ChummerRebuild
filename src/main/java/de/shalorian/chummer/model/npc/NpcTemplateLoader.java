package de.shalorian.chummer.model.npc;

import static de.shalorian.chummer.utils.XmlUtils.childText;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** Lädt {@code npcs.xml} (Beispiel-Schergen, GRW S. 325–328) beim ersten Zugriff und cacht die Vorlagen. */
public final class NpcTemplateLoader {

  private static final String MAGIC_ATTRIBUTE = "MAG";
  private static @Nullable List<NpcTemplate> cache;

  private NpcTemplateLoader() {
  }

  /** Alle Vorlagen in Dateireihenfolge (nach Professionalitätsstufe). */
  public static synchronized @NonNull List<NpcTemplate> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable NpcTemplate findByName(@NonNull String name) {
    return getAll().stream().filter(t -> t.name().equals(name)).findFirst().orElse(null);
  }

  private static @NonNull List<NpcTemplate> loadFromXml() {
    try (InputStream in = NpcTemplateLoader.class.getResourceAsStream(DataPaths.NPCS_XML)) {
      if (in == null) {
        throw new IllegalStateException("npcs.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();
      List<NpcTemplate> templates = new ArrayList<>();
      for (Element group : children(doc.getDocumentElement(), "group")) {
        Element grunt = children(group, "grunt").getFirst();
        List<Element> leader = children(group, "leader");
        templates.add(new NpcTemplate(childText(group, "name"), Integer.parseInt(group.getAttribute("rating")),
            childText(group, "description"), childText(group, "source"), Integer.parseInt(group.getAttribute("page")),
            Integer.parseInt(group.getAttribute("members")), statblock(grunt),
            leader.isEmpty() ? null : statblock(leader.getFirst())));
      }
      return List.copyOf(templates);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von npcs.xml", e);
    }
  }

  private static @NonNull NpcStatblockSpec statblock(@NonNull Element el) {
    Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      attributes.put(type, Integer.parseInt(el.getAttribute(type.name())));
    }
    String magic = el.getAttribute(MAGIC_ATTRIBUTE);
    List<NpcEntry> entries = new ArrayList<>();
    String gear = "";
    for (Element child : children(el, null)) {
      if (child.getTagName().equals("gear")) {
        gear = child.getTextContent().trim();
      } else {
        entries.add(entry(child));
      }
    }
    return new NpcStatblockSpec(el.getAttribute("metatype"), attributes, magic.isEmpty() ? 0 : Integer.parseInt(magic),
        entries, gear);
  }

  private static @NonNull NpcEntry entry(@NonNull Element el) {
    StringBuilder name = new StringBuilder();
    for (Node node = el.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof Text text) {
        name.append(text.getData());
      }
    }
    String rating = el.getAttribute("rating");
    List<NpcEntry> parts = children(el, "part").stream().map(NpcTemplateLoader::entry).toList();
    return new NpcEntry(el.getTagName(), name.toString().trim(), rating.isEmpty() ? 0 : Integer.parseInt(rating),
        el.getAttribute("detail"), parts);
  }

  /** Direkte Kind-Elemente, mit {@code tag} nur die gleichnamigen. */
  private static @NonNull List<Element> children(@NonNull Element parent, @Nullable String tag) {
    List<Element> result = new ArrayList<>();
    NodeList nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i) instanceof Element el && (tag == null || el.getTagName().equals(tag))) {
        result.add(el);
      }
    }
    return result;
  }
}
