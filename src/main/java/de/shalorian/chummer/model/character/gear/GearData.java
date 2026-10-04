package de.shalorian.chummer.model.character.gear;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.math.RoundingMode;
import org.jspecify.annotations.NonNull;

/**
 * Unveränderlicher Katalogeintrag eines Ausrüstungsgegenstands aus {@code gear.xml} (GRW-Tabellen
 * S. 368, 374–385 und 395, gegen das PDF-Bild übertragen). Preis und Verfügbarkeit stehen als Formel
 * wie im GRW ("Stufe x 200 ¥" → {@code "Rating * 200"}, ausgewertet über {@link RatingFormula}).
 *
 * <p><b>Packungsgröße:</b> Manche Preise gelten für mehrere Einheiten, z.B. "RFID-Marker (pro 20
 * Stück)" (S. 374), "Plastik (je 10 Stück)" (S. 382), "50 ¥ pro 100 m" (S. 383); Drogen, Chemikalien
 * und Toxine kosten "pro Dosis" (S. 382). {@link #cost()} ist der Preis einer Packung von {@link
 * #packSize()} Einheiten.
 *
 * <p><b>Preisspanne:</b> Bei Einträgen wie "5–200+ ¥" (Sim-Aufzeichnung, S. 377) oder "100+ ¥"
 * (Sim-Modul, S. 374) gibt der Spieler den Preis selbst ein; {@link #cost()} ist dann der
 * GRW-Mindestpreis, nach oben ist der Preis offen (Nutzerentscheidung 2026-10-04).
 *
 * @param minRating    niedrigste Stufe (bzw. Kraft); {@code 0} bei Gegenständen ohne Stufe
 * @param maxRating    höchste Stufe laut Tabelle ("Stufe 1–6"); {@code 0} ohne Stufe
 * @param deviceRating feste Gerätestufe laut Elektroniktabellen (S. 374), {@code 0} ohne Angabe
 * @param cost         Preisformel je Packung, bei Preisspanne der Mindestpreis
 * @param packSize     Anzahl Einheiten, für die {@link #cost()} gilt (mindestens 1)
 * @param variableCost ob der Preis eine Spanne ist, die der Spieler festlegt
 * @param extraKind    ob und wie ein Zusatztext beim Kauf anzugeben ist
 * @param extraLabel   Feldbezeichnung des Zusatztexts (z.B. "Fertigkeit"), leer ohne Zusatztext
 * @param note         Hinweis aus Tabelle oder Text (z.B. Waffenwerte, Monatsbeitrag), sonst leer
 */
public record GearData(
    @NonNull String name,
    @NonNull GearCategory category,
    int minRating,
    int maxRating,
    int deviceRating,
    @NonNull Availability availability,
    @NonNull String cost,
    int packSize,
    @NonNull GearUnit unit,
    boolean variableCost,
    @NonNull ExtraKind extraKind,
    @NonNull String extraLabel,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  public GearData {
    if (packSize < 1) {
      throw new IllegalArgumentException(name + ": Packungsgröße muss mindestens 1 sein");
    }
    if (minRating > maxRating || (maxRating > 0 && minRating < 1)) {
      throw new IllegalArgumentException(name + ": ungültiger Stufenbereich");
    }
  }

  /** Ob der Gegenstand in Stufen (bzw. Kraftstufen) erhältlich ist. */
  public boolean hasRating() {
    return maxRating > 0;
  }

  /** Niedrigste wählbare Stufe, bzw. {@code 0} ohne Stufe. */
  public int lowestRating() {
    return hasRating() ? minRating : 0;
  }

  /** Ob {@code rating} zulässig ist (Stufe in [min, max] bzw. {@code 0} ohne Stufe). */
  public boolean isValidRating(int rating) {
    return hasRating() ? rating >= minRating && rating <= maxRating : rating == 0;
  }

  /** Ob beim Kauf ein Zusatztext anzugeben ist. */
  public boolean needsExtra() {
    return extraKind != ExtraKind.NONE;
  }

  /** Ob {@code quantity} eine zulässige Menge ist: mindestens eine Packung, ganze Packungen. */
  public boolean isValidQuantity(int quantity) {
    return quantity >= packSize && quantity % packSize == 0;
  }

  /** Preis einer Packung in der angegebenen Stufe (bei Preisspanne der Mindestpreis). */
  public int packCostFor(int rating) {
    return RatingFormula.evaluate(cost, rating).setScale(0, RoundingMode.HALF_UP).intValueExact();
  }
}
