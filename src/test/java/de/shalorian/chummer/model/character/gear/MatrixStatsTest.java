package de.shalorian.chummer.model.character.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Wirksame Matrixwerte mit Aufrüstungen und Prozessorlimit (GRW S. 260/261, 271). */
class MatrixStatsTest {

  private static GearData find(String name) {
    return Objects.requireNonNull(GearDataLoader.findByName(name), name);
  }

  private static List<Gear> runningPrograms(int count) {
    List<Gear> programs = new ArrayList<>();
    String[] names = {"Analyse", "Befehl", "Editieren", "Realitätsfilter", "Scannen", "Schmöker",
        "Verschlüsselung", "Angriff", "Aufspüren", "Ausnutzen", "Blackout"};
    for (int i = 0; i < count; i++) {
      Gear program = new Gear(find(names[i]), 1);
      program.setRunning(true);
      programs.add(program);
    }
    return programs;
  }

  @Test
  void processorDropsByOnePerFullLimitOfRunningPrograms() {
    // S. 261: Prozessorlimit 5 → je 5 laufende Programme −1
    MatrixBase base = new MatrixBase(5, 3, 3, 5);
    assertEquals(5, MatrixStats.of(base, runningPrograms(4)).processor());
    assertEquals(4, MatrixStats.of(base, runningPrograms(5)).processor());
    assertEquals(4, MatrixStats.of(base, runningPrograms(9)).processor());
    assertEquals(3, MatrixStats.of(base, runningPrograms(10)).processor());
    assertEquals(5, MatrixStats.of(base, runningPrograms(10)).processorLimit());
  }

  @Test
  void processorNeverBelowZero() {
    MatrixStats stats = MatrixStats.of(new MatrixBase(1, 1, 1, 1), runningPrograms(11));
    assertEquals(0, stats.processor());
    assertEquals(1, stats.processorPenalty());
  }

  @Test
  void loadedSkillsoftsAreNoRunningPrograms() {
    // Talentsofts im Kommlink zählen nicht zum Prozessorlimit, nur Programme (S. 261, 377)
    List<Gear> installed = new ArrayList<>(runningPrograms(4));
    Gear soft = new Gear(find("Linguasoft"), 2, 1, "Japanisch", null);
    soft.setRunning(true);
    installed.add(soft);
    assertEquals(5, MatrixStats.of(new MatrixBase(5, 3, 3, 5), installed).processor());
  }

  @Test
  void stoppedProgramsDoNotCount() {
    List<Gear> programs = runningPrograms(5);
    programs.getFirst().setRunning(false);
    assertEquals(5, MatrixStats.of(new MatrixBase(5, 3, 3, 5), programs).processor());
  }

  @Test
  void systemCappedByUpgradedProcessorButNotByPenalty() {
    // Prozessor 3 + Modul 5, System 5 → System 5; die Senkung durch laufende Programme ändert das nicht (S. 271)
    List<Gear> installed = new ArrayList<>(runningPrograms(5));
    installed.add(new Gear(find("Prozessor-Modul"), 5));
    MatrixStats stats = MatrixStats.of(new MatrixBase(3, 3, 3, 5), installed);
    assertEquals(5, stats.hardwareProcessor());
    assertEquals(4, stats.processor());
    assertEquals(5, stats.system());
    assertFalse(stats.isSystemCapped());
  }

  @Test
  void upgradesReplaceTheOriginalValues() {
    List<Gear> installed = List.of(new Gear(find("Signal-Modul"), 4), new Gear(find("Firewall-Paket"), 5),
        new Gear(find("System-Paket"), 4));
    MatrixStats stats = MatrixStats.of(new MatrixBase(4, 2, 2, 2), installed);
    assertEquals(4, stats.signal());
    assertEquals(5, stats.firewall());
    assertEquals(4, stats.system());
  }

  @Test
  void programRunsWithAtMostSystem() {
    MatrixStats stats = MatrixStats.of(new MatrixBase(4, 3, 3, 4), List.of());
    assertEquals(4, stats.programRating(6));
    assertEquals(3, stats.programRating(3));
  }
}
