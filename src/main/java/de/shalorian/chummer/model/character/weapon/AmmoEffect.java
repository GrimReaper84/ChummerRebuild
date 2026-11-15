package de.shalorian.chummer.model.character.weapon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Wirkung einer Munitionssorte auf die Waffenwerte (GRW S. 369, Tabelle „Munition pro 10 Schuss“): Schadens- und
 * PB-Modifikator werden zu den Werten der Waffe addiert, Gelgeschosse machen den Schaden geistig, Schocker ersetzen
 * Schadenscode und PB. Waffen, deren Schaden schon Flechette einrechnet ({@code (f)}), bleiben bei Flechettegeschossen
 * unverändert.
 *
 * @param damageModifier Schadensmodifikator
 * @param apModifier     PB-Modifikator
 * @param damageType     neue Schadensart ({@code "G"} bei Gelgeschossen), sonst {@code null}
 * @param replaceDamage  Schadenscode, der den der Waffe ersetzt (Schocker), sonst {@code null}
 * @param replaceAp      PB, die die der Waffe ersetzt (Schocker), sonst {@code null}
 * @param flechette      ob es Flechettegeschosse sind
 * @param armorType      verwendete Panzerung, z.B. „Ballistisch“
 */
public record AmmoEffect(int damageModifier, int apModifier, @Nullable String damageType,
                         @Nullable String replaceDamage, @Nullable String replaceAp, boolean flechette,
                         @NonNull String armorType) {

  /** Schadenscode einer Waffe nach Auswertung der Stärke, z.B. „5K“, „6G(e)“, „8K(f)“. */
  private static final Pattern DAMAGE = Pattern.compile("(\\d+)([KG])(.*)");
  /** Modifikator mit Vorzeichen, z.B. „+1“, „−4“, „–1“. */
  private static final Pattern SIGNED = Pattern.compile("([+−–-])(\\d+)");
  /** Gelgeschosse: „(−1)G“. */
  private static final Pattern TYPED_MODIFIER = Pattern.compile("\\(([+−–-]\\d+)\\)([KG])");
  /** Ersatz-Schadenscode, z.B. „6G(e)“. */
  private static final Pattern REPLACEMENT = Pattern.compile("\\d+[KG](\\([ef]\\))?");
  private static final String FLECHETTE_MARK = "(f)";
  private static final String NONE = "–";

  /**
   * Wirkung aus den Tabellenspalten, oder {@code null}, wenn die Munition die Werte der Waffe übernimmt („wie Taser“,
   * „wie Kanone“, „wie Droge/Toxin“) und deshalb nicht geladen wird (Plan Kampfwerte, Annahme A1).
   */
  public static @Nullable AmmoEffect fromTable(@NonNull String name, @NonNull String damage, @NonNull String ap,
      @NonNull String armorType) {
    int damageModifier = 0;
    String damageType = null;
    String replaceDamage = null;
    Matcher typed = TYPED_MODIFIER.matcher(damage);
    if (typed.matches()) {
      damageModifier = signed(typed.group(1));
      damageType = typed.group(2);
    } else if (REPLACEMENT.matcher(damage).matches()) {
      replaceDamage = damage;
    } else if (SIGNED.matcher(damage).matches()) {
      damageModifier = signed(damage);
    } else if (!isNone(damage)) {
      return null;
    }
    int apModifier = 0;
    String replaceAp = null;
    if (SIGNED.matcher(ap).matches()) {
      apModifier = signed(ap);
    } else if (!isNone(ap)) {
      replaceAp = ap.replace('−', '–'); // Schreibweise der Waffentabelle (–Hälfte)
    }
    return new AmmoEffect(damageModifier, apModifier, damageType, replaceDamage, replaceAp,
        name.startsWith("Flechette"), armorType);
  }

  /** Schaden der Waffe mit dieser Munition; nicht auswertbare Codes bleiben unverändert. */
  public @NonNull String applyToDamage(@NonNull String weaponDamage) {
    if (ignoredBy(weaponDamage)) {
      return weaponDamage;
    }
    if (replaceDamage != null) {
      return replaceDamage;
    }
    Matcher m = DAMAGE.matcher(weaponDamage);
    if (!m.matches()) {
      return weaponDamage;
    }
    String type = damageType == null ? m.group(2) : damageType;
    return Math.max(0, Integer.parseInt(m.group(1)) + damageModifier) + type + m.group(3);
  }

  /**
   * PB der Waffe mit dieser Munition („–“ zählt als 0); eine nicht numerische PB der Waffe (z.B. „–Hälfte“) bleibt
   * unverändert.
   *
   * @param weaponDamage Schaden der Waffe, für die Flechette-Ausnahme
   */
  public @NonNull String applyToArmorPiercing(@NonNull String weaponAp, @NonNull String weaponDamage) {
    if (ignoredBy(weaponDamage)) {
      return weaponAp;
    }
    if (replaceAp != null) {
      return replaceAp;
    }
    int base;
    if (isNone(weaponAp)) {
      base = 0;
    } else if (SIGNED.matcher(weaponAp).matches()) {
      base = signed(weaponAp);
    } else {
      return weaponAp;
    }
    int total = base + apModifier;
    return total == 0 ? NONE : (total < 0 ? "–" + -total : "+" + total);
  }

  /** Flechettegeschosse in einer Waffe, deren Schaden Flechette schon einrechnet (S. 369). */
  private boolean ignoredBy(@NonNull String weaponDamage) {
    return flechette && weaponDamage.contains(FLECHETTE_MARK);
  }

  private static boolean isNone(@NonNull String text) {
    return text.isEmpty() || text.equals(NONE) || text.equals("-") || text.equals("−");
  }

  private static int signed(@NonNull String text) {
    int value = Integer.parseInt(text.substring(1));
    return text.charAt(0) == '+' ? value : -value;
  }
}
