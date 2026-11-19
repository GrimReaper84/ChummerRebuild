package de.shalorian.chummer.export.sheet;

import de.shalorian.chummer.App;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jspecify.annotations.NonNull;

/**
 * Schriften des Bogens, eingebettet als Teilmenge (SIL OFL, ADR-049): Inter für Text und Werte, Chakra Petch für
 * Kopfbalken. Eingebettete Schriften gehören zu einem {@link PDDocument} und werden deshalb je Dokument geladen. Anders
 * als die eingebaute Helvetica zeigen sie auch „−“, „×“ und „Ø“.
 *
 * @param regular Text
 * @param bold    Werte
 * @param heading Kopfbalken und Abschnittstitel
 */
record SheetFonts(@NonNull PDFont regular, @NonNull PDFont bold, @NonNull PDFont heading) {

  static @NonNull SheetFonts load(@NonNull PDDocument document) throws IOException {
    return new SheetFonts(font(document, "Inter-Regular.ttf"), font(document, "Inter-SemiBold.ttf"),
        font(document, "ChakraPetch-SemiBold.ttf"));
  }

  private static @NonNull PDFont font(@NonNull PDDocument document, @NonNull String file) throws IOException {
    try (InputStream in = App.class.getResourceAsStream("fonts/" + file)) {
      if (in == null) {
        throw new IOException("Schrift fehlt: " + file);
      }
      return PDType0Font.load(document, in);
    }
  }
}
