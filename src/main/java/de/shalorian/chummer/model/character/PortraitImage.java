package de.shalorian.chummer.model.character;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jspecify.annotations.NonNull;

/**
 * Bereitet ein Portrait für die Speicherdatei auf: liest PNG, JPG, GIF oder BMP, verkleinert auf höchstens
 * {@value #MAX_SIDE} Pixel an der längeren Seite und speichert es als PNG (mit Transparenz). So bleibt die
 * Charakterdatei klein; das alte Chummer speicherte das Bild in voller Größe.
 */
public final class PortraitImage {

  /** Längste Seite des gespeicherten Bilds in Pixeln. */
  public static final int MAX_SIDE = 512;

  private PortraitImage() {
  }

  /**
   * Das Bild als verkleinertes PNG.
   *
   * @throws IOException wenn die Daten kein lesbares Bild sind
   */
  public static byte @NonNull [] toStoredPng(byte @NonNull [] imageBytes) throws IOException {
    BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
    if (source == null) {
      throw new IOException("Kein lesbares Bild (erlaubt sind PNG, JPG, GIF und BMP)");
    }
    double scale = Math.min(1.0, (double) MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
    int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
    int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
    BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = target.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.drawImage(source, 0, 0, width, height, null);
    } finally {
      g.dispose();
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(target, "png", out);
    return out.toByteArray();
  }
}
