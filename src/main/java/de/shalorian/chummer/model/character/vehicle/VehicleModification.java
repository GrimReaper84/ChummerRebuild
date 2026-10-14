package de.shalorian.chummer.model.character.vehicle;

import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Legality;
import org.jspecify.annotations.NonNull;

/**
 * Fahrzeugmodifikationen laut Tabelle "Fahrzeugmodifikationen" des SR4-Grundregelwerks (S. 396, am
 * PDF-Bild geprüft). Das GRW kennt genau diese zwei; alle weiteren Mods des C#-Originals stammen aus
 * Zusatzbänden (v.a. <i>Arsenal</i>). Abweichend vom C#-Original kostet die Waffenhalterung 2.500 ¥
 * (dort 1.500 ¥).
 *
 * <ul>
 *   <li>Riggeradapter (S. 395): "Wenn diese 'Black Box' in einem Fahrzeug installiert ist, kann ein
 *       Charakter mit einer Riggerkontrolle (siehe S. 385) das Fahrzeug mittels eines direkten
 *       Glasfaserkabels oder einer WiFi-Verbindung riggen."</li>
 *   <li>Waffenhalterung (S. 395): "Ein Fahrzeug kann mit so vielen Waffenhalterungen ausgestattet
 *       werden, wie seinem Rumpf ÷ 3 (abgerundet) entspricht. Eine Waffenhalterung kann ein LMG oder
 *       eine kleinere Waffe sowie 250 Schuss Munition aufnehmen."</li>
 * </ul>
 */
public enum VehicleModification {
  RIGGER_ADAPTER("Riggeradapter", "4", Legality.LEGAL, 2_500),
  WEAPON_MOUNT("Waffenhalterung", "8", Legality.FORBIDDEN, 2_500);

  private final @NonNull String displayName;
  private final @NonNull String availabilityValue;
  private final @NonNull Legality legality;
  private final int cost;

  VehicleModification(@NonNull String displayName, @NonNull String availabilityValue,
      @NonNull Legality legality, int cost) {
    this.displayName = displayName;
    this.availabilityValue = availabilityValue;
    this.legality = legality;
    this.cost = cost;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  public @NonNull Availability availability() {
    return new Availability(availabilityValue, legality);
  }

  /** Preis in Nuyen. */
  public int cost() {
    return cost;
  }
}
