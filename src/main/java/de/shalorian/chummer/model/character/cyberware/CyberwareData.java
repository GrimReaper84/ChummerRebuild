package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.gear.GearCategory;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Unveränderlicher Katalogeintrag einer Cyber- oder Bioware aus {@code cyberware.xml} bzw. {@code bioware.xml}
 * (GRW-Tabellen S. 386–389 und 394). Essenzkosten, Preis, Verfügbarkeit und Kapazität sind Formel-Strings der
 * Standardvariante, die erst in einer {@link Cyberware}-Instanz für Stufe und Grade ausgewertet werden (siehe
 * {@link RatingFormula}).
 *
 * @param kind         Cyberware oder Bioware — ergibt sich aus der Quelldatei, nicht aus einem XML-Feld
 * @param essence      Essenzkosten-Formel der Standardvariante, z.B. {@code "0.1"} oder {@code "Rating * 0.5"}
 * @param cost         Preis-Formel (Nuyen) der Standardvariante; bei variablem Preis der feste Aufschlag (meist 0)
 * @param maxRating    höchste wählbare Stufe; {@code 0} bei Implantaten ohne Stufe
 * @param availability Verfügbarkeit der Standardvariante; die Grade verändert sie nicht (siehe {@link Availability})
 * @param priceMin     Untergrenze eines beim Kauf gewählten Preises (Kosmetische Modifikation), {@code 0} ohne
 * @param priceMax     Obergrenze dieses Preises, {@code 0} ohne
 * @param capacity     Kapazitätsformel eines Basissystems (Cyberaugen/-ohren: {@code "Rating * 4"}), sonst leer
 * @param capacityCost Kapazitätsbedarf beim Einbau in ein Basissystem ({@code "2"}, {@code "Rating"}); {@value
 *                     #INCLUDED_IN_BASE} = im Basissystem schon enthalten; leer = nicht einbaubar
 * @param effects      dauerhafte, rechnende Wirkungen
 * @param selection    was beim Kauf gewählt wird
 * @param restrictions Mehrfachkauf, Unverträglichkeiten und Voraussetzungen
 * @param acceptedGear Ausrüstungskategorie, deren Gerät eingebaut werden kann (Kommlink-Headware, S. 386), sonst
 *                     {@code null}
 * @param note         Kurzhinweis in eigenen Worten (situative Wirkung, Zusatzkosten), sonst leer
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
    @NonNull String page,
    int priceMin,
    int priceMax,
    @NonNull String capacity,
    @NonNull String capacityCost,
    @NonNull List<ImplantEffect> effects,
    @NonNull ImplantSelection selection,
    @NonNull ImplantRestrictions restrictions,
    @Nullable GearCategory acceptedGear,
    @NonNull String note
) implements CatalogEntry {

  /** Niedrigste Stufe eines stufenbehafteten Implantats (alle GRW-Tabellen beginnen bei Stufe 1). */
  public static final int MIN_RATING = 1;

  /** Kapazitätsbedarf eines Teils, das im Basissystem schon enthalten ist (GRW-Tabelle: Sternchen). */
  public static final String INCLUDED_IN_BASE = "*";

  public CyberwareData {
    effects = List.copyOf(effects);
  }

  /** Implantat ohne Wirkung, Kapazität und Beschränkung (Erstschnitt-Schema, Testdaten). */
  public CyberwareData(@NonNull String name, @NonNull String category, @NonNull EquipmentKind kind,
      @NonNull String essence, @NonNull String cost, int maxRating, @NonNull Availability availability,
      @NonNull String source, @NonNull String page) {
    this(name, category, kind, essence, cost, maxRating, availability, source, page, 0, 0, "", "", List.of(),
        ImplantSelection.NONE, ImplantRestrictions.NONE, null, "");
  }

  /** Ob das Implantat in Stufen erhältlich ist (z.B. "Dermalpanzerung (Stufe 1-3)"). */
  public boolean hasRating() {
    return maxRating > 0;
  }

  /** Ob der Spieler beim Kauf einen Preis innerhalb der Spanne wählt (Kosmetische Modifikation, S. 389). */
  public boolean hasVariablePrice() {
    return priceMax > 0;
  }

  /** Ob {@code price} als gewählter Preis zulässig ist. */
  public boolean isValidPrice(int price) {
    return hasVariablePrice() && price >= priceMin && price <= priceMax;
  }

  /** Ob das Implantat ein Basissystem mit Kapazität für Zubehör ist (Cyberaugen, Cyberohren). */
  public boolean hasCapacity() {
    return !capacity.isEmpty();
  }

  /** Ob das Implantat gegen Kapazität in ein Basissystem eingebaut werden kann (nicht bei {@value #INCLUDED_IN_BASE}). */
  public boolean isInstallable() {
    return !capacityCost.isEmpty() && !INCLUDED_IN_BASE.equals(capacityCost);
  }

  /** Kapazität des Basissystems bei dieser Stufe, {@code 0} ohne Kapazität. */
  public int capacityFor(int rating) {
    return hasCapacity() ? RatingFormula.evaluate(capacity, rating).intValueExact() : 0;
  }

  /** Kapazitätsbedarf beim Einbau in dieser Stufe; nur sinnvoll bei {@link #isInstallable()}. */
  public int capacityCostFor(int rating) {
    return RatingFormula.evaluate(capacityCost, rating).intValueExact();
  }

  /** Summe der Werte aller Wirkungen dieses Typs (und Ziels, falls angegeben) bei dieser Stufe. */
  public int effectValue(@NonNull ImplantEffectType type, @Nullable String target, int rating) {
    return effects.stream()
        .filter(e -> e.type() == type && (target == null || e.target().equals(target)))
        .mapToInt(e -> e.value(rating))
        .sum();
  }
}
