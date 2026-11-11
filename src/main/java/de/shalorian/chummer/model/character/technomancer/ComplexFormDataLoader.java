package de.shalorian.chummer.model.character.technomancer;

import static de.shalorian.chummer.utils.XmlUtils.childText;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ComplexFormDataLoader {

  private ComplexFormDataLoader() {
  }

  private static @Nullable Map<String, ComplexFormData> cache;

  public static synchronized @NonNull Map<String, ComplexFormData> getAllComplexForms() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable ComplexFormData findByName(@NonNull String name) {
    return getAllComplexForms().get(name);
  }

  private static @NonNull Map<String, ComplexFormData> loadFromXml() {
    try (InputStream in = ComplexFormDataLoader.class.getResourceAsStream(
        DataPaths.COMPLEXFORMS_XML)) {
      if (in == null) {
        throw new IllegalStateException("complexforms.xml nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, ComplexFormData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("complexform");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element)) {
          continue;
        }
        ComplexFormData data = parseComplexForm(element);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von complexforms.xml", e);
    }
  }

  private static @NonNull ComplexFormData parseComplexForm(@NonNull Element el) {
    return new ComplexFormData(
        childText(el, "name"),
        childText(el, "category"),
        childText(el, "skill"),
        childText(el, "source"),
        childText(el, "page"));
  }
}
