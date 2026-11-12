package de.shalorian.chummer.model.character.armor;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import de.shalorian.chummer.model.character.equipment.Availability;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag eines Rüstungs-Einbaus aus {@code armor_mods.xml}: Zubehör der Ganzkörperpanzerung bzw. des Urban
 * Explorer Overalls oder Panzerungsmodifikation (GRW-Tabellen S. 373, Beschreibungen S. 372).
 *
 * @param forArmor              bei Zubehör der Name des Teils, an das es gehört; bei Modifikationen leer
 * @param maxRating             höchste Stufe, {@code 0} ohne Stufe
 * @param ballistic             Bonus auf die Ballistik des Teils (Zubehör-Helm)
 * @param impact                Bonus auf den Stoßwert des Teils
 * @param protection            zusätzlicher Schutz in Höhe der Stufe
 * @param availabilitySurcharge ob die Verfügbarkeit ein Zuschlag auf die des Teils ist ("+6")
 * @param cost                  Preisformel ({@link RatingFormula}), z.B. {@code "Rating * 250"}
 * @param requires              Name eines Einbaus, der am selben Teil vorhanden sein muss, sonst leer
 * @param shieldAllowed         ob die Modifikation auch an einen Schild darf (S. 372)
 * @param weapon                Name der Waffe aus {@code weapons.xml}, die der Einbau stellt (Schockstreifen), sonst leer
 */
public record ArmorModData(
    @NonNull String name,
    @NonNull ArmorModKind kind,
    @NonNull String forArmor,
    int maxRating,
    int ballistic,
    int impact,
    @NonNull ArmorProtection protection,
    @NonNull Availability availability,
    boolean availabilitySurcharge,
    @NonNull String cost,
    @NonNull String requires,
    boolean shieldAllowed,
    @NonNull String weapon,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  /** Niedrigste Stufe einer Modifikation mit Stufe. */
  public static final int MIN_RATING = 1;

  public boolean hasRating() {
    return maxRating > 0;
  }

  /** Preis bei dieser Stufe ({@code 0} ohne Stufe). */
  public int costFor(int rating) {
    return RatingFormula.evaluate(cost, rating).intValueExact();
  }

  /** Ob die Stufe zulässig ist: 1 bis Höchststufe, bzw. 0 ohne Stufe. */
  public boolean isValidRating(int rating) {
    return hasRating() ? rating >= MIN_RATING && rating <= maxRating : rating == 0;
  }

  /** Kurzwirkung bei dieser Stufe, z.B. „Schutz +2/+2“, „gegen Elektrizität +3“, „Infiltration gegen IR +2 Würfel“. */
  public @NonNull String effectText(int rating) {
    if (ballistic != 0 || impact != 0) {
      return "Schutz +%d/+%d".formatted(ballistic, impact);
    }
    if (protection == ArmorProtection.NONE) {
      return weapon.isEmpty() ? "–" : "Waffe: " + weapon;
    }
    return protection.getLabel() + " +" + rating + (protection.addsToArmor() ? "" : " Würfel");
  }
}
