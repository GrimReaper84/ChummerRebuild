package de.shalorian.chummer.icons;

import de.shalorian.chummer.App;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.jspecify.annotations.NonNull;

/**
 * Zeichnet das Anwendungs-Icon (Nutzerentscheidung 2026-11-11, {@code DECISIONS.md} ADR-043): das Font-Awesome-Free-
 * Symbol „dragon“ (CC BY 4.0, umgefärbt) im Akzent-Türkis der Oberfläche auf einem abgerundeten Quadrat im dunklen
 * Petrol der Akzent-Hintergründe (Farben wie {@code style.css}, Nutzerentscheidung 2026-11-12, ADR-049), oben rechts
 * eine „4“ in Chakra Petch für die vierte Edition (Nutzerwunsch 2026-11-15). Schreibt die
 * Fenster-Icons in die Hauptressourcen und die Installer-Icons (PNG, ICO, ICNS) in den Ordner {@code packaging}; wird
 * vom Gradle-Task {@code generateIcons} gestartet, nie vom Build. Die erzeugten Dateien sind eingecheckt.
 */
public final class GenerateIcons {

  private static final String FONT = "/META-INF/resources/fontawesome6/6.5.2/fonts/fa-solid-900.ttf";
  /** Schrift der Überschriften (ADR-049) für die Ziffer. */
  private static final String DIGIT_FONT = "/de/shalorian/chummer/fonts/ChakraPetch-SemiBold.ttf";
  /** Die Edition. */
  private static final String DIGIT = "4";
  /** Font Awesome „dragon“ (fa-dragon). */
  private static final String DRAGON = "\uf6d5";
  /** {@code -cr-accent} aus {@code style.css}. */
  private static final Color ACCENT = new Color(0x4FD1C5);
  /** {@code -cr-accent-bg} aus {@code style.css}. */
  private static final Color BACKGROUND = new Color(0x1F3A39);
  /** Eckenradius des abgerundeten Quadrats relativ zu seiner Größe. */
  private static final double CORNER = 0.22;
  /** Größere Seite des Symbols relativ zur Kachel. */
  private static final double GLYPH = 0.60;
  /** Mitte des Drachen relativ zur Kachel, nach links unten gerückt, damit oben rechts Platz für die Ziffer ist. */
  private static final double GLYPH_X = 0.47;
  private static final double GLYPH_Y = 0.54;
  /** Höhe der Ziffer relativ zur Kachel. */
  private static final double DIGIT_HEIGHT = 0.26;
  /** Rechte bzw. obere Kante der Ziffer relativ zur Kachel. */
  private static final double DIGIT_RIGHT = 0.88;
  private static final double DIGIT_TOP = 0.10;
  /** Zusätzliche Strichstärke der Ziffer relativ zur Kachel, damit sie neben dem flächigen Drachen nicht zu dünn wirkt. */
  private static final double DIGIT_WEIGHT = 0.016;
  /** ICNS-Eintragstypen mit einem PNG der jeweiligen Größe (512 in 2x ist der 1024er-Eintrag). */
  private static final Map<String, Integer> ICNS_TYPES = icnsTypes();

  private GenerateIcons() {}

  /** @param args der Hauptressourcen-Ordner der Klasse {@code App} und der Ordner {@code packaging} */
  static void main(String @NonNull [] args) throws IOException, FontFormatException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Aufruf: GenerateIcons <Ressourcen-Ordner> <packaging-Ordner>");
    }
    Path resources = Files.createDirectories(Path.of(args[0]));
    Path packaging = Files.createDirectories(Path.of(args[1]));
    Shape marks = marks(loadFont(FONT), loadFont(DIGIT_FONT));
    for (int size : App.ICON_SIZES) {
      write(resources.resolve("icon-" + size + ".png"), png(draw(marks, size)));
    }
    write(packaging.resolve("icon.png"), png(draw(marks, 512)));
    write(packaging.resolve("icon.ico"), ico(marks));
    write(packaging.resolve("icon.icns"), icns(marks));
  }

  private static Font loadFont(String path) throws IOException, FontFormatException {
    try (InputStream in = GenerateIcons.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IOException("Schrift fehlt im Classpath: " + path);
      }
      return Font.createFont(Font.TRUETYPE_FONT, in);
    }
  }

  /** Drache und Ziffer als ein Umriss in Kachel-Einheiten (0 bis 1). */
  private static Shape marks(Font dragonFont, Font digitFont) {
    Shape dragon = outline(dragonFont, DRAGON);
    Rectangle2D d = dragon.getBounds2D();
    AffineTransform dragonAt = new AffineTransform();
    dragonAt.translate(GLYPH_X, GLYPH_Y);
    dragonAt.scale(GLYPH / Math.max(d.getWidth(), d.getHeight()), GLYPH / Math.max(d.getWidth(), d.getHeight()));
    dragonAt.translate(-d.getCenterX(), -d.getCenterY());
    Shape digit = outline(digitFont, DIGIT);
    Rectangle2D n = digit.getBounds2D();
    double digitScale = DIGIT_HEIGHT / n.getHeight();
    AffineTransform digitAt = new AffineTransform();
    digitAt.translate(DIGIT_RIGHT, DIGIT_TOP);
    digitAt.scale(digitScale, digitScale);
    digitAt.translate(-n.getMaxX(), -n.getMinY());
    Area area = new Area(dragonAt.createTransformedShape(dragon));
    Shape placedDigit = digitAt.createTransformedShape(digit);
    area.add(new Area(placedDigit));
    area.add(new Area(new BasicStroke((float) DIGIT_WEIGHT, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        .createStrokedShape(placedDigit)));
    return area;
  }

  private static Shape outline(Font font, String text) {
    FontRenderContext context = new FontRenderContext(null, true, true);
    return font.deriveFont(100f).createGlyphVector(context, text).getOutline();
  }

  /** Das Icon mit {@code size} Pixeln, direkt in dieser Größe gezeichnet (schärfer als ein verkleinertes großes). */
  static BufferedImage draw(Shape marks, int size) {
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      double arc = 2 * CORNER * size;
      g.setColor(BACKGROUND);
      g.fill(new RoundRectangle2D.Double(0, 0, size, size, arc, arc));
      g.setColor(ACCENT);
      g.fill(AffineTransform.getScaleInstance(size, size).createTransformedShape(marks));
    } finally {
      g.dispose();
    }
    return image;
  }

  private static byte[] png(BufferedImage image) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }

  /** Windows-Icon: Kopf, ein Verzeichniseintrag je Größe, danach die PNG-Bilder (seit Windows Vista unterstützt). */
  private static byte[] ico(Shape marks) throws IOException {
    List<byte[]> images = new ArrayList<>();
    for (int size : App.ICON_SIZES) {
      images.add(png(draw(marks, size)));
    }
    int headerSize = 6 + 16 * images.size();
    ByteBuffer header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
    header.putShort((short) 0).putShort((short) 1).putShort((short) images.size());
    int offset = headerSize;
    for (int i = 0; i < images.size(); i++) {
      int size = App.ICON_SIZES.get(i);
      header.put((byte) (size >= 256 ? 0 : size)).put((byte) (size >= 256 ? 0 : size)).put((byte) 0).put((byte) 0);
      header.putShort((short) 1).putShort((short) 32).putInt(images.get(i).length).putInt(offset);
      offset += images.get(i).length;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header.array());
    for (byte[] image : images) {
      out.write(image);
    }
    return out.toByteArray();
  }

  /** macOS-Icon: „icns“ mit Gesamtlänge, danach typisierte Einträge mit PNG-Bildern (Längen big-endian). */
  private static byte[] icns(Shape marks) throws IOException {
    ByteArrayOutputStream entries = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(entries);
    for (Map.Entry<String, Integer> type : ICNS_TYPES.entrySet()) {
      byte[] image = png(draw(marks, type.getValue()));
      data.writeBytes(type.getKey());
      data.writeInt(8 + image.length);
      data.write(image);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream file = new DataOutputStream(out);
    file.writeBytes("icns");
    file.writeInt(8 + entries.size());
    entries.writeTo(file);
    return out.toByteArray();
  }

  private static Map<String, Integer> icnsTypes() {
    Map<String, Integer> types = new LinkedHashMap<>();
    types.put("icp4", 16);
    types.put("icp5", 32);
    types.put("icp6", 64);
    types.put("ic07", 128);
    types.put("ic08", 256);
    types.put("ic09", 512);
    types.put("ic10", 1024);
    return types;
  }

  private static void write(Path file, byte[] bytes) throws IOException {
    try (OutputStream out = Files.newOutputStream(file)) {
      out.write(bytes);
    }
    System.out.println("Geschrieben: " + file);
  }
}
