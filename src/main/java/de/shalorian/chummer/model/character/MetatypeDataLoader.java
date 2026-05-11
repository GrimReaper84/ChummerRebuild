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

      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
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
        case "qualities" -> {
          NodeList posNodes = childEl.getElementsByTagName("positive");
          if (posNodes.getLength() > 0) {
            NodeList qNodes = ((Element) posNodes.item(0)).getElementsByTagName("quality");
            for (int j = 0; j < qNodes.getLength(); j++) {
              positiveQualities.add(qNodes.item(j).getTextContent().trim());
            }
          }
          NodeList negNodes = childEl.getElementsByTagName("negative");
          if (negNodes.getLength() > 0) {
            NodeList qNodes = ((Element) negNodes.item(0)).getElementsByTagName("quality");
            for (int j = 0; j < qNodes.getLength(); j++) {
              negativeQualities.add(qNodes.item(j).getTextContent().trim());
            }
          }
        }
        default -> { /* ignoriert */ }
      }
    }

    return new MetatypeData(name, bp, isVariant,
        Collections.unmodifiableMap(ranges),
        Collections.unmodifiableList(positiveQualities),
        Collections.unmodifiableList(negativeQualities));
  }
}