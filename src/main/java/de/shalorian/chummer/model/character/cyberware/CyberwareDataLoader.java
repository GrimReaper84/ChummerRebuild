package de.shalorian.chummer.model.character.cyberware;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.gear.GearCategory;
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
 * Lädt die Cyberware- und Bioware-Kataloge ({@code cyberware.xml}, {@code bioware.xml}) einmalig und
 * cached sie. Beide Dateien teilen dieselbe Eintragsstruktur; die {@link EquipmentKind} eines Eintrags
 * ergibt sich ausschließlich aus seiner Quelldatei. Namen sind über beide Kataloge hinweg eindeutig.
 */
public class CyberwareDataLoader {

  private CyberwareDataLoader() {
  }

  private static @Nullable Map<String, CyberwareData> cache;

  public static synchronized @NonNull Map<String, CyberwareData> getAllCyberware() {
    if (cache == null) {
      Map<String, CyberwareData> map = new LinkedHashMap<>();
      map.putAll(loadFromXml(DataPaths.CYBERWARE_XML, EquipmentKind.CYBERWARE));
      map.putAll(loadFromXml(DataPaths.BIOWARE_XML, EquipmentKind.BIOWARE));
      cache = Collections.unmodifiableMap(map);
    }
    return cache;
  }

  public static @Nullable CyberwareData findByName(@NonNull String name) {
    return getAllCyberware().get(name);
  }

  private static @NonNull Map<String, CyberwareData> loadFromXml(@NonNull String path,
      @NonNull EquipmentKind kind) {
    try (InputStream in = CyberwareDataLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(path + " nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, CyberwareData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element)) {
          continue;
        }
        CyberwareData data = parseItem(element, kind);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von " + path, e);
    }
  }

  private static @NonNull CyberwareData parseItem(@NonNull Element el, @NonNull EquipmentKind kind) {
    String allowGear = childText(el, "allowgear");
    return new CyberwareData(
        childText(el, "name"),
        childText(el, "category"),
        kind,
        childText(el, "essence"),
        childText(el, "cost"),
        intOrZero(childText(el, "rating")),
        Availability.parse(childText(el, "avail"), childText(el, "legality")),
        childText(el, "source"),
        childText(el, "page"),
        intOrZero(childText(el, "pricemin")),
        intOrZero(childText(el, "pricemax")),
        childText(el, "capacity"),
        childText(el, "capacitycost"),
        parseEffects(el),
        parseSelection(childText(el, "select")),
        new ImplantRestrictions(
            firstChild(el, "multiple") != null,
            allTexts(el, "excludes"),
            allTexts(el, "excludescategory"),
            childText(el, "requires"),
            firstChild(el, "initiativeboost") != null,
            firstChild(el, "installonly") != null,
            firstChild(el, "excludespaintolerance") != null),
        allowGear.isEmpty() ? null : GearCategory.fromDisplayName(allowGear),
        childText(el, "note"));
  }

  private static int intOrZero(@NonNull String text) {
    return text.isEmpty() ? 0 : Integer.parseInt(text);
  }

  /** {@code <select>skill</select>} bzw. {@code skillgroup}; fehlt der Tag, gibt es keine Auswahl. */
  private static @NonNull ImplantSelection parseSelection(@NonNull String code) {
    return switch (code) {
      case "" -> ImplantSelection.NONE;
      case "skill" -> ImplantSelection.SKILL;
      case "skillgroup" -> ImplantSelection.SKILL_GROUP;
      default -> throw new IllegalArgumentException("Unbekannte Implantat-Auswahl: " + code);
    };
  }

  private static @NonNull List<ImplantEffect> parseEffects(@NonNull Element el) {
    List<ImplantEffect> effects = new ArrayList<>();
    for (Element effect : directChildren(el, "effect")) {
      effects.add(new ImplantEffect(ImplantEffectType.fromCode(effect.getAttribute("type")),
          effect.getAttribute("target"), effect.getTextContent().trim()));
    }
    return effects;
  }

  /** Texte aller direkten Kind-Elemente {@code tag} (wiederholbare Tags wie {@code <excludes>}). */
  private static @NonNull List<String> allTexts(@NonNull Element el, @NonNull String tag) {
    return directChildren(el, tag).stream().map(e -> e.getTextContent().trim()).toList();
  }

  private static @NonNull List<Element> directChildren(@NonNull Element el, @NonNull String tag) {
    List<Element> result = new ArrayList<>();
    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element child && tag.equals(child.getTagName())) {
        result.add(child);
      }
    }
    return result;
  }
}
