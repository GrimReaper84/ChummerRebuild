package de.shalorian.chummer.view.converter;

import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public class CyberwareGradeStringConverter extends StringConverter<CyberwareGrade> {

  @Override
  public String toString(@Nullable CyberwareGrade grade) {
    return grade == null ? "" : grade.getDisplayName();
  }

  @Override
  public @Nullable CyberwareGrade fromString(String s) {
    return null;
  }
}
