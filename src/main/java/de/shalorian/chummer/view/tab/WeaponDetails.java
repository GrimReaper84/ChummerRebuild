package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.RangeBands;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponRange;
import de.shalorian.chummer.utils.Numbers;
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
 * @param damageFormula GRW-Schadensformel, wenn der Schaden aus der Stärke berechnet wurde, sonst leer
 * @param skill         Fertigkeit mit Wert, z.B. "Pistolen 4"
 * @param dicePool      Würfelpool der Waffe, {@code null} ohne mögliche Probe (auch bei Fahrzeugwaffen)
 * @param skillRating   Wert der Waffenfertigkeit (0: Probe ohne Fertigkeitswert, gedämpft angezeigt)
 * @param specialization Spezialisierung, wenn sie auf die Waffe passt (+2, S. 147), sonst {@code null}
 * @param armorType     Panzerung gegen die geladene Munition (S. 369), leer bei Waffen ohne Munition
 * @param rangeBands    Reichweitenstufen Kurz/Mittel/Weit/Extrem in Metern, leer bei Nahkampfwaffen
 * @param rangeNote     Tabellenzeile der Reichweiten (GRW S. 179) und ggf. Stärkebezug, leer bei
 *                      Nahkampfwaffen
 * @param note          Tabellenhinweis der Waffe (z.B. integrierte Unterlaufwaffe), sonst leer
 */
public record WeaponDetails(
    @NonNull String name,
    @NonNull String category,
    @NonNull String skill,
    @Nullable DicePool dicePool,
    int skillRating,
    @Nullable String specialization,
    @NonNull String damage,
    @NonNull String damageFormula,
    @NonNull String recoil,
    @NonNull String armorPiercing,
    @NonNull String armorType,
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

  public static @NonNull WeaponDetails of(@NonNull Weapon weapon, @NonNull Character character) {
    Equipment equipment = character.getEquipment();
    WeaponData data = weapon.getData();
    String damage = equipment.getWeaponDamageText(weapon);
    Skill skill = equipment.getWeaponSkill(weapon);
    DicePool pool = equipment.getWeaponDicePool(weapon).orElse(null);
    String specialization = equipment.isWeaponSpecialized(weapon) ? skill.getSpecialization() : null;
    return build(weapon, skill.getName() + " " + skill.getRating(), pool, skill.getRating(), specialization, damage,
        // Formel nur, wenn der Schaden aus der Stärke kommt (Munition ändert sonst auch feste Werte).
        data.damage().contains("STR") && !damage.equals(data.damage()) ? data.damage() : "",
        equipment.getWeaponArmorPiercingText(weapon), equipment.getWeaponArmorType(weapon),
        equipment.getWeaponRangeBands(weapon));
  }

  /**
   * Werte einer Waffe in einer Fahrzeug-Waffenhalterung: nur die Tabellenwerte, ohne Stärke und
   * Fertigkeit des Charakters (Nutzerentscheidung 2026-10-09). Halterungswaffen sind Feuerwaffen bzw.
   * Granatwerfer mit festem Schaden und festen Reichweiten; ihr Angriff steht in den Fahrzeugpools ({@code
   * VehiclePools}, GRW S. 287).
   */
  static @NonNull WeaponDetails ofMounted(@NonNull Weapon weapon) {
    return build(weapon, Numbers.NO_VALUE, null, 0, null, weapon.getData().damage(), "", weapon.getData().armorPiercing(), "",
        weapon.getRangeBands(0));
  }

  /** Gemeinsamer Aufbau; nur Fertigkeit, Schaden und Reichweiten hängen vom Einsatz ab. */
  private static @NonNull WeaponDetails build(@NonNull Weapon weapon, @NonNull String skillText,
      @Nullable DicePool dicePool, int skillRating, @Nullable String specialization, @NonNull String damage,
      @NonNull String damageFormula, @NonNull String armorPiercing, @NonNull String armorType,
      @Nullable RangeBands bands) {
    WeaponData data = weapon.getData();
    WeaponRange range = data.range();
    return new WeaponDetails(
        weapon.getDisplayName(),
        data.category().getDisplayName(),
        skillText,
        dicePool,
        skillRating,
        specialization,
        orDash(damage),
        damageFormula,
        weapon.getRecoil().displayText(),
        orDash(armorPiercing),
        armorType,
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
    String text = Numbers.nuyen(weapon.getNuyenCost());
    return weapon.getTotalNuyenCost() == weapon.getNuyenCost()
        ? text
        : text + " (gesamt " + Numbers.nuyen(weapon.getTotalNuyenCost()) + ")";
  }

  private static @NonNull String orDash(@NonNull String text) {
    return text.isEmpty() ? Numbers.NO_VALUE : text;
  }
}
