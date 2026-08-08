package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.Tradition;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

class TraditionStringConverter extends StringConverter<Tradition> {

  @Override
  public String toString(@Nullable Tradition t) {
    return t == null ? "" : t.getDisplayName();
  }

  @Override
  public @Nullable Tradition fromString(String s) {
    return null;
  }
}
