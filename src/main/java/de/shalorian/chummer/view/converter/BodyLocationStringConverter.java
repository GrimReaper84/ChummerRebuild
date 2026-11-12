package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

/** Zeigt eine Körperstelle mit ihrem deutschen Namen („linker Arm“). */
public class BodyLocationStringConverter extends StringConverter<BodyLocation> {

  @Override
  public String toString(@Nullable BodyLocation location) {
    return location == null ? "" : location.getDisplayName();
  }

  @Override
  public @Nullable BodyLocation fromString(String s) {
    return null;
  }
}
