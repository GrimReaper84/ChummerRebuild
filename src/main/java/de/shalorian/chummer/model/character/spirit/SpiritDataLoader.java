package de.shalorian.chummer.model.character.spirit;

import static de.shalorian.chummer.utils.XmlUtils.childText;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.magic.Tradition;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Lädt {@code spirits.xml} (Geister und Sprites) beim ersten Zugriff und cacht den Katalog. */
public final class SpiritDataLoader {

  private static @Nullable Map<String, SpiritData> cache;

  private SpiritDataLoader() {
  }

  public static synchronized @NonNull Map<String, SpiritData> getAll() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable SpiritData findByName(@NonNull String name) {
    return getAll().get(name);
  }

  private static @NonNull Map<String, SpiritData> loadFromXml() {
    try (InputStream in = SpiritDataLoader.class.getResourceAsStream(DataPaths.SPIRITS_XML)) {
      if (in == null) {
        throw new IllegalStateException("spirits.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, SpiritData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("spirit");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element element) {
          SpiritData data = parse(element);
          map.put(data.name(), data);
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von spirits.xml", e);
    }
  }

  private static @NonNull SpiritData parse(@NonNull Element el) {
    Map<Tradition, String> categories = new EnumMap<>(Tradition.class);
    String hermetic = childText(el, "hermetic");
    if (!hermetic.isEmpty()) {
      categories.put(Tradition.HERMETIC, hermetic);
    }
    String shamanic = childText(el, "shamanic");
    if (!shamanic.isEmpty()) {
      categories.put(Tradition.SHAMANIC, shamanic);
    }
    List<SpiritStat> stats = children(el, "stat").stream()
        .map(s -> new SpiritStat(s.getAttribute("label"), s.getTextContent().trim(),
            s.getAttribute("min").isEmpty() ? 0 : Integer.parseInt(s.getAttribute("min"))))
        .toList();
    return new SpiritData(
        childText(el, "name"),
        SpiritKind.fromCode(childText(el, "kind")),
        categories,
        stats,
        childText(el, "movement"),
        texts(el, "skill"),
        texts(el, "form"),
        texts(el, "power"),
        texts(el, "optional"),
        texts(el, "weakness"),
        childText(el, "source"),
        childText(el, "page"));
  }

  private static @NonNull List<String> texts(@NonNull Element el, @NonNull String tag) {
    return children(el, tag).stream().map(e -> e.getTextContent().trim()).toList();
  }

  private static @NonNull List<Element> children(@NonNull Element el, @NonNull String tag) {
    List<Element> result = new ArrayList<>();
    NodeList nodes = el.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i) instanceof Element child && tag.equals(child.getTagName())) {
        result.add(child);
      }
    }
    return result;
  }
}
