package de.shalorian.chummer.model.character.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** RK-Schreibweise "x (y)" laut GRW S. 357, Werte aus den Tabellen S. 363/366. */
class RecoilTest {

  @Test
  void parsesAllRulebookFormats() {
    assertEquals(Recoil.NONE, Recoil.parse("–"));
    assertEquals(Recoil.NONE, Recoil.parse(""));
    assertEquals(new Recoil(1, 1), Recoil.parse("1"));
    assertEquals(new Recoil(0, 1), Recoil.parse("(1)"));
    assertEquals(new Recoil(2, 3), Recoil.parse("2 (3)"));
    assertEquals(new Recoil(5, 6), Recoil.parse("5 (6)"));
    assertEquals(new Recoil(3, 10), Recoil.parse("3 (10)"));
  }

  @Test
  void displayTextRoundtripsRulebookFormats() {
    for (String text : new String[] {"–", "1", "(1)", "2 (3)", "5 (6)", "3 (10)"}) {
      assertEquals(text, Recoil.parse(text).displayText());
    }
  }

  @Test
  void rejectsUnknownFormats() {
    assertThrows(IllegalArgumentException.class, () -> Recoil.parse("2 (1)"));
    assertThrows(IllegalArgumentException.class, () -> Recoil.parse("x"));
  }

  @Test
  void gasVentAddsToBothValuesOtherAccessoriesOnlyInParentheses() {
    // Gasventil 2 auf "(1)" → "2 (3)" (wie HK MP-5 TX, GRW S. 363)
    assertEquals("2 (3)", Recoil.parse("(1)").plus(2, 0).displayText());
    // Zweibein auf "2 (3)" → "2 (5)" (Nutzerentscheidung 2026-09-30)
    assertEquals("2 (5)", Recoil.parse("2 (3)").plus(0, 2).displayText());
    // Schockpolster ohne Tabellen-RK → "(1)" (wie Ruger 100, GRW S. 366)
    assertEquals("(1)", Recoil.NONE.plus(0, 1).displayText());
  }
}
