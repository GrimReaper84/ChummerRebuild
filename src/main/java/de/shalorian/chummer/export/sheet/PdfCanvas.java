package de.shalorian.chummer.export.sheet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jspecify.annotations.NonNull;

/**
 * Zeichnen auf einer Bogenseite mit Ursprung oben links (y wächst nach unten, anders als in PDF): Kästen, schwarze
 * Kopfbalken, Linien, Bild und Text, der in eine Zelle eingepasst wird. Übernommen aus dem Fallout-Projekt (ADR-039
 * dort), mit eingebetteten Schriften ({@link SheetFonts}) statt Helvetica.
 */
final class PdfCanvas implements AutoCloseable {

  private static final float LINE_WIDTH = 0.6f;
  private static final float GRID_GRAY = 0.55f;
  private static final float SECTION_GRAY = 0.35f;
  private static final float LEADING = 1.15f;
  /** Abstand vom oberen Rand einer Zeile zur Grundlinie, als Anteil der Schriftgröße. */
  private static final float ASCENT = 0.8f;
  private static final float PADDING = 2f;
  private static final float SIZE_STEP = 0.5f;
  private static final float DASH = 2f;
  private static final @NonNull String ELLIPSIS = "…";
  private static final @NonNull Pattern LINE_BREAK = Pattern.compile("\n");
  private static final @NonNull Pattern SPACES = Pattern.compile(" +");
  private static final @NonNull Pattern WHITESPACE = Pattern.compile("[ \n]+");

  private final @NonNull PDDocument document;
  private final @NonNull PDPageContentStream stream;
  private final @NonNull SheetFonts fonts;
  private final float height;

  private PdfCanvas(@NonNull PDDocument document, @NonNull PDPageContentStream stream, @NonNull SheetFonts fonts,
      float height) {
    this.document = document;
    this.stream = stream;
    this.fonts = fonts;
    this.height = height;
  }

  /** Hängt eine A4-Seite an das Dokument und öffnet sie zum Zeichnen. */
  static @NonNull PdfCanvas newPage(@NonNull PDDocument document, @NonNull SheetFonts fonts) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    PdfCanvas canvas = new PdfCanvas(document, new PDPageContentStream(document, page), fonts,
        PDRectangle.A4.getHeight());
    canvas.stream.setLineWidth(LINE_WIDTH);
    return canvas;
  }

  @NonNull SheetFonts fonts() {
    return fonts;
  }

  /** Umriss eines Kastens. */
  void box(float x, float y, float width, float boxHeight) throws IOException {
    stream.setStrokingColor(0f);
    stream.addRect(x, height - y - boxHeight, width, boxHeight);
    stream.stroke();
  }

  /** Gestrichelter Umriss, z.B. Überzahl-Kästchen des Zustandsmonitors. */
  void dashedBox(float x, float y, float width, float boxHeight) throws IOException {
    stream.setLineDashPattern(new float[] {DASH, DASH}, 0);
    box(x, y, width, boxHeight);
    stream.setLineDashPattern(new float[0], 0);
  }

  /** Dünne graue Linie für Tabellenraster und Schreiblinien. */
  void gridLine(float x1, float y1, float x2, float y2) throws IOException {
    stream.setStrokingColor(GRID_GRAY);
    stream.moveTo(x1, height - y1);
    stream.lineTo(x2, height - y2);
    stream.stroke();
    stream.setStrokingColor(0f);
  }

  /** Schwarzer Balken mit weißer Beschriftung, als Feldbezeichnung und Tabellenkopf. */
  void bar(float x, float y, float width, float barHeight, @NonNull String label, boolean centered)
      throws IOException {
    filledBar(x, y, width, barHeight, label, centered, 0f);
  }

  /** Dunkelgrauer Balken als Abschnittstitel, abgesetzt vom schwarzen Tabellenkopf. */
  void section(float x, float y, float width, @NonNull String label) throws IOException {
    filledBar(x, y, width, SheetLayout.BAR_HEIGHT, label, false, SECTION_GRAY);
  }

  private void filledBar(float x, float y, float width, float barHeight, @NonNull String label, boolean centered,
      float gray) throws IOException {
    stream.setNonStrokingColor(gray);
    stream.addRect(x, height - y - barHeight, width, barHeight);
    stream.fill();
    stream.setNonStrokingColor(1f);
    text(x, y, width, barHeight, label, fonts.heading(),
        Math.min(SheetLayout.BAR_TEXT_SIZE, (barHeight - 2 * PADDING) / LEADING),
        centered);
    stream.setNonStrokingColor(0f);
  }

  /** Bild, mit gleichem Seitenverhältnis in den Kasten eingepasst und darin zentriert. */
  void image(float x, float y, float width, float boxHeight, byte @NonNull [] bytes) throws IOException {
    PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, "portrait");
    float scale = Math.min(width / image.getWidth(), boxHeight / image.getHeight());
    float drawWidth = image.getWidth() * scale;
    float drawHeight = image.getHeight() * scale;
    float left = x + (width - drawWidth) / 2;
    float top = y + (boxHeight - drawHeight) / 2;
    stream.drawImage(image, left, height - top - drawHeight, drawWidth, drawHeight);
  }

  /**
   * Text in einer Zelle: verkleinert sich von {@code maxSize} bis zu einem lesbaren Minimum, bricht an Wortgrenzen um
   * und kürzt im Notfall die letzte Zeile mit „…“. Der Block ist senkrecht zentriert.
   *
   * @return {@code false}, wenn Zeilen weggefallen sind
   */
  boolean text(float x, float y, float width, float cellHeight, @NonNull String text, @NonNull PDFont font,
      float maxSize, boolean centered) throws IOException {
    String clean = printable(text).strip();
    if (clean.isEmpty()) {
      return true;
    }
    float innerWidth = width - 2 * PADDING;
    float innerHeight = cellHeight - 2 * PADDING;
    float size = fittingSize(clean, font, maxSize, innerWidth, innerHeight);
    List<String> wrapped = wrap(clean, font, size, innerWidth);
    List<String> lines = cutToHeight(wrapped, font, size, innerWidth, innerHeight);
    float blockHeight = lines.size() * size * LEADING;
    float top = y + PADDING + Math.max(0, (innerHeight - blockHeight) / 2);
    stream.setFont(font, size);
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      float lineWidth = width(line, font, size);
      float left = centered ? x + (width - lineWidth) / 2 : x + PADDING;
      float baseline = top + i * size * LEADING + size * ASCENT;
      stream.beginText();
      stream.newLineAtOffset(left, height - baseline);
      stream.showText(line);
      stream.endText();
    }
    return lines.size() == wrapped.size();
  }

  /**
   * Die größte Größe von {@code maxSize} bis zum lesbaren Minimum, bei der das längste Wort in die Breite und die
   * umbrochenen Zeilen in die Höhe passen; sonst das Minimum.
   */
  private static float fittingSize(@NonNull String text, @NonNull PDFont font, float maxSize, float innerWidth,
      float innerHeight) throws IOException {
    float minSize = Math.min(maxSize, SheetLayout.MIN_FONT_SIZE);
    float size = maxSize;
    while (longestWord(text, font, size) > innerWidth && size - SIZE_STEP >= minSize) {
      size -= SIZE_STEP;
    }
    while (wrap(text, font, size, innerWidth).size() * size * LEADING > innerHeight && size - SIZE_STEP >= minSize) {
      size -= SIZE_STEP;
    }
    return size;
  }

  /** Die Zeilen, die in die Höhe passen (mindestens eine); die letzte endet mit „…“, wenn Zeilen wegfallen. */
  private static @NonNull List<String> cutToHeight(@NonNull List<String> lines, @NonNull PDFont font, float size,
      float innerWidth, float innerHeight) throws IOException {
    int fitting = Math.max(1, (int) Math.floor(innerHeight / (size * LEADING)));
    if (lines.size() <= fitting) {
      return lines;
    }
    List<String> cut = new ArrayList<>(lines.subList(0, fitting));
    cut.set(fitting - 1, ellipsize(cut.get(fitting - 1), font, size, innerWidth));
    return cut;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  /** Text ohne Steuerzeichen: Zeilenumbrüche bleiben für den Umbruch, Tabulatoren und Wagenrückläufe werden Leerzeichen. */
  static @NonNull String printable(@NonNull String text) {
    return text.replace('\t', ' ').replace("\r\n", "\n").replace('\r', '\n');
  }

  /** Text umbrochen in die Zeilen, die eine Zelle der Breite {@code width} bei {@code size} zeigt. */
  @NonNull List<String> wrapped(@NonNull String text, float size, float width) throws IOException {
    return wrap(printable(text).strip(), fonts.regular(), size, width - 2 * PADDING);
  }

  /** Zeilen von höchstens {@code width}; ausdrückliche Umbrüche bleiben, zu lange Wörter werden geteilt. */
  private static @NonNull List<String> wrap(@NonNull String text, @NonNull PDFont font, float size, float width)
      throws IOException {
    List<String> lines = new ArrayList<>();
    for (String paragraph : LINE_BREAK.split(text, -1)) {
      StringBuilder line = new StringBuilder();
      for (String word : SPACES.split(paragraph.strip(), -1)) {
        String candidate = line.isEmpty() ? word : line + " " + word;
        if (width(candidate, font, size) <= width) {
          line.setLength(0);
          line.append(candidate);
          continue;
        }
        if (!line.isEmpty()) {
          lines.add(line.toString());
          line.setLength(0);
        }
        line.append(splitLongWord(word, font, size, width, lines));
      }
      lines.add(line.toString());
    }
    return lines;
  }

  /** Hängt volle Stücke eines Worts, das breiter als {@code width} ist, an die Zeilen und gibt den Rest zurück. */
  private static @NonNull String splitLongWord(@NonNull String word, @NonNull PDFont font, float size, float width,
      @NonNull List<String> lines) throws IOException {
    String rest = word;
    while (width(rest, font, size) > width && rest.length() > 1) {
      int cut = rest.length() - 1;
      while (cut > 1 && width(rest.substring(0, cut), font, size) > width) {
        cut--;
      }
      lines.add(rest.substring(0, cut));
      rest = rest.substring(cut);
    }
    return rest;
  }

  /** Breite des breitesten Worts, damit der Text kleiner wird, bevor ein Wort geteilt werden muss. */
  private static float longestWord(@NonNull String text, @NonNull PDFont font, float size) throws IOException {
    float widest = 0;
    for (String word : WHITESPACE.split(text, -1)) {
      widest = Math.max(widest, width(word, font, size));
    }
    return widest;
  }

  private static @NonNull String ellipsize(@NonNull String line, @NonNull PDFont font, float size, float width)
      throws IOException {
    String shortened = line;
    while (!shortened.isEmpty() && width(shortened + ELLIPSIS, font, size) > width) {
      shortened = shortened.substring(0, shortened.length() - 1);
    }
    return shortened.stripTrailing() + ELLIPSIS;
  }

  private static float width(@NonNull String text, @NonNull PDFont font, float size) throws IOException {
    return font.getStringWidth(text) / 1000f * size;
  }
}
