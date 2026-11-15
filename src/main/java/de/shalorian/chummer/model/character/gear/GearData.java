package de.shalorian.chummer.model.character.gear;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.magic.FocusType;
import java.math.RoundingMode;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * <p><b>Preisbänder:</b> Die Matrixprogramme haben zwei Preisspalten, "Preis (bis Stufe 3)" und
 * "Preis (bis Stufe 6)" (S. 376, z.B. Hacking "Stufe x 500 ¥" bzw. "Stufe x 1.000 ¥"). {@link
 * #cost()} gilt dann bis Stufe 3, {@link #highCost()} ab Stufe {@value #HIGH_COST_FROM_RATING}.
 *
 * <p><b>Einbauten:</b> Geräte mit {@link #accepts()} nehmen Gegenstände dieser Kategorien auf
 * (Kommlink → Betriebssystem, Module, Pakete, Programme, Sim-Modul; Brille → Sichtverstärkung, Sensorpaket → Sensor-Funktion, Magschloss →
 * Zusätze). Hat das Gerät eine {@link #capacity()}, darf die Summe der {@link #capacityCost()} seiner
 * Einbauten sie nicht übersteigen. SR4-Grundregelwerk S. 379: Sichtverstärkungen belegen je 1 Punkt
 * (Ultraschall 2), Audioverstärkung "1 Punkt Kapazität (pro Stufe)"; S. 380: Die Sensoren eines Pakets
 * dürfen zusammen nicht mehr Kapazität belegen, als das Paket hat. Steht die Verfügbarkeit als Zuschlag in der Tabelle ("+4", "+4E"), erhöht sie die
 * Verfügbarkeit des Geräts ({@link #availabilitySurcharge()}); der Preis eines Einbaus zählt immer
 * zum Gerät.
 *
 * <p><b>Preisspanne:</b> Bei Einträgen wie "5–200+ ¥" (Sim-Aufzeichnung, S. 377) oder "100+ ¥"
 * (Sim-Modul, S. 374) gibt der Spieler den Preis selbst ein; {@link #cost()} ist dann der
 * GRW-Mindestpreis, nach oben ist der Preis offen (Nutzerentscheidung 2026-10-04).
 *
 * @param minRating    niedrigste Stufe (bzw. Kraft); {@code 0} bei Gegenständen ohne Stufe
 * @param maxRating    höchste Stufe laut Tabelle ("Stufe 1–6"); {@code 0} ohne Stufe
 * @param deviceRating feste Gerätestufe laut Elektroniktabellen (S. 374), {@code 0} ohne Angabe
 * @param processor    Prozessor eines Kommlinks (S. 374), sonst {@code 0}
 * @param signal       Signal eines Kommlinks (S. 374), sonst {@code 0}
 * @param firewall     Firewall eines Betriebssystems (S. 374), sonst {@code 0}
 * @param system       System eines Betriebssystems (S. 374), sonst {@code 0}
 * @param cost         Preisformel je Packung, bei Preisspanne der Mindestpreis; bei zwei Preisbändern
 *                     der Preis bis Stufe 3
 * @param highCost     Preisformel ab Stufe {@value #HIGH_COST_FROM_RATING} (Matrixprogramme), sonst
 *                     leer
 * @param availabilitySurcharge ob die Verfügbarkeit ein Zuschlag auf das Gerät ist ("+4")
 * @param accepts      Kategorien, deren Gegenstände das Gerät als Einbau aufnimmt (Kommlink: Betriebssystem,
 *                     Hardware-Module, Softwarepakete, Programme, Sim-Modul), sonst leer
 * @param capacity     Kapazitätsformel des Geräts ("Rating" bzw. fester Wert), leer ohne Kapazität
 * @param capacityCost Kapazitätsbedarf als Einbau ("1", "2", "Rating"), leer ohne Bedarf
 * @param repeatable   ob derselbe Einbau mehrfach in einem Gerät stecken darf ("pro Stück")
 * @param combat       Kampfwerte von Munition, Granaten und Raketen (nur Anzeige), sonst {@code null}
 * @param packSize     Anzahl Einheiten, für die {@link #cost()} gilt (mindestens 1)
 * @param variableCost ob der Preis eine Spanne ist, die der Spieler festlegt
 * @param extraKind    ob und wie ein Zusatztext beim Kauf anzugeben ist
 * @param matrixFlags  Merkmale für den Matrixzugang (Neuralinterface, Sim-Modul, Kommlink-Einbau), sonst leer
 * @param matrixAttribute Attribut, das ein Hardware-Modul oder Softwarepaket aufrüstet (S. 260), sonst {@code null}
 * @param focusType    Fokusart (Bindung und Wirkung, S. 233/234), sonst {@code null}
 * @param extraLabel   Feldbezeichnung des Zusatztexts (z.B. "Fertigkeit"), leer ohne Zusatztext
 * @param note         Hinweis aus Tabelle oder Text (z.B. Waffenwerte, Monatsbeitrag), sonst leer
 */
public record GearData(
    @NonNull String name,
    @NonNull GearCategory category,
    int minRating,
    int maxRating,
    int deviceRating,
    int processor,
    int signal,
    int firewall,
    int system,
    @NonNull Availability availability,
    @NonNull String cost,
    @NonNull String highCost,
    boolean availabilitySurcharge,
    @NonNull Set<GearCategory> accepts,
    @NonNull String capacity,
    @NonNull String capacityCost,
    boolean repeatable,
    @Nullable GearCombatStats combat,
    int packSize,
    @NonNull GearUnit unit,
    boolean variableCost,
    @NonNull ExtraKind extraKind,
    @NonNull Set<MatrixFlag> matrixFlags,
    @Nullable MatrixAttribute matrixAttribute,
    @Nullable FocusType focusType,
    @NonNull String extraLabel,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  /** Erste Stufe der Preisspalte "Preis (bis Stufe 6)" der Matrixprogramme (GRW S. 376). */
  public static final int HIGH_COST_FROM_RATING = 4;

  public GearData {
    accepts = Set.copyOf(accepts);
    matrixFlags = Set.copyOf(matrixFlags);
    if (packSize < 1) {
      throw new IllegalArgumentException(name + ": Packungsgröße muss mindestens 1 sein");
    }
    if (minRating > maxRating || (maxRating > 0 && minRating < 1)) {
      throw new IllegalArgumentException(name + ": ungültiger Stufenbereich");
    }
  }

  /**
   * Sim-Modul des Gegenstands (GRW S. 374): 0 ohne, 1 kaltes Sim (Sim-Modul, auch im Simrig enthalten), 2 heißes Sim
   * (modifiziertes Sim-Modul).
   */
  public int simLevel() {
    if (matrixFlags.contains(MatrixFlag.SIM_HOT)) {
      return 2;
    }
    return matrixFlags.contains(MatrixFlag.SIM_COLD) ? 1 : 0;
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

  /**
   * Ob der Gegenstand nur einzeln gekauft wird: Geräte, die Einbauten aufnehmen (jedes hat seine
   * eigenen), und Einbauten selbst, weil sie in genau einem Gerät stecken.
   */
  public boolean isSingleItem() {
    return !accepts.isEmpty() || category.isInstalledOnly();
  }

  /** Verfügbarkeit wie im GRW angezeigt, bei einem Zuschlag mit "+" (z.B. "+4E", "8", "–"). */
  public @NonNull String availabilityText(int rating) {
    return (availabilitySurcharge ? "+" : "") + availability.displayText(rating);
  }

  /** Ob das Gerät eine Kapazität für Einbauten hat. */
  public boolean hasCapacity() {
    return !capacity.isEmpty();
  }

  /** Kapazität des Geräts in der angegebenen Stufe, {@code 0} ohne Kapazität. */
  public int capacityFor(int rating) {
    return hasCapacity() ? RatingFormula.evaluate(capacity, rating).intValueExact() : 0;
  }

  /** Kapazitätsbedarf als Einbau in der angegebenen Stufe, {@code 0} ohne Bedarf. */
  public int capacityCostFor(int rating) {
    return capacityCost.isEmpty() ? 0 : RatingFormula.evaluate(capacityCost, rating).intValueExact();
  }

  /**
   * Ob {@code quantity} eine zulässige Menge ist: mindestens eine Packung, ganze Packungen; bei
   * {@link #isSingleItem()} genau 1.
   */
  public boolean isValidQuantity(int quantity) {
    if (isSingleItem()) {
      return quantity == 1;
    }
    return quantity >= packSize && quantity % packSize == 0;
  }

  /**
   * Preis einer Packung in der angegebenen Stufe (bei Preisspanne der Mindestpreis, bei zwei
   * Preisbändern ab Stufe {@value #HIGH_COST_FROM_RATING} laut {@link #highCost()}).
   */
  public int packCostFor(int rating) {
    String formula = !highCost.isEmpty() && rating >= HIGH_COST_FROM_RATING ? highCost : cost;
    return RatingFormula.evaluate(formula, rating).setScale(0, RoundingMode.HALF_UP).intValueExact();
  }
}
