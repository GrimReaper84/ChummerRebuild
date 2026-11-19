package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

/**
 * Würfelpools eines Fahrzeugs für die üblichen Proben, nach der Tabelle „Häufige Rigger-/Drohnenproben“ (GRW S. 287,
 * am Bild geprüft), plus die Fahrzeugprobe eines Fahrers (S. 198).
 *
 * <ul>
 *   <li><b>Hineingesprungen</b> (S. 286/287): Fertigkeiten des Riggers mit den Werten des Fahrzeugs; Prozessor tritt an
 *       die Stelle von Geschicklichkeit und Reaktion, Sensor an die der Intuition. Braucht einen Riggeradapter
 *       (S. 396). Eine Riggerkontrolle gibt +2 auf Proben mit Fahrzeugfertigkeit, Geschütze eingeschlossen (S. 385,
 *       Plan-Annahme A5).</li>
 *   <li><b>Autonom</b>: Pilot mit der passenden Autosoft; nur Drohnen (Annahme A4). Angreifen nur mit Zielerfassung
 *       (S. 202); fehlt eine andere Autosoft, würfelt der Pilot allein.</li>
 *   <li><b>Fernsteuerung</b>: Stufe des laufenden Programms Befehl auf dem aktiven Kommlink (höchstens System, S. 271)
 *       plus Fertigkeit des Riggers (Annahme A7).</li>
 *   <li>Schadenswiderstand überall Rumpf + Panzerung. Initiative hineingesprungen und ferngesteuert wie der Rigger
 *       (seine Matrixinitiative), autonom Pilot + Prozessor.</li>
 *   <li>Der Verletzungsmodifikator des Riggers gilt hineingesprungen und ferngesteuert, nicht autonom (Annahme A6);
 *       Fahrzeuge selbst haben keinen (Nutzerentscheidung 2026-11-15).</li>
 * </ul>
 */
public final class VehiclePools {

  /** Riggerkontrolle hineingesprungen (S. 385). */
  private static final int CONTROL_RIG_BONUS = 2;

  private static final String CONTROL_RIG = "Riggerkontrolle";
  private static final String COMMAND = "Befehl";
  private static final String GUNNERY = "Geschütze";
  private static final String DODGE = "Ausweichen";
  private static final String INFILTRATION = "Infiltration";
  private static final String PERCEPTION = "Wahrnehmung";
  private static final List<String> MELEE_SKILLS = List.of("Waffenloser Kampf", "Klingenwaffen", "Knüppel");
  private static final String PROCESSOR = "Prozessor";
  private static final String SENSOR = "Sensor";
  private static final String PILOT = "Pilot";

  private final @NonNull Character rigger;
  private final @NonNull Vehicle vehicle;
  private final int processor;
  private final int sensor;
  private final int pilot;
  private final int command;

  private VehiclePools(@NonNull Character rigger, @NonNull Vehicle vehicle) {
    this.rigger = rigger;
    this.vehicle = vehicle;
    this.processor = vehicle.getMatrixStats().processor();
    this.sensor = vehicle.getData().sensor();
    this.pilot = vehicle.getData().pilot();
    this.command = commandRating(rigger);
  }

  /** Die Zeilen der Tabelle S. 287 in ihrer Reihenfolge. */
  public static @NonNull List<VehiclePoolRow> of(@NonNull Character rigger, @NonNull Vehicle vehicle) {
    return new VehiclePools(rigger, vehicle).rows();
  }

  /**
   * Fahrzeugprobe eines Fahrers (S. 198): Fahrzeugfertigkeit + Reaktion ± Handling, mit Verletzungsmodifikator; leer,
   * wenn die Fertigkeit ohne Wert nicht genutzt werden kann.
   */
  public static @NonNull Optional<DicePool> driverPool(@NonNull Character driver, @NonNull Vehicle vehicle) {
    Skill skill = driver.getSkill(vehicle.getData().skill());
    if (skill == null) {
      return Optional.empty();
    }
    int handling = vehicle.getData().handling();
    return driver.getSkillDicePool(skill).map(p -> new DicePool(Math.max(0, p.dice() + handling),
        p.breakdown() + (handling == 0 ? "" : (handling > 0 ? " + " : " − ") + "Handling " + Math.abs(handling))));
  }

  private @NonNull List<VehiclePoolRow> rows() {
    String vehicleSkill = vehicle.getData().skill();
    String melee = bestMeleeSkill();
    List<VehiclePoolRow> rows = new ArrayList<>();
    rows.add(row("Initiative", adapter(riggerInitiative()),
        VehiclePoolCell.of(new DicePool(pilot + processor, "%s %d + %s %d".formatted(PILOT, pilot, PROCESSOR,
            processor))), riggerInitiative()));
    rows.add(row("Angriff", jumped(SENSOR, sensor, GUNNERY, true),
        autonomousAttack(), remote(GUNNERY)));
    rows.add(row("Nahkampfverteidigung", jumped(PROCESSOR, processor, melee, false),
        withAutosoft(PILOT, pilot, "Abwehr"), remote(melee)));
    rows.add(row("Fernkampfverteidigung", jumpedAttribute(), plain(PROCESSOR, processor), commandOnly()));
    rows.add(row("Volle Abwehr", jumped(PROCESSOR, processor, DODGE, false),
        withAutosoft(PROCESSOR, processor, "Abwehr"), remote(DODGE)));
    VehiclePoolCell resistance = VehiclePoolCell.of(new DicePool(vehicle.getData().body() + vehicle.getData().armor(),
        "Rumpf %d + Panzerung %d".formatted(vehicle.getData().body(), vehicle.getData().armor())));
    rows.add(row("Schadenswiderstand", resistance, resistance, resistance));
    rows.add(row("Infiltration", jumped(PROCESSOR, processor, INFILTRATION, false),
        withAutosoft(PILOT, pilot, "Verdeckte Operation"), remote(INFILTRATION)));
    rows.add(row("Manövrieren", jumped(PROCESSOR, processor, vehicleSkill, true),
        withAutosoft(PILOT, pilot, "Steuerung"), remote(vehicleSkill)));
    rows.add(row("Wahrnehmung", jumped(SENSOR, sensor, PERCEPTION, false),
        withAutosoft(SENSOR, sensor, "Clearsight"),
        withSkill(SENSOR, sensor, PERCEPTION, 0)));
    return rows;
  }

  private @NonNull VehiclePoolRow row(@NonNull String test, @NonNull VehiclePoolCell jumpedIn,
      @NonNull VehiclePoolCell autonomous, @NonNull VehiclePoolCell remote) {
    return new VehiclePoolRow(test, jumpedIn,
        vehicle.getData().category().isDrone() ? autonomous : VehiclePoolCell.none("nur Drohnen handeln autonom"),
        remote);
  }

  // --- hineingesprungen ---

  private @NonNull VehiclePoolCell jumped(@NonNull String attribute, int value, @NonNull String skill,
      boolean vehicleSkill) {
    int bonus = vehicleSkill && hasControlRig() ? CONTROL_RIG_BONUS : 0;
    return adapter(withSkill(attribute, value, skill, bonus));
  }

  private @NonNull VehiclePoolCell jumpedAttribute() {
    return adapter(VehiclePoolCell.of(rigger.getWoundedPool(processor, PROCESSOR + " " + processor)));
  }

  /** Hineinspringen nur mit Riggeradapter (S. 396). */
  private @NonNull VehiclePoolCell adapter(@NonNull VehiclePoolCell cell) {
    return vehicle.hasRiggerAdapter() ? cell : VehiclePoolCell.none("kein Riggeradapter (GRW S. 396)");
  }

  private @NonNull VehiclePoolCell riggerInitiative() {
    return VehiclePoolCell.of(new DicePool(rigger.getMatrixInitiative(), "Matrixinitiative des Riggers"));
  }

  // --- autonom ---

  private @NonNull VehiclePoolCell autonomousAttack() {
    return vehicle.getAutosoftRating("Zielerfassung") == 0
        ? VehiclePoolCell.none("keine Zielerfassung-Autosoft (GRW S. 202)")
        : withAutosoft(PILOT, pilot, "Zielerfassung");
  }

  /** Wert der Drohne + Stufe der Autosoft dieser Art; ohne sie würfelt der Wert allein (Plan-Annahme). */
  private @NonNull VehiclePoolCell withAutosoft(@NonNull String name, int value, @NonNull String type) {
    int rating = vehicle.getAutosoftRating(type);
    return VehiclePoolCell.of(new DicePool(value + rating, rating == 0
        ? "%s %d (ohne Autosoft %s)".formatted(name, value, type)
        : "%s %d + %s %d".formatted(name, value, type, rating)));
  }

  private static @NonNull VehiclePoolCell plain(@NonNull String name, int value) {
    return VehiclePoolCell.of(new DicePool(value, name + " " + value));
  }

  // --- Fernsteuerung ---

  private @NonNull VehiclePoolCell remote(@NonNull String skill) {
    if (command == 0) {
      return VehiclePoolCell.none("kein laufendes Programm Befehl auf dem aktiven Kommlink");
    }
    return withSkill(COMMAND, command, skill, 0);
  }

  private @NonNull VehiclePoolCell commandOnly() {
    return command == 0 ? VehiclePoolCell.none("kein laufendes Programm Befehl auf dem aktiven Kommlink")
        : VehiclePoolCell.of(rigger.getWoundedPool(command, COMMAND + " " + command));
  }

  /** Stufe des laufenden Programms Befehl auf dem aktiven Kommlink, höchstens System (S. 271); 0 ohne. */
  private static int commandRating(@NonNull Character rigger) {
    Gear commlink = rigger.getEquipment().getActiveCommlink();
    MatrixStats stats = commlink == null ? null : commlink.getMatrixStats();
    if (commlink == null || stats == null) {
      return 0;
    }
    return commlink.getInstalled().stream()
        .filter(g -> g.getName().equals(COMMAND) && g.isRunning())
        .mapToInt(g -> stats.programRating(g.getRating()))
        .max()
        .orElse(0);
  }

  // --- gemeinsam ---

  /**
   * Wert + Fertigkeit des Riggers über {@link Character#getSkillDicePool(Skill, String, int)} (gleiche Sperren wie bei
   * Fertigkeitsproben), plus {@code bonus} der Riggerkontrolle.
   */
  private @NonNull VehiclePoolCell withSkill(@NonNull String attribute, int value, @NonNull String skillName,
      int bonus) {
    Skill skill = rigger.getSkill(skillName);
    Optional<DicePool> pool = skill == null ? Optional.empty() : rigger.getSkillDicePool(skill, attribute, value);
    if (pool.isEmpty()) {
      return VehiclePoolCell.none(skillName + ": keine Probe möglich");
    }
    DicePool base = pool.get();
    return VehiclePoolCell.of(bonus == 0 ? base
        : new DicePool(base.dice() + bonus, base.breakdown() + " + Riggerkontrolle " + bonus));
  }

  private boolean hasControlRig() {
    return rigger.getEquipment().getCyberware().stream().anyMatch(c -> c.getData().name().equals(CONTROL_RIG));
  }

  /** Höchste Nahkampffertigkeit des Riggers (Tabelle nennt nur „Nahkampffertigkeit“, Plan-Annahme A10). */
  private @NonNull String bestMeleeSkill() {
    return MELEE_SKILLS.stream()
        .map(rigger::getSkill)
        .flatMap(s -> s == null ? Stream.empty() : Stream.of(s))
        .max(Comparator.comparingInt(Skill::getRating))
        .map(Skill::getName)
        .orElse(MELEE_SKILLS.getFirst());
  }
}
