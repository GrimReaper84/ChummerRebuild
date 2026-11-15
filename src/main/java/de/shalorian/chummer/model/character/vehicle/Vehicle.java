package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.gear.MatrixBase;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.gear.MatrixUpgrades;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekauftes Fahrzeug bzw. eine Drohne mit ihrer Ausstattung: Waffenhalterungen samt Waffen,
 * Riggeradapter und Ausrüstung. Mitgelieferte Teile laut Katalog ({@link VehicleData#includedMounts()},
 * {@link VehicleData#includedGear()}) werden beim Anlegen erzeugt, sind im Fahrzeugpreis enthalten
 * und lassen sich nicht entfernen; was in sie eingebaut wird (Waffen in Halterungen, Sensor-Funktionen
 * im Sensorpaket), kostet dagegen extra.
 *
 * <p>Fahrzeugkampf und Würfelpools sind nicht modelliert (SR4-Grundregelwerk S. 202/203: Für
 * Feuerwaffen auf einer Waffenhalterung entfällt der Bewegungsmodifikator, Drohnen greifen mit Pilot +
 * Zielerfassungs-Autosoft an); Fahrzeugwaffen gehören deshalb nicht zu den Waffen des Charakters.
 */
public class Vehicle {

  private static final int CONDITION_MONITOR_BASE = 8;
  private static final int BODY_PER_MOUNT = 3;
  /** Katalogname der Autosofts (S. 287). */
  private static final String AUTOSOFT = "Autosoft";

  private final @NonNull VehicleData data;
  private final List<WeaponMount> mounts = new ArrayList<>();
  private final List<Gear> includedGear = new ArrayList<>();
  private final List<Gear> gear = new ArrayList<>();
  private final List<Gear> matrixUpgrades = new ArrayList<>();
  private boolean riggerAdapterPurchased;
  private int damage;

  /**
   * Neues Fahrzeug mit seiner mitgelieferten Ausstattung.
   *
   * @throws IllegalStateException wenn mitgelieferte Ausrüstung im Ausrüstungskatalog fehlt (Datenfehler)
   */
  public Vehicle(@NonNull VehicleData data) {
    this.data = data;
    for (int i = 0; i < data.includedMounts(); i++) {
      mounts.add(new WeaponMount(true));
    }
    for (IncludedVehicleGear included : data.includedGear()) {
      GearData gearData = GearDataLoader.findByName(included.name());
      if (gearData == null) {
        throw new IllegalStateException(data.name() + ": unbekannte Ausrüstung " + included.name());
      }
      includedGear.add(new Gear(gearData, included.rating(), gearData.packSize(), included.extra(),
          null));
    }
  }

  public @NonNull VehicleData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  // --- Werte ---

  /**
   * Kästchen des Zustandsmonitors. SR4-Grundregelwerk S. 197: 8 plus halber Rumpf (aufgerundet);
   * GRW-Beispiel: Rumpf 3 ergibt 10 Kästchen.
   */
  public int getConditionMonitor() {
    return CONDITION_MONITOR_BASE + (data.body() + 1) / 2;
  }

  /**
   * Eingetragener Schaden in Kästchen (S. 197: nur körperlich). Fahrzeuge haben keinen Verletzungsmodifikator
   * (Nutzerentscheidung 2026-11-15, das GRW nennt keinen).
   */
  public int getDamage() {
    return damage;
  }

  /** Setzt den Schaden, begrenzt auf 0 bis zum vollen Zustandsmonitor. */
  public void setDamage(int damage) {
    this.damage = Math.clamp(damage, 0, getConditionMonitor());
  }

  /** Ob der Zustandsmonitor voll ist (Fahrzeug zerstört bzw. außer Gefecht). */
  public boolean isDestroyed() {
    return damage >= getConditionMonitor();
  }

  /** Gerätestufe nach Fahrzeugart (Tabelle Gerätestufen, GRW S. 261, siehe {@link VehicleCategory#deviceRating()}). */
  public int getDeviceRating() {
    return data.category().deviceRating();
  }

  /**
   * Durchschnittliche Stufe der im Sensorpaket eingebauten Sensoren, oder leer, solange keine
   * eingebaut sind (dann gilt die Sensorstufe laut Katalog). SR4-Grundregelwerk S. 380: Die Stufe des
   * Sensorpakets ist der aufgerundete Durchschnitt seiner Sensoren; Sensoren ohne eigene Stufe zählen mit
   * der Stufe des Pakets. Als Stufe des Pakets gilt die Sensorstufe laut
   * Katalog.
   */
  public @NonNull OptionalInt getAverageSensorRating() {
    Gear pack = getSensorPackage();
    if (pack == null || pack.getInstalled().isEmpty()) {
      return OptionalInt.empty();
    }
    List<Gear> sensors = pack.getInstalled();
    int sum = sensors.stream()
        .mapToInt(sensor -> sensor.getData().hasRating() ? sensor.getRating() : data.sensor())
        .sum();
    return OptionalInt.of((sum + sensors.size() - 1) / sensors.size());
  }

  /**
   * Höchste Stufe der Autosofts dieser Art im Fahrzeug (mitgeliefert oder gekauft), z.B. „Zielerfassung“; 0 ohne
   * (S. 287).
   */
  public int getAutosoftRating(@NonNull String type) {
    return Stream.concat(includedGear.stream(), gear.stream())
        .filter(g -> g.getName().equals(AUTOSOFT) && g.getExtra().equals(type))
        .mapToInt(Gear::getRating)
        .max()
        .orElse(0);
  }

  /** Das mitgelieferte Sensorpaket, oder {@code null}, falls der Katalog keines vorsieht. */
  public @Nullable Gear getSensorPackage() {
    return includedGear.stream()
        .filter(g -> g.getData().category() == GearCategory.SENSOR_PACKAGES)
        .findFirst().orElse(null);
  }

  // --- Riggeradapter ---

  /**
   * Ob das Fahrzeug einen Riggeradapter hat. SR4-Grundregelwerk S. 398: Drohnen haben ihn immer;
   * S. 284: Passagierfahrzeuge in der Regel nicht, er lässt sich aber nachrüsten.
   */
  public boolean hasRiggerAdapter() {
    return isRiggerAdapterIncluded() || riggerAdapterPurchased;
  }

  /** Ob der Riggeradapter mitgeliefert ist (bei allen Drohnen). */
  public boolean isRiggerAdapterIncluded() {
    return data.category().isDrone();
  }

  /** Ob ein Riggeradapter nachgerüstet wurde. */
  public boolean isRiggerAdapterPurchased() {
    return riggerAdapterPurchased;
  }

  /** Warum kein Riggeradapter eingebaut werden kann, oder {@code null}. */
  public @Nullable String riggerAdapterProblem() {
    if (isRiggerAdapterIncluded()) {
      return "Drohnen sind bereits mit einem Riggeradapter ausgestattet (GRW S. 398)";
    }
    if (riggerAdapterPurchased) {
      return getName() + " hat bereits einen Riggeradapter";
    }
    return null;
  }

  /**
   * Baut einen Riggeradapter ein.
   *
   * @throws IllegalStateException wenn {@link #riggerAdapterProblem()} einen Grund liefert
   */
  public void installRiggerAdapter() {
    String problem = riggerAdapterProblem();
    if (problem != null) {
      throw new IllegalStateException(problem);
    }
    riggerAdapterPurchased = true;
  }

  /** Baut einen nachgerüsteten Riggeradapter aus (ein mitgelieferter bleibt). */
  public void removeRiggerAdapter() {
    riggerAdapterPurchased = false;
  }

  // --- Matrix (GRW S. 260/261) ---

  /** Eingebaute Hardware-Module und Softwarepakete. */
  public @NonNull List<Gear> getMatrixUpgrades() {
    return Collections.unmodifiableList(matrixUpgrades);
  }

  /** Matrixwerte: alle Attribute = Gerätestufe (S. 261), ersetzt durch eingebaute Module und Pakete (S. 260). */
  public @NonNull MatrixStats getMatrixStats() {
    return MatrixStats.of(MatrixBase.ofDeviceRating(getDeviceRating()), matrixUpgrades);
  }

  /** Warum {@code upgrade} in Stufe {@code rating} nicht eingebaut werden kann, oder {@code null}. */
  public @Nullable String matrixUpgradeProblem(@NonNull GearData upgrade, int rating) {
    if (upgrade.matrixAttribute() == null) {
      return upgrade.name() + " ist kein Hardware-Modul oder Softwarepaket";
    }
    return MatrixUpgrades.problem(upgrade, rating, MatrixBase.ofDeviceRating(getDeviceRating()), matrixUpgrades);
  }

  /**
   * Baut ein Modul bzw. Paket ein.
   *
   * @throws IllegalArgumentException wenn {@link #matrixUpgradeProblem} einen Grund liefert
   */
  public void addMatrixUpgrade(@NonNull Gear upgrade) {
    String problem = matrixUpgradeProblem(upgrade.getData(), upgrade.getRating());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    matrixUpgrades.add(upgrade);
  }

  /** Baut genau dieses Modul bzw. Paket aus. */
  public void removeMatrixUpgrade(@NonNull Gear upgrade) {
    matrixUpgrades.remove(upgrade);
  }

  // --- Waffenhalterungen ---

  /** Alle Halterungen, mitgelieferte zuerst. */
  public @NonNull List<WeaponMount> getMounts() {
    return Collections.unmodifiableList(mounts);
  }

  /**
   * Höchstzahl der Waffenhalterungen. SR4-Grundregelwerk S. 395: Rumpf ÷ 3, abgerundet.
   * Mitgelieferte Halterungen zählen mit (Nutzerentscheidung 2026-10-09).
   */
  public int getMaxMounts() {
    return data.body() / BODY_PER_MOUNT;
  }

  /** Warum keine weitere Halterung eingebaut werden kann, oder {@code null}. */
  public @Nullable String mountProblem() {
    if (mounts.size() >= getMaxMounts()) {
      return getName() + " hat Rumpf " + data.body() + " und kann daher höchstens " + getMaxMounts()
          + " Waffenhalterung(en) tragen (Rumpf ÷ 3, GRW S. 395)";
    }
    return null;
  }

  /**
   * Baut eine Waffenhalterung ein.
   *
   * @throws IllegalStateException wenn {@link #mountProblem()} einen Grund liefert
   */
  public @NonNull WeaponMount addMount() {
    String problem = mountProblem();
    if (problem != null) {
      throw new IllegalStateException(problem);
    }
    WeaponMount mount = new WeaponMount(false);
    mounts.add(mount);
    return mount;
  }

  /**
   * Baut eine nachgerüstete Halterung samt Waffe aus.
   *
   * @throws IllegalArgumentException bei einer mitgelieferten Halterung
   */
  public void removeMount(@NonNull WeaponMount mount) {
    if (mount.isIncluded()) {
      throw new IllegalArgumentException("Mitgelieferte Halterungen lassen sich nicht ausbauen");
    }
    mounts.remove(mount);
  }

  /**
   * Warum {@code weapon} nicht in {@code mount} eingesetzt werden kann, oder {@code null}: Die Halterung
   * muss leer sein und die Waffe höchstens ein LMG (GRW S. 395, siehe {@link
   * de.shalorian.chummer.model.character.weapon.WeaponCategory#fitsVehicleMount()}).
   */
  public @Nullable String weaponProblem(@NonNull WeaponMount mount, @NonNull WeaponData weapon) {
    if (mount.getWeapon() != null) {
      return "Die Waffenhalterung ist bereits belegt";
    }
    if (!weapon.category().fitsVehicleMount()) {
      return weapon.name() + " passt nicht in eine Waffenhalterung (nur ein LMG oder eine kleinere"
          + " Waffe, GRW S. 395)";
    }
    return null;
  }

  /**
   * Setzt {@code weapon} in {@code mount} ein.
   *
   * @throws IllegalArgumentException wenn {@link #weaponProblem(WeaponMount, WeaponData)} einen Grund
   *                                  liefert oder die Halterung nicht zu diesem Fahrzeug gehört
   */
  public void mountWeapon(@NonNull WeaponMount mount, @NonNull Weapon weapon) {
    if (!mounts.contains(mount)) {
      throw new IllegalArgumentException("Halterung gehört nicht zu " + getName());
    }
    String problem = weaponProblem(mount, weapon.getData());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    mount.setWeapon(weapon);
  }

  /** Nimmt die Waffe aus {@code mount}. */
  public void unmountWeapon(@NonNull WeaponMount mount) {
    mount.setWeapon(null);
  }

  /** Die erste leere Halterung, oder {@code null}. */
  public @Nullable WeaponMount firstFreeMount() {
    return mounts.stream().filter(m -> m.getWeapon() == null).findFirst().orElse(null);
  }

  // --- Ausrüstung ---

  /** Mitgelieferte Ausrüstung (Sensorpaket, Autosofts, Medkit) in Katalogreihenfolge. */
  public @NonNull List<Gear> getIncludedGear() {
    return Collections.unmodifiableList(includedGear);
  }

  /** Gekaufte Ausrüstung in Kaufreihenfolge. */
  public @NonNull List<Gear> getGear() {
    return Collections.unmodifiableList(gear);
  }

  /** Ob {@code item} mitgeliefert ist (Objektreferenz). */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: gleiche Geräte sind nicht dasselbe Stück
  public boolean isIncluded(@NonNull Gear item) {
    return includedGear.stream().anyMatch(g -> g == item);
  }

  /**
   * Legt einen gekauften Gegenstand ins Fahrzeug; gleiche Gegenstände werden zusammengeführt wie
   * beim Charakter ({@link Gear#addOrMerge(List, Gear)}).
   *
   * @return der Gegenstand, der die Menge jetzt enthält
   */
  public @NonNull Gear addGear(@NonNull Gear item) {
    return Gear.addOrMerge(gear, item);
  }

  /**
   * Entfernt einen gekauften Gegenstand samt Einbauten bzw. baut einen Einbau aus seinem Gerät aus
   * (auch aus mitgelieferten Geräten wie dem Sensorpaket).
   *
   * @throws IllegalArgumentException bei mitgelieferter Ausrüstung
   */
  public void removeGear(@NonNull Gear item) {
    if (isIncluded(item)) {
      throw new IllegalArgumentException(item.getName() + " ist im Fahrzeug enthalten");
    }
    if (!gear.remove(item)) {
      gear.forEach(device -> device.uninstall(item));
      includedGear.forEach(device -> device.uninstall(item));
    }
  }

  /**
   * Mitgelieferte Ausstattung als Anzeigetexte, z.B. "Riggeradapter", "Waffenhalterung (Geschützturm)",
   * "Sensorpaket (Große Drohne)", "Autosoft (Stufe 3, Abwehr)".
   */
  public @NonNull List<String> describeIncludedEquipment() {
    List<String> parts = new ArrayList<>();
    if (isRiggerAdapterIncluded()) {
      parts.add(VehicleModification.RIGGER_ADAPTER.getDisplayName());
    }
    if (data.includedMounts() > 0) {
      String mount = VehicleModification.WEAPON_MOUNT.getDisplayName()
          + (data.mountNote().isEmpty() ? "" : " (" + data.mountNote() + ")");
      parts.add(data.includedMounts() == 1 ? mount : data.includedMounts() + "× " + mount);
    }
    includedGear.forEach(g -> parts.add(g.getDisplayName()));
    return parts;
  }

  // --- Kosten ---

  /**
   * Gesamtpreis in Nuyen: Katalogpreis (enthält die mitgelieferte Ausstattung) + nachgerüstete
   * Riggeradapter und Halterungen (GRW S. 396) + Waffen in allen Halterungen + gekaufte Ausrüstung +
   * Einbauten in mitgelieferter Ausrüstung (z.B. Sensor-Funktionen im Sensorpaket).
   */
  public int getNuyenCost() {
    int cost = data.cost();
    if (riggerAdapterPurchased) {
      cost += VehicleModification.RIGGER_ADAPTER.cost();
    }
    for (WeaponMount mount : mounts) {
      if (!mount.isIncluded()) {
        cost += VehicleModification.WEAPON_MOUNT.cost();
      }
      Weapon weapon = mount.getWeapon();
      if (weapon != null) {
        cost += weapon.getTotalNuyenCost();
      }
    }
    cost += gear.stream().mapToInt(Gear::getNuyenCost).sum();
    cost += matrixUpgrades.stream().mapToInt(Gear::getNuyenCost).sum();
    cost += includedGear.stream()
        .flatMap(device -> device.getInstalled().stream())
        .mapToInt(Gear::getNuyenCost)
        .sum();
    return cost;
  }

  /** Alle eingesetzten Waffen in Halterungsreihenfolge. */
  public @NonNull List<Weapon> getMountedWeapons() {
    return mounts.stream().map(WeaponMount::getWeapon).filter(Objects::nonNull).toList();
  }
}
