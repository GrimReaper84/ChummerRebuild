package de.shalorian.chummer.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Einstellungen der Oberfläche auf einem eigenen Test-Knoten, damit echte Einstellungen unberührt bleiben. */
class ViewPreferencesTest {

  private Preferences node;
  private ViewPreferences preferences;

  @BeforeEach
  void setUp() {
    node = Preferences.userRoot().node("chummer-rebuild-test-" + System.nanoTime());
    preferences = new ViewPreferences(node);
  }

  @AfterEach
  void tearDown() throws BackingStoreException {
    node.removeNode();
  }

  @Test
  void returnsDefaultsWhenNothingStored() {
    assertFalse(preferences.getBoolean("character.statusStatic", false));
    assertEquals(0.5, preferences.getDouble("character.statusButtonPosition", 0.5));
  }

  @Test
  void storedValuesAreReadBack() {
    preferences.putBoolean("character.statusStatic", true);
    preferences.putDouble("character.statusButtonPosition", 0.25);

    assertTrue(preferences.getBoolean("character.statusStatic", false));
    assertEquals(0.25, preferences.getDouble("character.statusButtonPosition", 0));
  }
}
