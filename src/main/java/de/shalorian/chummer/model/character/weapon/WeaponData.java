package de.shalorian.chummer.model.character.weapon;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.cyberware.RatingFormula;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.math.RoundingMode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Unveränderlicher Katalogeintrag einer Waffe aus {@code weapons.xml} (GRW-Kern-Teilmenge, Tabellen
 * S. 361, 363, 366, gegen das PDF-Bild verifiziert). Die Kampfwerte stehen bewusst als GRW-Text in der
 * deutschen Schreibweise ("(STR÷2+3)K", "HM/SM/AM", "15 (s)", "2 (3)"); den Schaden wertet {@link
 * WeaponDamage} aus, die übrigen Werte bleiben Anzeigetext (Rückstoß folgt in Teil C, siehe {@code
 * docs/plaene/waffen_plan.md}). Leere Texte bedeuten, dass
 * die Tabelle der Kategorie diese Spalte nicht führt (z.B. Modus bei Nahkampfwaffen).
 *
 * <p>Einzige Waffe mit Stufe ist der Bogen: GRW S. 361 führt ihn mit der Mindeststärke als Stufe
 * ("STR Min"), Verfügbarkeit "STR Min x 2" und Preis "STR Min x 100 ¥"; High-Tech-Bögen sind "auf eine
 * maximale Stärke von 8 begrenzt".
 *
 * @param reach         Nahkampf-Reichweite ("–", "1", "2"), leer bei Fernkampfwaffen
 * @param damage        Schadenscode als GRW-Text
 * @param armorPiercing Panzerbrechend (PB), z.B. "–1", "+5", "–Hälfte"
 * @param mode          Feuermodi (EM/HM/SM/AM), leer ohne Tabellenspalte
 * @param recoil        Rückstoßkompensation "x (y)", leer ohne Tabellenspalte
 * @param ammo          Munition mit Nachlademethode, z.B. "15 (s)"
 * @param range         Zeile der Reichweitentabelle (GRW S. 179), {@code null} bei Nahkampfwaffen
 * @param cost          Preis-Formel (Nuyen), z.B. {@code "350"} oder {@code "Rating * 100"}
 * @param maxRating     höchste Stufe; {@code 0} bei Waffen ohne Stufe
 * @param note          Tabellenhinweis ohne eigene Zeile (z.B. integrierte Unterlaufwaffe), sonst leer
 */
public record WeaponData(
    @NonNull String name,
    @NonNull WeaponCategory category,
    @NonNull String reach,
    @NonNull String damage,
    @NonNull String armorPiercing,
    @NonNull String mode,
    @NonNull String recoil,
    @NonNull String ammo,
    @Nullable WeaponRange range,
    @NonNull Availability availability,
    @NonNull String cost,
    int maxRating,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  /** Niedrigste Stufe einer Waffe mit Stufe (Bogen: Mindeststärke 1). */
  public static final int MIN_RATING = 1;

  /** Ob die Waffe in Stufen erhältlich ist (nur der Bogen). */
  public boolean hasRating() {
    return maxRating > 0;
  }

  /** Niedrigste wählbare Stufe, bzw. {@code 0} bei Waffen ohne Stufe. */
  public int lowestRating() {
    return hasRating() ? MIN_RATING : 0;
  }

  /** Ob {@code rating} für diese Waffe zulässig ist (Stufe in [1, max] bzw. {@code 0} ohne Stufe). */
  public boolean isValidRating(int rating) {
    return hasRating() ? rating >= MIN_RATING && rating <= maxRating : rating == 0;
  }

  /** Preis in Nuyen für die angegebene Stufe ({@code 0} bei Waffen ohne Stufe). */
  public int costFor(int rating) {
    return RatingFormula.evaluate(cost, rating).setScale(0, RoundingMode.HALF_UP).intValueExact();
  }
}
