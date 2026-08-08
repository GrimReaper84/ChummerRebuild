package de.shalorian.chummer.model.character;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SpellDataLoader {

  private SpellDataLoader() {
  }

  private static @Nullable Map<String, SpellData> cache;

  public static synchronized @NonNull Map<String, SpellData> getAllSpells() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable SpellData findByName(@NonNull String name) {
    return getAllSpells().get(name);
  }

  private static @NonNull Map<String, SpellData> loadFromXml() {
    try (InputStream in = SpellDataLoader.class.getResourceAsStream(DataPaths.SPELLS_XML)) {
      if (in == null) {
        throw new IllegalStateException("spells.xml nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, SpellData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("spell");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        SpellData data = parseSpell((Element) node);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von spells.xml", e);
    }
  }

  private static @NonNull SpellData parseSpell(@NonNull Element el) {
    return new SpellData(
        childText(el, "name"),
        childText(el, "descriptors"),
        childText(el, "category"),
        childText(el, "type"),
        childText(el, "range"),
        childText(el, "damage"),
        childText(el, "duration"),
        childText(el, "dv"),
        childText(el, "source"),
        childText(el, "page"));
  }

  private static @NonNull String childText(@NonNull Element parent, @NonNull String tag) {
    NodeList list = parent.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && tag.equals(node.getNodeName())) {
        String text = node.getTextContent();
        return text == null ? "" : text.trim();
      }
    }
    return "";
  }
}
