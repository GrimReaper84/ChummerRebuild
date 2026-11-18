package de.shalorian.chummer.model.character.armor;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import de.shalorian.chummer.utils.Numbers;
import org.jspecify.annotations.NonNull;

/**
 * Unveränderlicher Katalogeintrag eines Rüstungsteils aus {@code armor.xml} (GRW-Tabellen Kleidung und
 * Panzerung, S. 373, gegen das PDF-Bild verifiziert). Bei {@link ArmorCategory#isBonus()
 * Helmen und Schilden} sind Ballistik und Stoß Boni ("+1/+2"), sonst absolute Werte.
 *
 * @param ballistic Ballistik-Wert (B)
 * @param impact    Stoß-Wert (S)
 * @param cost      fester Preis in Nuyen; bei Kleidung der Aufschlag auf den frei gewählten Kleidungspreis
 * @param priceMin  Untergrenze des vom Spieler gewählten Preisanteils (Kleidung 20 ¥), {@code 0} ohne
 * @param priceMax  Obergrenze dieses Preisanteils (Kleidung 100.000 ¥), {@code 0} ohne
 * @param shield     ob das Teil ein Schild ist: −1 auf körperliche Proben, nur bestimmte Modifikationen (S. 372)
 * @param acceptsMods ob das Teil Panzerungsmodifikationen aufnimmt (Panzerung, Lederjacke, Schilde)
 * @param weapon     Name der Waffe aus {@code weapons.xml}, die das angelegte Teil stellt (Schutz-, Taserschild), sonst leer
 */
public record ArmorData(
    @NonNull String name,
    @NonNull ArmorCategory category,
    int ballistic,
    int impact,
    @NonNull Availability availability,
    int cost,
    int priceMin,
    int priceMax,
    @NonNull String source,
    @NonNull String page,
    boolean shield,
    boolean acceptsMods,
    @NonNull String weapon
) implements CatalogEntry, Sourced {

  /** Rüstung mit festem Preis (alles außer Kleidung). */
  public ArmorData(@NonNull String name, @NonNull ArmorCategory category, int ballistic, int impact,
      @NonNull Availability availability, int cost, @NonNull String source, @NonNull String page) {
    this(name, category, ballistic, impact, availability, cost, 0, 0, source, page, false, false, "");
  }

  /**
   * Ob der Spieler beim Kauf einen Preis innerhalb der Spanne wählt (Kleidung, GRW S. 373: 20–100.000 ¥; Feedback-
   * Kleidung und Lederjacke kosten diesen Kleidungspreis plus ihren Aufschlag).
   */
  public boolean hasVariablePrice() {
    return priceMax > 0;
  }

  /** Ob {@code price} als gewählter Preisanteil zulässig ist. */
  public boolean isValidPrice(int price) {
    return hasVariablePrice() && price >= priceMin && price <= priceMax;
  }

  /** Preis wie im GRW, z.B. {@code "900 ¥"}, {@code "20–100.000 ¥"} oder {@code "20–100.000 ¥ + 500 ¥"}. */
  public @NonNull String priceText() {
    if (!hasVariablePrice()) {
      return Numbers.nuyen(cost);
    }
    String range = Numbers.integer(priceMin) + "–" + Numbers.nuyen(priceMax);
    return cost == 0 ? range : range + " + " + Numbers.nuyen(cost);
  }

  /** Rüstung hat keine Stufe; Verfügbarkeitsformeln werden deshalb mit Stufe 0 ausgewertet. */
  private static final int NO_RATING = 0;

  /** Verfügbarkeit wie im GRW, z.B. {@code "14E"}. */
  public @NonNull String availabilityText() {
    return availability.displayText(NO_RATING);
  }

  /** Ob die Verfügbarkeit die Erschaffungsgrenze 12 einhält (GRW S. 111). */
  public boolean isAllowedAtCreation() {
    return availability.isAllowedAtCreation(NO_RATING);
  }

  /** Anzeige des Schutzwerts wie im GRW, z.B. {@code "8/6"} oder {@code "+1/+2"} bei Helmen/Schilden. */
  public @NonNull String protectionText() {
    String prefix = category.isBonus() ? "+" : "";
    return prefix + ballistic + "/" + prefix + impact;
  }
}
