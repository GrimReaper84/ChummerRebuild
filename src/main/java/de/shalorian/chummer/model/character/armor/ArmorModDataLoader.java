package de.shalorian.chummer.model.character.armor;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

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
import org.w3c.dom.NodeList;

/** Lädt den Katalog der Rüstungs-Einbauten ({@code armor_mods.xml}) einmalig und cached ihn. */
public final class ArmorModDataLoader {

  private ArmorModDataLoader() {
  }

  private static @Nullable Map<String, ArmorModData> cache;

  public static synchronized @NonNull Map<String, ArmorModData> getAllMods() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable ArmorModData findByName(@NonNull String name) {
    return getAllMods().get(name);
  }

  private static @NonNull Map<String, ArmorModData> loadFromXml() {
    String path = DataPaths.ARMOR_MODS_XML;
    try (InputStream in = ArmorModDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      Map<String, ArmorModData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element element) {
          ArmorModData data = parseItem(element);
          map.put(data.name(), data);
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull ArmorModData parseItem(@NonNull Element el) {
    String forArmor = childText(el, "for");
    String avail = childText(el, "avail");
    boolean surcharge = avail.startsWith("+");
    return new ArmorModData(
        childText(el, "name"),
        forArmor.isEmpty() ? ArmorModKind.MODIFICATION : ArmorModKind.ACCESSORY,
        forArmor,
        intOrZero(childText(el, "rating")),
        intOrZero(childText(el, "ballistic")),
        intOrZero(childText(el, "impact")),
        ArmorProtection.fromCode(childText(el, "protection")),
        Availability.parse(surcharge ? avail.substring(1) : avail, childText(el, "legality")),
        surcharge,
        childText(el, "cost"),
        childText(el, "requires"),
        firstChild(el, "shield") != null,
        childText(el, "weapon"),
        childText(el, "note"),
        childText(el, "source"),
        childText(el, "page"));
  }

  private static int intOrZero(@NonNull String text) {
    return text.isEmpty() ? 0 : Integer.parseInt(text);
  }
}
