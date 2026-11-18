package de.shalorian.chummer.model.npc;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.children;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.template.CharacterSpecReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Lädt {@code npcs.xml} (Beispiel-Schergen, GRW S. 325–328) beim ersten Zugriff und cacht die Vorlagen. */
public final class NpcTemplateLoader {

  private static @Nullable List<NpcTemplate> cache;

  private NpcTemplateLoader() {
  }

  /** Alle Vorlagen in Dateireihenfolge (nach Professionalitätsstufe). */
  public static synchronized @NonNull List<NpcTemplate> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable NpcTemplate findByName(@NonNull String name) {
    return getAll().stream().filter(t -> t.name().equals(name)).findFirst().orElse(null);
  }

  private static @NonNull List<NpcTemplate> loadFromXml() {
    try (InputStream in = NpcTemplateLoader.class.getResourceAsStream(DataPaths.NPCS_XML)) {
      if (in == null) {
        throw new IllegalStateException("npcs.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();
      List<NpcTemplate> templates = new ArrayList<>();
      for (Element group : children(doc.getDocumentElement(), "group")) {
        Element grunt = children(group, "grunt").getFirst();
        List<Element> leader = children(group, "leader");
        templates.add(new NpcTemplate(childText(group, "name"), Integer.parseInt(group.getAttribute("rating")),
            childText(group, "description"), childText(group, "source"), Integer.parseInt(group.getAttribute("page")),
            Integer.parseInt(group.getAttribute("members")), CharacterSpecReader.read(grunt),
            leader.isEmpty() ? null : CharacterSpecReader.read(leader.getFirst())));
      }
      return List.copyOf(templates);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von npcs.xml", e);
    }
  }
}
