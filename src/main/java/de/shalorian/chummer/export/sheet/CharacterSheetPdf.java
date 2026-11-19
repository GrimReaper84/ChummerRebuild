package de.shalorian.chummer.export.sheet;

import static de.shalorian.chummer.export.sheet.SheetLayout.BAR_HEIGHT;
import static de.shalorian.chummer.export.sheet.SheetLayout.FIELD_HEIGHT;
import static de.shalorian.chummer.export.sheet.SheetLayout.GAP;
import static de.shalorian.chummer.export.sheet.SheetLayout.MARGIN;
import static de.shalorian.chummer.export.sheet.SheetLayout.ROW_HEIGHT;
import static de.shalorian.chummer.export.sheet.SheetLayout.SMALL_GAP;

import de.shalorian.chummer.AppInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.jspecify.annotations.NonNull;

/**
 * Schreibt den Charakterbogen als PDF (ADR-067): Seite 1 mit Charakterdaten, Attributen, Fertigkeiten, Gaben &amp;
 * Handicaps, Aussehen, Attributproben und Zustandsmonitor, Seite 2 mit Connections, Implantaten, Kommlink, Ausrüstung,
 * Waffen, Panzerung, Fahrzeug, Zaubern/Kräften und Geistern, danach Fortsetzungsseiten für Zeilen ohne Platz, die
 * Zusatzbereiche und die Notizen. Eine eigene schwarz-weiße Zeichnung nach der Anordnung des GRW-Bogens (S. 406/407),
 * ohne dessen Artwork. Bleistiftfelder bleiben leer.
 */
public final class CharacterSheetPdf {

  private CharacterSheetPdf() {
  }

  /** Schreibt den Bogen nach {@code out}; der Strom wird nicht geschlossen. */
  public static void write(@NonNull SheetContent content, @NonNull OutputStream out) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDDocumentInformation info = document.getDocumentInformation();
      info.setTitle(content.name().isBlank() ? "Charakterbogen" : content.name());
      info.setCreator(AppInfo.NAME);
      SheetFonts fonts = SheetFonts.load(document);
      List<String> notes = new ArrayList<>();
      try (PdfCanvas canvas = newPage(document, fonts, content.name())) {
        if (!firstPage(canvas, content)) {
          notes.add("Beschreibung: " + content.description());
        }
      }
      try (PdfCanvas canvas = newPage(document, fonts, content.name())) {
        secondPage(canvas, content);
      }
      notes.addAll(content.notes());
      try (ContinuationPages more = new ContinuationPages(document, fonts, content.name())) {
        more.table("AKTIONSFERTIGKEITEN", SheetLayout.SKILL_COLUMNS, rest(content.activeSkills(),
            2 * SheetLayout.SKILL_ROWS));
        more.table("WISSENSFERTIGKEITEN", SheetLayout.SKILL_COLUMNS, rest(content.knowledgeSkills(),
            SheetLayout.SKILL_ROWS));
        more.table("GABEN & HANDICAPS", SheetLayout.QUALITY_COLUMNS, rest(content.qualities(),
            SheetLayout.QUALITY_ROWS));
        more.table("CONNECTIONS", SheetLayout.CONTACT_COLUMNS, rest(content.contacts(), SheetLayout.CONTACT_ROWS));
        more.table("CYBERWARE/BIOWARE", SheetLayout.IMPLANT_COLUMNS, rest(content.implants(),
            SheetLayout.IMPLANT_ROWS));
        more.table("PROGRAMME", SheetLayout.PROGRAM_COLUMNS, rest(content.programs(), SheetLayout.PROGRAM_ROWS));
        more.table("AUSRÜSTUNG", SheetLayout.GEAR_COLUMNS, rest(content.gear(), SheetLayout.GEAR_ROWS));
        more.table("FERNKAMPFWAFFEN", SheetLayout.RANGED_COLUMNS, rest(content.rangedWeapons(),
            SheetLayout.RANGED_ROWS));
        more.table("NAHKAMPF", SheetLayout.MELEE_COLUMNS, rest(content.meleeWeapons(), SheetLayout.MELEE_ROWS));
        more.table("PANZERUNG", SheetLayout.ARMOR_COLUMNS, rest(content.armor(), SheetLayout.ARMOR_ROWS));
        more.table("FAHRZEUGE", SheetLayout.VEHICLE_COLUMNS, rest(content.vehicles(), SheetLayout.VEHICLE_ROWS));
        more.table("ZAUBER/ADEPTENKRÄFTE/KOMPLEXE FORMEN", SheetLayout.MAGIC_COLUMNS, rest(content.magic(),
            SheetLayout.MAGIC_ROWS));
        more.table("GEISTER/SPRITES", SheetLayout.SPIRIT_COLUMNS, rest(content.spirits(), SheetLayout.SPIRIT_ROWS));
        more.table("SITUATIVE POOLS", SheetLayout.SITUATION_COLUMNS, content.situationalPools());
        more.table("FOKI", SheetLayout.FOCUS_COLUMNS, content.foci());
        more.table("INITIATION/WANDLUNG", SheetLayout.INITIATION_COLUMNS, content.initiation());
        more.notes(notes);
      }
      document.save(out);
    }
  }

  /** Neue Seite mit Fußzeile (Charaktername, Seitenzahl, Programmname). */
  static @NonNull PdfCanvas newPage(@NonNull PDDocument document, @NonNull SheetFonts fonts,
      @NonNull String characterName) throws IOException {
    PdfCanvas canvas = PdfCanvas.newPage(document, fonts);
    String name = characterName.isBlank() ? "" : characterName + " · ";
    try {
      canvas.text(MARGIN, SheetLayout.FOOTER_Y, SheetLayout.CONTENT_WIDTH, 10f,
          name + "Seite " + document.getNumberOfPages() + " · " + AppInfo.NAME, fonts.regular(),
          SheetLayout.LABEL_SIZE, true);
    } catch (IOException | RuntimeException e) {
      canvas.close();
      throw e;
    }
    return canvas;
  }

  /** Seite 1; {@code false}, wenn die Beschreibung nicht ganz in ihren Kasten passt. */
  private static boolean firstPage(@NonNull PdfCanvas canvas, @NonNull SheetContent content) throws IOException {
    header(canvas, content);
    attributes(canvas, content);
    float third = SheetLayout.THIRD_WIDTH;
    float y = SheetLayout.SKILLS_Y;
    int rows = SheetLayout.SKILL_ROWS;
    List<List<String>> active = content.activeSkills();
    table(canvas, MARGIN, y, third, "AKTIONSFERTIGKEITEN", SheetLayout.SKILL_COLUMNS, first(active, rows), rows);
    table(canvas, MARGIN + third + SMALL_GAP, y, third, "AKTIONSFERTIGKEITEN", SheetLayout.SKILL_COLUMNS,
        first(rest(active, rows), rows), rows);
    table(canvas, MARGIN + 2 * (third + SMALL_GAP), y, third, "WISSENSFERTIGKEITEN", SheetLayout.SKILL_COLUMNS,
        first(content.knowledgeSkills(), rows), rows);
    float lower = SheetLayout.LOWER_Y;
    table(canvas, MARGIN, lower, third, "GABEN & HANDICAPS", SheetLayout.QUALITY_COLUMNS,
        first(content.qualities(), SheetLayout.QUALITY_ROWS), SheetLayout.QUALITY_ROWS);
    boolean described = appearance(canvas, MARGIN + third + SMALL_GAP, lower, third, content);
    monitor(canvas, MARGIN + 2 * (third + SMALL_GAP), lower, third, content);
    return described;
  }

  /** Name über die Breite, darunter drei Spalten Charakterdaten; rechts das Portrait oder ein leerer Rahmen. */
  private static void header(@NonNull PdfCanvas canvas, @NonNull SheetContent content) throws IOException {
    float y = SheetLayout.HEADER_Y;
    float width = SheetLayout.INFO_WIDTH;
    canvas.bar(MARGIN, y, SheetLayout.NAME_LABEL_WIDTH, SheetLayout.NAME_HEIGHT, "NAME/ALIAS", false);
    canvas.box(MARGIN + SheetLayout.NAME_LABEL_WIDTH, y, width - SheetLayout.NAME_LABEL_WIDTH,
        SheetLayout.NAME_HEIGHT);
    canvas.text(MARGIN + SheetLayout.NAME_LABEL_WIDTH, y, width - SheetLayout.NAME_LABEL_WIDTH,
        SheetLayout.NAME_HEIGHT, content.name(), canvas.fonts().bold(), SheetLayout.NAME_SIZE, false);
    float column = (width - 2 * SMALL_GAP) / 3;
    List<List<SheetField>> columns = List.of(content.identity(), content.resources(), content.reputation());
    for (int c = 0; c < columns.size(); c++) {
      fields(canvas, MARGIN + c * (column + SMALL_GAP), SheetLayout.FIELDS_Y, column, SheetLayout.FIELD_LABEL_WIDTH,
          columns.get(c));
    }
    float portraitX = MARGIN + width + GAP;
    canvas.box(portraitX, y, SheetLayout.PORTRAIT_SIZE, SheetLayout.PORTRAIT_SIZE);
    byte[] portrait = content.portrait();
    if (portrait != null) {
      canvas.image(portraitX + 1, y + 1, SheetLayout.PORTRAIT_SIZE - 2, SheetLayout.PORTRAIT_SIZE - 2, portrait);
    }
  }

  /** Vier Attributblöcke nebeneinander wie im GRW. */
  private static void attributes(@NonNull PdfCanvas canvas, @NonNull SheetContent content) throws IOException {
    List<SheetBlock> blocks = content.attributes();
    float width = (SheetLayout.CONTENT_WIDTH - (blocks.size() - 1) * SMALL_GAP) / blocks.size();
    for (int i = 0; i < blocks.size(); i++) {
      float x = MARGIN + i * (width + SMALL_GAP);
      SheetBlock block = blocks.get(i);
      canvas.section(x, SheetLayout.ATTRIBUTES_Y, width, block.title().toUpperCase(Locale.GERMAN));
      fields(canvas, x, SheetLayout.ATTRIBUTES_Y + BAR_HEIGHT, width, SheetLayout.ATTRIBUTE_LABEL_WIDTH,
          block.fields());
    }
  }

  /** Felder untereinander: schwarzer Balken mit der Bezeichnung, daneben der Wert (leer = Bleistiftfeld). */
  private static void fields(@NonNull PdfCanvas canvas, float x, float y, float width, float labelWidth,
      @NonNull List<SheetField> fields) throws IOException {
    for (int i = 0; i < fields.size(); i++) {
      float rowY = y + i * FIELD_HEIGHT;
      SheetField field = fields.get(i);
      canvas.bar(x, rowY, labelWidth, FIELD_HEIGHT, field.label().toUpperCase(Locale.GERMAN), false);
      canvas.box(x + labelWidth, rowY, width - labelWidth, FIELD_HEIGHT);
      canvas.text(x + labelWidth, rowY, width - labelWidth, FIELD_HEIGHT, field.value(), canvas.fonts().bold(),
          SheetLayout.VALUE_SIZE, true);
    }
  }

  /** Aussehen und Beschreibung, darunter die Attributproben; {@code false}, wenn die Beschreibung gekürzt wurde. */
  private static boolean appearance(@NonNull PdfCanvas canvas, float x, float y, float width,
      @NonNull SheetContent content) throws IOException {
    canvas.section(x, y, width, "AUSSEHEN/BESCHREIBUNG");
    float top = y + BAR_HEIGHT;
    List<SheetField> fields = content.appearance();
    for (int i = 0; i < fields.size(); i++) {
      float rowY = top + i * ROW_HEIGHT;
      SheetField field = fields.get(i);
      float label = SheetLayout.APPEARANCE_LABEL_WIDTH;
      canvas.text(x, rowY, label, ROW_HEIGHT, field.label(), canvas.fonts().regular(), SheetLayout.LABEL_SIZE, false);
      canvas.text(x + label, rowY, width - label, ROW_HEIGHT, field.value(), canvas.fonts().regular(),
          SheetLayout.TEXT_SIZE, false);
      canvas.gridLine(x, rowY + ROW_HEIGHT, x + width, rowY + ROW_HEIGHT);
    }
    float descriptionY = top + fields.size() * ROW_HEIGHT;
    boolean fitted = canvas.text(x, descriptionY, width, SheetLayout.DESCRIPTION_HEIGHT, content.description(),
        canvas.fonts().regular(), SheetLayout.TEXT_SIZE, false);
    float boxHeight = descriptionY + SheetLayout.DESCRIPTION_HEIGHT - y - BAR_HEIGHT;
    canvas.box(x, top, width, boxHeight);
    List<List<String>> tests = content.attributeTests();
    table(canvas, x, top + boxHeight + SMALL_GAP, width, "ATTRIBUTPROBEN", SheetLayout.TEST_COLUMNS, tests,
        tests.size());
    return fitted;
  }

  /**
   * Zustandsmonitor: körperliche und geistige Spalte mit je drei leeren Kästchen pro Reihe; das Kästchen, ab dem ein
   * weiterer Verletzungsmodifikator gilt, trägt ihn klein (mit Schmerztoleranz u.Ä. verschoben). Unter der körperlichen
   * Spalte stehen die Überzahl-Kästchen gestrichelt.
   */
  private static void monitor(@NonNull PdfCanvas canvas, float x, float y, float width, @NonNull SheetContent content)
      throws IOException {
    canvas.section(x, y, width, "ZUSTANDSMONITOR");
    float top = y + BAR_HEIGHT + SheetLayout.MONITOR_TOP_GAP;
    float half = (width - SMALL_GAP) / 2;
    float available = SheetLayout.BOTTOM - top - SheetLayout.MONITOR_TITLE_HEIGHT - SheetLayout.OVERFLOW_GAP;
    int physicalRows = rows(content.physicalMarkers().size()) + rows(content.overflowBoxes());
    float step = Math.min(SheetLayout.MONITOR_STEP, available / Math.max(1, physicalRows));
    column(canvas, x, top, half, "Körperlich (" + content.physicalMarkers().size() + ")", content.physicalMarkers(),
        content.overflowBoxes(), step);
    column(canvas, x + half + SMALL_GAP, top, half, "Geistig (" + content.stunMarkers().size() + ")",
        content.stunMarkers(), 0, step);
  }

  private static void column(@NonNull PdfCanvas canvas, float x, float y, float width, @NonNull String title,
      @NonNull List<String> markers, int overflow, float step) throws IOException {
    canvas.text(x, y, width, SheetLayout.MONITOR_TITLE_HEIGHT, title, canvas.fonts().bold(), SheetLayout.TEXT_SIZE,
        false);
    float box = Math.min(SheetLayout.MONITOR_BOX, step - 2);
    float top = y + SheetLayout.MONITOR_TITLE_HEIGHT;
    for (int i = 0; i < markers.size(); i++) {
      int row = i / 3;
      float boxY = top + row * step;
      float boxX = x + (i % 3) * step;
      canvas.box(boxX, boxY, box, box);
      canvas.text(boxX, boxY, box, box, markers.get(i), canvas.fonts().regular(), SheetLayout.LABEL_SIZE, true);
    }
    float overflowTop = top + rows(markers.size()) * step + SheetLayout.OVERFLOW_GAP;
    for (int i = 0; i < overflow; i++) {
      int row = i / 3;
      canvas.dashedBox(x + (i % 3) * step, overflowTop + row * step, box, box);
    }
    if (overflow > 0) {
      canvas.text(x + 3 * step, overflowTop, width - 3 * step, box, "Überzahl", canvas.fonts().regular(),
          SheetLayout.LABEL_SIZE, false);
    }
  }

  private static int rows(int boxes) {
    return (boxes + 2) / 3;
  }

  /** Seite 2 in zwei Spalten wie im GRW. */
  private static void secondPage(@NonNull PdfCanvas canvas, @NonNull SheetContent content) throws IOException {
    float half = SheetLayout.HALF_WIDTH;
    table(canvas, MARGIN, MARGIN, half, "CONNECTIONS", SheetLayout.CONTACT_COLUMNS,
        first(content.contacts(), SheetLayout.CONTACT_ROWS), SheetLayout.CONTACT_ROWS);
    table(canvas, MARGIN, SheetLayout.IMPLANTS_Y, half, "CYBERWARE/BIOWARE", SheetLayout.IMPLANT_COLUMNS,
        first(content.implants(), SheetLayout.IMPLANT_ROWS), SheetLayout.IMPLANT_ROWS);
    commlink(canvas, MARGIN, SheetLayout.COMMLINK_Y, half, content);
    table(canvas, MARGIN, SheetLayout.GEAR_Y, half, "AUSRÜSTUNG", SheetLayout.GEAR_COLUMNS,
        first(content.gear(), SheetLayout.GEAR_ROWS), SheetLayout.GEAR_ROWS);

    float x = SheetLayout.RIGHT_X;
    table(canvas, x, MARGIN, half, "FERNKAMPFWAFFEN", SheetLayout.RANGED_COLUMNS,
        first(content.rangedWeapons(), SheetLayout.RANGED_ROWS), SheetLayout.RANGED_ROWS);
    table(canvas, x, SheetLayout.MELEE_Y, half, "NAHKAMPF", SheetLayout.MELEE_COLUMNS,
        first(content.meleeWeapons(), SheetLayout.MELEE_ROWS), SheetLayout.MELEE_ROWS);
    table(canvas, x, SheetLayout.ARMOR_Y, half, "PANZERUNG", SheetLayout.ARMOR_COLUMNS,
        first(content.armor(), SheetLayout.ARMOR_ROWS), SheetLayout.ARMOR_ROWS);
    table(canvas, x, SheetLayout.VEHICLE_Y, half, "FAHRZEUG", SheetLayout.VEHICLE_COLUMNS,
        first(content.vehicles(), SheetLayout.VEHICLE_ROWS), SheetLayout.VEHICLE_ROWS);
    table(canvas, x, SheetLayout.MAGIC_Y, half, "ZAUBER/ADEPTENKRÄFTE/KOMPLEXE FORMEN", SheetLayout.MAGIC_COLUMNS,
        first(content.magic(), SheetLayout.MAGIC_ROWS), SheetLayout.MAGIC_ROWS);
    table(canvas, x, SheetLayout.SPIRITS_Y, half, "GEISTER/SPRITES", SheetLayout.SPIRIT_COLUMNS,
        first(content.spirits(), SheetLayout.SPIRIT_ROWS), SheetLayout.SPIRIT_ROWS);
  }


  /** Kommlink mit den vier Matrixattributen, darunter die geladenen Programme. */
  private static void commlink(@NonNull PdfCanvas canvas, float x, float y, float width,
      @NonNull SheetContent content) throws IOException {
    canvas.section(x, y, width, "KOMMLINK/PROGRAMME");
    List<SheetField> fields = content.commlink();
    float top = y + BAR_HEIGHT;
    float labelWidth = SheetLayout.COMMLINK_LABEL_WIDTH;
    SheetField device = fields.getFirst();
    canvas.bar(x, top, labelWidth, ROW_HEIGHT, device.label().toUpperCase(Locale.GERMAN), false);
    canvas.box(x + labelWidth, top, width - labelWidth, ROW_HEIGHT);
    canvas.text(x + labelWidth, top, width - labelWidth, ROW_HEIGHT, device.value(), canvas.fonts().bold(),
        SheetLayout.TEXT_SIZE, false);
    float half = width / 2;
    for (int i = 1; i < fields.size(); i++) {
      float cellX = x + ((i - 1) % 2) * half;
      float cellY = top + ROW_HEIGHT * (1 + (i - 1) / 2);
      SheetField field = fields.get(i);
      canvas.bar(cellX, cellY, labelWidth, ROW_HEIGHT, field.label().toUpperCase(Locale.GERMAN), false);
      canvas.box(cellX + labelWidth, cellY, half - labelWidth, ROW_HEIGHT);
      canvas.text(cellX + labelWidth, cellY, half - labelWidth, ROW_HEIGHT, field.value(), canvas.fonts().bold(),
          SheetLayout.TEXT_SIZE, true);
    }
    float programsY = y + SheetLayout.COMMLINK_HEIGHT - SheetTable.height(SheetLayout.PROGRAM_ROWS, ROW_HEIGHT);
    SheetTable.draw(canvas, x, programsY, width, SheetLayout.PROGRAM_COLUMNS,
        first(content.programs(), SheetLayout.PROGRAM_ROWS), SheetLayout.PROGRAM_ROWS, ROW_HEIGHT);
  }

  /** Tabelle mit Abschnittstitel; gibt das y darunter zurück. */
  private static float table(@NonNull PdfCanvas canvas, float x, float y, float width, @NonNull String title,
      @NonNull List<Column> columns, @NonNull List<List<String>> rows, int rowCount) throws IOException {
    canvas.section(x, y, width, title);
    return SheetTable.draw(canvas, x, y + BAR_HEIGHT, width, columns, rows, rowCount, ROW_HEIGHT);
  }

  private static <T> @NonNull List<T> first(@NonNull List<T> list, int count) {
    return list.subList(0, Math.min(count, list.size()));
  }

  private static <T> @NonNull List<T> rest(@NonNull List<T> list, int count) {
    return list.subList(Math.min(count, list.size()), list.size());
  }
}
