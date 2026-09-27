package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.DataPaths;
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

/**
 * Lädt die Cyberware- und Bioware-Kataloge ({@code cyberware.xml}, {@code bioware.xml}) einmalig und
 * cached sie. Beide Dateien teilen dieselbe Eintragsstruktur; die {@link EquipmentKind} eines Eintrags
 * ergibt sich ausschließlich aus seiner Quelldatei. Namen sind über beide Kataloge hinweg eindeutig.
 */
public class CyberwareDataLoader {

  private CyberwareDataLoader() {
  }

  private static @Nullable Map<String, CyberwareData> cache;

  public static synchronized @NonNull Map<String, CyberwareData> getAllCyberware() {
    if (cache == null) {
      Map<String, CyberwareData> map = new LinkedHashMap<>();
      map.putAll(loadFromXml(DataPaths.CYBERWARE_XML, EquipmentKind.CYBERWARE));
      map.putAll(loadFromXml(DataPaths.BIOWARE_XML, EquipmentKind.BIOWARE));
      cache = Collections.unmodifiableMap(map);
    }
    return cache;
  }

  public static @Nullable CyberwareData findByName(@NonNull String name) {
    return getAllCyberware().get(name);
  }

  private static @NonNull Map<String, CyberwareData> loadFromXml(@NonNull String path,
      @NonNull EquipmentKind kind) {
    try (InputStream in = CyberwareDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, CyberwareData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        CyberwareData data = parseItem((Element) node, kind);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull CyberwareData parseItem(@NonNull Element el, @NonNull EquipmentKind kind) {
    String rating = childText(el, "rating");
    return new CyberwareData(
        childText(el, "name"),
        childText(el, "category"),
        kind,
        childText(el, "essence"),
        childText(el, "cost"),
        rating.isEmpty() ? 0 : Integer.parseInt(rating),
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
