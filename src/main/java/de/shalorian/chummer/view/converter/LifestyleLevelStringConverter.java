package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public class LifestyleLevelStringConverter extends StringConverter<LifestyleLevel> {

  @Override
  public String toString(@Nullable LifestyleLevel level) {
    return level == null ? "" : level.getDisplayName();
  }

  @Override
  public @Nullable LifestyleLevel fromString(String s) {
    return null;
  }
}
