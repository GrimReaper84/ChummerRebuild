package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearUnit;
import org.jspecify.annotations.NonNull;

/**
 * Anzeigewerte des Detailbereichs im Ausrüstungs-Unter-Tab, analog {@link WeaponDetails}: nur
 * fertige Texte, keine JavaFX-Typen, damit die Werte ohne UI testbar sind. Fehlende Werte werden als
 * "–" angezeigt.
 *
 * @param category     GRW-Tabelle, z.B. "Einbruchswerkzeug" (Seite siehe {@code source})
 * @param rating       gewählte Stufe bzw. "–"
 * @param deviceRating Gerätestufe laut Tabelle bzw. "–"
 * @param extraLabel   Bezeichnung des Zusatztexts (z.B. "Fertigkeit"), leer ohne Zusatztext
 * @param quantity     Menge mit Einheit, z.B. "40 Stück", "3 Dosen"
 * @param packPrice    Preis je Packung, z.B. "1 ¥ pro 20 Stück", "10 ¥ pro Dosis"
 * @param totalCost    Preis der ganzen Menge
 * @param note         Hinweis aus Tabelle oder Text, sonst leer
 */
record GearDetails(
    @NonNull String name,
    @NonNull String category,
    @NonNull String rating,
    @NonNull String deviceRating,
    @NonNull String extraLabel,
    @NonNull String extra,
    @NonNull String quantity,
    @NonNull String availability,
    @NonNull String packPrice,
    @NonNull String totalCost,
    @NonNull String source,
    @NonNull String note
) {

  private static final String NO_VALUE = "–";

  static @NonNull GearDetails of(@NonNull Gear gear) {
    GearData data = gear.getData();
    return new GearDetails(
        gear.getDisplayName(),
        data.category().getDisplayName(),
        data.hasRating() ? String.valueOf(gear.getRating()) : NO_VALUE,
        data.deviceRating() > 0 ? String.valueOf(data.deviceRating()) : NO_VALUE,
        data.extraLabel(),
        gear.getExtra(),
        data.unit().format(gear.getQuantity()),
        gear.getAvailabilityText(),
        packPriceText(gear),
        gear.getNuyenCost() + " ¥",
        data.source() + " S. " + data.page(),
        data.note());
  }

  /** z.B. "800 ¥ pro Stück", "1 ¥ pro 20 Stück", "50 ¥ pro 100 m", "25000 ¥ pro Jahr". */
  private static @NonNull String packPriceText(@NonNull Gear gear) {
    GearData data = gear.getData();
    GearUnit unit = data.unit();
    String pack = data.packSize() == 1 ? unit.getSingular() : unit.format(data.packSize());
    return gear.getPackPrice() + " ¥ pro " + pack;
  }
}
