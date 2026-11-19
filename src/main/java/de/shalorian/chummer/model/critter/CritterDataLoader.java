package de.shalorian.chummer.model.critter;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.childTexts;
import static de.shalorian.chummer.utils.XmlUtils.children;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.template.TemplateEntry;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Lädt {@code critters.xml} beim ersten Zugriff und cacht die Critter in Buchreihenfolge. */
public final class CritterDataLoader {

  private static final Set<String> SKILL_TAGS = Set.of("skill", "group", "knowledge", "special");
  private static @Nullable List<CritterData> cache;

  private CritterDataLoader() {
  }

  public static synchronized @NonNull List<CritterData> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable CritterData findByName(@NonNull String name) {
    return getAll().stream().filter(c -> c.name().equals(name)).findFirst().orElse(null);
  }

  private static @NonNull List<CritterData> loadFromXml() {
    try (InputStream in = CritterDataLoader.class.getResourceAsStream(DataPaths.CRITTERS_XML)) {
      if (in == null) {
        throw new IllegalStateException("critters.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      List<CritterData> critters = new ArrayList<>();
      for (Element el : children(doc.getDocumentElement(), "critter")) {
        critters.add(critter(el));
      }
      return List.copyOf(critters);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von critters.xml", e);
    }
  }

  private static @NonNull CritterData critter(@NonNull Element el) {
    Element attributesEl = children(el, "attributes").getFirst();
    Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      attributes.put(type, Integer.parseInt(attributesEl.getAttribute(type.name())));
    }
    String magic = attributesEl.getAttribute("MAG");
    List<TemplateEntry> skills = new ArrayList<>();
    for (Element child : children(el, null)) {
      if (SKILL_TAGS.contains(child.getTagName())) {
        skills.add(new TemplateEntry(child.getTagName(), child.getTextContent().trim(),
            Integer.parseInt(child.getAttribute("rating")), 0, child.getAttribute("detail"), List.of()));
      }
    }
    return new CritterData(childText(el, "name"), CritterCategory.valueOf(el.getAttribute("category")), attributes,
        Integer.parseInt(attributesEl.getAttribute("EDG")), magic.isEmpty() ? 0 : Integer.parseInt(magic),
        childText(el, "essence"), Integer.parseInt(childText(el, "passes")), childText(el, "movement"), skills,
        childTexts(el, "power"), childTexts(el, "weakness"), childTexts(el, "quality"), childText(el, "note"), childText(el, "source"),
        Integer.parseInt(el.getAttribute("page")));
  }
}
