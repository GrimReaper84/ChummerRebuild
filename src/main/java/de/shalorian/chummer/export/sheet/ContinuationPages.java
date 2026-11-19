package de.shalorian.chummer.export.sheet;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Seiten nach den zwei Bogenseiten: Tabellenzeilen, die keinen Platz fanden, die Zusatzbereiche und die Notizen, von
 * oben nach unten; eine neue Seite beginnt, sobald die nächste Zeile nicht mehr passt. Gibt es nichts fortzusetzen,
 * entsteht keine Seite. Übernommen aus dem Fallout-Projekt.
 */
final class ContinuationPages implements AutoCloseable {

  private static final float NOTE_SIZE = SheetLayout.TEXT_SIZE + 1;
  /** Eine Zeile in {@link #NOTE_SIZE} mit Zeilenabstand und Zellrand, damit Notizen nicht verkleinert werden. */
  private static final float NOTE_LINE_HEIGHT = 14f;
  /** Abstand zwischen dem Titel „NOTIZEN“ und der ersten Zeile; zählt mit, damit der Titel nie allein steht. */
  private static final float TITLE_GAP = 4f;

  private final @NonNull PDDocument document;
  private final @NonNull SheetFonts fonts;
  private final @NonNull String characterName;
  private @Nullable PdfCanvas canvas;
  private float y;

  ContinuationPages(@NonNull PDDocument document, @NonNull SheetFonts fonts, @NonNull String characterName) {
    this.document = document;
    this.fonts = fonts;
    this.characterName = characterName;
  }

  /** Tabelle mit Titel und den übrigen Zeilen; der Tabellenkopf wiederholt sich auf jeder neuen Seite. */
  void table(@NonNull String title, @NonNull List<Column> columns, @NonNull List<List<String>> rows)
      throws IOException {
    float rowHeight = SheetLayout.ROW_HEIGHT;
    int next = 0;
    boolean first = true;
    while (next < rows.size()) {
      PdfCanvas page = room(SheetLayout.BAR_HEIGHT + SheetTable.height(1, rowHeight));
      int fitting = Math.max(1, (int) ((SheetLayout.BOTTOM - y - SheetLayout.BAR_HEIGHT
          - SheetLayout.HEADER_ROW_HEIGHT) / rowHeight));
      int count = Math.min(fitting, rows.size() - next);
      page.section(SheetLayout.MARGIN, y, SheetLayout.CONTENT_WIDTH, first ? title : title + " (FORTS.)");
      y = SheetTable.draw(page, SheetLayout.MARGIN, y + SheetLayout.BAR_HEIGHT, SheetLayout.CONTENT_WIDTH, columns,
          rows.subList(next, next + count), count, rowHeight) + SheetLayout.GAP * 2;
      next += count;
      first = false;
    }
  }

  /** Die Notizen als umbrochene Zeilen über die Seitenbreite. */
  void notes(@NonNull List<String> notes) throws IOException {
    if (notes.isEmpty()) {
      return;
    }
    PdfCanvas page = room(SheetLayout.BAR_HEIGHT + TITLE_GAP + NOTE_LINE_HEIGHT);
    page.section(SheetLayout.MARGIN, y, SheetLayout.CONTENT_WIDTH, "NOTIZEN");
    y += SheetLayout.BAR_HEIGHT + TITLE_GAP;
    for (String note : notes) {
      for (String line : page.wrapped(note, NOTE_SIZE, SheetLayout.CONTENT_WIDTH)) {
        page = room(NOTE_LINE_HEIGHT);
        page.text(SheetLayout.MARGIN, y, SheetLayout.CONTENT_WIDTH, NOTE_LINE_HEIGHT, line, fonts.regular(),
            NOTE_SIZE, false);
        y += NOTE_LINE_HEIGHT;
      }
      y += 4;
    }
  }

  /** Die aktuelle Seite, wenn darauf noch {@code needed} Punkte frei sind, sonst eine neue. */
  private @NonNull PdfCanvas room(float needed) throws IOException {
    if (canvas != null && y + needed <= SheetLayout.BOTTOM) {
      return canvas;
    }
    if (canvas != null) {
      canvas.close();
    }
    PdfCanvas page = CharacterSheetPdf.newPage(document, fonts, characterName);
    canvas = page;
    y = SheetLayout.MARGIN;
    return page;
  }

  @Override
  public void close() throws IOException {
    if (canvas != null) {
      canvas.close();
    }
  }
}
