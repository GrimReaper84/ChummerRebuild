package de.shalorian.chummer.model.template;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.utils.XmlUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Liest eine {@link CharacterSpec} aus einem XML-Element der Vorlagen-Kataloge ({@code npcs.xml},
 * {@code archetypes.xml}): Attribute {@code metatype}, die acht Grundattribute, optional {@code MAG} bzw. {@code RES}
 * und {@code EDG}; Kind-Elemente sind {@link TemplateEntry Einträge} mit Text = Name und den Attributen {@code rating},
 * {@code amount}, {@code detail} sowie Kind-Elementen als Einbauten ({@code part}, bei Fahrzeugen {@code weapon} und
 * {@code gear}); {@code notes} ist der Ausrüstungstext.
 */
public final class CharacterSpecReader {

  private CharacterSpecReader() {
  }

  public static @NonNull CharacterSpec read(@NonNull Element el) {
    Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      attributes.put(type, Integer.parseInt(el.getAttribute(type.name())));
    }
    String magic = el.getAttribute("MAG").isEmpty() ? el.getAttribute("RES") : el.getAttribute("MAG");
    List<TemplateEntry> entries = new ArrayList<>();
    String edge = el.getAttribute("EDG");
    if (!edge.isEmpty()) {
      entries.add(new TemplateEntry("edge", "", Integer.parseInt(edge), 0, "", List.of()));
    }
    String gear = "";
    for (Element child : XmlUtils.children(el, null)) {
      if (child.getTagName().equals("notes")) {
        gear = child.getTextContent().trim();
      } else {
        entries.add(entry(child));
      }
    }
    return new CharacterSpec(el.getAttribute("metatype"), attributes, magic.isEmpty() ? 0 : Integer.parseInt(magic),
        entries, gear);
  }

  private static @NonNull TemplateEntry entry(@NonNull Element el) {
    StringBuilder name = new StringBuilder();
    for (Node node = el.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof Text text) {
        name.append(text.getData());
      }
    }
    List<TemplateEntry> parts = XmlUtils.children(el, null).stream().map(CharacterSpecReader::entry).toList();
    return new TemplateEntry(el.getTagName(), name.toString().trim(), number(el, "rating"), number(el, "amount"),
        el.getAttribute("detail"), parts);
  }

  private static int number(@NonNull Element el, @NonNull String attribute) {
    String value = el.getAttribute(attribute);
    return value.isEmpty() ? 0 : Integer.parseInt(value);
  }
}
