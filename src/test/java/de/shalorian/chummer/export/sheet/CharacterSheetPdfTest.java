package de.shalorian.chummer.export.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.template.ArchetypeLoader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

/** Der Bogen wird geschrieben, lässt sich wieder lesen und enthält die Werte des Charakters (ADR-067). */
class CharacterSheetPdfTest {

  private static Character samurai() {
    return Objects.requireNonNull(ArchetypeLoader.findByName("Straßensamurai")).create();
  }

  private static PDDocument written(Character character) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CharacterSheetPdf.write(SheetContent.of(character), out);
    return Loader.loadPDF(out.toByteArray());
  }

  private static String text(PDDocument document) throws IOException {
    return new PDFTextStripper().getText(document);
  }

  @Test
  void writesTheSheetPages() throws IOException {
    Character c = samurai();
    c.setName("Kira Schwarz");
    try (PDDocument document = written(c)) {
      assertTrue(document.getNumberOfPages() >= 2);
      String text = text(document);
      assertTrue(text.contains("Kira Schwarz"));
      assertTrue(text.contains("KÖRPERLICHE ATTRIBUTE"));
      assertTrue(text.contains("FERNKAMPFWAFFEN"));
      assertTrue(text.contains("Waffenloser Angriff"));
    }
  }

  /** Was nicht passt, landet auf einer Fortsetzungsseite. */
  @Test
  void overflowMakesContinuationPage() throws IOException {
    Character c = samurai();
    for (int i = 1; i <= SheetLayout.CONTACT_ROWS + 5; i++) {
      c.addContact("Kontakt Nummer " + i, null);
    }
    try (PDDocument document = written(c)) {
      assertTrue(document.getNumberOfPages() >= 3);
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setStartPage(3);
      String rest = stripper.getText(document);
      assertTrue(rest.contains("CONNECTIONS"));
      assertTrue(rest.contains("Kontakt Nummer " + (SheetLayout.CONTACT_ROWS + 5)));
    }
  }

  @Test
  void unicodeAndPortrait() throws IOException {
    Character c = samurai();
    c.setName("Größe − × Ø");
    BufferedImage image = new BufferedImage(8, 12, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream png = new ByteArrayOutputStream();
    ImageIO.write(image, "png", png);
    c.getBiography().setPortrait(png.toByteArray());
    try (PDDocument document = written(c)) {
      assertTrue(text(document).contains("Größe − × Ø"));
      long images = 0;
      for (var name : document.getPage(0).getResources().getXObjectNames()) {
        if (document.getPage(0).getResources().getXObject(name) instanceof PDImageXObject) {
          images++;
        }
      }
      assertEquals(1, images);
    }
  }
}
