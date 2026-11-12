package de.shalorian.chummer.model.character.adept;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import org.jspecify.annotations.NonNull;

/**
 * Eine gekaufte Adeptenkraft. Stufe und Auswahl prüft {@code Character} (Kraftpunkte, Höchststufe), diese Klasse
 * hält nur den Zustand. Kräfte ohne Stufen haben immer Stufe 1.
 */
public class AdeptPower {

  private final @NonNull AdeptPowerData data;
  private final @NonNull String choice;
  private int level = 1;

  /**
   * @param choice gewählte Fertigkeit, Attribut ({@link AttributeType}-Name) oder Freitext laut {@link
   *               AdeptPowerData#selection()}, sonst leer
   */
  public AdeptPower(@NonNull AdeptPowerData data, @NonNull String choice) {
    this.data = data;
    this.choice = choice;
  }

  public @NonNull AdeptPowerData getData() {
    return data;
  }

  public @NonNull String getChoice() {
    return choice;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    if (level < 1 || (!data.leveled() && level != 1)) {
      throw new IllegalArgumentException("%s: Stufe %d ungültig".formatted(data.name(), level));
    }
    this.level = level;
  }

  /** Name mit Auswahl, z.B. "Verbesserte Fertigkeit (Pistolen)" oder "Gesteigertes körperliches Attribut (Stärke)". */
  public @NonNull String displayName() {
    if (choice.isEmpty()) {
      return data.name();
    }
    String shown = data.selection() == QualitySelection.ATTRIBUTE
        ? AttributeType.valueOf(choice).getDisplayName()
        : choice;
    return "%s (%s)".formatted(data.name(), shown);
  }
}
