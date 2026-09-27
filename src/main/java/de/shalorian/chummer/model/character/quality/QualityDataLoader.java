package de.shalorian.chummer.model.character.quality;

import de.shalorian.chummer.model.character.DataPaths;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
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
      NodeList nodes = doc.getDocumentElement().getElementsByTagName("quality");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        QualityData data = parseQuality((Element) node);
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

    boolean contributeToLimit = !"no".equals(childText(el, "limit"));

    return new QualityData(name, bp, type, contributeToLimit,
        Collections.unmodifiableSet(parseBonuses(el)));
  }

  private static final @NonNull Map<String, QualityBonusType> BONUS_TAGS = Map.of(
      "uneducated", QualityBonusType.UNEDUCATED,
      "uncouth", QualityBonusType.UNCOUTH,
      "infirm", QualityBonusType.INFIRM,
      "grantsmagic", QualityBonusType.GRANTS_MAGIC,
      "grantsresonance", QualityBonusType.GRANTS_RESONANCE,
      "grantsspellcasting", QualityBonusType.GRANTS_SPELLCASTING,
      "doublescyberwareessence", QualityBonusType.DOUBLES_CYBERWARE_ESSENCE);

  private static @NonNull Set<QualityBonusType> parseBonuses(@NonNull Element el) {
    Set<QualityBonusType> bonuses = EnumSet.noneOf(QualityBonusType.class);
    NodeList bonusNodes = el.getElementsByTagName("bonus");
    if (bonusNodes.getLength() == 0) {
      return bonuses;
    }
    Element bonusEl = (Element) bonusNodes.item(0);
    for (Map.Entry<String, QualityBonusType> entry : BONUS_TAGS.entrySet()) {
      if (bonusEl.getElementsByTagName(entry.getKey()).getLength() > 0) {
        bonuses.add(entry.getValue());
      }
    }
    return bonuses;
  }

  private static @NonNull String childText(@NonNull Element parent, @NonNull String tag) {
    NodeList list = parent.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && tag.equals(node.getNodeName())) {
        String text = node.getTextContent();
        return text == null ? "" : text.trim();
      }
    }
    return "";
  }
}