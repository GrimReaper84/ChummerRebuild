package de.shalorian.chummer.model.character.weapon;

import static de.shalorian.chummer.utils.XmlUtils.childText;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Lädt den Zubehörkatalog ({@code weapon_accessories.xml}) einmalig und cached ihn. */
public class AccessoryDataLoader {

  private AccessoryDataLoader() {
  }

  private static @Nullable Map<String, AccessoryData> cache;

  public static synchronized @NonNull Map<String, AccessoryData> getAllAccessories() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable AccessoryData findByName(@NonNull String name) {
    return getAllAccessories().get(name);
  }

  private static @NonNull Map<String, AccessoryData> loadFromXml() {
    String path = DataPaths.WEAPON_ACCESSORIES_XML;
    try (InputStream in = AccessoryDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, AccessoryData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element)) {
          continue;
        }
        AccessoryData data = parseItem(element);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull AccessoryData parseItem(@NonNull Element el) {
    List<Mount> mounts = new ArrayList<>();
    NodeList mountNodes = el.getElementsByTagName("mount");
    for (int i = 0; i < mountNodes.getLength(); i++) {
      mounts.add(Mount.fromDisplayName(mountNodes.item(i).getTextContent().trim()));
    }
    String rc = childText(el, "rc");
    String rule = childText(el, "rule");
    return new AccessoryData(
        childText(el, "name"),
        mounts,
        rc.isEmpty() ? 0 : Integer.parseInt(rc),
        rule.isEmpty() ? null : AccessoryRule.valueOf(rule),
        el.getElementsByTagName("projectile").getLength() > 0,
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        childText(el, "cost"),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }
}
