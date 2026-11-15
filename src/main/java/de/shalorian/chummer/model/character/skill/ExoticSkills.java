package de.shalorian.chummer.model.character.skill;

import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Exotische Fertigkeiten (GRW S. 148, 155, 365): Exotische Fern- und Nahkampfwaffe und Exotisches Fahrzeug werden je
 * Waffe bzw. Fahrzeug einzeln gelernt. Die Grundfertigkeit bleibt als Platzhalter ohne Wert; gelernt wird je Ziel
 * eine eigene Instanz mit dem Namen „Grundfertigkeit (Ziel)“.
 */
public final class ExoticSkills {

  public static final String RANGED = "Exotische Fernkampfwaffe";
  public static final String MELEE = "Exotische Nahkampfwaffe";
  public static final String VEHICLE = "Exotisches Fahrzeug";
  private static final Set<String> BASES = Set.of(RANGED, MELEE, VEHICLE);

  private ExoticSkills() {
  }

  /** Ob {@code name} eine der drei exotischen Grundfertigkeiten ist. */
  public static boolean isBase(@NonNull String name) {
    return BASES.contains(name);
  }

  /**
   * Vorschläge für das Ziel: die Waffen des Katalogs, die diese Grundfertigkeit nutzen (Exotische Nahkampfwaffen bzw.
   * Spezielle Waffen, S. 361, 365); für Exotisches Fahrzeug keine, das GRW-Fahrzeugverzeichnis hat keine exotischen.
   * Freitext bleibt möglich (z.B. Laser, Kettensäge, Jetpack).
   */
  public static @NonNull List<String> targetSuggestions(@NonNull String base) {
    return WeaponDataLoader.getAllWeapons().values().stream()
        .filter(w -> isWeaponCategoryOf(w.category(), base))
        .map(WeaponData::name)
        .sorted(Collator.getInstance(Locale.GERMAN))
        .toList();
  }

  private static boolean isWeaponCategoryOf(@NonNull WeaponCategory category, @NonNull String base) {
    return !VEHICLE.equals(base) && category.skillName().equals(base);
  }

  /** Name einer Instanz, z.B. „Exotische Nahkampfwaffe (Monofilamentpeitsche)“. */
  public static @NonNull String instanceName(@NonNull String base, @NonNull String target) {
    return base + " (" + target.trim() + ")";
  }
}
