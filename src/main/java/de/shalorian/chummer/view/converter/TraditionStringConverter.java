package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.magic.Tradition;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public class TraditionStringConverter extends StringConverter<Tradition> {

  @Override
  public String toString(@Nullable Tradition t) {
    return t == null ? "" : t.getDisplayName();
  }

  @Override
  public @Nullable Tradition fromString(String s) {
    return null;
  }
}
