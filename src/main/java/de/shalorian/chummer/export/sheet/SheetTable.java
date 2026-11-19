package de.shalorian.chummer.export.sheet;

import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;

/** Zeichnet eine Tabelle des Bogens: schwarze Kopfzeile mit den Spaltentiteln und darunter ein Zellraster. */
final class SheetTable {

  private SheetTable() {
  }

  /** Höhe einer Tabelle mit Kopf und {@code rowCount} Zeilen. */
  static float height(int rowCount, float rowHeight) {
    return SheetLayout.HEADER_ROW_HEIGHT + rowCount * rowHeight;
  }

  /**
   * Zeichnet Kopf und {@code rowCount} Zeilen; die ersten werden mit {@code rows} gefüllt (ein Text je Spalte), der
   * Rest bleibt leer für den Bleistift. Gibt das y unter der Tabelle zurück.
   */
  static float draw(@NonNull PdfCanvas canvas, float x, float y, float width, @NonNull List<Column> columns,
      @NonNull List<List<String>> rows, int rowCount, float rowHeight) throws IOException {
    float[] widths = widths(columns, width);
    float left = x;
    for (int c = 0; c < columns.size(); c++) {
      canvas.bar(left, y, widths[c], SheetLayout.HEADER_ROW_HEIGHT, columns.get(c).header(),
          columns.get(c).kind() != ColumnKind.TEXT);
      left += widths[c];
    }
    float top = y + SheetLayout.HEADER_ROW_HEIGHT;
    float bottom = top + rowCount * rowHeight;
    left = x;
    for (int c = 0; c < columns.size() - 1; c++) {
      left += widths[c];
      canvas.gridLine(left, top, left, bottom);
    }
    for (int r = 0; r < rowCount; r++) {
      float rowTop = top + r * rowHeight;
      if (r > 0) {
        canvas.gridLine(x, rowTop, x + width, rowTop);
      }
      List<String> cells = r < rows.size() ? rows.get(r) : List.of();
      left = x;
      for (int c = 0; c < columns.size(); c++) {
        String cell = c < cells.size() ? cells.get(c) : "";
        canvas.text(left, rowTop, widths[c], rowHeight, cell, canvas.fonts().regular(), SheetLayout.TEXT_SIZE,
            columns.get(c).kind() == ColumnKind.CENTERED);
        left += widths[c];
      }
    }
    canvas.box(x, top, width, bottom - top);
    return bottom;
  }

  private static float @NonNull [] widths(@NonNull List<Column> columns, float width) {
    float total = 0;
    for (Column column : columns) {
      total += column.weight();
    }
    float[] widths = new float[columns.size()];
    if (total <= 0) {
      return widths;
    }
    for (int c = 0; c < columns.size(); c++) {
      widths[c] = columns.get(c).weight() / total * width;
    }
    return widths;
  }
}
