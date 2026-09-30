package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.RangeBands;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponRange;
import org.jspecify.annotations.NonNull;

/**
 * Anzeigewerte einer Zeile der Waffentabelle. Die Zellen des {@code TreeTableView} lesen ausschließlich
 * diese Texte, damit keine Fachlogik in Cell-Factories landet. Leere Tabellenspalten des GRW werden als
 * "–" angezeigt. Schaden, Reichweiten und Fertigkeitswert hängen vom Charakter ab (Stärke,
 * Fertigkeiten) und werden deshalb bei jedem Refresh neu erzeugt.
 *
 * @param weapon        die dargestellte Waffe (für Aktionen wie Entfernen)
 * @param damage        Schaden für den Charakter berechnet, z.B. "6K"
 * @param damageFormula GRW-Schadensformel als Tooltip, leer bei festem Schaden
 * @param ranges        Reichweitenstufen K/M/W/E in Metern, "–" bei Nahkampfwaffen
 * @param rangeRow      Zeile der GRW-Reichweitentabelle als Tooltip, leer bei Nahkampfwaffen
 * @param skill         Fertigkeit mit Wert, z.B. "Pistolen 4"
 */
record WeaponTableRow(
    @NonNull Weapon weapon,
    @NonNull String name,
    @NonNull String category,
    @NonNull String reach,
    @NonNull String damage,
    @NonNull String damageFormula,
    @NonNull String armorPiercing,
    @NonNull String mode,
    @NonNull String recoil,
    @NonNull String ammo,
    @NonNull String ranges,
    @NonNull String rangeRow,
    @NonNull String skill,
    @NonNull String availability,
    @NonNull String cost,
    @NonNull String source,
    @NonNull String note
) {

  private static final String NO_VALUE = "–";

  static @NonNull WeaponTableRow of(@NonNull Weapon weapon, @NonNull Character character) {
    WeaponData data = weapon.getData();
    RangeBands bands = character.getWeaponRangeBands(weapon);
    WeaponRange range = data.range();
    Skill skill = character.getWeaponSkill(weapon);
    String damage = character.getWeaponDamageText(weapon);
    return new WeaponTableRow(
        weapon,
        weapon.getDisplayName(),
        data.category().getDisplayName(),
        orDash(data.reach()),
        orDash(damage),
        // Berechnet wurde genau dann, wenn der Text von der GRW-Formel abweicht.
        damage.equals(data.damage()) ? "" : "GRW-Formel: " + data.damage(),
        orDash(data.armorPiercing()),
        orDash(data.mode()),
        orDash(data.recoil()),
        orDash(data.ammo()),
        bands == null ? NO_VALUE : bands.displayText(),
        range == null ? "" : rangeTooltip(range),
        skill.getName() + " " + skill.getRating(),
        weapon.getAvailabilityText(),
        weapon.getNuyenCost() + " ¥",
        data.source() + " S. " + data.page(),
        data.note());
  }

  private static @NonNull String rangeTooltip(@NonNull WeaponRange range) {
    String text = "Reichweiten wie " + range.getDisplayName() + " (GRW S. 179)";
    return range.isStrengthBased() ? text + ", nach Stärke (Bogen: Mindeststärke)" : text;
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? NO_VALUE : text;
  }
}
