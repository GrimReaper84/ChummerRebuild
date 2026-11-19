package de.shalorian.chummer.model.critter;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.children;

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

/** Lädt {@code critter_powers.xml} beim ersten Zugriff und cacht die Kräfte nach Namen. */
public final class CritterPowerDataLoader {

  private static @Nullable Map<String, CritterPowerData> cache;

  private CritterPowerDataLoader() {
  }

  /** Alle Kräfte in Dateireihenfolge (alphabetisch, Sprite-Kräfte am Ende). */
  public static synchronized @NonNull Map<String, CritterPowerData> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable CritterPowerData findByName(@NonNull String name) {
    return getAll().get(name);
  }

  /**
   * Kraft zu einem Eintrag wie „Natürliche Waffe (Biss: Schaden 3K, PB 0)“ oder „Immunität (normale Waffen)“: der
   * Teil vor der Klammer; {@code null}, wenn ihn kein Katalogeintrag trägt.
   */
  public static @Nullable CritterPowerData findForEntry(@NonNull String entry) {
    return findByName(CritterTraits.baseName(entry));
  }

  private static @NonNull Map<String, CritterPowerData> loadFromXml() {
    try (InputStream in = CritterPowerDataLoader.class.getResourceAsStream(DataPaths.CRITTER_POWERS_XML)) {
      if (in == null) {
        throw new IllegalStateException("critter_powers.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      Map<String, CritterPowerData> powers = new LinkedHashMap<>();
      for (Element el : children(doc.getDocumentElement(), "power")) {
        CritterPowerData data = new CritterPowerData(childText(el, "name"), childText(el, "type"),
            childText(el, "action"), childText(el, "range"), childText(el, "duration"),
            CritterPowerEffect.valueOf(childText(el, "effect")), childText(el, "note"),
            Boolean.parseBoolean(el.getAttribute("sprite")), childText(el, "source"),
            Integer.parseInt(childText(el, "page")));
        powers.put(data.name(), data);
      }
      return Collections.unmodifiableMap(powers);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von critter_powers.xml", e);
    }
  }
}
