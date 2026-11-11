package de.shalorian.chummer.model.character.armor;

import static de.shalorian.chummer.utils.XmlUtils.childText;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.equipment.Availability;
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

/** Lädt den Rüstungskatalog ({@code armor.xml}) einmalig und cached ihn. */
public class ArmorDataLoader {

  private ArmorDataLoader() {
  }

  private static @Nullable Map<String, ArmorData> cache;

  public static synchronized @NonNull Map<String, ArmorData> getAllArmor() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable ArmorData findByName(@NonNull String name) {
    return getAllArmor().get(name);
  }

  private static @NonNull Map<String, ArmorData> loadFromXml() {
    String path = DataPaths.ARMOR_XML;
    try (InputStream in = ArmorDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, ArmorData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element)) {
          continue;
        }
        ArmorData data = parseItem(element);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull ArmorData parseItem(@NonNull Element el) {
    return new ArmorData(
        childText(el, "name"),
        ArmorCategory.fromDisplayName(childText(el, "category")),
        Integer.parseInt(childText(el, "ballistic")),
        Integer.parseInt(childText(el, "impact")),
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        Integer.parseInt(childText(el, "cost")),
        childText(el, "source"),
        childText(el, "page"));
  }
}
