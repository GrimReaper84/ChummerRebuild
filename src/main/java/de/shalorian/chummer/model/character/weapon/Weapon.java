package de.shalorian.chummer.model.character.weapon;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
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
 * Halterung der Waffenkategorie (je Halterung ein Teil), Zubehör ohne Halterung ist mehrfach möglich. Eingebautes Zubehör belegt keine Halterung.
 * Dazu kommen die Sonderregeln aus {@link AccessoryRule} und die Beschränkung der Projektilwaffen.
 *
 * <p><b>Pfeile:</b> Ein Bogen führt seine Pfeile als Stapel ({@link Gear} der Kategorie {@link
 * GearCategory#ARROWS}, Nutzerentscheidung 2026-10-04: Pfeile im Waffen-Tab am Bogen). Gleiche Pfeile
 * werden zusammengeführt; ihr Preis zählt zur Waffe. GRW S. 361: Der Schaden richtet sich nach der
 * niedrigeren Stufe von Pfeil und Bogen ({@link #getArrowDamageText(Gear)}).
 */
public class Weapon {

  private final @NonNull WeaponData data;
  private final int rating;
  private final List<WeaponAccessory> accessories = new ArrayList<>();
  private final List<Gear> arrows = new ArrayList<>();
  private final @NonNull String builtInSource;
  private final @Nullable Integer fixedStrength;

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
    this.builtInSource = "";
    this.fixedStrength = null;
  }

  /** Waffe ohne Stufe. */
  public Weapon(@NonNull WeaponData data) {
    this(data, 0);
  }

  /**
   * Eingebaute Waffe ohne Stufe ({@link WeaponData#builtIn()}), z.B. aus Rüstung oder einem Cyberglied.
   *
   * @param builtInSource woher sie kommt, für die Anzeige („aus Rüstung“, „Implantat, linker Arm“)
   * @param fixedStrength Stärke, nach der sie rechnet (STR des Cyberglieds, S. 389/390), oder {@code null} für die des
   *                      Charakters
   */
  public Weapon(@NonNull WeaponData data, @NonNull String builtInSource, @Nullable Integer fixedStrength) {
    this.data = data;
    this.rating = 0;
    this.builtInSource = builtInSource;
    this.fixedStrength = fixedStrength;
  }

  /** Herkunft einer eingebauten Waffe, sonst leer. */
  public @NonNull String getBuiltInSource() {
    return builtInSource;
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

  /**
   * Preis der Waffe einschließlich des gekauften Zubehörs und der Pfeile; eingebautes Zubehör ist im
   * Preis enthalten.
   */
  public int getTotalNuyenCost() {
    return getNuyenCost() + accessories.stream().mapToInt(a -> getAccessoryNuyenCost(a.data())).sum()
        + arrows.stream().mapToInt(Gear::getNuyenCost).sum();
  }

  /**
   * Preis eines Zubehörteils an dieser Waffe; das interne Smartgunsystem kostet den Waffenpreis (GRW
   * S. 368).
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
   * @throws IllegalStateException bei fest eingebautem Zubehör (GRW S. 367: Gasventile bleiben nach
   *                               dem Einbau drin)
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

  // --- Pfeile ---

  /** Ob die Waffe Pfeile führt (nur Bögen). */
  public boolean takesArrows() {
    return data.category() == WeaponCategory.BOWS;
  }

  /** Pfeilstapel in Kaufreihenfolge. */
  public @NonNull List<Gear> getArrows() {
    return Collections.unmodifiableList(arrows);
  }

  /**
   * Fügt Pfeile hinzu; gleiche Pfeile (Sorte und Stufe) werden mit einem vorhandenen Stapel
   * zusammengeführt.
   *
   * @return der Stapel, der die Pfeile jetzt enthält
   * @throws IllegalArgumentException wenn die Waffe kein Bogen ist oder {@code item} kein Pfeil
   */
  public @NonNull Gear addArrows(@NonNull Gear item) {
    if (!takesArrows() || item.getData().category() != GearCategory.ARROWS) {
      throw new IllegalArgumentException(item.getName() + " passt nicht zu " + data.name());
    }
    for (Gear stack : arrows) {
      if (stack.isSameKind(item)) {
        stack.setQuantity(stack.getQuantity() + item.getQuantity());
        return stack;
      }
    }
    arrows.add(item);
    return item;
  }

  /** Entfernt genau diesen Pfeilstapel. */
  public void removeArrows(@NonNull Gear stack) {
    arrows.remove(stack);
  }

  /**
   * Schaden mit diesen Pfeilen: Bogenschaden nach der niedrigeren Stufe von Pfeil und Bogen (GRW
   * S. 361), z.B. Bogen Stufe 6 mit Pfeilen Stufe 4 → "(4 + 2)K" = "6K".
   */
  public @NonNull String getArrowDamageText(@NonNull Gear stack) {
    return WeaponDamage.evaluate(data.damage(), Math.min(stack.getRating(), rating));
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
   * Stärke, nach der Schaden und Reichweite berechnet werden: beim Bogen seine Mindeststärke (= Stufe), bei einer
   * Waffe in einem Cyberglied dessen Stärke (S. 389/390), sonst die des Charakters. GRW S. 361: Beim Bogen bestimmt
   * die Mindeststärke Schaden und Reichweite. Der Schaden mit bestimmten Pfeilen steht in
   * {@link #getArrowDamageText(Gear)}.
   */
  private int effectiveStrength(int strength) {
    if (data.hasRating()) {
      return rating;
    }
    return fixedStrength == null ? strength : fixedStrength;
  }
}
