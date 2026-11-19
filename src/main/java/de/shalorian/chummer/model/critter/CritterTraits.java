package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.template.TemplateEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Was einen {@code Character} zum Critter macht (GRW S. 337–343): der Katalogeintrag, Kräfte und Schwächen (mit Zusatz
 * in Klammern, von der Spielleitung änderbar) und Sonderfertigkeiten ohne Eintrag in {@code skills.xml} (Fliegen).
 * Rechnerisch wirken Natürliche Waffe, Panzer und Verstärkter Panzer; alle übrigen Kräfte sind Beschreibung.
 */
public class CritterTraits {

  private static final Pattern ARMOR = Pattern.compile("\\s*(\\d+)\\s*(?:/\\s*(\\d+)\\s*)?");
  /** Höchste Stufe einer Fertigkeit, die der Critter laut Katalog nicht hat (Annahme, Plan R2). */
  public static final int UNLISTED_SKILL_MAX = 3;

  private final @NonNull CritterData data;
  private final List<String> powers;
  private final List<String> weaknesses;
  private final Map<String, Integer> specialSkills = new LinkedHashMap<>();

  /** Neu aus dem Katalog: Kräfte, Schwächen und Sonderfertigkeiten wie im GRW. */
  public CritterTraits(@NonNull CritterData data) {
    this(data, data.powers(), data.weaknesses());
    data.skills().stream().filter(s -> s.kind().equals("special"))
        .forEach(s -> specialSkills.put(s.name(), s.rating()));
  }

  /** Mit gespeicherten Kräften und Schwächen (Sonderfertigkeiten setzt {@link #setSpecialSkill}). */
  public CritterTraits(@NonNull CritterData data, @NonNull List<String> powers, @NonNull List<String> weaknesses) {
    this.data = data;
    this.powers = new ArrayList<>(powers);
    this.weaknesses = new ArrayList<>(weaknesses);
  }

  public @NonNull CritterData getData() {
    return data;
  }

  public @NonNull List<String> getPowers() {
    return Collections.unmodifiableList(powers);
  }

  public void addPower(@NonNull String power) {
    powers.add(power);
  }

  public void removePower(@NonNull String power) {
    powers.remove(power);
  }

  public @NonNull List<String> getWeaknesses() {
    return Collections.unmodifiableList(weaknesses);
  }

  public void addWeakness(@NonNull String weakness) {
    weaknesses.add(weakness);
  }

  public void removeWeakness(@NonNull String weakness) {
    weaknesses.remove(weakness);
  }

  public @NonNull Map<String, Integer> getSpecialSkills() {
    return Collections.unmodifiableMap(specialSkills);
  }

  public void setSpecialSkill(@NonNull String name, int rating) {
    specialSkills.put(name, rating);
  }

  /** Katalogname eines Eintrags „Name (Zusatz)“: der Teil vor der Klammer. */
  public static @NonNull String baseName(@NonNull String entry) {
    int open = entry.indexOf('(');
    return (open < 0 ? entry : entry.substring(0, open)).trim();
  }

  /** Zusatz eines Eintrags „Name (Zusatz)“, leer ohne Klammer. */
  public static @NonNull String extra(@NonNull String entry) {
    int open = entry.indexOf('(');
    int close = entry.lastIndexOf(')');
    return open >= 0 && close > open ? entry.substring(open + 1, close).trim() : "";
  }

  /** Setzt einen Eintrag „Name (Zusatz)“ zusammen; ohne Zusatz nur der Name. */
  public static @NonNull String entry(@NonNull String name, @NonNull String extra) {
    return extra.isBlank() ? name : name + " (" + extra.trim() + ")";
  }

  /** Höchststufe einer Fertigkeit: Katalogwert (bzw. Gruppenwert) + 3, sonst {@value #UNLISTED_SKILL_MAX} (S. 337). */
  public int getSkillMaximum(@NonNull Skill skill) {
    for (TemplateEntry entry : data.skills()) {
      boolean listed = entry.kind().equals("skill") ? skill.getName().equals(entry.name())
          : entry.kind().equals("group") && entry.name().equals(skill.getGroupName());
      if (listed) {
        return entry.rating() + CritterMetatype.ADJUSTMENT;
      }
    }
    return UNLISTED_SKILL_MAX;
  }

  /** Natürliche Waffen aus den Kräften; ein Eintrag ohne lesbaren Zusatz wird übersprungen. */
  public @NonNull List<NaturalWeapon> getNaturalWeapons() {
    List<NaturalWeapon> weapons = new ArrayList<>();
    for (String power : powers) {
      if (effect(power) == CritterPowerEffect.NATURAL_WEAPON) {
        try {
          weapons.add(NaturalWeapon.parse(extra(power)));
        } catch (IllegalArgumentException _) {
          // Zusatz frei geändert: keine Waffe ableitbar, die Kraft bleibt als Text sichtbar
        }
      }
    }
    return weapons;
  }

  /** Ballistische Panzerung aus Panzer und Verstärktem Panzer, z.B. „Panzer (2/3)“ → 2 (S. 340, 342). */
  public int getArmorBallistic() {
    return armor(true);
  }

  /** Stoßpanzerung, z.B. „Panzer (2/3)“ → 3; „(8)“ gilt für beide. */
  public int getArmorImpact() {
    return armor(false);
  }

  /** Ob Schaden bis zur Panzerung ganz abprallt (Verstärkter Panzer, S. 342). */
  public boolean isHardened() {
    return powers.stream().anyMatch(p -> effect(p) == CritterPowerEffect.HARDENED_ARMOR);
  }

  private int armor(boolean ballistic) {
    int total = 0;
    for (String power : powers) {
      CritterPowerEffect effect = effect(power);
      if (effect == CritterPowerEffect.ARMOR || effect == CritterPowerEffect.HARDENED_ARMOR) {
        Matcher m = ARMOR.matcher(extra(power));
        if (m.matches()) {
          String impact = m.group(2);
          total += Integer.parseInt(ballistic || impact == null ? m.group(1) : impact);
        }
      }
    }
    return total;
  }

  private static @Nullable CritterPowerEffect effect(@NonNull String power) {
    CritterPowerData data = CritterPowerDataLoader.findForEntry(power);
    return data == null ? null : data.effect();
  }
}
