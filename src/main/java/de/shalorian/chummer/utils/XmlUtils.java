package de.shalorian.chummer.utils;

import org.jspecify.annotations.NonNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Gemeinsame Hilfsfunktionen der XML-Katalog-Loader. */
public final class XmlUtils {

  private XmlUtils() {
  }

  /**
   * Getrimmter Text des ersten direkten Kind-Elements {@code tag} von {@code parent}, bzw. {@code ""},
   * wenn es fehlt. Nur direkte Kinder, damit gleichnamige Tags verschachtelter Elemente nicht
   * versehentlich gelesen werden.
   */
  public static @NonNull String childText(@NonNull Element parent, @NonNull String tag) {
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
