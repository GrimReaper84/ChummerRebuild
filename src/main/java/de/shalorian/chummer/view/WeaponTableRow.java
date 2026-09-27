package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import org.jspecify.annotations.NonNull;

/**
 * Anzeigewerte einer Zeile der Waffentabelle. Die Zellen des {@code TreeTableView} lesen ausschließlich
 * diese Texte, damit keine Fachlogik in Cell-Factories landet. Leere Tabellenspalten des GRW werden als
 * "–" angezeigt.
 *
 * @param weapon die dargestellte Waffe (für Aktionen wie Entfernen)
 */
record WeaponTableRow(
    @NonNull Weapon weapon,
    @NonNull String name,
    @NonNull String category,
    @NonNull String reach,
    @NonNull String damage,
    @NonNull String armorPiercing,
    @NonNull String mode,
    @NonNull String recoil,
    @NonNull String ammo,
    @NonNull String availability,
    @NonNull String cost,
    @NonNull String source,
    @NonNull String note
) {

  private static final String NO_VALUE = "–";

  static @NonNull WeaponTableRow of(@NonNull Weapon weapon) {
    WeaponData data = weapon.getData();
    return new WeaponTableRow(
        weapon,
        weapon.getDisplayName(),
        data.category().getDisplayName(),
        orDash(data.reach()),
        orDash(data.damage()),
        orDash(data.armorPiercing()),
        orDash(data.mode()),
        orDash(data.recoil()),
        orDash(data.ammo()),
        weapon.getAvailabilityText(),
        weapon.getNuyenCost() + " ¥",
        data.source() + " S. " + data.page(),
        data.note());
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? NO_VALUE : text;
  }
}
