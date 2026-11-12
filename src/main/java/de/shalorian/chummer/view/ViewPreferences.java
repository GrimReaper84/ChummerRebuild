package de.shalorian.chummer.view;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.jspecify.annotations.NonNull;

/**
 * Einstellungen der Oberfläche, die Neuaufbau und Neustart überdauern, z.B. ob der Status statisch angezeigt wird. Sie
 * gelten für alle Charaktere und liegen in den Java-Preferences des Benutzers, nie in der Charakterdatei. Ein
 * defektes Preferences-Backend kostet nur diese Einstellungen: Lesen liefert die Standardwerte, Schreiben entfällt
 * (übernommen aus dem Fallout2d20-Projekt).
 */
public final class ViewPreferences {

  private final @NonNull Preferences node;

  ViewPreferences(@NonNull Preferences node) {
    this.node = node;
  }

  /** Die Einstellungen des aktuellen Benutzers; der Knoten wird erst beim Zugriff angelegt. */
  public static @NonNull ViewPreferences user() {
    return new ViewPreferences(Preferences.userNodeForPackage(ViewPreferences.class));
  }

  public boolean getBoolean(@NonNull String key, boolean defaultValue) {
    try {
      return node.getBoolean(key, defaultValue);
    } catch (IllegalStateException | SecurityException _) {
      return defaultValue;
    }
  }

  public void putBoolean(@NonNull String key, boolean value) {
    try {
      node.putBoolean(key, value);
      node.flush();
    } catch (BackingStoreException | IllegalStateException | SecurityException _) {
      // nur Oberflächenzustand; ihn zu verlieren schadet nicht
    }
  }

  public double getDouble(@NonNull String key, double defaultValue) {
    try {
      return node.getDouble(key, defaultValue);
    } catch (IllegalStateException | SecurityException _) {
      return defaultValue;
    }
  }

  public void putDouble(@NonNull String key, double value) {
    try {
      node.putDouble(key, value);
      node.flush();
    } catch (BackingStoreException | IllegalStateException | SecurityException _) {
      // nur Oberflächenzustand; ihn zu verlieren schadet nicht
    }
  }
}
