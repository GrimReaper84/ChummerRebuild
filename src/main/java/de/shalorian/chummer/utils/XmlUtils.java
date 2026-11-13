package de.shalorian.chummer.utils;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    Element child = firstChild(parent, tag);
    if (child == null) {
      return "";
    }
    String text = child.getTextContent();
    return text == null ? "" : text.trim();
  }

  /** Getrimmte Texte aller direkten Kind-Elemente {@code tag} von {@code parent} in Dokumentreihenfolge. */
  public static @NonNull List<String> childTexts(@NonNull Element parent, @NonNull String tag) {
    List<String> texts = new ArrayList<>();
    NodeList list = parent.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i) instanceof Element element && tag.equals(element.getTagName())) {
        texts.add(element.getTextContent().trim());
      }
    }
    return texts;
  }

  /**
   * Erstes direktes Kind-Element {@code tag} von {@code parent}, bzw. {@code null}, wenn es fehlt;
   * z.B. um Attribute eines Elements zu lesen. Wie {@link #childText} nur direkte Kinder.
   */
  public static @Nullable Element firstChild(@NonNull Element parent, @NonNull String tag) {
    NodeList list = parent.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      if (node instanceof Element element && tag.equals(element.getTagName())) {
        return element;
      }
    }
    return null;
  }
}
