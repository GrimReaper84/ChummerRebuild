package de.shalorian.chummer.model.character.quality;

import static de.shalorian.chummer.utils.XmlUtils.childText;
import static de.shalorian.chummer.utils.XmlUtils.firstChild;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QualityDataLoader {

  private QualityDataLoader() {
  }

  private static @Nullable Map<String, QualityData> cache;

  public static synchronized @NonNull Map<String, QualityData> getAllQualities() {
    if (cache == null) {
      cache = Collections.unmodifiableMap(loadFromXml());
    }
    return cache;
  }

  public static @Nullable QualityData findByName(@NonNull String name) {
    return getAllQualities().get(name);
  }

  private static @NonNull Map<String, QualityData> loadFromXml() {
    try (InputStream in = QualityDataLoader.class.getResourceAsStream(DataPaths.QUALITIES_XML)) {
      if (in == null) {
        throw new IllegalStateException("qualities.xml nicht gefunden");
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document doc = factory.newDocumentBuilder().parse(in);
      doc.getDocumentElement().normalize();

      Map<String, QualityData> map = new LinkedHashMap<>();
      NodeList nodes = doc.getDocumentElement().getChildNodes();

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (!(node instanceof Element element) || !"quality".equals(element.getTagName())) {
          continue;
        }
        QualityData data = parseQuality(element);
        map.put(data.name(), data);
      }
      return map;
    } catch (Exception e) {
      throw new RuntimeException("Fehler beim Laden von qualities.xml", e);
    }
  }

  private static @NonNull QualityData parseQuality(@NonNull Element el) {
    String name = childText(el, "name");
    int bp = Integer.parseInt(childText(el, "bp"));
    QualityType type = "Positive".equals(childText(el, "category"))
        ? QualityType.POSITIVE : QualityType.NEGATIVE;
    String pageText = childText(el, "page");
    int page = pageText.isEmpty() ? 0 : Integer.parseInt(pageText);

    QualitySelection selection = QualitySelection.NONE;
    String selectLabel = "";
    Element select = firstChild(el, "select");
    if (select != null) {
      selection = QualitySelection.valueOf(select.getTextContent().trim().toUpperCase(Locale.ROOT));
      selectLabel = select.getAttribute("label");
    }

    String group = childText(el, "group");
    QualityRules rules = new QualityRules(
        group.isEmpty() ? null : group,
        firstChild(el, "multiple") != null,
        childTexts(el, "requires"),
        childTexts(el, "excludes"),
        childTexts(el, "metatypes"),
        firstChild(el, "metatype") != null);

    return new QualityData(name, bp, type, Collections.unmodifiableSet(parseBonuses(el)), page,
        childText(el, "note"), selection, selectLabel, rules);
  }

  /** Texte aller Kind-Elemente des Listen-Elements {@code tag}, leer ohne Liste. */
  private static @NonNull List<String> childTexts(@NonNull Element el, @NonNull String tag) {
    Element list = firstChild(el, tag);
    if (list == null) {
      return List.of();
    }
    List<String> texts = new ArrayList<>();
    NodeList children = list.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element child) {
        texts.add(child.getTextContent().trim());
      }
    }
    return texts;
  }

  private static final @NonNull Map<String, QualityBonusType> BONUS_TAGS = Map.ofEntries(
      Map.entry("uneducated", QualityBonusType.UNEDUCATED),
      Map.entry("uncouth", QualityBonusType.UNCOUTH),
      Map.entry("infirm", QualityBonusType.INFIRM),
      Map.entry("grantsmagic", QualityBonusType.GRANTS_MAGIC),
      Map.entry("grantsresonance", QualityBonusType.GRANTS_RESONANCE),
      Map.entry("grantsspellcasting", QualityBonusType.GRANTS_SPELLCASTING),
      Map.entry("doublescyberwareessence", QualityBonusType.DOUBLES_CYBERWARE_ESSENCE),
      Map.entry("exceptionalattribute", QualityBonusType.EXCEPTIONAL_ATTRIBUTE),
      Map.entry("lucky", QualityBonusType.LUCKY),
      Map.entry("aptitude", QualityBonusType.APTITUDE),
      Map.entry("incompetent", QualityBonusType.INCOMPETENT),
      Map.entry("burnoutessence", QualityBonusType.BURNOUT_ESSENCE));

  private static @NonNull Set<QualityBonusType> parseBonuses(@NonNull Element el) {
    Set<QualityBonusType> bonuses = EnumSet.noneOf(QualityBonusType.class);
    Element bonusEl = firstChild(el, "bonus");
    if (bonusEl == null) {
      return bonuses;
    }
    NodeList children = bonusEl.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element child) {
        QualityBonusType bonus = BONUS_TAGS.get(child.getTagName());
        if (bonus == null) {
          throw new IllegalStateException("Unbekannter Bonus-Tag <%s> bei %s".formatted(child.getTagName(),
              childText(el, "name")));
        }
        bonuses.add(bonus);
      }
    }
    return bonuses;
  }
}
