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

/** Lädt {@code critter_weaknesses.xml} beim ersten Zugriff und cacht die Schwächen nach Namen. */
public final class CritterWeaknessDataLoader {

  private static @Nullable Map<String, CritterWeaknessData> cache;

  private CritterWeaknessDataLoader() {
  }

  /** Alle Schwächen in Dateireihenfolge. */
  public static synchronized @NonNull Map<String, CritterWeaknessData> getAll() {
    if (cache == null) {
      cache = loadFromXml();
    }
    return cache;
  }

  public static @Nullable CritterWeaknessData findByName(@NonNull String name) {
    return getAll().get(name);
  }

  /** Schwäche zu einem Eintrag wie „Allergie (Sonnenlicht, Leicht)“: der Teil vor der Klammer. */
  public static @Nullable CritterWeaknessData findForEntry(@NonNull String entry) {
    return findByName(CritterTraits.baseName(entry));
  }

  private static @NonNull Map<String, CritterWeaknessData> loadFromXml() {
    try (InputStream in = CritterWeaknessDataLoader.class.getResourceAsStream(DataPaths.CRITTER_WEAKNESSES_XML)) {
      if (in == null) {
        throw new IllegalStateException("critter_weaknesses.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      Map<String, CritterWeaknessData> weaknesses = new LinkedHashMap<>();
      for (Element el : children(doc.getDocumentElement(), "weakness")) {
        CritterWeaknessData data = new CritterWeaknessData(childText(el, "name"), childText(el, "note"),
            childText(el, "source"), Integer.parseInt(childText(el, "page")));
        weaknesses.put(data.name(), data);
      }
      return Collections.unmodifiableMap(weaknesses);
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von critter_weaknesses.xml", e);
    }
  }
}
