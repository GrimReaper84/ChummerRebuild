package de.shalorian.chummer.model.character;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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

public class MetatypeDataLoader {

  private static @Nullable List<MetatypeData> cache;


  private MetatypeDataLoader() {
  }

  public static synchronized @NonNull List<MetatypeData> getAllMetatypes() {
    if (cache == null) {
      cache = Collections.unmodifiableList(loadFromXml());
    }
    return cache;
  }

  public static @NonNull MetatypeData find(@NonNull String name) {
    return getAllMetatypes().stream()
        .filter(m -> m.name().equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unbekannter Metatyp: \"" + name + "\""));
  }

  private static @NonNull List<MetatypeData> loadFromXml() {
    try (InputStream in = MetatypeDataLoader.class.getResourceAsStream(DataPaths.METATYPES_XML)) {
      if (in == null) {
        throw new IllegalStateException("metatypes.xml nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      List<MetatypeData> list = new ArrayList<>();
      NodeList children = doc.getDocumentElement().getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        Element metatypeEl = (Element) node;

        list.add(parseEntry(metatypeEl, false));

        NodeList grandchildren = metatypeEl.getChildNodes();
        for (int j = 0; j < grandchildren.getLength(); j++) {
          Node child = grandchildren.item(j);
          if (child.getNodeType() == Node.ELEMENT_NODE
              && "variant".equals(((Element) child).getTagName())) {
            list.add(parseEntry((Element) child, true));
          }
        }
      }
      return list;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von metatypes.xml", e);
    }
  }

  private static @NonNull MetatypeData parseEntry(@NonNull Element el, boolean isVariant) {
    String name = el.getAttribute("name");
    int bp = Integer.parseInt(el.getAttribute("bp"));
    Map<AttributeType, AttributeRange> ranges = new EnumMap<>(AttributeType.class);
    List<String> positiveQualities = new ArrayList<>();
    List<String> negativeQualities = new ArrayList<>();
    AttributeRange edgeRange = null;

    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element childEl = (Element) child;
      switch (childEl.getTagName()) {
        case "attribute" -> {
          AttributeType type = AttributeType.valueOf(childEl.getAttribute("name"));
          int min = Integer.parseInt(childEl.getAttribute("min"));
          int max = Integer.parseInt(childEl.getAttribute("max"));
          ranges.put(type, new AttributeRange(min, max));
        }
        case "edge" -> {
          int min = Integer.parseInt(childEl.getAttribute("min"));
          int max = Integer.parseInt(childEl.getAttribute("max"));
          edgeRange = new AttributeRange(min, max);
        }
        case "qualities" -> {
          positiveQualities.addAll(extractQualityNames(childEl, "positive"));
          negativeQualities.addAll(extractQualityNames(childEl, "negative"));
        }
        default -> { /* ignoriert */ }
      }
    }

    if (edgeRange == null) {
      throw new IllegalStateException("Kein <edge>-Bereich für Metatyp \"" + name + "\"");
    }

    return new MetatypeData(name, bp, isVariant,
        Collections.unmodifiableMap(ranges),
        edgeRange,
        Collections.unmodifiableList(positiveQualities),
        Collections.unmodifiableList(negativeQualities));
  }

  private static @NonNull List<String> extractQualityNames(@NonNull Element qualitiesEl,
      @NonNull String tagName) {
    NodeList tagNodes = qualitiesEl.getElementsByTagName(tagName);
    if (tagNodes.getLength() == 0) {
      return List.of();
    }
    NodeList qNodes = ((Element) tagNodes.item(0)).getElementsByTagName("quality");
    List<String> names = new ArrayList<>();
    for (int i = 0; i < qNodes.getLength(); i++) {
      names.add(qNodes.item(i).getTextContent().trim());
    }
    return names;
  }
}