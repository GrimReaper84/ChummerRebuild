package de.shalorian.chummer.model.character.adept;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Lädt {@code adept_powers.xml} beim ersten Zugriff und cacht den Katalog. */
public final class AdeptPowerDataLoader {

  private static final @NonNull Map<String, AdeptPowerEffect> EFFECT_TAGS = Map.of(
      "improvedreflexes", AdeptPowerEffect.IMPROVED_REFLEXES,
      "improvedattribute", AdeptPowerEffect.IMPROVED_ATTRIBUTE,
      "improvedskill", AdeptPowerEffect.IMPROVED_SKILL,
      "improvedperception", AdeptPowerEffect.IMPROVED_PERCEPTION,
      "painresistance", AdeptPowerEffect.PAIN_RESISTANCE,
      "mysticarmor", AdeptPowerEffect.MYSTIC_ARMOR,
      "astralperception", AdeptPowerEffect.ASTRAL_PERCEPTION);

  private static @Nullable Map<String, AdeptPowerData> cache;

  private AdeptPowerDataLoader() {
  }

  public static synchronized @NonNull Map<String, AdeptPowerData> getAllPowers() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable AdeptPowerData findByName(@NonNull String name) {
    return getAllPowers().get(name);
  }

  private static @NonNull Map<String, AdeptPowerData> loadFromXml() {
    try (InputStream in = AdeptPowerDataLoader.class.getResourceAsStream(DataPaths.ADEPT_POWERS_XML)) {
      if (in == null) {
        throw new IllegalStateException("adept_powers.xml nicht gefunden");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, AdeptPowerData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("power");
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i) instanceof Element element) {
          AdeptPowerData data = parsePower(element);
          map.put(data.name(), data);
        }
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von adept_powers.xml", e);
    }
  }

  private static @NonNull AdeptPowerData parsePower(@NonNull Element el) {
    String name = childText(el, "name");
    List<BigDecimal> costTable = new ArrayList<>();
    Element table = firstChild(el, "costtable");
    if (table != null) {
      NodeList children = table.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i) instanceof Element child) {
          costTable.add(new BigDecimal(child.getTextContent().trim()));
        }
      }
    }
    String costText = childText(el, "cost");
    BigDecimal cost = costText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(costText);

    Element levels = firstChild(el, "levels");
    int maxLevel = levels == null || levels.getAttribute("max").isEmpty()
        ? 0 : Integer.parseInt(levels.getAttribute("max"));

    QualitySelection selection = QualitySelection.NONE;
    String selectLabel = "";
    Element select = firstChild(el, "select");
    if (select != null) {
      selection = QualitySelection.valueOf(select.getTextContent().trim().toUpperCase(Locale.ROOT));
      selectLabel = select.getAttribute("label");
    }

    String effectText = childText(el, "effect");
    AdeptPowerEffect effect = AdeptPowerEffect.NONE;
    if (!effectText.isEmpty()) {
      effect = EFFECT_TAGS.get(effectText);
      if (effect == null) {
        throw new IllegalStateException("Unbekannte Wirkung <%s> bei %s".formatted(effectText, name));
      }
    }

    return new AdeptPowerData(name, Integer.parseInt(childText(el, "page")), cost, costTable, levels != null,
        maxLevel, selection, selectLabel, firstChild(el, "multiple") != null, effect, childText(el, "note"));
  }
}
