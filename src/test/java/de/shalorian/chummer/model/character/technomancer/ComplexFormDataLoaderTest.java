package de.shalorian.chummer.model.character.technomancer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Komplexe Formen = Standard- und Hacking-Programme des GRW (S. 271–273, am Seitenbild geprüft). */
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
  void containsAllRulebookPrograms() {
    Map<String, ComplexFormData> forms = ComplexFormDataLoader.getAllComplexForms();
    assertEquals(7, forms.values().stream().filter(f -> f.category().equals("Standard")).count());
    assertEquals(14, forms.values().stream().filter(f -> f.category().equals("Hacking")).count());
    forms.values().forEach(f -> assertTrue(f.page().matches("27[123]"), f.name()));
    assertEquals("Keine", ComplexFormDataLoader.findByName("Panzerung").skill());
    assertEquals("Keine", ComplexFormDataLoader.findByName("ECCM").skill());
    assertNull(ComplexFormDataLoader.findByName("Biofeedback-Filter"));
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
