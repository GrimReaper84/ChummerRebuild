package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.gear.Gear;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

/** Munitionsstapel für die Auswahl der geladenen Munition: „APDS-Geschosse (Schwere Pistolen), 20 Schuss“. */
public class AmmoStringConverter extends StringConverter<Gear> {

  @Override
  public String toString(@Nullable Gear ammo) {
    return ammo == null ? "" : ammo.getDisplayName() + ", " + ammo.getQuantity() + " Schuss";
  }

  @Override
  public @Nullable Gear fromString(String s) {
    return null;
  }
}
