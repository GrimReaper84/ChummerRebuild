package de.shalorian.chummer.icons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.shalorian.chummer.App;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Die von {@link GenerateIcons} geschriebenen, eingecheckten Icons sind vorhanden und haben die erwarteten Größen und Formate. */
class AppIconTest {

  @Test
  void windowIconsInEverySize() throws IOException {
    for (int size : App.ICON_SIZES) {
      try (InputStream in = App.class.getResourceAsStream("icon-" + size + ".png")) {
        assertNotNull(in, "icon-" + size + ".png");
        BufferedImage image = ImageIO.read(in);
        assertEquals(size, image.getWidth());
        assertEquals(size, image.getHeight());
      }
    }
  }

  @Test
  void installerIcons() throws IOException {
    BufferedImage png = ImageIO.read(Path.of("packaging/icon.png").toFile());
    assertEquals(512, png.getWidth());
    byte[] ico = Files.readAllBytes(Path.of("packaging/icon.ico"));
    assertArrayEquals(new byte[] {0, 0, 1, 0}, Arrays.copyOf(ico, 4), "ICO-Kopf");
    byte[] icns = Files.readAllBytes(Path.of("packaging/icon.icns"));
    assertEquals("icns", new String(icns, 0, 4, StandardCharsets.US_ASCII));
    assertEquals(icns.length, ((icns[4] & 0xff) << 24) | ((icns[5] & 0xff) << 16) | ((icns[6] & 0xff) << 8)
        | (icns[7] & 0xff), "ICNS-Länge");
  }
}
