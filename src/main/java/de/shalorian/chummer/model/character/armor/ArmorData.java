package de.shalorian.chummer.model.character.armor;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import org.jspecify.annotations.NonNull;

/**
 * Unveränderlicher Katalogeintrag eines Rüstungsteils aus {@code armor.xml} (GRW-Tabelle
 * <i>Panzerung</i>, S. 373, gegen das PDF-Bild verifiziert). Bei {@link ArmorCategory#isBonus()
 * Helmen und Schilden} sind Ballistik und Stoß Boni ("+1/+2"), sonst absolute Werte.
 *
 * @param ballistic Ballistik-Wert (B)
 * @param impact    Stoß-Wert (S)
 * @param cost      Preis in Nuyen
 */
public record ArmorData(
    @NonNull String name,
    @NonNull ArmorCategory category,
    int ballistic,
    int impact,
    @NonNull Availability availability,
    int cost,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

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
