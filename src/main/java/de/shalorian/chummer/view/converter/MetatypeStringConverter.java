package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.metatype.MetatypeData;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public class MetatypeStringConverter extends StringConverter<MetatypeData> {

  @Override
  public String toString(@Nullable MetatypeData m) {
    if (m == null) {
      return "";
    }
    String prefix = m.isVariant() ? "  " : "";
    String bp = m.bpCost() > 0 ? "  (" + m.bpCost() + " BP)" : "";
    return prefix + m.name() + bp;
  }

  @Override
  public @Nullable MetatypeData fromString(String s) {
    return null;
  }
}