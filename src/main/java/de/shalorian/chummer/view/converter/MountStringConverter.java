package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.weapon.Mount;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public class MountStringConverter extends StringConverter<Mount> {

  @Override
  public String toString(@Nullable Mount mount) {
    return mount == null ? "" : mount.getDisplayName();
  }

  @Override
  public @Nullable Mount fromString(String s) {
    return null;
  }
}
