package de.shalorian.chummer.view.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Breite des Slide-Panels (ohne JavaFX-Laufzeit). */
class SlidePanelTest {

  @Test
  void panelTakesAThirdOfTheHost() {
    assertEquals(400, SlidePanel.width(1200, 100), 1e-9);
  }

  @Test
  void panelIsAtLeastAsWideAsItsContent() {
    assertEquals(440, SlidePanel.width(900, 400), 1e-9);
  }

  @Test
  void panelNeverExceedsTheHost() {
    assertEquals(300, SlidePanel.width(300, 400), 1e-9);
  }
}
