package de.shalorian.chummer.model.character.magic;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.childTexts;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.situation.SituationKind;
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
import org.w3c.dom.NodeList;

/** Lädt {@code mentors.xml} (Schutzpatrone, GRW S. 234–237) beim ersten Zugriff und cacht den Katalog. */
public final class MentorDataLoader {

  private static @Nullable Map<String, MentorData> cache;

  private MentorDataLoader() {
  }

  public static synchronized @NonNull Map<String, MentorData> getAll() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable MentorData findByName(@NonNull String name) {
    return getAll().get(name);
  }

  /** Der Patron zur gespeicherten Auswahl der Gabe („Katze (Infiltration)“), oder {@code null}. */
  public static @Nullable MentorData findByChoice(@NonNull String label) {
    return getAll().values().stream().filter(m -> m.matches(label)).findFirst().orElse(null);
  }

  /** Alle Auswahltexte für die Gabe Schutzpatron in Katalogreihenfolge. */
  public static @NonNull List<String> choiceLabels() {
    return getAll().values().stream().flatMap(m -> m.choiceLabels().stream()).toList();
  }

  private static @NonNull Map<String, MentorData> loadFromXml() {
    try (InputStream in = MentorDataLoader.class.getResourceAsStream(DataPaths.MENTORS_XML)) {
      if (in == null) {
        throw new IllegalStateException("mentors.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, MentorData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("mentor");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element element) {
          MentorData data = parse(element);
          map.put(data.name(), data);
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von mentors.xml", e);
    }
  }

  private static @NonNull MentorData parse(@NonNull Element el) {
    List<MentorBonus> bonuses = new ArrayList<>();
    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element child && "bonus".equals(child.getTagName())) {
        bonuses.add(new MentorBonus(SituationKind.valueOf(child.getAttribute("kind")), child.getAttribute("detail"),
            Integer.parseInt(child.getAttribute("dice"))));
      }
    }
    Element choice = firstChild(el, "choice");
    return new MentorData(
        childText(el, "name"),
        bonuses,
        choice == null ? null : SituationKind.valueOf(choice.getAttribute("kind")),
        choice == null ? 0 : Integer.parseInt(choice.getAttribute("dice")),
        choice == null ? List.of() : childTexts(choice, "option"),
        childText(el, "drawback"),
        childText(el, "source"),
        childText(el, "page"));
  }
}
