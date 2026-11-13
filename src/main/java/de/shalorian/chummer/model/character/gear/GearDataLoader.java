package de.shalorian.chummer.model.character.gear;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.childTexts;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
        if (!(node instanceof Element element)) {
          continue;
        }
        GearData data = parseItem(element);
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
    String costFor = childText(el, "costfor");
    // Verfügbarkeit als Zuschlag auf das Gerät ("+4", GRW S. 379/382)
    String avail = childText(el, "avail");
    Element extra = firstChild(el, "extra");
    String matrixAttribute = childText(el, "matrixattribute");
    return new GearData(
        childText(el, "name"),
        GearCategory.fromDisplayName(childText(el, "category")),
        minRating,
        maxRating,
        optionalInt(el, "device"),
        optionalInt(el, "processor"),
        optionalInt(el, "signal"),
        optionalInt(el, "firewall"),
        optionalInt(el, "system"),
        Availability.parse(avail.startsWith("+") ? avail.substring(1) : avail,
            childText(el, "legality")),
        childText(el, "cost"),
        childText(el, "costhigh"),
        avail.startsWith("+"),
        childTexts(el, "accepts").stream().map(GearCategory::fromDisplayName).collect(Collectors.toSet()),
        childText(el, "capacity"),
        childText(el, "capacitycost"),
        Boolean.parseBoolean(childText(el, "repeatable")),
        combatStats(el),
        costFor.isEmpty() ? 1 : Integer.parseInt(costFor),
        GearUnit.fromSingular(childText(el, "unit")),
        Boolean.parseBoolean(childText(el, "variablecost")),
        extraKind(extra),
        childTexts(el, "matrix").stream().map(MatrixFlag::fromCode).collect(Collectors.toSet()),
        matrixAttribute.isEmpty() ? null : MatrixAttribute.valueOf(matrixAttribute),
        extra == null ? "" : extra.getTextContent().trim(),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }

  /**
   * Kampfwerte aus {@code <damage>}, {@code <ap>}, {@code <blast>}, {@code <armortype>}; {@code null},
   * wenn der Eintrag keinen Schaden hat.
   */
  private static @Nullable GearCombatStats combatStats(@NonNull Element el) {
    String damage = childText(el, "damage");
    return damage.isEmpty() ? null : new GearCombatStats(damage, childText(el, "ap"),
        childText(el, "blast"), childText(el, "armortype"));
  }

  /** Ganzzahl aus einem optionalen Element, {@code 0} wenn es fehlt. */
  private static int optionalInt(@NonNull Element el, @NonNull String tag) {
    String text = childText(el, tag);
    return text.isEmpty() ? 0 : Integer.parseInt(text);
  }

  /**
   * {@code <extra>} ohne Attribut = Freitext; {@code kind} = {@link ExtraKind}: {@code LICENSE}
   * (Lizenz-Vorauswahl plus Freitext), {@code WEAPON_CLASS}, {@code GRENADE_FORM} bzw. {@code AUTOSOFT} (feste Auswahl).
   */
  private static @NonNull ExtraKind extraKind(@Nullable Element extra) {
    if (extra == null) {
      return ExtraKind.NONE;
    }
    String kind = extra.getAttribute("kind");
    return kind.isEmpty() ? ExtraKind.TEXT : ExtraKind.valueOf(kind);
  }
}
