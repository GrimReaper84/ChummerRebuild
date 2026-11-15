package de.shalorian.chummer.model.character.vehicle;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

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

/**
 * Lädt den Fahrzeugkatalog ({@code vehicles.xml}) einmalig und cached ihn. Ob die mitgelieferte
 * Ausrüstung zum Ausrüstungskatalog passt, prüft der {@link Vehicle}-Konstruktor (über {@code Gear});
 * abgesichert durch einen Ladetest über alle Katalogfahrzeuge.
 */
public class VehicleDataLoader {

  private VehicleDataLoader() {
  }

  private static @Nullable Map<String, VehicleData> cache;

  public static synchronized @NonNull Map<String, VehicleData> getAllVehicles() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable VehicleData findByName(@NonNull String name) {
    return getAllVehicles().get(name);
  }

  private static @NonNull Map<String, VehicleData> loadFromXml() {
    String path = DataPaths.VEHICLES_XML;
    try (InputStream in = VehicleDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, VehicleData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element)) {
          continue;
        }
        VehicleData data = parseItem(element);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull VehicleData parseItem(@NonNull Element el) {
    String name = childText(el, "name");
    String[] accel = childText(el, "accel").split("/", -1);
    if (accel.length != 2) {
      throw new IllegalStateException(name + ": Beschleunigung muss \"x/y\" sein");
    }
    Element mounts = firstChild(el, "mounts");
    String includedMounts = mounts == null ? "" : mounts.getTextContent().trim();
    return new VehicleData(
        name,
        VehicleCategory.fromDisplayName(childText(el, "category")),
        childText(el, "skill"),
        Integer.parseInt(childText(el, "handling")),
        Integer.parseInt(accel[0].trim()),
        Integer.parseInt(accel[1].trim()),
        Integer.parseInt(childText(el, "speed")),
        Integer.parseInt(childText(el, "pilot")),
        Integer.parseInt(childText(el, "body")),
        Integer.parseInt(childText(el, "armor")),
        Integer.parseInt(childText(el, "sensor")),
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        Integer.parseInt(childText(el, "cost")),
        includedMounts.isEmpty() ? 0 : Integer.parseInt(includedMounts),
        mounts == null ? "" : mounts.getAttribute("note"),
        parseIncludedGear(el),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }

  /** {@code <gear rating="3" extra="Clearsight">Autosoft</gear>}: mitgelieferte Ausrüstung. */
  private static @NonNull List<IncludedVehicleGear> parseIncludedGear(@NonNull Element el) {
    List<IncludedVehicleGear> included = new ArrayList<>();
    NodeList nodes = el.getElementsByTagName("gear");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element node = (Element) nodes.item(i);
      String rating = node.getAttribute("rating");
      included.add(new IncludedVehicleGear(node.getTextContent().trim(),
          rating.isEmpty() ? 0 : Integer.parseInt(rating), node.getAttribute("extra")));
    }
    return included;
  }
}
