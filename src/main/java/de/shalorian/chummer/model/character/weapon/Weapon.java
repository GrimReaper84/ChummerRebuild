package de.shalorian.chummer.model.character.weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Eine gekaufte Waffe eines Charakters: referenziert einen {@link WeaponData}-Katalogeintrag, — nur
 * beim Bogen — die gewählte Stufe (Mindeststärke) und das montierte Zubehör. Dieselbe Waffe darf
 * mehrfach gekauft werden, daher arbeitet {@code Equipment#removeWeapon} über die Objektreferenz.
 *
 * <p>Montageregeln (SR4-Grundregelwerk S. 357, 367/368): Zubehör braucht eine passende, freie
 * Halterung der Waffenkategorie ("An jeder Halterung kann nur jeweils ein Zubehörteil montiert
 * werden"), Zubehör ohne Halterung ist mehrfach möglich. Eingebautes Zubehör belegt keine Halterung.
 * Dazu kommen die Sonderregeln aus {@link AccessoryRule} und die Beschränkung der Projektilwaffen.
 */
public class Weapon {

  private final @NonNull WeaponData data;
  private final int rating;
  private final List<WeaponAccessory> accessories = new ArrayList<>();

  /**
   * Waffe mit Stufe.
   *
   * @param rating bei Waffen mit Stufe in [{@value WeaponData#MIN_RATING}, {@link
   *               WeaponData#maxRating()}], sonst {@code 0}
   * @throws IllegalArgumentException bei unzulässiger Stufe
   */
  public Weapon(@NonNull WeaponData data, int rating) {
    if (!data.isValidRating(rating)) {
      throw new IllegalArgumentException(
          "%s: Stufe %d ist nicht zulässig (max. %d)".formatted(data.name(), rating,
              data.maxRating()));
    }
    this.data = data;
    this.rating = rating;
  }

  /** Waffe ohne Stufe. */
  public Weapon(@NonNull WeaponData data) {
    this(data, 0);
  }

  public @NonNull WeaponData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Gewählte Stufe, bzw. {@code 0} bei Waffen ohne Stufe. */
  public int getRating() {
    return rating;
  }

  /** Anzeigename inkl. Stufe, z.B. "Bogen (Stufe 4)". */
  public @NonNull String getDisplayName() {
    return data.hasRating() ? data.name() + " (Stufe " + rating + ")" : data.name();
  }

  /** Preis der Waffe selbst (ohne Zubehör). */
  public int getNuyenCost() {
    return data.costFor(rating);
  }

  /** Preis der Waffe einschließlich des gekauften Zubehörs; eingebautes Zubehör ist im Preis enthalten. */
  public int getTotalNuyenCost() {
    return getNuyenCost() + accessories.stream().mapToInt(a -> getAccessoryNuyenCost(a.data())).sum();
  }

  /**
   * Preis eines Zubehörteils an dieser Waffe; das interne Smartgunsystem kostet den Waffenpreis (GRW
   * S. 368: "verdoppelt den Preis der Waffe").
   */
  public int getAccessoryNuyenCost(@NonNull AccessoryData accessory) {
    return accessory.costFor(getNuyenCost());
  }

  /**
   * Rückstoßkompensation einschließlich des gekauften Zubehörs: Tabellenwert plus RK des Zubehörs,
   * fest eingebautes Zubehör (Gasventilsystem) in beiden Werten, alles andere nur im Klammerwert
   * (siehe {@link Recoil}). Eingebautes Zubehör ist im Tabellenwert schon enthalten.
   */
  public @NonNull Recoil getRecoil() {
    int fixedRc = 0;
    int removableRc = 0;
    for (WeaponAccessory accessory : accessories) {
      if (accessory.data().isPermanent()) {
        fixedRc += accessory.data().rc();
      } else {
        removableRc += accessory.data().rc();
      }
    }
    return data.recoil().plus(fixedRc, removableRc);
  }

  // --- Zubehör ---

  /** Gekauftes Zubehör in Kaufreihenfolge (ohne eingebautes, siehe {@link WeaponData#included()}). */
  public @NonNull List<WeaponAccessory> getAccessories() {
    return Collections.unmodifiableList(accessories);
  }

  /**
   * Warum {@code accessory} nicht an diese Waffe passt, oder {@code null}, wenn es sich montieren
   * lässt (dann gibt es auch mindestens eine freie Halterung, siehe {@link #freeMounts}).
   */
  public @Nullable String mountProblem(@NonNull AccessoryData accessory) {
    WeaponCategory category = data.category();
    if (!category.acceptsAccessories()) {
      return "Nahkampf- und Wurfwaffen haben kein Zubehör";
    }
    if (category.isProjectile() && !accessory.projectile()) {
      return "An Projektilwaffen passen nur Lasermarkierer oder Smartgunsystem (GRW S. 357)";
    }
    AccessoryRule rule = accessory.rule();
    if (rule != null && !rule.allows(data)) {
      return accessory.name() + ": " + rule.getRestriction() + " (GRW S. 367/368)";
    }
    if (rule != null && rule.isOnePerWeapon()) {
      String holder = ruleHolder(rule);
      if (holder != null) {
        return "Die Waffe hat bereits " + holder;
      }
    }
    if (accessory.needsMount() && freeMounts(accessory).isEmpty()) {
      boolean fits = accessory.mounts().stream().anyMatch(category.mounts()::contains);
      return fits
          ? "Passende Halterung (" + accessory.mountsText() + ") ist bereits belegt"
          : category.getDisplayName() + " haben keine Halterung " + accessory.mountsText()
              + " (GRW S. 357)";
    }
    return null;
  }

  /** Freie Halterungen der Waffe, an die {@code accessory} passt (leer bei Zubehör ohne Halterung). */
  public @NonNull List<Mount> freeMounts(@NonNull AccessoryData accessory) {
    Set<Mount> available = data.category().mounts();
    return accessory.mounts().stream()
        .filter(available::contains)
        .filter(mount -> accessories.stream().noneMatch(a -> a.mount() == mount))
        .toList();
  }

  /**
   * Montiert ein Zubehörteil. Die Verfügbarkeit bei der Erschaffung prüft die UI (wie bei Waffen).
   *
   * @throws IllegalArgumentException wenn das Teil laut {@link #mountProblem} nicht passt oder die
   *                                  Halterung nicht frei bzw. nicht angegeben ist
   */
  public void addAccessory(@NonNull WeaponAccessory accessory) {
    AccessoryData accessoryData = accessory.data();
    String problem = mountProblem(accessoryData);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    Mount mount = accessory.mount();
    boolean mountValid = accessoryData.needsMount()
        ? mount != null && freeMounts(accessoryData).contains(mount)
        : mount == null;
    if (!mountValid) {
      throw new IllegalArgumentException("%s: Halterung %s ist nicht zulässig".formatted(
          accessoryData.name(), mount == null ? "fehlt" : mount.getDisplayName()));
    }
    accessories.add(accessory);
  }

  /**
   * Entfernt ein Zubehörteil.
   *
   * @throws IllegalStateException bei fest eingebautem Zubehör (GRW S. 367: "Einmal installierte
   *                               Gasventilsysteme können nicht mehr entfernt werden.")
   */
  public void removeAccessory(@NonNull WeaponAccessory accessory) {
    if (accessory.data().isPermanent()) {
      throw new IllegalStateException(accessory.getName() + " kann nicht entfernt werden");
    }
    accessories.remove(accessory);
  }

  /**
   * Name des Teils mit dieser Regel (mitgeliefertes zuerst) für den Hinweistext, oder {@code null},
   * wenn die Waffe keines hat.
   */
  private @Nullable String ruleHolder(@NonNull AccessoryRule rule) {
    return data.included().stream().filter(i -> i.rule() == rule)
        .map(i -> i.name() + " (mitgeliefert)")
        .findFirst()
        .orElseGet(() -> accessories.stream().filter(a -> a.data().rule() == rule)
            .map(WeaponAccessory::getName).findFirst().orElse(null));
  }

  /** Verfügbarkeit dieser Waffe in ihrer Stufe, wie im GRW angezeigt (z.B. "8" beim Bogen Stufe 4). */
  public @NonNull String getAvailabilityText() {
    return data.availability().displayText(rating);
  }

  /**
   * Schaden für einen Träger mit der angegebenen Stärke (siehe {@link WeaponDamage}), z.B. "6K" beim
   * Katana mit STR 5. Codes ohne Stärkebezug werden unverändert geliefert.
   */
  public @NonNull String getDamageText(int strength) {
    return WeaponDamage.evaluate(data.damage(), effectiveStrength(strength));
  }

  /**
   * Reichweitenstufen für einen Schützen bzw. Werfer mit der angegebenen Stärke, oder {@code null}
   * bei Nahkampfwaffen.
   */
  public @Nullable RangeBands getRangeBands(int strength) {
    WeaponRange range = data.range();
    return range == null ? null : range.bands(effectiveStrength(strength));
  }

  /**
   * Stärke, nach der Schaden und Reichweite berechnet werden: beim Bogen seine Mindeststärke (= Stufe),
   * sonst die des Charakters. GRW S. 361: "Die Mindeststärke der Waffe wird auch für die Bestimmung
   * des Schadens und der Reichweite benutzt". Pfeile sind noch nicht modelliert; ihre Stufe (S. 361:
   * "Berechnen Sie den Schaden anhand der niedrigsten Stufe des Pfeils oder Bogens") bleibt außen vor.
   */
  private int effectiveStrength(int strength) {
    return data.hasRating() ? rating : strength;
  }
}
