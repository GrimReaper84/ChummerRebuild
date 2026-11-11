package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.RangeBands;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponRange;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Anzeigewerte des Detailbereichs im Waffen-Unter-Tab. Enthält nur fertige Texte und keine
 * JavaFX-Typen, damit keine Fachlogik im Layout-Code landet und die Werte ohne UI testbar sind.
 * Schaden, Reichweiten und Fertigkeitswert hängen vom Charakter ab (Stärke, Fertigkeiten, siehe Waffen
 * Teil B) und werden deshalb bei jedem Refresh neu erzeugt. Leere Tabellenspalten des GRW werden als
 * "–" angezeigt.
 *
 * @param damage        Schaden für den Charakter berechnet, z.B. "6K"
 * @param recoil        RK inkl. gekauftem Zubehör (Waffen-Plan Teil C)
 * @param cost          Waffenpreis, mit gekauftem Zubehör zusätzlich die Summe
 * @param damageFormula GRW-Schadensformel, wenn der Schaden berechnet wurde, sonst leer
 * @param skill         Fertigkeit mit Wert, z.B. "Pistolen 4"
 * @param rangeBands    Reichweitenstufen Kurz/Mittel/Weit/Extrem in Metern, leer bei Nahkampfwaffen
 * @param rangeNote     Tabellenzeile der Reichweiten (GRW S. 179) und ggf. Stärkebezug, leer bei
 *                      Nahkampfwaffen
 * @param note          Tabellenhinweis der Waffe (z.B. integrierte Unterlaufwaffe), sonst leer
 */
record WeaponDetails(
    @NonNull String name,
    @NonNull String category,
    @NonNull String skill,
    @NonNull String damage,
    @NonNull String damageFormula,
    @NonNull String recoil,
    @NonNull String armorPiercing,
    @NonNull String reach,
    @NonNull String mode,
    @NonNull String ammo,
    @NonNull String availability,
    @NonNull String cost,
    @NonNull String source,
    @NonNull List<String> rangeBands,
    @NonNull String rangeNote,
    @NonNull String note
) {

  private static final String NO_VALUE = "–";

  static @NonNull WeaponDetails of(@NonNull Weapon weapon, @NonNull Character character) {
    WeaponData data = weapon.getData();
    String damage = character.getEquipment().getWeaponDamageText(weapon);
    Skill skill = character.getEquipment().getWeaponSkill(weapon);
    return build(weapon, skill.getName() + " " + skill.getRating(), damage,
        // Berechnet wurde genau dann, wenn der Text von der GRW-Formel abweicht.
        damage.equals(data.damage()) ? "" : data.damage(),
        character.getEquipment().getWeaponRangeBands(weapon));
  }

  /**
   * Werte einer Waffe in einer Fahrzeug-Waffenhalterung: nur die Tabellenwerte, ohne Stärke und
   * Fertigkeit des Charakters (Nutzerentscheidung 2026-10-09). Halterungswaffen sind Feuerwaffen bzw.
   * Granatwerfer mit festem Schaden und festen Reichweiten; Würfelpools für Rigger und Drohnen (GRW
   * S. 203, 287) sind zurückgestellt.
   */
  static @NonNull WeaponDetails ofMounted(@NonNull Weapon weapon) {
    return build(weapon, NO_VALUE, weapon.getData().damage(), "", weapon.getRangeBands(0));
  }

  /** Gemeinsamer Aufbau; nur Fertigkeit, Schaden und Reichweiten hängen vom Einsatz ab. */
  private static @NonNull WeaponDetails build(@NonNull Weapon weapon, @NonNull String skillText,
      @NonNull String damage, @NonNull String damageFormula, @Nullable RangeBands bands) {
    WeaponData data = weapon.getData();
    WeaponRange range = data.range();
    return new WeaponDetails(
        weapon.getDisplayName(),
        data.category().getDisplayName(),
        skillText,
        orDash(damage),
        damageFormula,
        weapon.getRecoil().displayText(),
        orDash(data.armorPiercing()),
        orDash(data.reach()),
        orDash(data.mode()),
        orDash(data.ammo()),
        weapon.getAvailabilityText(),
        costText(weapon),
        data.source() + " S. " + data.page(),
        bands == null ? List.of() : bands.bandTexts(),
        range == null ? "" : rangeNote(range),
        data.note());
  }

  /**
   * Tabellenzeile der Reichweiten; bei stärkeabhängigen Zeilen mit Bezug: Bögen nach ihrer
   * Mindeststärke (GRW S. 180), Wurfwaffen nach der Stärke des Werfers.
   */
  private static @NonNull String rangeNote(@NonNull WeaponRange range) {
    String text = "Reichweiten wie " + range.getDisplayName() + " (GRW S. 179)";
    if (!range.isStrengthBased()) {
      return text;
    }
    return text + (range == WeaponRange.BOW ? ", nach Mindeststärke" : ", nach Stärke");
  }

  /** z.B. "350 ¥" bzw. mit Zubehör oder Pfeilen "350 ¥ (gesamt 750 ¥)". */
  private static @NonNull String costText(@NonNull Weapon weapon) {
    String text = weapon.getNuyenCost() + " ¥";
    return weapon.getTotalNuyenCost() == weapon.getNuyenCost()
        ? text
        : text + " (gesamt " + weapon.getTotalNuyenCost() + " ¥)";
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? NO_VALUE : text;
  }
}
