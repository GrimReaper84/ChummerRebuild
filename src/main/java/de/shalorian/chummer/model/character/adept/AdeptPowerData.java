package de.shalorian.chummer.model.character.adept;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag einer Adeptenkraft aus {@code adept_powers.xml} (SR4-Grundregelwerk S. 229–232). Die Kosten in
 * Kraftpunkten rechnet {@link AdeptPowers#getCost}, weil zwei Kräfte Sonderregeln haben (Verbesserte
 * Fertigkeit nach Fertigkeitskategorie, Gesteigertes körperliches Attribut über dem natürlichen Maximum).
 *
 * @param page        gedruckte GRW-Seite
 * @param cost        Kraftpunkte je Stufe bei Stufenkräften, sonst fester Preis; bei {@code costTable} ungenutzt
 * @param costTable   Kosten der Stufen 1, 2, … aus einer Tabelle (Gesteigerte Reflexe), sonst leer
 * @param leveled     ob die Kraft Stufen hat
 * @param maxLevel    feste Höchststufe (z.B. 3), {@code 0} wenn nur die Magie begrenzt
 * @param selection   was beim Kauf gewählt wird; Attribute sind auf die körperlichen beschränkt
 * @param selectLabel Bezeichnung des Auswahlfelds, leer ohne Auswahl
 * @param multiple    ob die Kraft mehrfach mit verschiedener Auswahl gekauft werden darf
 * @param note        Kurzwirkung in eigenen Worten
 */
public record AdeptPowerData(
    @NonNull String name,
    int page,
    @NonNull BigDecimal cost,
    @NonNull List<BigDecimal> costTable,
    boolean leveled,
    int maxLevel,
    @NonNull QualitySelection selection,
    @NonNull String selectLabel,
    boolean multiple,
    @NonNull AdeptPowerEffect effect,
    @NonNull String note
) implements CatalogEntry {

  public AdeptPowerData {
    costTable = List.copyOf(costTable);
  }
}
