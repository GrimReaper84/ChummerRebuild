package de.shalorian.chummer.model.character.weapon;

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

/** Lädt den Waffenkatalog ({@code weapons.xml}) einmalig und cached ihn. */
public class WeaponDataLoader {

  private WeaponDataLoader() {
  }

  private static @Nullable Map<String, WeaponData> cache;

  public static synchronized @NonNull Map<String, WeaponData> getAllWeapons() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable WeaponData findByName(@NonNull String name) {
    return getAllWeapons().get(name);
  }

  private static @NonNull Map<String, WeaponData> loadFromXml() {
    String path = DataPaths.WEAPONS_XML;
    try (InputStream in = WeaponDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, WeaponData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        WeaponData data = parseItem((Element) node);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull WeaponData parseItem(@NonNull Element el) {
    String rating = childText(el, "rating");
    String range = childText(el, "range");
    return new WeaponData(
        childText(el, "name"),
        WeaponCategory.fromDisplayName(childText(el, "category")),
        childText(el, "reach"),
        childText(el, "damage"),
        childText(el, "ap"),
        childText(el, "mode"),
        childText(el, "rc"),
        childText(el, "ammo"),
        range.isEmpty() ? null : WeaponRange.fromDisplayName(range),
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        childText(el, "cost"),
        rating.isEmpty() ? 0 : Integer.parseInt(rating),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }
}
