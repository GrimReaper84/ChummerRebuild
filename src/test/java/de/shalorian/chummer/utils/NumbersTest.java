package de.shalorian.chummer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class NumbersTest {

  @AfterEach
  void resetStyle() {
    Numbers.setStyle(NumberStyle.DEFAULT);
  }

  @Test
  void defaultKeepsFormerFormat() {
    assertEquals("1500", Numbers.integer(1500));
    assertEquals("150000 ¥", Numbers.nuyen(150_000));
    assertEquals("0 ¥", Numbers.nuyen(0));
    assertEquals("−2000", Numbers.integer(-2000));
    assertEquals("5,80", Numbers.essence(5.8));
    assertEquals("0,105", Numbers.essence(0.105));
    assertEquals("1,25", Numbers.decimal(new BigDecimal("1.250")));
    assertEquals("4", Numbers.decimal(new BigDecimal("4.00")));
  }

  @Test
  void groupedThousandsWithComma() {
    Numbers.setStyle(new NumberStyle(true, true));
    assertEquals("1.500", Numbers.integer(1500));
    assertEquals("150.000 ¥", Numbers.nuyen(150_000));
    assertEquals("999", Numbers.integer(999));
    assertEquals("−2.000", Numbers.integer(-2000));
    assertEquals("5,80", Numbers.essence(5.8));
  }

  @Test
  void decimalPointGroupsWithComma() {
    Numbers.setStyle(new NumberStyle(true, false));
    assertEquals("1,500", Numbers.integer(1500));
    assertEquals("5.80", Numbers.essence(5.8));
    assertEquals("0.105", Numbers.essence(0.105));
    assertEquals("1.25", Numbers.decimal(new BigDecimal("1.25")));
  }

  @Test
  void decimalPointWithoutGrouping() {
    Numbers.setStyle(new NumberStyle(false, false));
    assertEquals("1500 ¥", Numbers.nuyen(1500));
    assertEquals("5.80", Numbers.essence(5.8));
  }

  @Test
  void parseIgnoresGroupingOfCurrentStyle() {
    assertEquals(1500, Numbers.parseInteger(" 1500 "));
    Numbers.setStyle(new NumberStyle(true, true));
    assertEquals(1500, Numbers.parseInteger("1.500"));
    Numbers.setStyle(new NumberStyle(true, false));
    assertEquals(1500, Numbers.parseInteger("1,500"));
    assertThrows(NumberFormatException.class, () -> Numbers.parseInteger("abc"));
  }
}
