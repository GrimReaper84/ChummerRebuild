package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import org.jspecify.annotations.NonNull;

/**
 * Unveränderlicher Katalogeintrag einer Cyber- oder Bioware aus {@code cyberware.xml} bzw. {@code
 * bioware.xml} (verifizierte GRW-Kern-Teilmenge). Essenzkosten und Preis sind Formel-Strings in der
 * Standard-Kategorie, die erst in einer {@link Cyberware}-Instanz für Stufe und Grade ausgewertet
 * werden (siehe {@link RatingFormula}).
 *
 * @param kind      Cyberware oder Bioware — ergibt sich aus der Quelldatei, nicht aus einem XML-Feld
 * @param essence   Essenzkosten-Formel der Standardvariante, z.B. {@code "0.1"} oder {@code "Rating * 0.5"}
 * @param cost      Preis-Formel (Nuyen) der Standardvariante, z.B. {@code "750"} oder {@code "Rating * 5000"}
 * @param maxRating höchste wählbare Stufe; {@code 0} bei Implantaten ohne Stufe
 * @param availability Verfügbarkeit der Standardvariante (GRW-Tabellen S. 386–394); die Grade
 *                     verändert sie nicht (siehe {@link Availability})
 */
public record CyberwareData(
    @NonNull String name,
    @NonNull String category,
    @NonNull EquipmentKind kind,
    @NonNull String essence,
    @NonNull String cost,
    int maxRating,
    @NonNull Availability availability,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  /** Niedrigste Stufe eines stufenbehafteten Implantats (alle GRW-Tabellen beginnen bei Stufe 1). */
  public static final int MIN_RATING = 1;

  /** Ob das Implantat in Stufen erhältlich ist (z.B. "Dermalpanzerung (Stufe 1-3)"). */
  public boolean hasRating() {
    return maxRating > 0;
  }
}
