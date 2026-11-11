package de.shalorian.chummer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppInfoTest {

  /**
   * Der Build schreibt die Gradle-Version in eine Ressource; eine Release-Version ist numerisch (MSI verlangt das).
   * Scheitert bewusst, wenn der Test ohne Gradle läuft (IDE ohne Gradle-Delegation): Dann fehlt die Ressource und
   * {@link AppInfo#version()} liefert „dev“.
   */
  @Test
  void versionComesFromTheBuild() {
    String version = AppInfo.version();
    assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"), version);
  }
}
