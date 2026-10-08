package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.gear.CommlinkStats;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.GearCombatStats;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearUnit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * @param capacity     beim Gerät "belegt von gesamt" (z.B. "2 von 3 belegt"), beim Einbau der
 *                     Kapazitätsbedarf wie im GRW ("[2]"), sonst leer
 * @param combat       Kampfwerte von Munition, Granaten und Raketen, sonst {@code null}
 * @param ammunition   ob die Kampfwerte Modifikatoren der Munition sind ("Schadensmod.",
 *                     "PB-Modifikator", GRW S. 369)
 * @param commlink     ob der Gegenstand ein Kommlink ist (dann sind die vier Matrixwerte belegt)
 * @param processor    Prozessor eines Kommlinks, leer bei anderen Gegenständen (ebenso die drei
 *                     folgenden Werte)
 * @param signal       Signal eines Kommlinks
 * @param firewall     Firewall des installierten Betriebssystems, "–" ohne Betriebssystem
 * @param system       wirksames System, ggf. mit Hinweis auf die Begrenzung durch den Prozessor
 *                     (GRW S. 260)
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
    @NonNull String note,
    @NonNull String capacity,
    @Nullable GearCombatStats combat,
    boolean ammunition,
    boolean commlink,
    @NonNull String processor,
    @NonNull String signal,
    @NonNull String firewall,
    @NonNull String system
) {

  private static final String NO_VALUE = "–";

  static @NonNull GearDetails of(@NonNull Gear gear) {
    GearData data = gear.getData();
    CommlinkStats stats = gear.getCommlinkStats();
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
        data.note(),
        capacityText(gear),
        data.combat(),
        data.category() == GearCategory.AMMUNITION,
        stats != null,
        stats == null ? "" : String.valueOf(stats.processor()),
        stats == null ? "" : String.valueOf(stats.signal()),
        stats == null ? "" : orNoValue(stats.firewall()),
        stats == null ? "" : systemText(stats));
  }

  private static @NonNull String capacityText(@NonNull Gear gear) {
    GearData data = gear.getData();
    if (data.hasCapacity()) {
      return gear.getUsedCapacity() + " von " + gear.getCapacity() + " belegt";
    }
    int cost = data.capacityCostFor(gear.getRating());
    return cost > 0 ? "[" + cost + "]" : "";
  }

  private static @NonNull String orNoValue(int value) {
    return value > 0 ? String.valueOf(value) : NO_VALUE;
  }

  /** z.B. "3", "1 (auf Prozessor begrenzt, Betriebssystem 3)" oder "–" ohne Betriebssystem. */
  private static @NonNull String systemText(@NonNull CommlinkStats stats) {
    if (!stats.isSystemCapped()) {
      return orNoValue(stats.system());
    }
    return stats.system() + " (auf Prozessor begrenzt, Betriebssystem " + stats.nominalSystem()
        + ")";
  }

  /** z.B. "800 ¥ pro Stück", "1 ¥ pro 20 Stück", "50 ¥ pro 100 m", "25000 ¥ pro Jahr". */
  private static @NonNull String packPriceText(@NonNull Gear gear) {
    GearData data = gear.getData();
    GearUnit unit = data.unit();
    String pack = data.packSize() == 1 ? unit.getSingular() : unit.format(data.packSize());
    return gear.getPackPrice() + " ¥ pro " + pack;
  }
}
