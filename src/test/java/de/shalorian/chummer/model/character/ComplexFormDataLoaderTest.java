package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Deckt die initiale, verifizierte Komplexe-Formen-Teilmenge ab (siehe {@code complexforms.xml}-
 * Kommentar und {@code docs/plaene/technomancer_plan.md} Schritt 1) — Kategorien "Standard" und
 * "Hacking", analog zur bewusst unvollständigen Zauber-Teilmenge.
 */
class ComplexFormDataLoaderTest {

  @Test
  void loadsBothExpectedCategories() {
    Map<String, ComplexFormData> forms = ComplexFormDataLoader.getAllComplexForms();
    long standardCount = forms.values().stream().filter(f -> f.category().equals("Standard")).count();
    long hackingCount = forms.values().stream().filter(f -> f.category().equals("Hacking")).count();

    assertTrue(standardCount >= 2, "Standard sollte mindestens 2 Einträge haben");
    assertTrue(hackingCount >= 2, "Hacking sollte mindestens 2 Einträge haben");
  }

  @Test
  void findByNameReturnsKnownComplexForm() {
    ComplexFormData analyse = ComplexFormDataLoader.findByName("Analyse");
    assertEquals("Analyse", analyse.name());
    assertEquals("Standard", analyse.category());
    assertEquals("SR4", analyse.source());
  }

  @Test
  void findByNameReturnsNullForUnknownComplexForm() {
    assertNull(ComplexFormDataLoader.findByName("Keine echte Komplexe Form"));
  }
}
