package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Portrait wird verkleinert und als PNG gespeichert (ADR-059). */
class PortraitImageTest {

  private static byte[] image(int width, int height, String format) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, format, out);
    return out.toByteArray();
  }

  private static BufferedImage read(byte[] png) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(png));
  }

  @Test
  void largeImageIsScaledDown() throws IOException {
    BufferedImage stored = read(PortraitImage.toStoredPng(image(2000, 1000, "jpg")));
    assertEquals(512, stored.getWidth());
    assertEquals(256, stored.getHeight());
  }

  @Test
  void smallImageKeepsItsSize() throws IOException {
    BufferedImage stored = read(PortraitImage.toStoredPng(image(100, 150, "png")));
    assertEquals(100, stored.getWidth());
    assertEquals(150, stored.getHeight());
  }

  @Test
  void garbageIsRejected() {
    assertThrows(IOException.class, () -> PortraitImage.toStoredPng(new byte[] {1, 2, 3}));
  }
}
