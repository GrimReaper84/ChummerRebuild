package de.shalorian.chummer.model.character.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Aktive Regelwerke eines Charakters (ADR-061). */
class RuleOptionsTest {

  private static Sourced from(String code) {
    return () -> code;
  }

  @Test
  void defaultIsCoreBookOnly() {
    RuleOptions options = new RuleOptions();
    assertTrue(options.isBookEnabled("SR4"));
    assertTrue(options.isBookEnabled(""));
    assertFalse(options.isBookEnabled("AR"));
    assertEquals(Set.of(), options.getExtraBooks());
  }

  @Test
  void coreBookCannotBeDisabled() {
    RuleOptions options = new RuleOptions();
    options.setBookEnabled("SR4", false);
    assertTrue(options.isBookEnabled("SR4"));
    assertEquals(Set.of(), options.getExtraBooks());
  }

  @Test
  void extraBookCanBeToggled() {
    RuleOptions options = new RuleOptions();
    options.setBookEnabled("AR", true);
    assertTrue(options.isBookEnabled("AR"));
    assertTrue(options.allows(from("AR")));
    options.setBookEnabled("AR", false);
    assertFalse(options.allows(from("AR")));
  }

  @Test
  void entriesWithoutSourceAreAlwaysAllowed() {
    RuleOptions options = new RuleOptions();
    assertTrue(options.allows("ohne Quelle"));
    assertTrue(options.allows(from("SR4")));
    assertTrue(options.allows(from(" ")));
    assertTrue(options.allows(Objects.requireNonNull(QualityDataLoader.findByName("Adept"))));
  }

  @Test
  void copyFromTakesAllSettings() {
    RuleOptions source = new RuleOptions();
    source.setLimbArmorAverage(true);
    source.setLateAwakening(true);
    source.setMissedLifestylePayments(true);
    source.setDecoupleAtCreation(true);
    source.setRemoveIncludedParts(true);
    source.setBookEnabled("AR", true);
    RuleOptions target = new RuleOptions();
    target.setBookEnabled("XYZ", true);

    target.copyFrom(source);
    assertTrue(target.isLimbArmorAverage());
    assertTrue(target.isLateAwakening());
    assertTrue(target.isMissedLifestylePayments());
    assertTrue(target.isDecoupleAtCreation());
    assertTrue(target.isRemoveIncludedParts());
    assertEquals(Set.of("AR"), target.getExtraBooks());
  }
}
