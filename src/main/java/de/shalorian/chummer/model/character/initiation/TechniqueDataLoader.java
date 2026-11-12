package de.shalorian.chummer.model.character.initiation;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
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

/** Lädt {@code metamagic.xml} (Metamagien und Echos) beim ersten Zugriff und cacht den Katalog. */
public final class TechniqueDataLoader {

  private static @Nullable Map<String, TechniqueData> cache;

  private TechniqueDataLoader() {
  }

  public static synchronized @NonNull Map<String, TechniqueData> getAllTechniques() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable TechniqueData findByName(@NonNull String name) {
    return getAllTechniques().get(name);
  }

  private static @NonNull Map<String, TechniqueData> loadFromXml() {
    try (InputStream in = TechniqueDataLoader.class.getResourceAsStream(DataPaths.METAMAGIC_XML)) {
      if (in == null) {
        throw new IllegalStateException("metamagic.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, TechniqueData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("technique");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element element) {
          TechniqueData data = parse(element);
          map.put(data.name(), data);
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von metamagic.xml", e);
    }
  }

  private static @NonNull TechniqueData parse(@NonNull Element el) {
    String maxTimes = childText(el, "maxtimes");
    return new TechniqueData(
        childText(el, "name"),
        TechniqueKind.fromCode(childText(el, "kind")),
        firstChild(el, "noadept") != null,
        firstChild(el, "adeptonly") != null,
        maxTimes.isEmpty() ? 1 : Integer.parseInt(maxTimes),
        childText(el, "effect"),
        childText(el, "source"),
        childText(el, "page"));
  }
}
