package de.shalorian.chummer.export.sheet;

import java.util.List;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.jspecify.annotations.NonNull;

/**
 * Lagen und Größen des Bogens in Punkt (1/72 Zoll) auf A4, gemessen von oben links. Die Anordnung folgt dem
 * GRW-Bogen (S. 406/407); die Zeilenzahlen der Tabellen sind so gewählt, dass beide Seiten voll werden. Die Tabellen am
 * unteren Seitenrand nehmen den restlichen Platz.
 */
final class SheetLayout {

  static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
  static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
  static final float MARGIN = 28f;
  static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
  static final float BOTTOM = PAGE_HEIGHT - MARGIN;
  static final float GAP = 8f;
  static final float SMALL_GAP = 6f;
  static final float BAR_HEIGHT = 12f;
  static final float HEADER_ROW_HEIGHT = 12f;
  static final float ROW_HEIGHT = 13f;
  static final float FIELD_HEIGHT = 15f;
  static final float MIN_FONT_SIZE = 5f;
  static final float TEXT_SIZE = 7.5f;
  static final float LABEL_SIZE = 6f;
  static final float VALUE_SIZE = 9f;
  static final float NAME_SIZE = 13f;
  static final float BAR_TEXT_SIZE = 7.5f;
  static final float FOOTER_Y = PAGE_HEIGHT - 20f;

  // Seite 1: Kopf mit Portrait
  static final float HEADER_Y = MARGIN;
  static final float PORTRAIT_SIZE = 92f;
  static final float INFO_WIDTH = CONTENT_WIDTH - PORTRAIT_SIZE - GAP;
  static final float NAME_HEIGHT = 20f;
  static final float NAME_LABEL_WIDTH = 70f;
  static final float FIELDS_Y = HEADER_Y + NAME_HEIGHT + 4f;
  static final float FIELD_LABEL_WIDTH = 62f;

  // Seite 1: Attribute
  static final float ATTRIBUTES_Y = HEADER_Y + PORTRAIT_SIZE + GAP;
  static final float ATTRIBUTE_LABEL_WIDTH = 78f;

  // Seite 1: Fertigkeiten und der untere Bereich
  static final float THIRD_WIDTH = (CONTENT_WIDTH - 2 * SMALL_GAP) / 3;
  static final float LOWER_HEIGHT = 220f;
  static final float LOWER_Y = BOTTOM - LOWER_HEIGHT;
  static final float SKILLS_Y = ATTRIBUTES_Y + BAR_HEIGHT + 4 * FIELD_HEIGHT + GAP;
  static final int SKILL_ROWS = rowsBetween(SKILLS_Y, LOWER_Y - GAP);
  static final int QUALITY_ROWS = rowsBetween(LOWER_Y, BOTTOM);
  static final float DESCRIPTION_HEIGHT = 50f;
  static final float APPEARANCE_LABEL_WIDTH = 50f;
  static final float MONITOR_BOX = 13f;
  static final float MONITOR_STEP = 15f;
  /** Abstand unter dem Abschnittstitel des Zustandsmonitors. */
  static final float MONITOR_TOP_GAP = 2f;
  /** Höhe der Spaltenüberschrift „Körperlich (n)“ bzw. „Geistig (n)“ samt Abstand zu den Kästchen. */
  static final float MONITOR_TITLE_HEIGHT = 14f;
  /** Abstand zwischen den Kästchen der Spalte und den Überzahl-Kästchen. */
  static final float OVERFLOW_GAP = 4f;

  // Seite 2: zwei Spalten; jede Tabelle hat ihre feste Lage, die Lagen bauen aufeinander auf
  static final float HALF_WIDTH = (CONTENT_WIDTH - 2 * SMALL_GAP) / 2;
  static final float RIGHT_X = MARGIN + HALF_WIDTH + 2 * SMALL_GAP;
  static final int CONTACT_ROWS = 8;
  static final int IMPLANT_ROWS = 12;
  static final int PROGRAM_ROWS = 6;
  static final float COMMLINK_LABEL_WIDTH = 60f;
  /** Kommlink-Block: Titel, Gerätezeile, zwei Zeilen Matrixattribute und die Programmtabelle. */
  static final float COMMLINK_HEIGHT = BAR_HEIGHT + 3 * ROW_HEIGHT + SheetTable.height(PROGRAM_ROWS, ROW_HEIGHT);
  static final float IMPLANTS_Y = MARGIN + tableHeight(CONTACT_ROWS) + GAP;
  static final float COMMLINK_Y = IMPLANTS_Y + tableHeight(IMPLANT_ROWS) + GAP;
  static final float GEAR_Y = COMMLINK_Y + COMMLINK_HEIGHT + GAP;
  static final int GEAR_ROWS = rowsBetween(GEAR_Y, BOTTOM);
  static final int RANGED_ROWS = 6;
  static final int MELEE_ROWS = 4;
  static final int ARMOR_ROWS = 3;
  static final int VEHICLE_ROWS = 2;
  static final int MAGIC_ROWS = 20;
  static final float MELEE_Y = MARGIN + tableHeight(RANGED_ROWS) + GAP;
  static final float ARMOR_Y = MELEE_Y + tableHeight(MELEE_ROWS) + GAP;
  static final float VEHICLE_Y = ARMOR_Y + tableHeight(ARMOR_ROWS) + GAP;
  static final float MAGIC_Y = VEHICLE_Y + tableHeight(VEHICLE_ROWS) + GAP;
  static final float SPIRITS_Y = MAGIC_Y + tableHeight(MAGIC_ROWS) + GAP;
  static final int SPIRIT_ROWS = rowsBetween(SPIRITS_Y, BOTTOM);

  static final @NonNull List<Column> SKILL_COLUMNS = List.of(Column.text("BEZEICHNUNG", 100),
      Column.centered("WERT", 22), Column.centered("ATTR.", 24), Column.centered("POOL", 30));
  static final @NonNull List<Column> QUALITY_COLUMNS = List.of(Column.text("NAME", 130), Column.centered("ART", 46));
  static final @NonNull List<Column> TEST_COLUMNS = List.of(Column.text("PROBE", 95), Column.centered("ATTR.", 50),
      Column.centered("POOL", 30));
  static final @NonNull List<Column> CONTACT_COLUMNS = List.of(Column.text("NAME", 170),
      Column.centered("LOYALITÄT", 45), Column.centered("CONNECTION", 50));
  static final @NonNull List<Column> IMPLANT_COLUMNS = List.of(Column.text("IMPLANTAT", 120),
      Column.centered("STUFE", 28), Column.centered("ESSENZ", 34), Column.text("ANMERKUNGEN", 82));
  /** Programme und Ausrüstung mit dem Anzeigenamen der App, der Stufe und Anzahl schon enthält. */
  static final @NonNull List<Column> PROGRAM_COLUMNS = List.of(Column.text("PROGRAMM", 220),
      Column.centered("LÄUFT", 44));
  static final @NonNull List<Column> GEAR_COLUMNS = List.of(Column.text("AUSRÜSTUNG", 264));
  static final @NonNull List<Column> RANGED_COLUMNS = List.of(Column.text("WAFFE", 78), Column.centered("SCHADEN", 32),
      Column.centered("PB", 18), Column.centered("MODUS", 36), Column.centered("RK", 20), Column.centered("MUNI", 46),
      Column.centered("POOL", 28));
  static final @NonNull List<Column> MELEE_COLUMNS = List.of(Column.text("NAHKAMPFWAFFE", 120),
      Column.centered("REICHWEITE", 44), Column.centered("SCHADEN", 40), Column.centered("PB", 26),
      Column.centered("POOL", 34));
  static final @NonNull List<Column> ARMOR_COLUMNS = List.of(Column.text("PANZERUNG", 110),
      Column.centered("BALL./STOSS", 50), Column.text("ANMERKUNGEN", 104));
  static final @NonNull List<Column> VEHICLE_COLUMNS = List.of(Column.text("FAHRZEUG", 100),
      Column.centered("HANDL.", 24), Column.centered("BESCHL.", 30), Column.centered("GESCHW.", 28),
      Column.centered("PILOT", 20), Column.centered("RUMPF", 22), Column.centered("PANZ.", 22),
      Column.centered("SENSOR", 24));
  static final @NonNull List<Column> MAGIC_COLUMNS = List.of(Column.text("ZAUBER/KRAFT/FORM", 86),
      Column.text("ART", 64), Column.centered("REICHW.", 38), Column.centered("DAUER", 30),
      Column.centered("ENTZUG/WERT", 46));
  static final @NonNull List<Column> SPIRIT_COLUMNS = List.of(Column.text("GEIST/SPRITE", 110),
      Column.centered("GEBUNDEN/REG.", 70), Column.centered("KRAFT", 40), Column.centered("DIENSTE", 44));
  static final @NonNull List<Column> SITUATION_COLUMNS = List.of(Column.text("ANLASS", 150),
      Column.centered("WÜRFEL", 40), Column.text("ZUSAMMENSETZUNG", 330));
  static final @NonNull List<Column> FOCUS_COLUMNS = List.of(Column.text("FOKUS", 360),
      Column.centered("ZUSTAND", 160));
  static final @NonNull List<Column> INITIATION_COLUMNS = List.of(Column.text("GRAD / METAMAGIE BZW. ECHO", 420),
      Column.centered("WERT", 100));

  private SheetLayout() {
  }

  /** Höhe einer Tabelle mit Abschnittstitel. */
  static float tableHeight(int rows) {
    return BAR_HEIGHT + SheetTable.height(rows, ROW_HEIGHT);
  }

  /** Zeilen einer Tabelle mit Abschnittstitel, die zwischen {@code top} und {@code bottom} passen. */
  private static int rowsBetween(float top, float bottom) {
    return (int) ((bottom - top - BAR_HEIGHT - HEADER_ROW_HEIGHT) / ROW_HEIGHT);
  }
}
