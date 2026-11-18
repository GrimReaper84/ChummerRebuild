package de.shalorian.chummer.model.template;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.children;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Lädt {@code archetypes.xml} (Archetypen, GRW S. 124–139) beim ersten Zugriff und cacht die Vorlagen. */
public final class ArchetypeLoader {

  private static @Nullable List<ArchetypeTemplate> cache;

  private ArchetypeLoader() {
  }

  /** Alle Archetypen in Dateireihenfolge (Buchreihenfolge). */
  public static synchronized @NonNull List<ArchetypeTemplate> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable ArchetypeTemplate findByName(@NonNull String name) {
    return getAll().stream().filter(t -> t.name().equals(name)).findFirst().orElse(null);
  }

  private static @NonNull List<ArchetypeTemplate> loadFromXml() {
    try (InputStream in = ArchetypeLoader.class.getResourceAsStream(DataPaths.ARCHETYPES_XML)) {
      if (in == null) {
        throw new IllegalStateException("archetypes.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();
      List<ArchetypeTemplate> templates = new ArrayList<>();
      for (Element el : children(doc.getDocumentElement(), "archetype")) {
        templates.add(new ArchetypeTemplate(childText(el, "name"), childText(el, "description"),
            childText(el, "source"), Integer.parseInt(el.getAttribute("page")),
            CharacterSpecReader.read(children(el, "character").getFirst())));
      }
      return List.copyOf(templates);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von archetypes.xml", e);
    }
  }
}
