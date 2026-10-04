package de.shalorian.chummer.model.character.gear;

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

/** Lädt den Ausrüstungskatalog ({@code gear.xml}) einmalig und cached ihn. */
public class GearDataLoader {

  private GearDataLoader() {
  }

  private static @Nullable Map<String, GearData> cache;

  public static synchronized @NonNull Map<String, GearData> getAllGear() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable GearData findByName(@NonNull String name) {
    return getAllGear().get(name);
  }

  private static @NonNull Map<String, GearData> loadFromXml() {
    String path = DataPaths.GEAR_XML;
    try (InputStream in = GearDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, GearData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        GearData data = parseItem((Element) node);
        if (map.put(data.name(), data) != null) {
          throw new IllegalStateException("Doppelter Ausrüstungsname: " + data.name());
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull GearData parseItem(@NonNull Element el) {
    // <rating>min-max</rating>, fehlt bei Gegenständen ohne Stufe
    String rating = childText(el, "rating");
    int minRating = 0;
    int maxRating = 0;
    if (!rating.isEmpty()) {
      int dash = rating.indexOf('-');
      minRating = Integer.parseInt(rating.substring(0, dash).trim());
      maxRating = Integer.parseInt(rating.substring(dash + 1).trim());
    }
    String device = childText(el, "device");
    String costFor = childText(el, "costfor");
    Element extra = firstChild(el, "extra");
    return new GearData(
        childText(el, "name"),
        GearCategory.fromDisplayName(childText(el, "category")),
        minRating,
        maxRating,
        device.isEmpty() ? 0 : Integer.parseInt(device),
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        childText(el, "cost"),
        costFor.isEmpty() ? 1 : Integer.parseInt(costFor),
        GearUnit.fromSingular(childText(el, "unit")),
        Boolean.parseBoolean(childText(el, "variablecost")),
        extraKind(extra),
        extra == null ? "" : extra.getTextContent().trim(),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }

  /** {@code <extra>} ohne Attribut = Freitext, {@code kind="LICENSE"} = Lizenz-Vorauswahl. */
  private static @NonNull ExtraKind extraKind(@Nullable Element extra) {
    if (extra == null) {
      return ExtraKind.NONE;
    }
    String kind = extra.getAttribute("kind");
    return kind.isEmpty() ? ExtraKind.TEXT : ExtraKind.valueOf(kind);
  }

  private static @Nullable Element firstChild(@NonNull Element parent, @NonNull String tag) {
    NodeList nodes = parent.getElementsByTagName(tag);
    return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
  }
}
