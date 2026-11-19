package de.shalorian.chummer.model.character.rulebook;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Lädt {@code books.xml} beim ersten Zugriff und cacht die Liste der Regelwerke (ADR-061). Genau ein Eintrag muss das
 * Grundregelwerk sein.
 */
public final class RulebookDataLoader {

  private static @Nullable List<RulebookData> cache;

  private RulebookDataLoader() {
  }

  /** Alle Regelwerke in Dateireihenfolge. */
  public static synchronized @NonNull List<RulebookData> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  /** Das Grundregelwerk. */
  public static @NonNull RulebookData getCore() {
    return getAll().stream().filter(RulebookData::core).findFirst().orElseThrow();
  }

  /** Quelle für Tooltips, z.B. „Grundregelwerk, S. 205“; unbekannte Kürzel bleiben stehen. */
  public static @NonNull String describeSource(@NonNull String code, @NonNull String page) {
    String book = getAll().stream().filter(b -> b.code().equals(code)).map(RulebookData::name).findFirst()
        .orElse(code);
    return book + ", S. " + page;
  }

  private static @NonNull List<RulebookData> loadFromXml() {
    try (InputStream in = RulebookDataLoader.class.getResourceAsStream(DataPaths.BOOKS_XML)) {
      if (in == null) {
        throw new IllegalStateException("books.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      List<RulebookData> books = new ArrayList<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("book");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element el) {
          books.add(new RulebookData(childText(el, "code"), childText(el, "name"), firstChild(el, "core") != null));
        }
      }
      List<RulebookData> cores = books.stream().filter(RulebookData::core).toList();
      if (cores.size() != 1) {
        throw new IllegalStateException("books.xml braucht genau ein Grundregelwerk, gefunden: " + cores.size());
      }
      return List.copyOf(books);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von books.xml", e);
    }
  }
}
