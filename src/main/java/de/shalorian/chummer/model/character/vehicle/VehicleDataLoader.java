package de.shalorian.chummer.model.character.vehicle;

import static de.shalorian.chummer.utils.XmlUtils.childText;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
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
 * Lädt den Fahrzeugkatalog ({@code vehicles.xml}) einmalig und cached ihn. Mitgelieferte
 * Ausrüstung muss im Ausrüstungskatalog existieren und zu dessen Stufen und Zusatztexten passen,
 * sonst gilt der Katalog als fehlerhaft.
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
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        VehicleData data = parseItem((Element) node);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull VehicleData parseItem(@NonNull Element el) {
    String name = childText(el, "name");
    String[] accel = childText(el, "accel").split("/");
    String mounts = childText(el, "mounts");
    Element mountsElement = firstChild(el, "mounts");
    return new VehicleData(
        name,
        VehicleCategory.fromDisplayName(childText(el, "category")),
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
        mounts.isEmpty() ? 0 : Integer.parseInt(mounts),
        mountsElement == null ? "" : mountsElement.getAttribute("note"),
        parseIncludedGear(name, el),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }

  /** {@code <gear rating="3" extra="Clearsight">Autosoft</gear>}: mitgelieferte Ausrüstung. */
  private static @NonNull List<IncludedVehicleGear> parseIncludedGear(@NonNull String vehicle,
      @NonNull Element el) {
    List<IncludedVehicleGear> included = new ArrayList<>();
    NodeList nodes = el.getElementsByTagName("gear");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element node = (Element) nodes.item(i);
      String rating = node.getAttribute("rating");
      IncludedVehicleGear gear = new IncludedVehicleGear(node.getTextContent().trim(),
          rating.isEmpty() ? 0 : Integer.parseInt(rating), node.getAttribute("extra"));
      checkIncludedGear(vehicle, gear);
      included.add(gear);
    }
    return included;
  }

  private static void checkIncludedGear(@NonNull String vehicle, @NonNull IncludedVehicleGear gear) {
    GearData data = GearDataLoader.findByName(gear.name());
    if (data == null || !data.isValidRating(gear.rating())
        || (data.extraKind().isChoice() && !data.extraKind().suggestions().contains(gear.extra()))) {
      throw new IllegalStateException(vehicle + ": mitgelieferte Ausrüstung passt nicht zum Katalog: "
          + gear);
    }
  }

  private static @Nullable Element firstChild(@NonNull Element parent, @NonNull String tag) {
    NodeList nodes = parent.getElementsByTagName(tag);
    return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
  }
}
