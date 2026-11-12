package de.shalorian.chummer.view.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Position des schwebenden Status-Knopfs nach dem Ziehen (0 unten, 1 oben). */
class FloatingButtonPositionTest {

  @Test
  void positionFollowsTheDragAndStaysInRange() {
    assertEquals(0.5, FloatingButtonPosition.afterDrag(0.25, -100, 400), 1e-9);
    assertEquals(1.0, FloatingButtonPosition.afterDrag(0.9, -400, 400), 1e-9);
    assertEquals(0.0, FloatingButtonPosition.afterDrag(0.1, 400, 400), 1e-9);
  }

  @Test
  void buttonWithoutRoomKeepsItsPosition() {
    assertEquals(0.3, FloatingButtonPosition.afterDrag(0.3, 50, 0), 1e-9);
  }
}
